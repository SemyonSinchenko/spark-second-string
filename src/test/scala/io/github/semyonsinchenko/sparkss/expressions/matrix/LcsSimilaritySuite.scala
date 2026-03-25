package io.github.semyonsinchenko.sparkss.expressions.matrix

import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.funsuite.AnyFunSuite

class LcsSimilaritySuite extends AnyFunSuite {

  private def score(left: String, right: String): Double = {
    LcsSimilarity.similarity(UTF8String.fromString(left), UTF8String.fromString(right))
  }

  test("identical strings should return 1.0") {
    assert(score("spark", "spark") === 1.0)
  }

  test("low-overlap strings should return low normalized score") {
    assert(score("abcde", "axxxx") === (1.0 / 5.0))
  }

  test("order-sensitive inputs should produce lower score than reorder-insensitive overlap") {
    assert(score("abcd", "dcba") === 0.25)
  }

  test("both empty strings should return 1.0") {
    assert(score("", "") === 1.0)
  }

  test("one empty string should return 0.0") {
    assert(score("", "abc") === 0.0)
    assert(score("abc", "") === 0.0)
  }

  test("different lengths should normalize by max length") {
    assert(score("abcdef", "ace") === 0.5)
  }
}
