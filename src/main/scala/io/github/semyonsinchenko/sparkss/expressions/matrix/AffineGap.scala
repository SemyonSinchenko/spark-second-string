package io.github.semyonsinchenko.sparkss.expressions.matrix

import io.github.semyonsinchenko.sparkss.expressions.MatrixMetricExpression
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult.{TypeCheckFailure, TypeCheckSuccess}
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class AffineGap(
    left: Expression,
    right: Expression,
    mismatchPenalty: Int = AffineGap.DefaultMismatchPenalty,
    gapOpenPenalty: Int = AffineGap.DefaultGapOpenPenalty,
    gapExtendPenalty: Int = AffineGap.DefaultGapExtendPenalty
) extends MatrixMetricExpression {

  private final val AffineGapModule = "io.github.semyonsinchenko.sparkss.expressions.matrix.AffineGap$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  override protected def evalMatrixMetric(left: UTF8String, right: UTF8String): Double = {
    AffineGap.similarity(left, right, mismatchPenalty, gapOpenPenalty, gapExtendPenalty)
  }

  override protected def genMatrixMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$AffineGapModule.similarity($leftValue, $rightValue, $mismatchPenalty, $gapOpenPenalty, $gapExtendPenalty)"
  }

  override def checkInputDataTypes(): TypeCheckResult = {
    super.checkInputDataTypes() match {
      case TypeCheckSuccess =>
        if (mismatchPenalty >= 0) {
          TypeCheckFailure(s"mismatchPenalty must be < 0, but got $mismatchPenalty")
        } else if (gapOpenPenalty >= 0) {
          TypeCheckFailure(s"gapOpenPenalty must be < 0, but got $gapOpenPenalty")
        } else if (gapExtendPenalty >= 0) {
          TypeCheckFailure(s"gapExtendPenalty must be < 0, but got $gapExtendPenalty")
        } else {
          TypeCheckSuccess
        }
      case failure => failure
    }
  }
}

object AffineGap {

  private[sparkss] final val DefaultMismatchPenalty = -1
  private[sparkss] final val DefaultGapOpenPenalty = -2
  private[sparkss] final val DefaultGapExtendPenalty = -1
  private final val Infinity = Int.MaxValue / 4

  private val workspace = new ThreadLocal[Array[Array[Int]]]

  private def getWorkspace(minSize: Int): Array[Array[Int]] = {
    val ws = workspace.get()
    if (ws != null && ws(0).length >= minSize) ws
    else {
      val newWs = Array.fill(6)(new Array[Int](minSize))
      workspace.set(newWs)
      newWs
    }
  }

  private[sparkss] def similarity(left: UTF8String, right: UTF8String): Double = {
    similarity(left, right, DefaultMismatchPenalty, DefaultGapOpenPenalty, DefaultGapExtendPenalty)
  }

  private[sparkss] def similarity(
      left: UTF8String,
      right: UTF8String,
      mismatchPenalty: Int,
      gapOpenPenalty: Int,
      gapExtendPenalty: Int
  ): Double = {
    val mismatchCost = -mismatchPenalty
    val gapOpenCost = -gapOpenPenalty
    val gapExtendCost = -gapExtendPenalty

    val resolved = new MatrixMetricKernelHelper.ResolvedStrings(left, right)
    val leftLength = resolved.leftLength
    val rightLength = resolved.rightLength

    val boundary = MatrixMetricKernelHelper.boundarySimilarity(leftLength, rightLength)
    if (MatrixMetricKernelHelper.hasBoundaryResult(boundary)) {
      return boundary
    }

    val rows = getWorkspace(rightLength + 1)
    var previousMatch = rows(0)
    var currentMatch = rows(1)
    var previousGapInLeft = rows(2)
    var currentGapInLeft = rows(3)
    var previousGapInRight = rows(4)
    var currentGapInRight = rows(5)

    java.util.Arrays.fill(previousMatch, 0, rightLength + 1, Infinity)
    java.util.Arrays.fill(currentMatch, 0, rightLength + 1, Infinity)
    java.util.Arrays.fill(previousGapInLeft, 0, rightLength + 1, Infinity)
    java.util.Arrays.fill(currentGapInLeft, 0, rightLength + 1, Infinity)
    java.util.Arrays.fill(previousGapInRight, 0, rightLength + 1, Infinity)
    java.util.Arrays.fill(currentGapInRight, 0, rightLength + 1, Infinity)

    previousMatch(0) = 0

    var j = 1
    while (j <= rightLength) {
      previousGapInLeft(j) = gapCost(j, gapOpenCost, gapExtendCost)
      j += 1
    }

    var i = 1
    while (i <= leftLength) {
      currentMatch(0) = Infinity
      currentGapInLeft(0) = Infinity
      currentGapInRight(0) = gapCost(i, gapOpenCost, gapExtendCost)

      val leftChar = resolved.leftCharAt(i - 1)

      j = 1
      while (j <= rightLength) {
        val substitution = if (leftChar == resolved.rightCharAt(j - 1)) 0 else mismatchCost

        currentMatch(j) = safePlus(
          min3(previousMatch(j - 1), previousGapInLeft(j - 1), previousGapInRight(j - 1)),
          substitution
        )

        currentGapInLeft(j) = min3(
          safePlus(currentMatch(j - 1), gapOpenCost + gapExtendCost),
          safePlus(currentGapInLeft(j - 1), gapExtendCost),
          safePlus(currentGapInRight(j - 1), gapOpenCost + gapExtendCost)
        )

        currentGapInRight(j) = min3(
          safePlus(previousMatch(j), gapOpenCost + gapExtendCost),
          safePlus(previousGapInRight(j), gapExtendCost),
          safePlus(previousGapInLeft(j), gapOpenCost + gapExtendCost)
        )

        j += 1
      }

      val tempMatch = previousMatch
      previousMatch = currentMatch
      currentMatch = tempMatch

      val tempGapInLeft = previousGapInLeft
      previousGapInLeft = currentGapInLeft
      currentGapInLeft = tempGapInLeft

      val tempGapInRight = previousGapInRight
      previousGapInRight = currentGapInRight
      currentGapInRight = tempGapInRight

      i += 1
    }

    val distance = min3(
      previousMatch(rightLength),
      previousGapInLeft(rightLength),
      previousGapInRight(rightLength)
    )
    MatrixMetricKernelHelper.normalizeDistance(distance, leftLength, rightLength)
  }

  private def safePlus(value: Int, increment: Int): Int = {
    if (value >= Infinity - increment) {
      Infinity
    } else {
      value + increment
    }
  }

  private def min3(first: Int, second: Int, third: Int): Int = {
    Math.min(first, Math.min(second, third))
  }

  private def gapCost(length: Int, gapOpenPenalty: Int, gapExtendPenalty: Int): Int = {
    gapOpenPenalty + (length * gapExtendPenalty)
  }
}
