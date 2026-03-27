package io.github.semyonsinchenko.sparkss.fuzzy

import io.github.semyonsinchenko.sparkss.sql.StringSimilaritySparkSessionExtensions._
import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters._

class FuzzyTestingSuite extends AnyFunSuite with BeforeAndAfterAll {
  private var spark: SparkSession = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    spark = SparkSession
      .builder()
      .master("local[1]")
      .appName("FuzzyTestingSuite")
      .config("spark.ui.showConsoleProgress", "false")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")
    spark.registerStringSimilarityFunctions()
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

  test("cli parser uses defaults and honors explicit values") {
    val defaults = FuzzyTestingCli.parseArgs(Array("--out", "out.md"))
    assert(defaults == Right(FuzzyTestingCli.CliOptions(seed = 42L, rows = 10000L, out = "out.md", saveOutput = None)))

    val explicit =
      FuzzyTestingCli.parseArgs(Array("--seed", "7", "--rows", "123", "--out", "x.md", "--save-output", "out-dir"))
    assert(
      explicit == Right(FuzzyTestingCli.CliOptions(seed = 7L, rows = 123L, out = "x.md", saveOutput = Some("out-dir")))
    )
  }

  test("cli parser requires --out") {
    val parsed = FuzzyTestingCli.parseArgs(Array("--seed", "7"))
    assert(parsed.left.exists(_.contains("--out")))
  }

  test("deterministic input generation is stable for same seed and rows") {
    val first = FuzzyTestingPipeline.generateInputData(spark, seed = 42L, rows = 20L).collect().toSeq
    val second = FuzzyTestingPipeline.generateInputData(spark, seed = 42L, rows = 20L).collect().toSeq
    assert(first == second)
  }

  test("deterministic generation keeps stable case composition for same seed and rows") {
    val first = FuzzyTestingPipeline.generateInputDataWithCase(spark, seed = 42L, rows = 200L)
    val second = FuzzyTestingPipeline.generateInputDataWithCase(spark, seed = 42L, rows = 200L)

    val firstPairs = first.select("left", "right", "case_type").collect().toSeq
    val secondPairs = second.select("left", "right", "case_type").collect().toSeq
    assert(firstPairs == secondPairs)

    val firstCounts = first.groupBy("case_type").count().collect().map(row => row.getString(0) -> row.getLong(1)).toMap
    val secondCounts = second.groupBy("case_type").count().collect().map(row => row.getString(0) -> row.getLong(1)).toMap
    assert(firstCounts == secondCounts)
  }

  test("deterministic input generation changes with seed") {
    val first = FuzzyTestingPipeline.generateInputData(spark, seed = 42L, rows = 20L).collect().toSeq
    val second = FuzzyTestingPipeline.generateInputData(spark, seed = 43L, rows = 20L).collect().toSeq
    assert(first != second)
  }

  test("different seeds produce different pairs and cohort distributions") {
    val first = FuzzyTestingPipeline.generateInputDataWithCase(spark, seed = 42L, rows = 1000L)
    val second = FuzzyTestingPipeline.generateInputDataWithCase(spark, seed = 43L, rows = 1000L)

    val firstPairs = first.select("left", "right", "case_type").collect().toSeq
    val secondPairs = second.select("left", "right", "case_type").collect().toSeq
    assert(firstPairs != secondPairs)

    val firstCounts = first.groupBy("case_type").count().collect().map(row => row.getString(0) -> row.getLong(1)).toMap
    val secondCounts = second.groupBy("case_type").count().collect().map(row => row.getString(0) -> row.getLong(1)).toMap
    assert(firstCounts != secondCounts)
  }

  test("case generation preserves schema row count and disjoint invariants") {
    val generated = FuzzyTestingPipeline.generateInputDataWithCase(spark, seed = 7L, rows = 200L)
    assert(generated.columns.toSeq == Seq("left", "right", "case_type"))
    assert(generated.count() == 200L)

    val disjointRows = generated.filter("case_type = 'disjoint'").select("left", "right").collect().toSeq
    assert(disjointRows.nonEmpty)
    disjointRows.foreach { row =>
      val leftChars = row.getString(0).toSet
      val rightChars = row.getString(1).toSet
      assert(leftChars.intersect(rightChars).isEmpty)
    }
  }

  test("small-row runs remain deterministic and schema-stable") {
    val generated = FuzzyTestingPipeline.generateInputDataWithCase(spark, seed = 9L, rows = 3L)
    assert(generated.columns.toSeq == Seq("left", "right", "case_type"))
    assert(generated.count() == 3L)
    generated.collect().foreach { row =>
      assert(row.getString(0) != null)
      assert(row.getString(1) != null)
      assert(row.getString(2).nonEmpty)
    }
  }

