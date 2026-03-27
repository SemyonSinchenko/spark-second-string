package io.github.semyonsinchenko.sparkss.fuzzy

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Matrix
import org.apache.spark.ml.stat.Correlation
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

import java.nio.file.Paths
import java.util.Random

private[fuzzy] final case class DeltaBands(within5: Long, within10: Long, within30: Long, over30: Long)

private[fuzzy] final case class MetricReport(
    metric: String,
    comparedRowCount: Long,
    excludedNullScaledCount: Long,
    pearson: Double,
    spearman: Double,
    deltaBands: DeltaBands
)

private[fuzzy] final case class FuzzyTestingReport(seed: Long, rows: Long, metrics: Seq[MetricReport])

private[fuzzy] object FuzzyTestingPipeline {
  private val Epsilon = 1e-9
  private val GeneratorModeProperty = "sparkss.fuzzy.generator.mode"
  private val RandomCasesMode = "random-cases"
  private val LegacyPrefixMode = "legacy-prefix"

  private val SharedAlphabet = "abcdefghijklmnopqrstuvwxyz0123456789"
  private val DisjointLeftAlphabet = "abcdefghijklm"
  private val DisjointRightAlphabet = "nopqrstuvwxyz0123456789"
  private val PunctuationAlphabet = "!?,.;:-_/'\"()[]{}"
  private val RepeatedCharacters = "abxyz"

  private[fuzzy] final case class GeneratedInputRow(left: String, right: String, case_type: String)

  private sealed trait GenerationCase {
    def caseType: String
    def weight: Int
  }

  private case object ExactMatchCase extends GenerationCase {
    override val caseType: String = "exact_match"
    override val weight: Int = 10
  }

  private case object HighOverlapCase extends GenerationCase {
    override val caseType: String = "high_overlap"
    override val weight: Int = 15
  }

  private case object MediumOverlapCase extends GenerationCase {
    override val caseType: String = "medium_overlap"
    override val weight: Int = 15
  }

  private case object LowOverlapCase extends GenerationCase {
    override val caseType: String = "low_overlap"
    override val weight: Int = 15
  }

  private case object DisjointCase extends GenerationCase {
    override val caseType: String = "disjoint"
    override val weight: Int = 15
  }

  private case object AsymmetricLengthCase extends GenerationCase {
    override val caseType: String = "asymmetric_length"
    override val weight: Int = 10
  }

  private case object WhitespacePunctuationCase extends GenerationCase {
    override val caseType: String = "whitespace_punctuation"
    override val weight: Int = 10
  }

  private case object RepeatedCharacterCase extends GenerationCase {
    override val caseType: String = "repeated_character"
    override val weight: Int = 5
  }

  private case object EmptyStringCase extends GenerationCase {
    override val caseType: String = "empty_string"
    override val weight: Int = 5
  }

  private val GenerationCases = Vector(
    ExactMatchCase,
    HighOverlapCase,
    MediumOverlapCase,
    LowOverlapCase,
    DisjointCase,
    AsymmetricLengthCase,
    WhitespacePunctuationCase,
    RepeatedCharacterCase,
    EmptyStringCase
  )
  private val TotalCaseWeight = GenerationCases.map(_.weight).sum

  private type Scaler = (Column, Column, Column) => Column

  private final case class MetricSpec(
      metric: String,
      nativeFunction: String,
      legacyUdfName: String,
      legacyClassName: String,
      scaler: Scaler,
      emptyPairScaledValue: Option[Double]
  )

  private val MetricSpecs = Seq(
    MetricSpec(
      metric = "needleman_wunsch",
      nativeFunction = "needleman_wunsch",
      legacyUdfName = "legacy_needleman_wunsch",
      legacyClassName = "com.wcohen.secondstring.NeedlemanWunsch",
      scaler = needlemanWunschScaler,
      emptyPairScaledValue = Some(1.0)
    ),
    MetricSpec(
      metric = "smith_waterman",
      nativeFunction = "smith_waterman",
      legacyUdfName = "legacy_smith_waterman",
      legacyClassName = "com.wcohen.secondstring.SmithWaterman",
      scaler = smithWatermanScaler,
      emptyPairScaledValue = Some(1.0)
    ),
    MetricSpec(
      metric = "jaro_winkler",
      nativeFunction = "jaro_winkler",
      legacyUdfName = "legacy_jaro_winkler",
      legacyClassName = "com.wcohen.secondstring.JaroWinkler",
      scaler = identityScaler,
      emptyPairScaledValue = Some(1.0)
    ),
    MetricSpec(
      metric = "monge_elkan",
      nativeFunction = "monge_elkan",
      legacyUdfName = "legacy_monge_elkan",
      legacyClassName = "com.wcohen.secondstring.MongeElkan",
      scaler = identityScaler,
      emptyPairScaledValue = Some(1.0)
    )
  )

  private val MetricSpecByName = MetricSpecs.map(spec => spec.metric -> spec).toMap

  def run(spark: SparkSession, seed: Long, rows: Long, saveOutputDir: Option[String] = None): FuzzyTestingReport = {
    LegacySecondStringUdfs.registerAll(spark, MetricSpecs.map(spec => spec.legacyUdfName -> spec.legacyClassName))
    val generated = generateInputData(spark, seed, rows).cache()
    generated.count()

    try {
      val metrics = MetricSpecs.map { spec =>
        val scored = scoreMetric(generated, spec).cache()
        val totalRowCount = scored.count()
        try {
          saveOutputDir.foreach(dir => writeMetricCsv(scored, spec.metric, dir))

          val compared = scored.filter(col("second_string_scaled").isNotNull).cache()
          val comparedRowCount = compared.count()
          try {
            val excludedNullScaledCount = totalRowCount - comparedRowCount
            val (pearson, spearman) = correlations(compared, comparedRowCount)
            val bands = deltaBands(compared)
            MetricReport(spec.metric, comparedRowCount, excludedNullScaledCount, pearson, spearman, bands)
          } finally {
            compared.unpersist(blocking = false)
          }
        } finally {
          scored.unpersist(blocking = false)
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
        col("input_left"),
        col("input_right"),
        col("native_score").as("native"),
        col("second_string_raw"),
        col("second_string_scaled"),
        col("relative_delta").as("relative_diff")
      )
      .coalesce(1)
      .write
      .mode("overwrite")
      .option("header", "true")
      .csv(target)
  }

  private[fuzzy] def scoredDataFrameForMetric(input: DataFrame, metric: String): DataFrame = {
    scoreMetric(input, metricSpecFor(metric))
  }

  private[fuzzy] def applyLegacyScalingForMetric(
      input: DataFrame,
      metric: String,
      rawColumn: String = "second_string_raw",
      leftColumn: String = "input_left",
      rightColumn: String = "input_right"
  ): DataFrame = {
    val spec = metricSpecFor(metric)
    input.withColumn("second_string_scaled", scaledLegacyBaseline(col(rawColumn), col(leftColumn), col(rightColumn), spec))
  }

  private[fuzzy] def generateInputData(spark: SparkSession, seed: Long, rows: Long): DataFrame = {
    generateInputDataWithCase(spark, seed, rows).select("left", "right")
  }

  private[fuzzy] def generateInputDataWithCase(spark: SparkSession, seed: Long, rows: Long): DataFrame = {
    if (resolveGeneratorMode(spark) == LegacyPrefixMode) {
      generateLegacyPrefixInputDataWithCase(spark, seed, rows)
    } else {
      import spark.implicits._
      spark.range(rows).map { id =>
        val selectedCase = selectGenerationCase(seed, id)
        val (left, right) = buildPairForCase(seed, id, selectedCase)
        GeneratedInputRow(left = left, right = right, case_type = selectedCase.caseType)
      }.toDF()
    }
  }

  private def resolveGeneratorMode(spark: SparkSession): String = {
    val configured = spark.conf
      .getOption(GeneratorModeProperty)
      .orElse(Option(System.getProperty(GeneratorModeProperty)))
      .map(_.trim)
      .filter(_.nonEmpty)
      .getOrElse(RandomCasesMode)

    configured match {
      case LegacyPrefixMode => LegacyPrefixMode
      case _ => RandomCasesMode
    }
  }

  private def generateLegacyPrefixInputDataWithCase(spark: SparkSession, seed: Long, rows: Long): DataFrame = {
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
        ).as("right"),
        lit("legacy_prefix").as("case_type")
      )
  }

  private def selectGenerationCase(seed: Long, rowIndex: Long): GenerationCase = {
    if (rowIndex < GenerationCases.length) {
      val startOffset = Math.floorMod(mix64(seed), GenerationCases.length.toLong).toInt
      GenerationCases((rowIndex.toInt + startOffset) % GenerationCases.length)
    } else {
      val random = randomForRow(seed, rowIndex, streamId = 0)
      val selectedWeight = random.nextInt(TotalCaseWeight)

      var runningWeight = 0
      GenerationCases.find { generationCase =>
        runningWeight += generationCase.weight
        selectedWeight < runningWeight
      }.getOrElse(GenerationCases.last)
    }
  }

  private def buildPairForCase(seed: Long, rowIndex: Long, generationCase: GenerationCase): (String, String) = {
    val random = randomForRow(seed, rowIndex, streamId = 1)
    generationCase match {
      case ExactMatchCase =>
        val value = randomString(random, minLength = 6, maxLength = 24, alphabet = SharedAlphabet)
        (value, value)

      case HighOverlapCase =>
        buildOverlapPair(random, minLength = 8, maxLength = 28, editRatio = 0.15)

      case MediumOverlapCase =>
        buildOverlapPair(random, minLength = 8, maxLength = 28, editRatio = 0.35)

      case LowOverlapCase =>
        val left = randomString(random, minLength = 8, maxLength = 28, alphabet = SharedAlphabet)
        val sharedPrefixLength = Math.min(2, left.length)
        val rightBody = randomString(random, minLength = 8, maxLength = 28, alphabet = "uvwxyz0123456789")
        val right = left.take(sharedPrefixLength) + rightBody
        (left, right)

      case DisjointCase =>
        val left = randomString(random, minLength = 6, maxLength = 20, alphabet = DisjointLeftAlphabet)
        val right = randomString(random, minLength = 6, maxLength = 20, alphabet = DisjointRightAlphabet)
        (left, right)

      case AsymmetricLengthCase =>
        val shortValue = randomString(random, minLength = 2, maxLength = 6, alphabet = SharedAlphabet)
        val longValue = shortValue + randomString(random, minLength = 25, maxLength = 45, alphabet = SharedAlphabet)
        if (random.nextBoolean()) {
          (shortValue, longValue)
        } else {
          (longValue, shortValue)
        }

      case WhitespacePunctuationCase =>
        val leftTokens = (0 until randomLength(random, 3, 6)).map { _ =>
          randomString(random, minLength = 2, maxLength = 6, alphabet = SharedAlphabet)
        }
        val rightTokens = (0 until randomLength(random, 3, 6)).map { _ =>
          randomString(random, minLength = 2, maxLength = 6, alphabet = SharedAlphabet)
        }
        val leftDelimiter = randomDelimiter(random)
        val rightDelimiter = randomDelimiter(random)
        (leftTokens.mkString(leftDelimiter), rightTokens.mkString(rightDelimiter))

      case RepeatedCharacterCase =>
        val leftChar = RepeatedCharacters.charAt(random.nextInt(RepeatedCharacters.length))
        val rightChar = RepeatedCharacters.charAt(random.nextInt(RepeatedCharacters.length))
        val left = leftChar.toString * randomLength(random, 8, 24)
        val right = rightChar.toString * randomLength(random, 8, 24)
        (left, right)

      case EmptyStringCase =>
        random.nextInt(3) match {
          case 0 => ("", "")
          case 1 => ("", randomString(random, minLength = 3, maxLength = 20, alphabet = SharedAlphabet))
          case _ => (randomString(random, minLength = 3, maxLength = 20, alphabet = SharedAlphabet), "")
        }
    }
  }

  private def buildOverlapPair(random: Random, minLength: Int, maxLength: Int, editRatio: Double): (String, String) = {
    val left = randomString(random, minLength, maxLength, SharedAlphabet)
    val edits = Math.max(1, Math.round(left.length * editRatio).toInt)
    val right = applyEdits(left, edits, random)
    (left, right)
  }

  private def applyEdits(base: String, edits: Int, random: Random): String = {
    var chars = base.toVector

    (0 until edits).foreach { _ =>
      val editKind = if (chars.isEmpty) 2 else random.nextInt(3)
      editKind match {
        case 0 =>
          val index = random.nextInt(chars.length)
          chars = chars.updated(index, randomChar(random, SharedAlphabet))
        case 1 =>
          if (chars.length > 1) {
            val index = random.nextInt(chars.length)
            chars = chars.patch(index, Nil, 1)
          }
        case _ =>
          val index = random.nextInt(chars.length + 1)
          chars = chars.patch(index, Seq(randomChar(random, SharedAlphabet)), 0)
      }
    }

    if (chars.isEmpty) {
      randomChar(random, SharedAlphabet).toString
    } else {
      chars.mkString
    }
  }

  private def randomDelimiter(random: Random): String = {
    val whitespace = if (random.nextBoolean()) " " else "  "
    val punctuation = randomChar(random, PunctuationAlphabet).toString
    if (random.nextBoolean()) {
      s"$punctuation$whitespace"
    } else {
      s"$whitespace$punctuation$whitespace"
    }
  }

  private def randomString(random: Random, minLength: Int, maxLength: Int, alphabet: String): String = {
    val length = randomLength(random, minLength, maxLength)
    val builder = new StringBuilder(length)
    (0 until length).foreach { _ =>
      builder.append(randomChar(random, alphabet))
    }
    builder.result()
  }

  private def randomLength(random: Random, minLength: Int, maxLength: Int): Int = {
    minLength + random.nextInt(maxLength - minLength + 1)
  }

  private def randomChar(random: Random, alphabet: String): Char = {
    alphabet.charAt(random.nextInt(alphabet.length))
  }

  private def randomForRow(seed: Long, rowIndex: Long, streamId: Int): Random = {
    new Random(deriveRowSeed(seed, rowIndex, streamId))
  }

  private[fuzzy] def deriveRowSeed(seed: Long, rowIndex: Long, streamId: Int): Long = {
    val stream = streamId.toLong * 0x9e3779b97f4a7c15L
    val row = rowIndex * 0xbf58476d1ce4e5b9L
    mix64(seed ^ row ^ stream)
  }

  private def mix64(input: Long): Long = {
    var z = input + 0x9e3779b97f4a7c15L
    z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L
    z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL
    z ^ (z >>> 31)
  }

  private def scoreMetric(input: DataFrame, spec: MetricSpec): DataFrame = {
    input
      .selectExpr(
        "left AS input_left",
        "right AS input_right",
        s"${spec.nativeFunction}(left, right) AS native_score",
        s"${spec.legacyUdfName}(left, right) AS second_string_raw"
      )
      .withColumn(
        "second_string_scaled",
        scaledLegacyBaseline(col("second_string_raw"), col("input_left"), col("input_right"), spec)
      )
      .withColumn(
        "relative_delta",
        abs(col("native_score") - col("second_string_scaled")) /
          greatest((abs(col("native_score")) + abs(col("second_string_scaled"))) / lit(2.0), lit(Epsilon))
      )
      .select("input_left", "input_right", "native_score", "second_string_raw", "second_string_scaled", "relative_delta")
  }

  private def correlations(scored: DataFrame, rowCount: Long): (Double, Double) = {
    if (rowCount < 2) {
      (Double.NaN, Double.NaN)
    } else {
      val vectorized = new VectorAssembler()
        .setInputCols(Array("native_score", "second_string_scaled"))
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

  private def metricSpecFor(metric: String): MetricSpec = {
    MetricSpecByName.getOrElse(metric, throw new IllegalArgumentException(s"Unsupported metric: $metric"))
  }

  private def scaledLegacyBaseline(raw: Column, inputLeft: Column, inputRight: Column, spec: MetricSpec): Column = {
    val emptyPairScaled = spec.emptyPairScaledValue match {
      case Some(value) => lit(value)
      case None => lit(null).cast("double")
    }

    val candidate = when(length(inputLeft) === lit(0) && length(inputRight) === lit(0), emptyPairScaled)
      .otherwise(spec.scaler(raw, inputLeft, inputRight))

    sanitizeAndClamp(raw, candidate)
  }

  private def sanitizeAndClamp(raw: Column, candidate: Column): Column = {
    val rawIsInvalid = raw.isNull || isnan(raw) || raw === lit(Double.PositiveInfinity) || raw === lit(Double.NegativeInfinity)
    when(rawIsInvalid, lit(null).cast("double"))
      .otherwise(greatest(lit(0.0), least(lit(1.0), candidate)))
  }

  private def maxLength(inputLeft: Column, inputRight: Column): Column = {
    greatest(length(inputLeft), length(inputRight)).cast("double")
  }

  private def minLength(inputLeft: Column, inputRight: Column): Column = {
    least(length(inputLeft), length(inputRight)).cast("double")
  }

  private def needlemanWunschScaler(raw: Column, inputLeft: Column, inputRight: Column): Column = {
    val maxLen = maxLength(inputLeft, inputRight)
    (raw + maxLen) / maxLen
  }

  private def smithWatermanScaler(raw: Column, inputLeft: Column, inputRight: Column): Column = {
    val minLen = minLength(inputLeft, inputRight)
    when(minLen <= lit(0.0), lit(0.0)).otherwise(raw / (lit(2.0) * minLen))
  }

  private def identityScaler(raw: Column, inputLeft: Column, inputRight: Column): Column = raw
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
