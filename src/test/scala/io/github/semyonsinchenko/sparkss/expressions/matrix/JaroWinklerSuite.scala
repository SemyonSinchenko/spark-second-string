package io.github.semyonsinchenko.sparkss.expressions.matrix

import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.funsuite.AnyFunSuite

class JaroWinklerSuite extends AnyFunSuite {

  private def score(left: String, right: String): Double = {
    JaroWinkler.similarity(UTF8String.fromString(left), UTF8String.fromString(right))
  }

  private def assertClose(actual: Double, expected: Double): Unit = {
    assert(Math.abs(actual - expected) <= 1e-12)
  }

  test("both empty strings should return 1.0") {
    assert(score("", "") === 1.0)
  }

  test("one empty string should return 0.0") {
    assert(score("", "abc") === 0.0)
    assert(score("abc", "") === 0.0)
  }

  test("no matching characters should return 0.0") {
    assert(score("abc", "xyz") === 0.0)
  }

  test("identical strings should return 1.0") {
    assert(score("spark", "spark") === 1.0)
  }

  test("canonical examples should match known jaro winkler scores") {
    assertClose(score("martha", "marhta"), 0.9611111111111111)
    assertClose(score("dwayne", "duane"), 0.8400000000000001)
  }

  test("repeated character inputs should be deterministic") {
    val first = score("aaaa", "aaab")
    val second = score("aaaa", "aaab")
    assertClose(first, 0.8833333333333333)
    assert(first === second)
  }

  test("asymmetric lengths should remain in [0.0, 1.0]") {
    val result = score("abcdefghijklmnopqrstuvwxyz", "abc")
    assert(result >= 0.0)
    assert(result <= 1.0)
  }
}
