package io.github.semyonsinchenko.sparkss.expressions.matrix

import org.apache.spark.sql.catalyst.analysis.TypeCheckResult.TypeCheckFailure
import org.apache.spark.sql.catalyst.expressions.Literal
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

  test("analysis-time validation rejects non-negative affine penalties") {
    val zeroMismatch = AffineGap(Literal("a"), Literal("b"), 0, -2, -1).checkInputDataTypes()
    val positiveMismatch = AffineGap(Literal("a"), Literal("b"), 1, -2, -1).checkInputDataTypes()
    val zeroGapOpen = AffineGap(Literal("a"), Literal("b"), -1, 0, -1).checkInputDataTypes()
    val positiveGapOpen = AffineGap(Literal("a"), Literal("b"), -1, 2, -1).checkInputDataTypes()
    val zeroGapExtend = AffineGap(Literal("a"), Literal("b"), -1, -2, 0).checkInputDataTypes()
    val positiveGapExtend = AffineGap(Literal("a"), Literal("b"), -1, -2, 3).checkInputDataTypes()

    assert(zeroMismatch.isInstanceOf[TypeCheckFailure])
    assert(positiveMismatch.isInstanceOf[TypeCheckFailure])
    assert(zeroGapOpen.isInstanceOf[TypeCheckFailure])
    assert(positiveGapOpen.isInstanceOf[TypeCheckFailure])
    assert(zeroGapExtend.isInstanceOf[TypeCheckFailure])
    assert(positiveGapExtend.isInstanceOf[TypeCheckFailure])

    assert(zeroMismatch.asInstanceOf[TypeCheckFailure].message.contains("mismatchPenalty must be < 0"))
    assert(positiveMismatch.asInstanceOf[TypeCheckFailure].message.contains("mismatchPenalty must be < 0"))
    assert(zeroGapOpen.asInstanceOf[TypeCheckFailure].message.contains("gapOpenPenalty must be < 0"))
    assert(positiveGapOpen.asInstanceOf[TypeCheckFailure].message.contains("gapOpenPenalty must be < 0"))
    assert(zeroGapExtend.asInstanceOf[TypeCheckFailure].message.contains("gapExtendPenalty must be < 0"))
    assert(positiveGapExtend.asInstanceOf[TypeCheckFailure].message.contains("gapExtendPenalty must be < 0"))
  }
}