  test("legacy generator mode remains available as a fallback") {
    val key = "sparkss.fuzzy.generator.mode"
    val previous = spark.conf.getOption(key)
    spark.conf.set(key, "legacy-prefix")

    try {
      val generated = FuzzyTestingPipeline.generateInputDataWithCase(spark, seed = 42L, rows = 20L)
      assert(generated.select("case_type").distinct().collect().map(_.getString(0)).toSeq == Seq("legacy_prefix"))

      val sampled = generated.select("left", "right").head()
      val leftTokens = sampled.getString(0).split(" ").toSeq
      val rightTokens = sampled.getString(1).split(" ").toSeq
      assert(leftTokens.head.length == 8)
      assert(rightTokens.head.length == 4)
      assert(leftTokens.head.take(4) == rightTokens.head)
    } finally {
      previous match {
        case Some(value) => spark.conf.set(key, value)
        case None => spark.conf.unset(key)
      }
    }
  }

  test("scored dataframe contains raw scaled and relative delta columns") {
    LegacySecondStringUdfs.registerAll(
      spark,
      Seq(
        "legacy_needleman_wunsch" -> "com.wcohen.secondstring.NeedlemanWunsch",
        "legacy_smith_waterman" -> "com.wcohen.secondstring.SmithWaterman",
        "legacy_jaro_winkler" -> "com.wcohen.secondstring.JaroWinkler",
        "legacy_monge_elkan" -> "com.wcohen.secondstring.MongeElkan"
      )
    )

    val input = FuzzyTestingPipeline.generateInputData(spark, seed = 5L, rows = 50L)
    val scored = FuzzyTestingPipeline.scoredDataFrameForMetric(input, "smith_waterman")

    val columns = scored.columns.toSeq
    assert(columns.contains("input_left"))
    assert(columns.contains("input_right"))
    assert(columns.contains("native_score"))
    assert(columns.contains("second_string_raw"))
    assert(columns.contains("second_string_scaled"))
    assert(columns.contains("relative_delta"))

    val minDelta = scored.selectExpr("min(relative_delta)").head().getDouble(0)
    assert(minDelta >= 0.0)
  }

  test("metric scaling clamps invalid raw values and handles empty input deterministically") {
    val sparkSession = spark
    import sparkSession.implicits._

    // NW scaler: (raw + maxLen) / maxLen; for "abc"/"abc" maxLen=3
    val input = Seq(
      ("all-empty", "", "", 999.0: java.lang.Double),
      ("clamped-high", "abc", "abc", 2.0: java.lang.Double),
      ("clamped-low", "abc", "abc", -5.0: java.lang.Double),
      ("bounded", "abc", "abc", -1.0: java.lang.Double),
      ("nan", "abc", "ab", Double.NaN: java.lang.Double),
      ("pos-inf", "abc", "ab", Double.PositiveInfinity: java.lang.Double),
      ("neg-inf", "abc", "ab", Double.NegativeInfinity: java.lang.Double),
      ("missing", "abc", "ab", null.asInstanceOf[java.lang.Double])
    ).toDF("case_id", "input_left", "input_right", "second_string_raw")

    val scaled = FuzzyTestingPipeline
      .applyLegacyScalingForMetric(input, "needleman_wunsch")
      .select("case_id", "second_string_scaled")
      .collect()
      .map(row => row.getString(0) -> (if (row.isNullAt(1)) None else Some(row.getDouble(1))))
      .toMap

    assert(scaled("all-empty").contains(1.0))
    assert(scaled("clamped-high").contains(1.0))
    assert(scaled("clamped-low").contains(0.0))
    assert(scaled("bounded").exists(value => value > 0.0 && value < 1.0))
    assert(scaled("nan").isEmpty)
    assert(scaled("pos-inf").isEmpty)
    assert(scaled("neg-inf").isEmpty)
    assert(scaled("missing").isEmpty)
  }

  test("length-aware scalers use row lengths for needleman_wunsch and smith_waterman") {
    val sparkSession = spark
    import sparkSession.implicits._

    val input = Seq(
      ("abcd", "xy", -2.0),
      ("abcd", "xy", 4.0)
    ).toDF("input_left", "input_right", "second_string_raw")

    val needleScaled = FuzzyTestingPipeline
      .applyLegacyScalingForMetric(input.filter("second_string_raw = -2.0"), "needleman_wunsch")
      .head()
      .getAs[Double]("second_string_scaled")
    val expectedNeedle = (-2.0 + 4.0) / 4.0
    assert(Math.abs(needleScaled - expectedNeedle) < 1e-12)

    val smithScaled = FuzzyTestingPipeline
      .applyLegacyScalingForMetric(input.filter("second_string_raw = 4.0"), "smith_waterman")
      .head()
      .getAs[Double]("second_string_scaled")
    val expectedSmith = 4.0 / (2.0 * 2.0)
    assert(Math.abs(smithScaled - expectedSmith) < 1e-12)
  }

