package io.github.semyonsinchenko.sparkss.expressions.matrix

import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.funsuite.AnyFunSuite

class NeedlemanWunschSuite extends AnyFunSuite {

  private def score(left: String, right: String): Double = {
    NeedlemanWunsch.similarity(UTF8String.fromString(left), UTF8String.fromString(right))
  }

  test("both empty strings should return 1.0") {
    assert(score("", "") === 1.0)
  }

  test("one empty string should return 0.0") {
    assert(score("", "abc") === 0.0)
    assert(score("abc", "") === 0.0)
  }

  test("identical strings should return 1.0") {
    assert(score("spark", "spark") === 1.0)
  }

  test("no-overlap strings should return 0.0") {
    assert(score("abc", "xyz") === 0.0)
  }

  test("repeated characters should be scored deterministically") {
    val first = score("aaaa", "aaab")
    val second = score("aaaa", "aaab")
    assert(first === 0.75)
    assert(second === first)
  }

  test("asymmetric-length strings remain bounded") {
    val result = score("abcdef", "abc")
    assert(result >= 0.0)
    assert(result <= 1.0)
  }

  test("whitespace-only strings remain bounded") {
    val result = score("   ", " ")
    assert(result >= 0.0)
    assert(result <= 1.0)
  }

  test("punctuation-bearing strings remain bounded") {
    val result = score("a,b.c!", "a b c?")
    assert(result >= 0.0)
    assert(result <= 1.0)
  }
}
