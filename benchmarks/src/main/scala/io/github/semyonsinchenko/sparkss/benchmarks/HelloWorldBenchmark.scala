package io.github.semyonsinchenko.sparkss.benchmarks

import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

/**
 * Hello World benchmark for spark-second-string project.
 * This is a placeholder to validate the JMH benchmark setup.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class HelloWorldBenchmark {

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def helloWorld(): String = {
    "Hello, spark-second-string benchmarks!"
  }
}
