package io.github.semyonsinchenko.sparkss.expressions.matrix

import io.github.semyonsinchenko.sparkss.expressions.MatrixMetricExpression
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class Levenshtein(left: Expression, right: Expression) extends MatrixMetricExpression {

  private final val LevenshteinModule = "io.github.semyonsinchenko.sparkss.expressions.matrix.Levenshtein$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  override protected def evalMatrixMetric(left: UTF8String, right: UTF8String): Double = {
    Levenshtein.similarity(left, right)
  }

  override protected def genMatrixMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$LevenshteinModule.similarity($leftValue, $rightValue)"
  }
}

object Levenshtein {

  private[sparkss] def similarity(left: UTF8String, right: UTF8String): Double = {
    val leftString = left.toString
    val rightString = right.toString
    val leftLength = leftString.length
    val rightLength = rightString.length

    val boundary = MatrixMetricKernelHelper.boundarySimilarity(leftLength, rightLength)
    if (MatrixMetricKernelHelper.hasBoundaryResult(boundary)) {
      return boundary
    }

    var previousRow = MatrixMetricKernelHelper.createInitializedDistanceRow(rightLength + 1)
    var currentRow = MatrixMetricKernelHelper.createWorkspaceRow(rightLength + 1)

    var i = 1
    while (i <= leftLength) {
      currentRow(0) = i
      val leftChar = leftString.charAt(i - 1)

      var j = 1
      while (j <= rightLength) {
        val substitutionCost = if (leftChar == rightString.charAt(j - 1)) 0 else 1
        val deletion = previousRow(j) + 1
        val insertion = currentRow(j - 1) + 1
        val substitution = previousRow(j - 1) + substitutionCost
        currentRow(j) = Math.min(Math.min(deletion, insertion), substitution)
        j += 1
      }

      val temp = previousRow
      previousRow = currentRow
      currentRow = temp
      i += 1
    }

    MatrixMetricKernelHelper.normalizeDistance(previousRow(rightLength), leftLength, rightLength)
  }
}
