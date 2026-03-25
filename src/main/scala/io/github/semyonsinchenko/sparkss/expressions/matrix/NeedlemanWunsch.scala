package io.github.semyonsinchenko.sparkss.expressions.matrix

import io.github.semyonsinchenko.sparkss.expressions.MatrixMetricExpression
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class NeedlemanWunsch(left: Expression, right: Expression) extends MatrixMetricExpression {

  private final val NeedlemanWunschModule =
    "io.github.semyonsinchenko.sparkss.expressions.matrix.NeedlemanWunsch$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  override protected def evalMatrixMetric(left: UTF8String, right: UTF8String): Double = {
    NeedlemanWunsch.similarity(left, right)
  }

  override protected def genMatrixMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$NeedlemanWunschModule.similarity($leftValue, $rightValue)"
  }
}

object NeedlemanWunsch {

  private final val MatchScore = 1
  private final val MismatchPenalty = -1
  private final val GapPenalty = -1

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

    var j = 0
    while (j <= rightLength) {
      previousRow(j) = j * GapPenalty
      j += 1
    }

    var i = 1
    while (i <= leftLength) {
      currentRow(0) = i * GapPenalty
      val leftChar = leftString.charAt(i - 1)

      j = 1
      while (j <= rightLength) {
        val rightChar = rightString.charAt(j - 1)
        val substitutionScore = if (leftChar == rightChar) MatchScore else MismatchPenalty
        val diagonal = previousRow(j - 1) + substitutionScore
        val up = previousRow(j) + GapPenalty
        val leftCell = currentRow(j - 1) + GapPenalty
        currentRow(j) = Math.max(Math.max(diagonal, up), leftCell)
        j += 1
      }

      val temp = previousRow
      previousRow = currentRow
      currentRow = temp
      i += 1
    }

    normalize(previousRow(rightLength), leftLength, rightLength)
  }

  private def normalize(rawScore: Int, leftLength: Int, rightLength: Int): Double = {
    val maxLength = Math.max(leftLength, rightLength)
    if (maxLength == 0) {
      return 1.0
    }

    val normalized = (rawScore.toDouble + maxLength.toDouble) / (2.0 * maxLength.toDouble)
    MatrixMetricKernelHelper.clampToUnitInterval(normalized)
  }
}
