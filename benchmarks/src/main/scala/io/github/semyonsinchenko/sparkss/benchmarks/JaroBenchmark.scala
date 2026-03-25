package io.github.semyonsinchenko.sparkss.benchmarks

import io.github.semyonsinchenko.sparkss.expressions.matrix.Jaro
import io.github.semyonsinchenko.sparkss.expressions.matrix.JaroWinkler
import org.apache.spark.unsafe.types.UTF8String
import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class JaroBenchmark {

  @Param(Array("short-high-overlap", "short-low-overlap", "medium-high-overlap", "medium-low-overlap", "long-high-overlap", "long-low-overlap"))
  var scenario: String = _

  private var left: UTF8String = _
  private var right: UTF8String = _

  @Setup(Level.Trial)
  def setup(): Unit = {
    val pair = scenario match {
      case "short-high-overlap" =>
        ("martha", "marhta")
      case "short-low-overlap" =>
        ("spark", "flint")
      case "medium-high-overlap" =>
        ("distributed systems are fast", "distributed system are fast")
      case "medium-low-overlap" =>
        ("distributed systems are fast", "batch processing can be costly")
      case "long-high-overlap" =>
        (
          "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda mu",
          "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda nu"
        )
      case "long-low-overlap" =>
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
  def jaro(): Double = {
    Jaro.similarity(left, right)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def jaroWinkler(): Double = {
    JaroWinkler.similarity(left, right)
  }
}
