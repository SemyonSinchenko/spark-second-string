package io.github.semyonsinchenko.sparkss.expressions.matrix

import io.github.semyonsinchenko.sparkss.expressions.MatrixMetricExpression
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class AffineGap(left: Expression, right: Expression) extends MatrixMetricExpression {

  private final val AffineGapModule = "io.github.semyonsinchenko.sparkss.expressions.matrix.AffineGap$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  override protected def evalMatrixMetric(left: UTF8String, right: UTF8String): Double = {
    AffineGap.similarity(left, right)
  }

  override protected def genMatrixMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$AffineGapModule.similarity($leftValue, $rightValue)"
  }
}

object AffineGap {

  private final val MismatchPenalty = 1
  private final val GapOpenPenalty = 2
  private final val GapExtendPenalty = 1
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
      previousGapInLeft(j) = gapCost(j)
      j += 1
    }

    var i = 1
    while (i <= leftLength) {
      currentMatch(0) = Infinity
      currentGapInLeft(0) = Infinity
      currentGapInRight(0) = gapCost(i)

      val leftChar = resolved.leftCharAt(i - 1)

      j = 1
      while (j <= rightLength) {
        val substitution = if (leftChar == resolved.rightCharAt(j - 1)) 0 else MismatchPenalty

        currentMatch(j) = safePlus(
          min3(previousMatch(j - 1), previousGapInLeft(j - 1), previousGapInRight(j - 1)),
          substitution
        )

        currentGapInLeft(j) = min3(
          safePlus(currentMatch(j - 1), GapOpenPenalty + GapExtendPenalty),
          safePlus(currentGapInLeft(j - 1), GapExtendPenalty),
          safePlus(currentGapInRight(j - 1), GapOpenPenalty + GapExtendPenalty)
        )

        currentGapInRight(j) = min3(
          safePlus(previousMatch(j), GapOpenPenalty + GapExtendPenalty),
          safePlus(previousGapInRight(j), GapExtendPenalty),
          safePlus(previousGapInLeft(j), GapOpenPenalty + GapExtendPenalty)
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

  private def gapCost(length: Int): Int = {
    GapOpenPenalty + (length * GapExtendPenalty)
  }
}
