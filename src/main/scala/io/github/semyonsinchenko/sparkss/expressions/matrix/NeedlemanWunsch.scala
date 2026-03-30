package io.github.semyonsinchenko.sparkss.expressions.matrix

import io.github.semyonsinchenko.sparkss.expressions.MatrixMetricExpression
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult.{TypeCheckFailure, TypeCheckSuccess}
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class NeedlemanWunsch(
    left: Expression,
    right: Expression,
    matchScore: Int = NeedlemanWunsch.DefaultMatchScore,
    mismatchPenalty: Int = NeedlemanWunsch.DefaultMismatchPenalty,
    gapPenalty: Int = NeedlemanWunsch.DefaultGapPenalty
) extends MatrixMetricExpression {

  private final val NeedlemanWunschModule =
    "io.github.semyonsinchenko.sparkss.expressions.matrix.NeedlemanWunsch$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  override protected def evalMatrixMetric(left: UTF8String, right: UTF8String): Double = {
    NeedlemanWunsch.similarity(left, right, matchScore, mismatchPenalty, gapPenalty)
  }

  override protected def genMatrixMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$NeedlemanWunschModule.similarity($leftValue, $rightValue, $matchScore, $mismatchPenalty, $gapPenalty)"
  }

  override def checkInputDataTypes(): TypeCheckResult = {
    super.checkInputDataTypes() match {
      case TypeCheckSuccess =>
        if (matchScore <= 0) {
          TypeCheckFailure(s"matchScore must be > 0, but got $matchScore")
        } else if (mismatchPenalty >= 0) {
          TypeCheckFailure(s"mismatchPenalty must be < 0, but got $mismatchPenalty")
        } else if (gapPenalty >= 0) {
          TypeCheckFailure(s"gapPenalty must be < 0, but got $gapPenalty")
        } else {
          TypeCheckSuccess
        }
      case failure => failure
    }
  }
}

object NeedlemanWunsch {

  private[sparkss] final val DefaultMatchScore = 1
  private[sparkss] final val DefaultMismatchPenalty = -1
  private[sparkss] final val DefaultGapPenalty = -1

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
    similarity(left, right, DefaultMatchScore, DefaultMismatchPenalty, DefaultGapPenalty)
  }

  private[sparkss] def similarity(
      left: UTF8String,
      right: UTF8String,
      matchScore: Int,
      mismatchPenalty: Int,
      gapPenalty: Int
  ): Double = {
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

    var j = 0
    while (j <= rightLength) {
      previousRow(j) = j * gapPenalty
      j += 1
    }

    var i = 1
    while (i <= leftLength) {
      currentRow(0) = i * gapPenalty
      val leftChar = resolved.leftCharAt(i - 1)

      j = 1
      while (j <= rightLength) {
        val rightChar = resolved.rightCharAt(j - 1)
        val substitutionScore = if (leftChar == rightChar) matchScore else mismatchPenalty
        val diagonal = previousRow(j - 1) + substitutionScore
        val up = previousRow(j) + gapPenalty
        val leftCell = currentRow(j - 1) + gapPenalty
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
