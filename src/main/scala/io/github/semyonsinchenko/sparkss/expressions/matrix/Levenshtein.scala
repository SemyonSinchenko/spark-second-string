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

  private val workspace = new ThreadLocal[Array[Array[Int]]]

  private def getWorkspace(minSize: Int): Array[Array[Int]] = {
    val ws = workspace.get()
    if (ws != null && ws(0).length >= minSize) ws
    else {
      val newWs = Array(new Array[Int](minSize), new Array[Int](minSize))
      workspace.set(newWs)
      newWs
    }
  }

  private[sparkss] def similarity(left: UTF8String, right: UTF8String): Double = {
    val resolved = new MatrixMetricKernelHelper.ResolvedStrings(left, right)
    val leftLength = resolved.leftLength
    val rightLength = resolved.rightLength

    val boundary = MatrixMetricKernelHelper.boundarySimilarity(leftLength, rightLength)
    if (MatrixMetricKernelHelper.hasBoundaryResult(boundary)) {
      return boundary
    }

    val rows = getWorkspace(rightLength + 1)
    var previousRow = rows(0)
    var currentRow = rows(1)
    var index = 0
    while (index <= rightLength) {
      previousRow(index) = index
      index += 1
    }

    var i = 1
    while (i <= leftLength) {
      currentRow(0) = i
      val leftChar = resolved.leftCharAt(i - 1)

      var j = 1
      while (j <= rightLength) {
        val substitutionCost = if (leftChar == resolved.rightCharAt(j - 1)) 0 else 1
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
