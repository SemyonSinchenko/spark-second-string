package io.github.semyonsinchenko.sparkss.expressions.matrix

import io.github.semyonsinchenko.sparkss.expressions.support.ScoreAssertions
import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.funsuite.AnyFunSuite

class SmithWatermanSuite extends AnyFunSuite with ScoreAssertions {

  private def score(left: String, right: String): Double = {
    SmithWaterman.similarity(UTF8String.fromString(left), UTF8String.fromString(right))
  }

  private def assertClose(actual: Double, expected: Double): Unit = {
    assert(Math.abs(actual - expected) <= 1e-12)
  }

  test("identical strings should return 1.0") {
    assert(score("spark", "spark") === 1.0)
  }

  test("both empty strings should return 1.0") {
    assert(score("", "") === 1.0)
  }

  test("one empty string should return 0.0") {
    assert(score("", "abc") === 0.0)
    assert(score("abc", "") === 0.0)
  }

  test("disjoint strings should return 0.0") {
    assert(score("abc", "xyz") === 0.0)
  }

  test("canonical local alignment score remains stable") {
    assertClose(score("ACACACTA", "AGCACACA"), 0.75)
  }

  test("substring match should score high") {
    assertClose(score("abc", "xabcx"), 1.0)
  }

  test("repeated characters should be scored deterministically") {
    assertDeterministicAndBounded(score("aaaa", "aaab"))
  }

  test("asymmetric-length strings remain bounded") {
    assertBounded(score("abcdef", "abc"))
  }

  test("whitespace-only strings remain bounded") {
    assertBounded(score("   ", " "))
  }

  test("punctuation-bearing strings remain bounded") {
    assertBounded(score("a,b.c!", "a b c?"))
  }

  test("normalization uses matchScore times min-length") {
    assertClose(score("abc", "xabcx"), 1.0)
  }
}
