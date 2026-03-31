package io.github.semyonsinchenko.sparkss.benchmarks

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

private object LegacySecondStringBridge {
  private val scoreMethodName = "score"

  private final case class LegacyScorer(className: String) extends ((String, String) => Double) with Serializable {
    @transient private lazy val instanceAndMethod: (AnyRef, java.lang.reflect.Method) = {
      val algorithmClass = try {
        Class.forName(className)
      } catch {
        case _: ClassNotFoundException =>
          throw new IllegalStateException(
            s"Legacy algorithm class '$className' is unavailable. Add legacy Java SecondString dependency to run UDF baseline benchmarks."
          )
      }

      val scoreMethod = algorithmClass.getMethods.find { method =>
        method.getName == scoreMethodName &&
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

  def scorerFor(className: String): (String, String) => Double = {
    LegacyScorer(className)
  }
}

@State(Scope.Thread)
abstract class LegacyMatrixUdfBenchmarkBase {
  @Param(Array(
    "short-low-overlap",
    "short-medium-overlap",
    "short-high-overlap",
    "medium-low-overlap",
    "medium-medium-overlap",
    "medium-high-overlap",
    "long-low-overlap",
    "long-medium-overlap",
    "long-high-overlap"
  ))
  var scenario: String = _

  private var spark: SparkSession = _
  private var scenarioDataFrame: DataFrame = _

  protected def udfName: String
  protected def legacyClassName: String

  @Setup(Level.Trial)
  def setup(): Unit = {
    spark = SparkSession
      .builder()
      .appName(s"${this.getClass.getSimpleName}-legacy-udf")
      .master("local[1]")
      .config("spark.ui.enabled", "false")
      .config("spark.ui.showConsoleProgress", "false")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    spark.udf.register(udfName, LegacySecondStringBridge.scorerFor(legacyClassName))

    val sparkSession = spark
    import sparkSession.implicits._
    val (left, right) = BenchmarkInputData.matrixPairFor(scenario)
    val prepared = Seq.fill(BenchmarkSuiteConfig.RowCount)((left, right)).toDF("left", "right").cache()
    prepared.count()
    scenarioDataFrame = prepared
  }

  @TearDown(Level.Trial)
  def tearDown(): Unit = {
    if (scenarioDataFrame != null) {
      scenarioDataFrame.unpersist(blocking = false)
    }
    if (spark != null) {
      spark.stop()
    }
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 6, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 6, time = 1, timeUnit = TimeUnit.SECONDS)
  def legacyUdf(): Double = {
    val score = scenarioDataFrame
      .selectExpr(s"avg($udfName(left, right)) AS score")
      .collect()(0)

    score.getAs[Double]("score")
  }
}

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class LegacyAffineGapUdfBenchmark extends LegacyMatrixUdfBenchmarkBase {
  override protected val udfName: String = "legacy_affine_gap"
  override protected val legacyClassName: String = "com.wcohen.secondstring.AffineGap"
}

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class LegacyNeedlemanWunschUdfBenchmark extends LegacyMatrixUdfBenchmarkBase {
  override protected val udfName: String = "legacy_needleman_wunsch"
  override protected val legacyClassName: String = "com.wcohen.secondstring.NeedlemanWunsch"
}

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class LegacySmithWatermanUdfBenchmark extends LegacyMatrixUdfBenchmarkBase {
  override protected val udfName: String = "legacy_smith_waterman"
  override protected val legacyClassName: String = "com.wcohen.secondstring.SmithWaterman"
}

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class LegacyJaroWinklerUdfBenchmark extends LegacyMatrixUdfBenchmarkBase {
  override protected val udfName: String = "legacy_jaro_winkler"
  override protected val legacyClassName: String = "com.wcohen.secondstring.JaroWinkler"
}

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class LegacyMongeElkanUdfBenchmark extends LegacyMatrixUdfBenchmarkBase {
  override protected val udfName: String = "legacy_monge_elkan"
  override protected val legacyClassName: String = "com.wcohen.secondstring.MongeElkan"
}
