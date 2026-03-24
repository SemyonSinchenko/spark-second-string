package io.github.semyonsinchenko.sparkss.expressions

import io.github.semyonsinchenko.sparkss.StringSimilarityFunctions
import io.github.semyonsinchenko.sparkss.sql.StringSimilaritySparkSessionExtensions._
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

    def evaluateWithCodegen(enabled: Boolean): Seq[Any] = {
      spark.conf.set("spark.sql.codegen.wholeStage", enabled.toString)
      frame
        .select(StringSimilarityFunctions.jaccard(col("left"), col("right")).as("score"))
        .collect()
        .map(_.get(0))
        .toSeq
    }

    val generated = evaluateWithCodegen(enabled = true)
    val interpreted = evaluateWithCodegen(enabled = false)

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
}