  test("relative delta is computed from scaled legacy baseline") {
    val sparkSession = spark
    import sparkSession.implicits._

    LegacySecondStringUdfs.registerAll(
      spark,
      Seq(
        "legacy_needleman_wunsch" -> "com.wcohen.secondstring.NeedlemanWunsch",
        "legacy_smith_waterman" -> "com.wcohen.secondstring.SmithWaterman",
        "legacy_jaro_winkler" -> "com.wcohen.secondstring.JaroWinkler",
        "legacy_monge_elkan" -> "com.wcohen.secondstring.MongeElkan"
      )
    )

    val scored = FuzzyTestingPipeline
      .scoredDataFrameForMetric(Seq(("kitten", "sitting")).toDF("left", "right"), "needleman_wunsch")
      .head()

    val native = scored.getAs[Double]("native_score")
    val raw = scored.getAs[Double]("second_string_raw")
    val scaled = scored.getAs[Double]("second_string_scaled")
    val actual = scored.getAs[Double]("relative_delta")

    val expectedFromScaled = Math.abs(native - scaled) / Math.max((Math.abs(native) + Math.abs(scaled)) / 2.0, 1e-9)
    val expectedFromRaw = Math.abs(native - raw) / Math.max((Math.abs(native) + Math.abs(raw)) / 2.0, 1e-9)

    assert(Math.abs(actual - expectedFromScaled) < 1e-12)
    assert(Math.abs(raw - scaled) > 1e-12)
    assert(Math.abs(actual - expectedFromRaw) > 1e-12)
  }

  test("markdown renderer keeps stable sections and required headers") {
    val report = FuzzyTestingReport(
      seed = 42L,
      rows = 10L,
      metrics = Seq(
        MetricReport(
          metric = "smith_waterman",
          comparedRowCount = 10L,
          excludedNullScaledCount = 2L,
          pearson = 0.9,
          spearman = 0.8,
          deltaBands = DeltaBands(within5 = 4L, within10 = 3L, within30 = 2L, over30 = 1L)
        )
      )
    )

    val markdown = MarkdownReportRenderer.render(report)
    assert(markdown.contains("# Fuzzy Testing Report"))
    assert(markdown.contains("## Cross-metric Summary"))
    assert(markdown.contains("excluded NULL scaled rows"))
    assert(markdown.contains("pearson | 0.900000"))
    assert(markdown.contains("spearman | 0.800000"))
    assert(markdown.contains("## Metric: smith_waterman"))
    assert(markdown.contains("Excluded NULL scaled rows: 2"))
    assert(markdown.contains("ALL | 10 | 2 | - | -"))
  }

  test("pipeline run is deterministic and returns all expected metrics without gating") {
    val first = FuzzyTestingPipeline.run(spark, seed = 42L, rows = 20L)
    val second = FuzzyTestingPipeline.run(spark, seed = 42L, rows = 20L)

    val metricNames = first.metrics.map(_.metric).toSet
    assert(metricNames == Set("needleman_wunsch", "smith_waterman", "jaro_winkler", "monge_elkan"))
    assert(first.seed == second.seed)
    assert(first.rows == second.rows)
    assert(first.metrics.size == second.metrics.size)
    first.metrics.zip(second.metrics).foreach { case (left, right) =>
      assert(left.metric == right.metric)
      assert(left.comparedRowCount == right.comparedRowCount)
      assert(left.excludedNullScaledCount == right.excludedNullScaledCount)
      assert(left.deltaBands == right.deltaBands)
      assert((left.pearson.isNaN && right.pearson.isNaN) || left.pearson == right.pearson)
      assert((left.spearman.isNaN && right.spearman.isNaN) || left.spearman == right.spearman)
    }
  }

  test("save-output writes one csv table per metric with required columns") {
    val outputDir = Files.createTempDirectory("fuzzy-output-")
    try {
      FuzzyTestingPipeline.run(spark, seed = 42L, rows = 10L, saveOutputDir = Some(outputDir.toString))

      val metrics = Seq("needleman_wunsch", "smith_waterman", "jaro_winkler", "monge_elkan")
      metrics.foreach { metric =>
        val metricDir = outputDir.resolve(metric)
        assert(Files.isDirectory(metricDir), s"missing metric directory: $metricDir")

        val listStream = Files.list(metricDir)
        val csvFiles = try {
          listStream
            .iterator()
            .asScala
            .filter(path => path.getFileName.toString.startsWith("part-") && path.getFileName.toString.endsWith(".csv"))
            .toSeq
        } finally {
          listStream.close()
        }
        assert(csvFiles.size == 1, s"expected exactly one csv part for $metric, found ${csvFiles.size}")

        val header = Files.readAllLines(csvFiles.head).get(0)
        assert(header == "input_left,input_right,native,second_string_raw,second_string_scaled,relative_diff")
      }
    } finally {
      deleteRecursively(outputDir)
    }
  }

  private def deleteRecursively(path: Path): Unit = {
    if (Files.exists(path)) {
      Files
        .walk(path)
        .iterator()
        .asScala
        .toSeq
        .sortBy(_.toString.length)
        .reverse
        .foreach(Files.deleteIfExists)
    }
  }
}
