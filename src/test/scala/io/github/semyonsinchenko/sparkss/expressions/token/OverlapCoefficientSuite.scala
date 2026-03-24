package io.github.semyonsinchenko.sparkss.expressions.token

import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.funsuite.AnyFunSuite

class OverlapCoefficientSuite extends AnyFunSuite {

  private def score(left: String, right: String): Double = {
    OverlapCoefficient.similarity(UTF8String.fromString(left), UTF8String.fromString(right))
  }

  test("identical strings should return 1.0") {
    assert(score("hello world", "hello world") === 1.0)
  }

  test("completely different strings should return 0.0") {
    assert(score("hello", "world") === 0.0)
  }

  test("partial overlap should return expected score") {
    assert(score("a b c", "a b d") === (2.0 / 3.0))
    assert(score("a b", "a c") === 0.5)
  }

  test("both empty strings should return 1.0") {
    assert(score("", "") === 1.0)
  }

  test("one empty string should return 0.0") {
    assert(score("", "hello") === 0.0)
    assert(score("hello", "") === 0.0)
  }

  test("duplicate tokens do not change cardinality") {
    assert(score("a a a b", "a b") === 1.0)
    assert(score("a a b", "a c c") === 0.5)
  }

  test("whitespace normalization handles mixed separators") {
    assert(score("hello  world", "hello world") === 1.0)
    assert(score("hello\tworld", "hello world") === 1.0)
    assert(score("hello\nworld", "hello world") === 1.0)
  }
}
