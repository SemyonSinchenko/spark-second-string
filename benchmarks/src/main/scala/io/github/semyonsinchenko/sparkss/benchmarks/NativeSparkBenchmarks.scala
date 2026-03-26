package io.github.semyonsinchenko.sparkss.benchmarks

import io.github.semyonsinchenko.sparkss.sql.StringSimilaritySparkSessionExtensions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

@State(Scope.Thread)
abstract class NativeSparkMatrixBenchmarkBase {
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
  private var scenarioDataFrames: Map[String, DataFrame] = Map.empty

  protected def functionName: String

  @Setup(Level.Trial)
  def setup(): Unit = {
    spark = SparkSession
      .builder()
      .appName(s"${this.getClass.getSimpleName}-native-spark")
      .master("local[1]")
      .config("spark.ui.enabled", "false")
      .config("spark.ui.showConsoleProgress", "false")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")
    spark.registerStringSimilarityFunctions()

    val sparkSession = spark
    import sparkSession.implicits._
    scenarioDataFrames = BenchmarkIdentifiers.MatrixScenarios.map { scenarioId =>
      val (left, right) = BenchmarkInputData.matrixPairFor(scenarioId)
      val prepared = Seq.fill(BenchmarkSuiteConfig.RowCount)((left, right)).toDF("left", "right").cache()
      prepared.count()
      scenarioId -> prepared
    }.toMap
  }

  @TearDown(Level.Trial)
  def tearDown(): Unit = {
    scenarioDataFrames.values.foreach(_.unpersist(blocking = false))
    if (spark != null) {
      spark.stop()
    }
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 6, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 6, time = 1, timeUnit = TimeUnit.SECONDS)
  def nativeSparkSql(): Double = {
    val score = scenarioDataFrames(scenario)
      .selectExpr(s"avg($functionName(left, right)) AS score")
      .collect()(0)

    score.getAs[Double]("score")
  }
}

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class NativeSparkAffineGapBenchmark extends NativeSparkMatrixBenchmarkBase {
  override protected val functionName: String = "affine_gap"
}

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class NativeSparkNeedlemanWunschBenchmark extends NativeSparkMatrixBenchmarkBase {
  override protected val functionName: String = "needleman_wunsch"
}

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class NativeSparkSmithWatermanBenchmark extends NativeSparkMatrixBenchmarkBase {
  override protected val functionName: String = "smith_waterman"
}

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class NativeSparkJaroWinklerBenchmark extends NativeSparkMatrixBenchmarkBase {
  override protected val functionName: String = "jaro_winkler"
}

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class NativeSparkMongeElkanBenchmark extends NativeSparkMatrixBenchmarkBase {
  override protected val functionName: String = "monge_elkan"
}
