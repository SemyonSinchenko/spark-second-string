package io.github.semyonsinchenko.sparkss.benchmarks

import io.github.semyonsinchenko.sparkss.expressions.phonetic.{DoubleMetaphone, RefinedSoundex, Soundex}
import io.github.semyonsinchenko.sparkss.expressions.token.Jaccard
import org.apache.spark.unsafe.types.UTF8String
import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class PhoneticBenchmark {

  @Param(Array("short", "medium", "long"))
  var scenario: String = _

  private var left: UTF8String = _
  private var right: UTF8String = _

  @Setup(Level.Trial)
  def setup(): Unit = {
    val pair = scenario match {
      case "short" => ("Stephen Smith", "Steven Schmidt")
      case "medium" => ("Katherine Johnson", "Catherine Jonson")
      case "long" =>
        (
          "Alexanderson-McLaughlin III", "Aleksandersen Mcloklin Third"
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
  def soundex(): UTF8String = {
    Soundex.encode(left)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def refinedSoundex(): UTF8String = {
    RefinedSoundex.encode(left)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def doubleMetaphone(): UTF8String = {
    DoubleMetaphone.encode(left)
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
  def soundexThenJaccard(): Double = {
    Jaccard.similarity(Soundex.encode(left), Soundex.encode(right))
  }
}
