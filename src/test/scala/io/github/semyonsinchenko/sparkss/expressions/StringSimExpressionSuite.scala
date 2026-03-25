package io.github.semyonsinchenko.sparkss.expressions

import io.github.semyonsinchenko.sparkss.StringSimilarityFunctions
import io.github.semyonsinchenko.sparkss.sql.StringSimilaritySparkSessionExtensions._
import org.apache.spark.sql.Column
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, concat_ws, lit, trim}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

class StringSimExpressionSuite extends AnyFunSuite with BeforeAndAfterAll {

  private var spark: SparkSession = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    spark = SparkSession.builder().master("local[1]").appName("StringSimExpressionSuite").getOrCreate()
  }

  override protected def afterAll(): Unit = {
    try {
      if (spark != null) {
        spark.stop()
      }
    } finally {
      super.afterAll()
    }
  }

  private def evaluateWithCodegen(
      frame: org.apache.spark.sql.DataFrame,
      metric: (Column, Column) => Column,
      enabled: Boolean
  ): Seq[Any] = {
    spark.conf.set("spark.sql.codegen.wholeStage", enabled.toString)
    frame
      .select(metric(col("left"), col("right")).as("score"))
      .collect()
      .map(_.get(0))
      .toSeq
  }

  private def assertClose(actual: Double, expected: Double): Unit = {
    assert(Math.abs(actual - expected) <= 1e-12)
  }

  test("expression propagates null values") {
    val s = spark
    import s.implicits._

    val frame = Seq(
      (Some("a b"), Some("a")),
      (None, Some("a")),
      (Some("a"), None)
    ).toDF("left", "right")

    val scores = frame
      .select(StringSimilarityFunctions.jaccard(col("left"), col("right")).as("score"))
      .collect()
      .map(_.get(0))

    assert(scores(0) == 0.5)
    assert(scores(1) == null)
    assert(scores(2) == null)
  }

  test("sorensen dice expression propagates null values") {
    val s = spark
    import s.implicits._

    val frame = Seq(
      (Some("a b"), Some("a")),
      (None, Some("a")),
      (Some("a"), None)
    ).toDF("left", "right")

    val scores = frame
      .select(StringSimilarityFunctions.sorensenDice(col("left"), col("right")).as("score"))
      .collect()
      .map(_.get(0))

    assert(scores(0) == (2.0 / 3.0))
    assert(scores(1) == null)
    assert(scores(2) == null)
  }

  test("overlap coefficient expression propagates null values") {
    val s = spark
    import s.implicits._

    val frame = Seq(
      (Some("a b"), Some("a")),
      (None, Some("a")),
      (Some("a"), None)
    ).toDF("left", "right")

    val scores = frame
      .select(StringSimilarityFunctions.overlapCoefficient(col("left"), col("right")).as("score"))
      .collect()
      .map(_.get(0))

    assert(scores(0) == 1.0)
    assert(scores(1) == null)
    assert(scores(2) == null)
  }

  test("expression evaluates nested child expressions correctly") {
    val s = spark
    import s.implicits._

    val frame = Seq(("hello", "hello world")).toDF("prefix", "target")

    val score = frame
      .select(
        StringSimilarityFunctions
          .jaccard(concat_ws(" ", col("prefix"), lit("world")), trim(col("target")))
          .as("score")
      )
      .head()
      .getDouble(0)

    assert(score === 1.0)
  }

  test("sorensen dice evaluates nested child expressions correctly") {
    val s = spark
    import s.implicits._

    val frame = Seq(("hello", "hello world")).toDF("prefix", "target")

    val score = frame
      .select(
        StringSimilarityFunctions
          .sorensenDice(concat_ws(" ", col("prefix"), lit("world")), trim(col("target")))
          .as("score")
      )
      .head()
      .getDouble(0)

    assert(score === 1.0)
  }

  test("overlap coefficient evaluates nested child expressions correctly") {
    val s = spark
    import s.implicits._

    val frame = Seq(("hello", "hello world")).toDF("prefix", "target")

    val score = frame
      .select(
        StringSimilarityFunctions
          .overlapCoefficient(concat_ws(" ", col("prefix"), lit("world")), trim(col("target")))
          .as("score")
      )
      .head()
      .getDouble(0)

    assert(score === 1.0)
  }

  test("interpreted and codegen execution return identical outputs") {
    val s = spark
    import s.implicits._

    val frame = Seq(
      ("a b c", "a b d"),
      ("", ""),
      ("", "x"),
      ("x x y", "x y"),
      (null.asInstanceOf[String], "x"),
      ("x", null.asInstanceOf[String])
    ).toDF("left", "right")

    val generated = evaluateWithCodegen(frame, StringSimilarityFunctions.jaccard, enabled = true)
    val interpreted = evaluateWithCodegen(frame, StringSimilarityFunctions.jaccard, enabled = false)

    assert(generated == interpreted)
  }

  test("sorensen dice interpreted and codegen execution return identical outputs") {
    val s = spark
    import s.implicits._

    val frame = Seq(
      ("a b c", "a b d"),
      ("", ""),
      ("", "x"),
      ("x x y", "x y"),
      (null.asInstanceOf[String], "x"),
      ("x", null.asInstanceOf[String])
    ).toDF("left", "right")

    val generated = evaluateWithCodegen(frame, StringSimilarityFunctions.sorensenDice, enabled = true)
    val interpreted = evaluateWithCodegen(frame, StringSimilarityFunctions.sorensenDice, enabled = false)

    assert(generated == interpreted)
  }

  test("overlap coefficient interpreted and codegen execution return identical outputs") {
    val s = spark
    import s.implicits._

    val frame = Seq(
      ("a b c", "a b d"),
      ("", ""),
      ("", "x"),
      ("x x y", "x y"),
      (null.asInstanceOf[String], "x"),
      ("x", null.asInstanceOf[String])
    ).toDF("left", "right")

    val generated = evaluateWithCodegen(frame, StringSimilarityFunctions.overlapCoefficient, enabled = true)
    val interpreted = evaluateWithCodegen(frame, StringSimilarityFunctions.overlapCoefficient, enabled = false)

    assert(generated == interpreted)
  }

  test("dsl constructors and sql registration use the same expression") {
    val s = spark
    import s.implicits._

    val frame = Seq(("a b", "a c")).toDF("left", "right")

    val dslScore = frame
      .select(StringSimilarityFunctions.jaccard("left", "right").as("score"))
      .head()
      .getDouble(0)

    spark.registerStringSimilarityFunctions()
    frame.createOrReplaceTempView("pairs")
    val sqlScore = spark.sql("SELECT jaccard(left, right) AS score FROM pairs").head().getDouble(0)

    assert(dslScore === (1.0 / 3.0))
    assert(sqlScore === dslScore)
  }

  test("sorensen dice dsl constructors and sql registration use the same expression") {
    val s = spark
    import s.implicits._

    val frame = Seq(("a b", "a c")).toDF("left", "right")

    val dslScore = frame
      .select(StringSimilarityFunctions.sorensenDice("left", "right").as("score"))
      .head()
      .getDouble(0)

    spark.registerStringSimilarityFunctions()
    frame.createOrReplaceTempView("pairs")
    val sqlScore = spark.sql("SELECT sorensen_dice(left, right) AS score FROM pairs").head().getDouble(0)

    assert(dslScore === 0.5)
    assert(sqlScore === dslScore)
  }

  test("overlap coefficient dsl constructors and sql registration use the same expression") {
    val s = spark
    import s.implicits._

    val frame = Seq(("a b", "a c")).toDF("left", "right")

    val dslScore = frame
      .select(StringSimilarityFunctions.overlapCoefficient("left", "right").as("score"))
      .head()
      .getDouble(0)

    spark.registerStringSimilarityFunctions()
    frame.createOrReplaceTempView("pairs")
    val sqlScore = spark.sql("SELECT overlap_coefficient(left, right) AS score FROM pairs").head().getDouble(0)

    assert(dslScore === 0.5)
    assert(sqlScore === dslScore)
  }

  test("cosine expression propagates null values") {
    val s = spark
    import s.implicits._

    val frame = Seq(
      (Some("a b"), Some("a")),
      (None, Some("a")),
      (Some("a"), None)
    ).toDF("left", "right")

    val scores = frame
      .select(StringSimilarityFunctions.cosine(col("left"), col("right")).as("score"))
      .collect()
      .map(_.get(0))

    assert(scores(0) == (1.0 / Math.sqrt(2.0)))
    assert(scores(1) == null)
    assert(scores(2) == null)
  }

  test("levenshtein expression propagates null values") {
    val s = spark
    import s.implicits._

    val frame = Seq(
      (Some("spark"), Some("spork")),
      (None, Some("a")),
      (Some("a"), None)
    ).toDF("left", "right")

    val scores = frame
      .select(StringSimilarityFunctions.levenshtein(col("left"), col("right")).as("score"))
      .collect()
      .map(_.get(0))

    assert(scores(0) == 0.8)
    assert(scores(1) == null)
    assert(scores(2) == null)
  }

  test("jaro expression propagates null values") {
    val s = spark
    import s.implicits._

    val frame = Seq(
      (Some("martha"), Some("marhta")),
      (None, Some("a")),
      (Some("a"), None)
    ).toDF("left", "right")

    val scores = frame
      .select(StringSimilarityFunctions.jaro(col("left"), col("right")).as("score"))
      .collect()
      .map(_.get(0))

    assertClose(scores(0).asInstanceOf[Double], 17.0 / 18.0)
    assert(scores(1) == null)
    assert(scores(2) == null)
  }

  test("jaro winkler expression propagates null values") {
    val s = spark
    import s.implicits._

    val frame = Seq(
      (Some("martha"), Some("marhta")),
      (None, Some("a")),
      (Some("a"), None)
    ).toDF("left", "right")

    val scores = frame
      .select(StringSimilarityFunctions.jaroWinkler(col("left"), col("right")).as("score"))
      .collect()
      .map(_.get(0))

    assertClose(scores(0).asInstanceOf[Double], 0.9611111111111111)
    assert(scores(1) == null)
    assert(scores(2) == null)
  }

  test("braun blanquet and lcs similarity expressions propagate null values") {
    val s = spark
    import s.implicits._

    val frame = Seq(
      (Some("a b"), Some("a")),
      (Some("spark"), Some("spork")),
      (None, Some("a")),
      (Some("a"), None)
    ).toDF("left", "right")

    val rows = frame
      .select(
        StringSimilarityFunctions.braunBlanquet(col("left"), col("right")).as("braun"),
        StringSimilarityFunctions.lcsSimilarity(col("left"), col("right")).as("lcs")
      )
      .collect()

    assert(rows(0).getDouble(0) == 0.5)
    assert(rows(1).getDouble(1) == 0.8)
    assert(rows(2).get(0) == null)
    assert(rows(2).get(1) == null)
    assert(rows(3).get(0) == null)
    assert(rows(3).get(1) == null)
  }

  test("cosine and levenshtein evaluate nested child expressions correctly") {
    val s = spark
    import s.implicits._

    val frame = Seq(("hello", "hello world", "spark", "spork")).toDF("prefix", "target", "a", "b")

    val row = frame
      .select(
        StringSimilarityFunctions
          .cosine(concat_ws(" ", col("prefix"), lit("world")), trim(col("target")))
          .as("cosine"),
        StringSimilarityFunctions
          .levenshtein(trim(col("a")), trim(col("b")))
          .as("levenshtein")
      )
      .head()

    assert(row.getDouble(0) === 1.0)
    assert(row.getDouble(1) === 0.8)
  }

  test("jaro evaluates nested child expressions correctly") {
    val s = spark
    import s.implicits._

    val frame = Seq((" martha ", "marhta ")).toDF("a", "b")

    val score = frame
      .select(
        StringSimilarityFunctions
          .jaro(trim(col("a")), trim(col("b")))
          .as("jaro")
      )
      .head()
      .getDouble(0)

    assertClose(score, 17.0 / 18.0)
  }

  test("jaro winkler evaluates nested child expressions correctly") {
    val s = spark
    import s.implicits._

    val frame = Seq((" martha ", "marhta ")).toDF("a", "b")

    val score = frame
      .select(
        StringSimilarityFunctions
          .jaroWinkler(trim(col("a")), trim(col("b")))
          .as("jaro_winkler")
      )
      .head()
      .getDouble(0)

    assertClose(score, 0.9611111111111111)
  }

  test("braun blanquet and lcs similarity evaluate nested child expressions correctly") {
    val s = spark
    import s.implicits._

    val frame = Seq(("hello", "hello world", "abcd", "acbd")).toDF("prefix", "target", "a", "b")

    val row = frame
      .select(
        StringSimilarityFunctions
          .braunBlanquet(concat_ws(" ", col("prefix"), lit("world")), trim(col("target")))
          .as("braun"),
        StringSimilarityFunctions
          .lcsSimilarity(trim(col("a")), trim(col("b")))
          .as("lcs")
      )
      .head()

    assert(row.getDouble(0) === 1.0)
    assert(row.getDouble(1) === 0.75)
  }

  test("cosine and levenshtein interpreted and codegen execution return identical outputs") {
    val s = spark
    import s.implicits._

    val frame = Seq(
      ("a b c", "a b d"),
      ("", ""),
      ("", "x"),
      ("x x y", "x y"),
      ("spark", "spork"),
      (null.asInstanceOf[String], "x"),
      ("x", null.asInstanceOf[String])
    ).toDF("left", "right")

    val generatedCosine = evaluateWithCodegen(frame, StringSimilarityFunctions.cosine, enabled = true)
    val interpretedCosine = evaluateWithCodegen(frame, StringSimilarityFunctions.cosine, enabled = false)
    val generatedLevenshtein = evaluateWithCodegen(frame, StringSimilarityFunctions.levenshtein, enabled = true)
    val interpretedLevenshtein = evaluateWithCodegen(frame, StringSimilarityFunctions.levenshtein, enabled = false)

    assert(generatedCosine == interpretedCosine)
    assert(generatedLevenshtein == interpretedLevenshtein)
  }

  test("jaro interpreted and codegen execution return identical outputs") {
    val s = spark
    import s.implicits._

    val frame = Seq(
      ("martha", "marhta"),
      ("dwayne", "duane"),
      ("", ""),
      ("", "x"),
      ("aaaa", "aaab"),
      (null.asInstanceOf[String], "x"),
      ("x", null.asInstanceOf[String])
    ).toDF("left", "right")

    val generated = evaluateWithCodegen(frame, StringSimilarityFunctions.jaro, enabled = true)
    val interpreted = evaluateWithCodegen(frame, StringSimilarityFunctions.jaro, enabled = false)

    assert(generated == interpreted)
  }

  test("jaro winkler interpreted and codegen execution return identical outputs") {
    val s = spark
    import s.implicits._

    val frame = Seq(
      ("martha", "marhta"),
      ("dwayne", "duane"),
      ("", ""),
      ("", "x"),
      ("aaaa", "aaab"),
      (null.asInstanceOf[String], "x"),
      ("x", null.asInstanceOf[String])
    ).toDF("left", "right")

    val generated = evaluateWithCodegen(frame, StringSimilarityFunctions.jaroWinkler, enabled = true)
    val interpreted = evaluateWithCodegen(frame, StringSimilarityFunctions.jaroWinkler, enabled = false)

    assert(generated == interpreted)
  }

  test("jaro codegen execution path returns concrete scores") {
    val s = spark
    import s.implicits._

    spark.conf.set("spark.sql.codegen.wholeStage", "true")

    val rows = Seq(
      ("martha", "marhta"),
      ("abc", "xyz")
    ).toDF("left", "right")
      .select(StringSimilarityFunctions.jaro(col("left"), col("right")).as("score"))
      .collect()

    assertClose(rows(0).getDouble(0), 17.0 / 18.0)
    assert(rows(1).getDouble(0) === 0.0)
  }

  test("jaro winkler codegen execution path returns concrete scores") {
    val s = spark
    import s.implicits._

    spark.conf.set("spark.sql.codegen.wholeStage", "true")

    val rows = Seq(
      ("martha", "marhta"),
      ("abc", "xyz")
    ).toDF("left", "right")
      .select(StringSimilarityFunctions.jaroWinkler(col("left"), col("right")).as("score"))
      .collect()

    assertClose(rows(0).getDouble(0), 0.9611111111111111)
    assert(rows(1).getDouble(0) === 0.0)
  }

  test("braun blanquet and lcs similarity interpreted and codegen execution return identical outputs") {
    val s = spark
    import s.implicits._

    val frame = Seq(
      ("a b c", "a b d"),
      ("", ""),
      ("", "x"),
      ("x x y", "x y"),
      ("spark", "spork"),
      (null.asInstanceOf[String], "x"),
      ("x", null.asInstanceOf[String])
    ).toDF("left", "right")

    val generatedBraun = evaluateWithCodegen(frame, StringSimilarityFunctions.braunBlanquet, enabled = true)
    val interpretedBraun = evaluateWithCodegen(frame, StringSimilarityFunctions.braunBlanquet, enabled = false)
    val generatedLcs = evaluateWithCodegen(frame, StringSimilarityFunctions.lcsSimilarity, enabled = true)
    val interpretedLcs = evaluateWithCodegen(frame, StringSimilarityFunctions.lcsSimilarity, enabled = false)

    assert(generatedBraun == interpretedBraun)
    assert(generatedLcs == interpretedLcs)
  }

  test("cosine and levenshtein dsl constructors and sql registration use the same expression") {
    val s = spark
    import s.implicits._

    val frame = Seq(("a b", "a c"), ("spark", "spork")).toDF("left", "right")

    val dslCosine = frame
      .select(StringSimilarityFunctions.cosine("left", "right").as("score"))
      .head()
      .getDouble(0)
    val dslLevenshtein = frame
      .select(StringSimilarityFunctions.levenshtein("left", "right").as("score"))
      .collect()(1)
      .getDouble(0)

    spark.registerStringSimilarityFunctions()
    frame.createOrReplaceTempView("pairs")
    val sqlCosine = spark.sql("SELECT cosine(left, right) AS score FROM pairs").head().getDouble(0)
    val sqlLevenshtein = spark.sql("SELECT levenshtein(left, right) AS score FROM pairs").collect()(1).getDouble(0)

    assert(sqlCosine === dslCosine)
    assert(sqlLevenshtein === dslLevenshtein)
  }

  test("jaro dsl constructors and sql registration use the same expression") {
    val s = spark
    import s.implicits._

    val frame = Seq(("martha", "marhta")).toDF("left", "right")

    val dslScore = frame
      .select(StringSimilarityFunctions.jaro("left", "right").as("score"))
      .head()
      .getDouble(0)

    spark.registerStringSimilarityFunctions()
    frame.createOrReplaceTempView("pairs")
    val sqlScore = spark.sql("SELECT jaro(left, right) AS score FROM pairs").head().getDouble(0)

    assertClose(dslScore, 17.0 / 18.0)
    assert(sqlScore === dslScore)
  }

  test("jaro winkler dsl constructors and sql registration use the same expression") {
    val s = spark
    import s.implicits._

    val frame = Seq(("martha", "marhta")).toDF("left", "right")

    val dslScore = frame
      .select(StringSimilarityFunctions.jaroWinkler("left", "right").as("score"))
      .head()
      .getDouble(0)

    spark.registerStringSimilarityFunctions()
    frame.createOrReplaceTempView("pairs")
    val sqlScore = spark.sql("SELECT jaro_winkler(left, right) AS score FROM pairs").head().getDouble(0)

    assertClose(dslScore, 0.9611111111111111)
    assert(sqlScore === dslScore)
  }

  test("braun blanquet and lcs similarity dsl constructors and sql registration use the same expression") {
    val s = spark
    import s.implicits._

    val frame = Seq(("a b", "a c"), ("spark", "spork")).toDF("left", "right")

    val dslBraun = frame
      .select(StringSimilarityFunctions.braunBlanquet("left", "right").as("score"))
      .head()
      .getDouble(0)
    val dslLcs = frame
      .select(StringSimilarityFunctions.lcsSimilarity("left", "right").as("score"))
      .collect()(1)
      .getDouble(0)

    spark.registerStringSimilarityFunctions()
    frame.createOrReplaceTempView("pairs")
    val sqlBraun = spark.sql("SELECT braun_blanquet(left, right) AS score FROM pairs").head().getDouble(0)
    val sqlLcs = spark.sql("SELECT lcs_similarity(left, right) AS score FROM pairs").collect()(1).getDouble(0)

    assert(sqlBraun === dslBraun)
    assert(sqlLcs === dslLcs)
  }

  test("token and matrix metric families preserve interpreted and codegen parity") {
    val s = spark
    import s.implicits._

    val frame = Seq(
      ("a b c", "a b d"),
      ("", ""),
      ("", "x"),
      ("x x y", "x y"),
      ("spark", "spork"),
      (null.asInstanceOf[String], "x"),
      ("x", null.asInstanceOf[String])
    ).toDF("left", "right")

    val tokenMetrics = Seq(
      "jaccard" -> ((left: Column, right: Column) => StringSimilarityFunctions.jaccard(left, right)),
      "sorensen_dice" -> ((left: Column, right: Column) => StringSimilarityFunctions.sorensenDice(left, right)),
      "overlap_coefficient" -> ((left: Column, right: Column) =>
        StringSimilarityFunctions.overlapCoefficient(left, right)
      ),
      "cosine" -> ((left: Column, right: Column) => StringSimilarityFunctions.cosine(left, right)),
      "braun_blanquet" -> ((left: Column, right: Column) => StringSimilarityFunctions.braunBlanquet(left, right))
    )
    val matrixMetrics = Seq(
      "levenshtein" -> ((left: Column, right: Column) => StringSimilarityFunctions.levenshtein(left, right)),
      "lcs_similarity" -> ((left: Column, right: Column) => StringSimilarityFunctions.lcsSimilarity(left, right)),
      "jaro" -> ((left: Column, right: Column) => StringSimilarityFunctions.jaro(left, right)),
      "jaro_winkler" -> ((left: Column, right: Column) => StringSimilarityFunctions.jaroWinkler(left, right))
    )

    (tokenMetrics ++ matrixMetrics).foreach { case (name, metric) =>
      val generated = evaluateWithCodegen(frame, metric, enabled = true)
      val interpreted = evaluateWithCodegen(frame, metric, enabled = false)
      assert(generated == interpreted, s"Codegen parity failed for $name")
    }
  }
}
