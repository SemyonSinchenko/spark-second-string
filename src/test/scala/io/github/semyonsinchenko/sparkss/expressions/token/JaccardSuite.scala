package io.github.semyonsinchenko.sparkss.expressions.token

import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.unsafe.types.UTF8String

/** Test suite for Jaccard similarity getSim method.
  */
class JaccardSuite extends AnyFunSuite {

  private def score(left: String, right: String): Double = {
    Jaccard.similarity(UTF8String.fromString(left), UTF8String.fromString(right))
  }

  test("identical strings should return 1.0") {
    assert(score("hello world", "hello world") === 1.0)
  }

  test("completely different strings should return 0.0") {
    assert(score("hello", "world") === 0.0)
  }

  test("partial overlap should return value between 0.0 and 1.0") {
    val result = score("hello world", "hello spark")
    assert(result > 0.0)
    assert(result < 1.0)
  }

  test("both empty strings should return 1.0") {
    assert(score("", "") === 1.0)
  }

  test("one empty string should return 0.0") {
    assert(score("", "hello") === 0.0)
    assert(score("hello", "") === 0.0)
  }

  test("single token comparison") {
    assert(score("hello", "hello") === 1.0)
    assert(score("hello", "world") === 0.0)
  }

  test("multiple tokens with partial overlap") {
    // "a b c" vs "a b d" -> intersection: {a,b}, union: {a,b,c,d}
    // similarity = 2/4 = 0.5
    assert(score("a b c", "a b d") === 0.5)
  }

  test("different number of tokens") {
    // "a b" vs "a b c" -> intersection: {a,b}, union: {a,b,c}
    // similarity = 2/3
    val result = score("a b", "a b c")
    assert(result === 2.0 / 3.0)
  }

  test("duplicate tokens do not change cardinality") {
    assert(score("a a a b", "a b") === 1.0)
    assert(score("a a b", "a c c") === (1.0 / 3.0))
  }

  test("whitespace normalization handles mixed separators") {
    assert(score("hello  world", "hello world") === 1.0)
    assert(score("hello\tworld", "hello world") === 1.0)
    assert(score("hello\nworld", "hello world") === 1.0)
  }
}
