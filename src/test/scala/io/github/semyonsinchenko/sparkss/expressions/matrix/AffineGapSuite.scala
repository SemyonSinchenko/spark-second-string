package io.github.semyonsinchenko.sparkss.expressions.matrix

import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.funsuite.AnyFunSuite

class AffineGapSuite extends AnyFunSuite {

  private def score(left: String, right: String): Double = {
    AffineGap.similarity(UTF8String.fromString(left), UTF8String.fromString(right))
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

  test("representative content classes remain deterministic and normalized") {
    val fixtures = Seq(
      ("   ", "  "),
      ("a,b.c!", "a b c?"),
      ("aaaa", "aaab"),
      ("abcdef", "abc"),
      ("alpha beta gamma", "alpha gamma")
    )

    fixtures.foreach { case (left, right) =>
      val first = score(left, right)
      val second = score(left, right)
      assert(first === second)
      assert(first >= 0.0)
      assert(first <= 1.0)
    }
  }

  test("normalization clamps out-of-range affine distances") {
    val normalized = MatrixMetricKernelHelper.normalizeDistance(distance = 20, leftLength = 4, rightLength = 4)
    assert(normalized === 0.0)
  }
}
