package io.github.semyonsinchenko.sparkss.expressions.token

import org.scalatest.funsuite.AnyFunSuite

/** Test suite for Jaccard similarity getSim method.
  */
class JaccardSuite extends AnyFunSuite {

  private def jaccard: Jaccard = Jaccard(null, null)

  test("identical strings should return 1.0") {
    assert(jaccard.getSim("hello world", "hello world") === 1.0)
  }

  test("completely different strings should return 0.0") {
    assert(jaccard.getSim("hello", "world") === 0.0)
  }

  test("partial overlap should return value between 0.0 and 1.0") {
    val result = jaccard.getSim("hello world", "hello spark")
    assert(result > 0.0)
    assert(result < 1.0)
  }

  test("both empty strings should return 1.0") {
    assert(jaccard.getSim("", "") === 1.0)
  }

  test("one empty string should return 0.0") {
    assert(jaccard.getSim("", "hello") === 0.0)
    assert(jaccard.getSim("hello", "") === 0.0)
  }

  test("single token comparison") {
    assert(jaccard.getSim("hello", "hello") === 1.0)
    assert(jaccard.getSim("hello", "world") === 0.0)
  }

  test("multiple tokens with partial overlap") {
    // "a b c" vs "a b d" -> intersection: {a,b}, union: {a,b,c,d}
    // similarity = 2/4 = 0.5
    assert(jaccard.getSim("a b c", "a b d") === 0.5)
  }

  test("different number of tokens") {
    // "a b" vs "a b c" -> intersection: {a,b}, union: {a,b,c}
    // similarity = 2/3
    val result = jaccard.getSim("a b", "a b c")
    assert(result === 2.0 / 3.0)
  }

  test("whitespace handling") {
    assert(jaccard.getSim("hello  world", "hello world") === 1.0)
  }
}
