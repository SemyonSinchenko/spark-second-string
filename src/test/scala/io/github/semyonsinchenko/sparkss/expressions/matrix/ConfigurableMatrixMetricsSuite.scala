package io.github.semyonsinchenko.sparkss.expressions.matrix

import org.apache.spark.sql.catalyst.analysis.TypeCheckResult.{TypeCheckFailure, TypeCheckSuccess}
import org.apache.spark.sql.catalyst.expressions.Literal
import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.funsuite.AnyFunSuite

class ConfigurableMatrixMetricsSuite extends AnyFunSuite {

  private def utf8(value: String): UTF8String = UTF8String.fromString(value)

  test("default-parameter overloads preserve legacy matrix behavior") {
    val left = utf8("spark")
    val right = utf8("spork")

    assert(JaroWinkler.similarity(left, right) === JaroWinkler.similarity(left, right, 0.1, 4))
    assert(NeedlemanWunsch.similarity(left, right) === NeedlemanWunsch.similarity(left, right, 1, -1, -1))
    assert(SmithWaterman.similarity(left, right) === SmithWaterman.similarity(left, right, 2, -1, -1))
    assert(AffineGap.similarity(left, right) === AffineGap.similarity(left, right, 1, 2, 1))
  }

  test("custom matrix parameters affect scores") {
    val left = utf8("martha")
    val right = utf8("marhta")

    val jwDefault = JaroWinkler.similarity(left, right, 0.1, 4)
    val jwCustom = JaroWinkler.similarity(left, right, 0.2, 6)
    assert(jwCustom >= jwDefault)

    val nwDefault = NeedlemanWunsch.similarity(left, right, 1, -1, -1)
    val nwCustom = NeedlemanWunsch.similarity(left, right, 2, -2, -1)
    assert(nwCustom !== nwDefault)

    val swDefault = SmithWaterman.similarity(left, right, 2, -1, -1)
    val swCustom = SmithWaterman.similarity(left, right, 3, -1, -2)
    assert(swCustom !== swDefault)

    val affineDefault = AffineGap.similarity(left, right, 1, 2, 1)
    val affineCustom = AffineGap.similarity(left, right, 2, 3, 2)
    assert(affineCustom !== affineDefault)
  }

  test("analysis-time validation rejects invalid matrix parameters") {
    assert(JaroWinkler(Literal("a"), Literal("b"), 0.0, 4).checkInputDataTypes().isInstanceOf[TypeCheckFailure])
    assert(JaroWinkler(Literal("a"), Literal("b"), 0.1, 11).checkInputDataTypes().isInstanceOf[TypeCheckFailure])

    assert(
      NeedlemanWunsch(Literal("a"), Literal("b"), 0, -1, -1).checkInputDataTypes().isInstanceOf[TypeCheckFailure]
    )
    assert(
      SmithWaterman(Literal("a"), Literal("b"), 2, 1, -1).checkInputDataTypes().isInstanceOf[TypeCheckFailure]
    )
    assert(AffineGap(Literal("a"), Literal("b"), 1, 0, 1).checkInputDataTypes().isInstanceOf[TypeCheckFailure])

    assert(JaroWinkler(Literal("a"), Literal("b"), 0.1, 4).checkInputDataTypes() === TypeCheckSuccess)
  }

  test("withNewChildren preserves non-default matrix parameters") {
    val expr = SmithWaterman(Literal("left"), Literal("right"), matchScore = 5, mismatchPenalty = -2, gapPenalty = -3)
    val replaced = expr.withNewChildren(Seq(Literal("a"), Literal("b"))).asInstanceOf[SmithWaterman]

    assert(replaced.matchScore === 5)
    assert(replaced.mismatchPenalty === -2)
    assert(replaced.gapPenalty === -3)
  }
}
