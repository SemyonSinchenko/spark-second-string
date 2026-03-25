package io.github.semyonsinchenko.sparkss.expressions.matrix

import io.github.semyonsinchenko.sparkss.expressions.MatrixMetricExpression
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class LcsSimilarity(left: Expression, right: Expression) extends MatrixMetricExpression {

  private final val LcsSimilarityModule =
    "io.github.semyonsinchenko.sparkss.expressions.matrix.LcsSimilarity$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  override protected def evalMatrixMetric(left: UTF8String, right: UTF8String): Double = {
    LcsSimilarity.similarity(left, right)
  }

  override protected def genMatrixMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$LcsSimilarityModule.similarity($leftValue, $rightValue)"
  }
}

object LcsSimilarity {

  private[sparkss] def similarity(left: UTF8String, right: UTF8String): Double = {
    val leftString = left.toString
    val rightString = right.toString
    val leftLength = leftString.length
    val rightLength = rightString.length

    val boundary = MatrixMetricKernelHelper.boundarySimilarity(leftLength, rightLength)
    if (MatrixMetricKernelHelper.hasBoundaryResult(boundary)) {
      return boundary
    }

    var previousRow = MatrixMetricKernelHelper.createWorkspaceRow(rightLength + 1)
    var currentRow = MatrixMetricKernelHelper.createWorkspaceRow(rightLength + 1)

    var i = 1
    while (i <= leftLength) {
      currentRow(0) = 0
      val leftChar = leftString.charAt(i - 1)

      var j = 1
      while (j <= rightLength) {
        if (leftChar == rightString.charAt(j - 1)) {
          currentRow(j) = previousRow(j - 1) + 1
        } else {
          currentRow(j) = Math.max(previousRow(j), currentRow(j - 1))
        }
        j += 1
      }

      val temp = previousRow
      previousRow = currentRow
      currentRow = temp
      i += 1
    }

    val maxLength = Math.max(leftLength, rightLength)
    val rawScore = previousRow(rightLength).toDouble / maxLength.toDouble
    MatrixMetricKernelHelper.clampToUnitInterval(rawScore)
  }
}
