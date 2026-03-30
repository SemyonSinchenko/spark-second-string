package io.github.semyonsinchenko.sparkss.expressions.matrix

import io.github.semyonsinchenko.sparkss.expressions.MatrixMetricExpression
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult.{TypeCheckFailure, TypeCheckSuccess}
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class JaroWinkler(
    left: Expression,
    right: Expression,
    prefixScale: Double = JaroWinkler.DefaultPrefixScale,
    prefixCap: Int = JaroWinkler.DefaultPrefixCap
) extends MatrixMetricExpression {

  private final val JaroWinklerModule =
    "io.github.semyonsinchenko.sparkss.expressions.matrix.JaroWinkler$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  override protected def evalMatrixMetric(left: UTF8String, right: UTF8String): Double = {
    JaroWinkler.similarity(left, right, prefixScale, prefixCap)
  }

  override protected def genMatrixMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$JaroWinklerModule.similarity($leftValue, $rightValue, $prefixScale, $prefixCap)"
  }

  override def checkInputDataTypes(): TypeCheckResult = {
    super.checkInputDataTypes() match {
      case TypeCheckSuccess =>
        if (prefixScale <= 0.0 || prefixScale > 0.25) {
          TypeCheckFailure(s"prefixScale must be in (0.0, 0.25], but got $prefixScale")
        } else if (prefixCap < 1 || prefixCap > 10) {
          TypeCheckFailure(s"prefixCap must be in [1, 10], but got $prefixCap")
        } else {
          TypeCheckSuccess
        }
      case failure => failure
    }
  }
}

object JaroWinkler {

  private[sparkss] final val DefaultPrefixScale = 0.1
  private[sparkss] final val DefaultPrefixCap = 4

  private[sparkss] def similarity(left: UTF8String, right: UTF8String): Double = {
    similarity(left, right, DefaultPrefixScale, DefaultPrefixCap)
  }

  private[sparkss] def similarity(left: UTF8String, right: UTF8String, prefixScale: Double, prefixCap: Int): Double = {
    val resolved = new MatrixMetricKernelHelper.ResolvedStrings(left, right)
    val leftLength = resolved.leftLength
    val rightLength = resolved.rightLength

    val boundary = MatrixMetricKernelHelper.boundarySimilarity(leftLength, rightLength)
    if (MatrixMetricKernelHelper.hasBoundaryResult(boundary)) {
      return boundary
    }

    val jaro = Jaro.computeFromResolved(resolved, leftLength, rightLength)
    if (jaro <= 0.0) {
      return 0.0
    }

    val maxPrefix = Math.min(Math.min(leftLength, rightLength), prefixCap)
    var prefixLength = 0
    while (prefixLength < maxPrefix && resolved.leftCharAt(prefixLength) == resolved.rightCharAt(prefixLength)) {
      prefixLength += 1
    }

    val rawScore = jaro + (prefixLength.toDouble * prefixScale * (1.0 - jaro))
    MatrixMetricKernelHelper.clampToUnitInterval(rawScore)
  }
}
