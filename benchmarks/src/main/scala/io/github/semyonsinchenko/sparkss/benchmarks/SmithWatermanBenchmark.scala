package io.github.semyonsinchenko.sparkss.benchmarks

import io.github.semyonsinchenko.sparkss.expressions.matrix.{Jaro, LcsSimilarity, Levenshtein, NeedlemanWunsch, SmithWaterman}
import org.apache.spark.unsafe.types.UTF8String
import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class SmithWatermanBenchmark {

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

  private var left: UTF8String = _
  private var right: UTF8String = _

  @Setup(Level.Trial)
  def setup(): Unit = {
    val pair = scenario match {
      case "short-low-overlap" =>
        ("spark", "flint")
      case "short-medium-overlap" =>
        ("spark", "shark")
      case "short-high-overlap" =>
        ("spark", "spork")
      case "medium-low-overlap" =>
        ("distributed systems are fast", "batch processing can be costly")
      case "medium-medium-overlap" =>
        ("distributed systems are fast", "distributed batch jobs are costly")
      case "medium-high-overlap" =>
        ("distributed systems are fast", "distributed system are fast")
      case "long-low-overlap" =>
        (
          "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda mu",
          "omicron pi rho sigma tau upsilon phi chi psi omega aleph beth"
        )
      case "long-medium-overlap" =>
        (
          "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda mu",
          "alpha beta gamma sigma tau upsilon eta theta iota omega lambda nu"
        )
      case "long-high-overlap" =>
        (
          "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda mu",
          "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda nu"
        )
      case _ =>
        throw new IllegalArgumentException(s"Unknown scenario: $scenario")
    }

    left = UTF8String.fromString(pair._1)
    right = UTF8String.fromString(pair._2)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def smithWaterman(): Double = {
    SmithWaterman.similarity(left, right)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def smithWatermanCustomParameters(): Double = {
    SmithWaterman.similarity(left, right, 3, -1, -2)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def needlemanWunschBaseline(): Double = {
    NeedlemanWunsch.similarity(left, right)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def levenshteinBaseline(): Double = {
    Levenshtein.similarity(left, right)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def lcsSimilarityBaseline(): Double = {
    LcsSimilarity.similarity(left, right)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def jaroBaseline(): Double = {
    Jaro.similarity(left, right)
  }
}
