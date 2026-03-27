package io.github.semyonsinchenko.sparkss.fuzzy

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Matrix
import org.apache.spark.ml.stat.Correlation
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.nio.file.Paths

private[fuzzy] final case class DeltaBands(within5: Long, within10: Long, within30: Long, over30: Long)

private[fuzzy] final case class MetricReport(
    metric: String,
    rowCount: Long,
    pearson: Double,
    spearman: Double,
    deltaBands: DeltaBands
)

private[fuzzy] final case class FuzzyTestingReport(seed: Long, rows: Long, metrics: Seq[MetricReport])

private[fuzzy] object FuzzyTestingPipeline {
  private val Epsilon = 1e-9

  private final case class MetricSpec(metric: String, nativeFunction: String, legacyUdfName: String, legacyClassName: String)

  private val MetricSpecs = Seq(
    MetricSpec("affine_gap", "affine_gap", "legacy_affine_gap", "com.wcohen.secondstring.AffineGap"),
    MetricSpec(
      "needleman_wunsch",
      "needleman_wunsch",
      "legacy_needleman_wunsch",
      "com.wcohen.secondstring.NeedlemanWunsch"
    ),
    MetricSpec(
      "smith_waterman",
      "smith_waterman",
      "legacy_smith_waterman",
      "com.wcohen.secondstring.SmithWaterman"
    ),
    MetricSpec("jaro_winkler", "jaro_winkler", "legacy_jaro_winkler", "com.wcohen.secondstring.JaroWinkler"),
    MetricSpec("monge_elkan", "monge_elkan", "legacy_monge_elkan", "com.wcohen.secondstring.MongeElkan")
  )

  def run(spark: SparkSession, seed: Long, rows: Long, saveOutputDir: Option[String] = None): FuzzyTestingReport = {
    LegacySecondStringUdfs.registerAll(spark, MetricSpecs.map(spec => spec.legacyUdfName -> spec.legacyClassName))
    val generated = generateInputData(spark, seed, rows).cache()
    generated.count()

    try {
      val metrics = MetricSpecs.map { spec =>
        val compared = scoreMetric(generated, spec).cache()
        compared.count()
        try {
          saveOutputDir.foreach(dir => writeMetricCsv(compared, spec.metric, dir))
          val rowCount = compared.count()
          val (pearson, spearman) = correlations(compared, rowCount)
          val bands = deltaBands(compared)
          MetricReport(spec.metric, rowCount, pearson, spearman, bands)
        } finally {
          compared.unpersist(blocking = false)
        }
      }
      FuzzyTestingReport(seed = seed, rows = rows, metrics = metrics)
    } finally {
      generated.unpersist(blocking = false)
    }
  }

  private def writeMetricCsv(scored: DataFrame, metric: String, outputDir: String): Unit = {
    val target = Paths.get(outputDir, metric).toString
    scored
      .select(
        col("left").as("input_left"),
        col("right").as("input_right"),
        col("native_score").as("native"),
        col("baseline_score").as("second_string"),
        col("relative_delta").as("relative_diff")
      )
      .coalesce(1)
      .write
      .mode("overwrite")
      .option("header", "true")
      .csv(target)
  }

  private[fuzzy] def scoredDataFrameForMetric(input: DataFrame, metric: String): DataFrame = {
    val spec = MetricSpecs.find(_.metric == metric).getOrElse {
      throw new IllegalArgumentException(s"Unsupported metric: $metric")
    }
    scoreMetric(input, spec)
  }

  private[fuzzy] def generateInputData(spark: SparkSession, seed: Long, rows: Long): DataFrame = {
    val seedLiteral = lit(seed.toString)

    spark.range(rows)
      .withColumn("left_hash", sha2(concat(seedLiteral, lit("-left-"), col("id").cast("string")), 256))
      .withColumn("right_hash", sha2(concat(seedLiteral, lit("-right-"), col("id").cast("string")), 256))
      .select(
        concat_ws(
          " ",
          substring(col("left_hash"), 1, 8),
          substring(col("left_hash"), 9, 8),
          substring(col("left_hash"), 17, 8)
        ).as("left"),
        concat_ws(
          " ",
          substring(col("left_hash"), 1, 4),
          substring(col("right_hash"), 9, 8),
          substring(col("right_hash"), 17, 8)
        ).as("right")
      )
  }

  private def scoreMetric(input: DataFrame, spec: MetricSpec): DataFrame = {
    input
      .selectExpr(
        "left",
        "right",
        s"${spec.nativeFunction}(left, right) AS native_score",
        s"${spec.legacyUdfName}(left, right) AS baseline_score"
      )
      .withColumn(
        "relative_delta",
        abs(col("native_score") - col("baseline_score")) /
          greatest((abs(col("native_score")) + abs(col("baseline_score"))) / lit(2.0), lit(Epsilon))
      )
      .select("left", "right", "native_score", "baseline_score", "relative_delta")
  }

  private def correlations(scored: DataFrame, rowCount: Long): (Double, Double) = {
    if (rowCount < 2) {
      (Double.NaN, Double.NaN)
    } else {
      val vectorized = new VectorAssembler()
        .setInputCols(Array("native_score", "baseline_score"))
        .setOutputCol("features")
        .transform(scored)
        .select("features")

      val pearson = extractCorrelation(vectorized, "pearson")
      val spearman = extractCorrelation(vectorized, "spearman")
      (pearson, spearman)
    }
  }

  private def extractCorrelation(df: DataFrame, method: String): Double = {
    val matrix = Correlation.corr(df, "features", method).head().getAs[Matrix](0)
    matrix(0, 1)
  }

  private def deltaBands(scored: DataFrame): DeltaBands = {
    val counts = scored
      .agg(
        sum(when(col("relative_delta") <= 0.05, 1L).otherwise(0L)).cast("long").as("within5"),
        sum(when(col("relative_delta") > 0.05 && col("relative_delta") <= 0.10, 1L).otherwise(0L))
          .cast("long")
          .as("within10"),
        sum(when(col("relative_delta") > 0.10 && col("relative_delta") <= 0.30, 1L).otherwise(0L))
          .cast("long")
          .as("within30"),
        sum(when(col("relative_delta") > 0.30, 1L).otherwise(0L)).cast("long").as("over30")
      )
      .head()

    DeltaBands(
      within5 = counts.getAs[Long]("within5"),
      within10 = counts.getAs[Long]("within10"),
      within30 = counts.getAs[Long]("within30"),
      over30 = counts.getAs[Long]("over30")
    )
  }
}

