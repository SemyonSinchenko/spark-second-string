package io.github.semyonsinchenko.sparkss.expressions.matrix

import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.funsuite.AnyFunSuite

class LevenshteinSuite extends AnyFunSuite {

  private def score(left: String, right: String): Double = {
    Levenshtein.similarity(UTF8String.fromString(left), UTF8String.fromString(right))
  }

  test("identical strings should return 1.0") {
    assert(score("spark", "spark") === 1.0)
  }

  test("single edit should return normalized similarity") {
    assert(score("spark", "spork") === 0.8)
  }

  test("multi edit should return normalized similarity") {
    val result = score("kitten", "sitting")
    assert(result === (1.0 - (3.0 / 7.0)))
  }

  test("different lengths should be normalized by max length") {
    assert(score("ab", "abc") === (1.0 - (1.0 / 3.0)))
  }

  test("both empty strings should return 1.0") {
    assert(score("", "") === 1.0)
  }

  test("one empty string should return 0.0") {
    assert(score("", "abc") === 0.0)
    assert(score("abc", "") === 0.0)
  }
}
