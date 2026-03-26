package io.github.semyonsinchenko.sparkss.expressions.matrix

import io.github.semyonsinchenko.sparkss.expressions.MatrixMetricExpression
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class JaroWinkler(left: Expression, right: Expression) extends MatrixMetricExpression {

  private final val JaroWinklerModule =
    "io.github.semyonsinchenko.sparkss.expressions.matrix.JaroWinkler$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  override protected def evalMatrixMetric(left: UTF8String, right: UTF8String): Double = {
    JaroWinkler.similarity(left, right)
  }

  override protected def genMatrixMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$JaroWinklerModule.similarity($leftValue, $rightValue)"
  }
}

object JaroWinkler {

  private final val PrefixScale = 0.1
  private final val PrefixCap = 4

  private[sparkss] def similarity(left: UTF8String, right: UTF8String): Double = {
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

    val maxPrefix = Math.min(Math.min(leftLength, rightLength), PrefixCap)
    var prefixLength = 0
    while (prefixLength < maxPrefix && resolved.leftCharAt(prefixLength) == resolved.rightCharAt(prefixLength)) {
      prefixLength += 1
    }

    val rawScore = jaro + (prefixLength.toDouble * PrefixScale * (1.0 - jaro))
    MatrixMetricKernelHelper.clampToUnitInterval(rawScore)
  }
}
