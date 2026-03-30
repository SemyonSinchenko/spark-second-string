package io.github.semyonsinchenko.sparkss.benchmarks

import io.github.semyonsinchenko.sparkss.expressions.token.{BraunBlanquet, Cosine, Jaccard, OverlapCoefficient, SorensenDice}
import org.apache.spark.unsafe.types.UTF8String
import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class TokenMetricsBenchmark {

  @Param(Array("high-overlap", "low-overlap", "subset-like"))
  var scenario: String = _

  private var left: UTF8String = _
  private var right: UTF8String = _

  @Setup(Level.Trial)
  def setup(): Unit = {
    val pair = scenario match {
      case "high-overlap" =>
        (
          "alpha beta gamma delta epsilon",
          "alpha beta gamma delta zeta"
        )
      case "low-overlap" =>
        (
          "alpha beta gamma delta epsilon",
          "theta iota kappa lambda alpha"
        )
      case "subset-like" =>
        (
          "alpha beta gamma delta epsilon",
          "alpha beta"
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
  def jaccard(): Double = {
    Jaccard.similarity(left, right)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def jaccardBigrams(): Double = {
    Jaccard.similarity(left, right, 2)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def sorensenDice(): Double = {
    SorensenDice.similarity(left, right)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def sorensenDiceBigrams(): Double = {
    SorensenDice.similarity(left, right, 2)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def overlapCoefficient(): Double = {
    OverlapCoefficient.similarity(left, right)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def overlapCoefficientBigrams(): Double = {
    OverlapCoefficient.similarity(left, right, 2)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def cosine(): Double = {
    Cosine.similarity(left, right)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def cosineBigrams(): Double = {
    Cosine.similarity(left, right, 2)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def braunBlanquet(): Double = {
    BraunBlanquet.similarity(left, right)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def braunBlanquetBigrams(): Double = {
    BraunBlanquet.similarity(left, right, 2)
  }
}
