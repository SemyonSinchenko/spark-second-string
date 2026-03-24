package io.github.semyonsinchenko.sparkss.benchmarks

import io.github.semyonsinchenko.sparkss.expressions.matrix.Levenshtein
import org.apache.spark.unsafe.types.UTF8String
import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class LevenshteinBenchmark {

  @Param(Array("short-low-edit", "short-high-edit", "medium-low-edit", "medium-high-edit", "long-low-edit", "long-high-edit"))
  var scenario: String = _

  private var left: UTF8String = _
  private var right: UTF8String = _

  @Setup(Level.Trial)
  def setup(): Unit = {
    val pair = scenario match {
      case "short-low-edit" =>
        ("spark", "spork")
      case "short-high-edit" =>
        ("spark", "flint")
      case "medium-low-edit" =>
        ("distributed systems are fast", "distributed system are fast")
      case "medium-high-edit" =>
        ("distributed systems are fast", "batch jobs can be costly")
      case "long-low-edit" =>
        (
          "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda mu",
          "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda nu"
        )
      case "long-high-edit" =>
        (
          "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda mu",
          "omicron pi rho sigma tau upsilon phi chi psi omega aleph beth"
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
  def levenshtein(): Double = {
    Levenshtein.similarity(left, right)
  }
}