private[fuzzy] object LegacySecondStringUdfs {
  private val ScoreMethodName = "score"

  private final case class LegacyScorer(className: String) extends ((String, String) => Double) with Serializable {
    @transient private lazy val instanceAndMethod: (AnyRef, java.lang.reflect.Method) = {
      val algorithmClass = try {
        Class.forName(className)
      } catch {
        case _: ClassNotFoundException =>
          throw new IllegalStateException(
            s"Legacy algorithm class '$className' is unavailable. Add legacy Java SecondString dependency to run fuzzy testing."
          )
      }

      val scoreMethod = algorithmClass.getMethods.find { method =>
        method.getName == ScoreMethodName &&
        method.getParameterCount == 2 &&
        method.getParameterTypes.sameElements(Array(classOf[String], classOf[String]))
      }.getOrElse {
        throw new IllegalStateException(
          s"Legacy algorithm class '$className' does not expose score(String, String)."
        )
      }

      val instance = algorithmClass.getDeclaredConstructor().newInstance().asInstanceOf[AnyRef]
      (instance, scoreMethod)
    }

    override def apply(left: String, right: String): Double = {
      val (instance, scoreMethod) = instanceAndMethod
      scoreMethod.invoke(instance, left, right).asInstanceOf[Double]
    }
  }

  def registerAll(spark: SparkSession, udfToLegacyClass: Seq[(String, String)]): Unit = {
    udfToLegacyClass.foreach { case (udfName, legacyClassName) =>
      spark.udf.register(udfName, LegacyScorer(legacyClassName))
    }
  }
}
