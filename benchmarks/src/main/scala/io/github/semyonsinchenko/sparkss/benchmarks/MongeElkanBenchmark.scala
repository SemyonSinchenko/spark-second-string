package io.github.semyonsinchenko.sparkss.benchmarks

import io.github.semyonsinchenko.sparkss.expressions.matrix.{Jaro, Levenshtein}
import io.github.semyonsinchenko.sparkss.expressions.token.{BraunBlanquet, Cosine, Jaccard, MongeElkan, SorensenDice}
import org.apache.spark.unsafe.types.UTF8String
import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class MongeElkanBenchmark {

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
        ("nova quark pixel", "ember cobalt ion")
      case "short-medium-overlap" =>
        ("alpha spark delta", "alpha shark theta")
      case "short-high-overlap" =>
        ("alpha beta gamma", "alpha beta gammaa")
      case "medium-low-overlap" =>
        ("alpha beta gamma delta epsilon zeta", "theta iota kappa lambda mu nu")
      case "medium-medium-overlap" =>
        ("alpha beta gamma delta epsilon zeta", "alpha theta gamma lambda epsilon nu")
      case "medium-high-overlap" =>
        ("alpha beta gamma delta epsilon zeta", "alpha beta gamma delta epsilon zetaa")
      case "long-low-overlap" =>
        (
          "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda mu nu xi omicron pi",
          "rho sigma tau upsilon phi chi psi omega aleph beth gimel daleth he waw zayin heth"
        )
      case "long-medium-overlap" =>
        (
          "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda mu nu xi omicron pi",
          "alpha sigma gamma upsilon epsilon chi eta omega iota beth lambda daleth nu waw omicron heth"
        )
      case "long-high-overlap" =>
        (
          "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda mu nu xi omicron pi",
          "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda mu nu xi omicron pia"
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
  def mongeElkan(): Double = {
    MongeElkan.similarity(left, right)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def mongeElkanJaroInnerMetric(): Double = {
    MongeElkan.similarity(left, right, "jaro", 0)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def mongeElkanBigrams(): Double = {
    MongeElkan.similarity(left, right, "jaro_winkler", 2)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def jaccardBaseline(): Double = {
    Jaccard.similarity(left, right)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def sorensenDiceBaseline(): Double = {
    SorensenDice.similarity(left, right)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def cosineBaseline(): Double = {
    Cosine.similarity(left, right)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def braunBlanquetBaseline(): Double = {
    BraunBlanquet.similarity(left, right)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def jaroBaseline(): Double = {
    Jaro.similarity(left, right)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def levenshteinBaseline(): Double = {
    Levenshtein.similarity(left, right)
  }
}
