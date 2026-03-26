package io.github.semyonsinchenko.sparkss.expressions.matrix

import io.github.semyonsinchenko.sparkss.expressions.MatrixMetricExpression
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class SmithWaterman(left: Expression, right: Expression) extends MatrixMetricExpression {

  private final val SmithWatermanModule =
    "io.github.semyonsinchenko.sparkss.expressions.matrix.SmithWaterman$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  override protected def evalMatrixMetric(left: UTF8String, right: UTF8String): Double = {
    SmithWaterman.similarity(left, right)
  }

  override protected def genMatrixMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$SmithWatermanModule.similarity($leftValue, $rightValue)"
  }
}

object SmithWaterman {

  private final val MatchScore = 2
  private final val MismatchPenalty = -1
  private final val GapPenalty = -1

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
    java.util.Arrays.fill(previousRow, 0, rightLength + 1, 0)
    var bestScore = 0

    var i = 1
    while (i <= leftLength) {
      currentRow(0) = 0
      val leftChar = resolved.leftCharAt(i - 1)

      var j = 1
      while (j <= rightLength) {
        val substitutionScore = if (leftChar == resolved.rightCharAt(j - 1)) MatchScore else MismatchPenalty
        val diagonal = previousRow(j - 1) + substitutionScore
        val up = previousRow(j) + GapPenalty
        val leftCell = currentRow(j - 1) + GapPenalty
        val cellScore = Math.max(0, Math.max(diagonal, Math.max(up, leftCell)))
        currentRow(j) = cellScore

        if (cellScore > bestScore) {
          bestScore = cellScore
        }

        j += 1
      }

      val temp = previousRow
      previousRow = currentRow
      currentRow = temp
      i += 1
    }

    normalize(bestScore, leftLength, rightLength)
  }

  private def normalize(rawScore: Int, leftLength: Int, rightLength: Int): Double = {
    val maxScore = MatchScore.toDouble * Math.min(leftLength, rightLength).toDouble
    if (maxScore <= 0.0) {
      return 1.0
    }

    MatrixMetricKernelHelper.clampToUnitInterval(rawScore.toDouble / maxScore)
  }
}
