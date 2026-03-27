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

  test("deterministic input generation changes with seed") {
    val first = FuzzyTestingPipeline.generateInputData(spark, seed = 42L, rows = 20L).collect().toSeq
    val second = FuzzyTestingPipeline.generateInputData(spark, seed = 43L, rows = 20L).collect().toSeq
    assert(first != second)
  }

  test("scored dataframe contains native baseline and relative delta columns") {
    LegacySecondStringUdfs.registerAll(
      spark,
      Seq(
        "legacy_affine_gap" -> "com.wcohen.secondstring.AffineGap",
        "legacy_needleman_wunsch" -> "com.wcohen.secondstring.NeedlemanWunsch",
        "legacy_smith_waterman" -> "com.wcohen.secondstring.SmithWaterman",
        "legacy_jaro_winkler" -> "com.wcohen.secondstring.JaroWinkler",
        "legacy_monge_elkan" -> "com.wcohen.secondstring.MongeElkan"
      )
    )

    val input = FuzzyTestingPipeline.generateInputData(spark, seed = 5L, rows = 50L)
    val scored = FuzzyTestingPipeline.scoredDataFrameForMetric(input, "affine_gap")

    val columns = scored.columns.toSeq
    assert(columns.contains("left"))
    assert(columns.contains("right"))
    assert(columns.contains("native_score"))
    assert(columns.contains("baseline_score"))
    assert(columns.contains("relative_delta"))

    val minDelta = scored.selectExpr("min(relative_delta)").head().getDouble(0)
    assert(minDelta >= 0.0)
  }

  test("markdown renderer keeps stable sections and required headers") {
    val report = FuzzyTestingReport(
      seed = 42L,
      rows = 10L,
      metrics = Seq(
        MetricReport(
          metric = "affine_gap",
          rowCount = 10L,
          pearson = 0.9,
          spearman = 0.8,
          deltaBands = DeltaBands(within5 = 4L, within10 = 3L, within30 = 2L, over30 = 1L)
        )
      )
    )

    val markdown = MarkdownReportRenderer.render(report)
    assert(markdown.contains("# Fuzzy Testing Report"))
    assert(markdown.contains("## Cross-metric Summary"))
    assert(markdown.contains("+-5% (count)"))
    assert(markdown.contains("pearson | 0.900000"))
    assert(markdown.contains("spearman | 0.800000"))
    assert(markdown.contains("## Metric: affine_gap"))
  }

  test("pipeline run returns all expected metrics without gating") {
    val report = FuzzyTestingPipeline.run(spark, seed = 42L, rows = 20L)

    val metricNames = report.metrics.map(_.metric).toSet
    assert(metricNames == Set("affine_gap", "needleman_wunsch", "smith_waterman", "jaro_winkler", "monge_elkan"))
    assert(report.metrics.forall(_.rowCount == 20L))
  }

  test("save-output writes one csv table per metric with required columns") {
    val outputDir = Files.createTempDirectory("fuzzy-output-")
    try {
      FuzzyTestingPipeline.run(spark, seed = 42L, rows = 10L, saveOutputDir = Some(outputDir.toString))

      val metrics = Seq("affine_gap", "needleman_wunsch", "smith_waterman", "jaro_winkler", "monge_elkan")
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
        assert(header == "input_left,input_right,native,second_string,relative_diff")
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
