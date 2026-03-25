package io.github.semyonsinchenko.sparkss.expressions.matrix

import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.funsuite.AnyFunSuite

class JaroSuite extends AnyFunSuite {

  private def score(left: String, right: String): Double = {
    Jaro.similarity(UTF8String.fromString(left), UTF8String.fromString(right))
  }

  private def assertClose(actual: Double, expected: Double): Unit = {
    assert(Math.abs(actual - expected) <= 1e-12)
  }

  test("identical strings should return 1.0") {
    assert(score("spark", "spark") === 1.0)
  }

  test("single transposition should match canonical jaro score") {
    assertClose(score("martha", "marhta"), 17.0 / 18.0)
  }

  test("partial overlap should match canonical jaro score") {
    assertClose(score("dwayne", "duane"), 37.0 / 45.0)
  }

  test("disjoint strings should return 0.0") {
    assert(score("abc", "xyz") === 0.0)
  }

  test("repeated character inputs should be handled deterministically") {
    assertClose(score("aaaa", "aaab"), 5.0 / 6.0)
    assertClose(score("aaaa", "aaab"), 5.0 / 6.0)
  }

  test("asymmetric lengths should normalize to a bounded score") {
    assertClose(score("abcd", "abc"), 11.0 / 12.0)
  }

  test("both empty strings should return 1.0") {
    assert(score("", "") === 1.0)
  }

  test("one empty string should return 0.0") {
    assert(score("", "abc") === 0.0)
    assert(score("abc", "") === 0.0)
  }

  test("scores remain clamped to [0.0, 1.0]") {
    val samples = Seq(
      score("martha", "marhta"),
      score("aaaa", "aaab"),
      score("abc", "xyz")
    )
    assert(samples.forall(value => value >= 0.0 && value <= 1.0))
  }
}
