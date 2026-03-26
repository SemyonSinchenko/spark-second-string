package io.github.semyonsinchenko.sparkss.expressions.matrix

import io.github.semyonsinchenko.sparkss.expressions.MatrixMetricExpression
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class Jaro(left: Expression, right: Expression) extends MatrixMetricExpression {

  private final val JaroModule = "io.github.semyonsinchenko.sparkss.expressions.matrix.Jaro$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  override protected def evalMatrixMetric(left: UTF8String, right: UTF8String): Double = {
    Jaro.similarity(left, right)
  }

  override protected def genMatrixMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$JaroModule.similarity($leftValue, $rightValue)"
  }
}

object Jaro {

  private val workspace = new ThreadLocal[Array[Array[Boolean]]]

  private def getWorkspace(leftSize: Int, rightSize: Int): Array[Array[Boolean]] = {
    val minSize = Math.max(leftSize, rightSize)
    val ws = workspace.get()
    if (ws != null && ws(0).length >= minSize) ws
    else {
      val newWs = Array(new Array[Boolean](minSize), new Array[Boolean](minSize))
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

    computeFromResolved(resolved, leftLength, rightLength)
  }

  private[matrix] def computeFromResolved(
      resolved: MatrixMetricKernelHelper.ResolvedStrings,
      leftLength: Int,
      rightLength: Int
  ): Double = {
    val matchDistance = Math.max(0, (Math.max(leftLength, rightLength) / 2) - 1)
    val rows = getWorkspace(leftLength, rightLength)
    val leftMatches = rows(0)
    val rightMatches = rows(1)
    java.util.Arrays.fill(leftMatches, 0, leftLength, false)
    java.util.Arrays.fill(rightMatches, 0, rightLength, false)

    var matches = 0
    var leftIndex = 0
    while (leftIndex < leftLength) {
      val windowStart = Math.max(0, leftIndex - matchDistance)
      val windowEnd = Math.min(leftIndex + matchDistance + 1, rightLength)

      var rightIndex = windowStart
      var found = false
      while (rightIndex < windowEnd && !found) {
        if (!rightMatches(rightIndex) && resolved.leftCharAt(leftIndex) == resolved.rightCharAt(rightIndex)) {
          leftMatches(leftIndex) = true
          rightMatches(rightIndex) = true
          matches += 1
          found = true
        }
        rightIndex += 1
      }
      leftIndex += 1
    }

    if (matches == 0) {
      return 0.0
    }

    var transpositions = 0
    var rightCursor = 0
    leftIndex = 0
    while (leftIndex < leftLength) {
      if (leftMatches(leftIndex)) {
        while (!rightMatches(rightCursor)) {
          rightCursor += 1
        }
        if (resolved.leftCharAt(leftIndex) != resolved.rightCharAt(rightCursor)) {
          transpositions += 1
        }
        rightCursor += 1
      }
      leftIndex += 1
    }

    val matchesAsDouble = matches.toDouble
    val transpositionsAsDouble = transpositions.toDouble / 2.0
    val rawScore = (
      (matchesAsDouble / leftLength.toDouble) +
        (matchesAsDouble / rightLength.toDouble) +
        ((matchesAsDouble - transpositionsAsDouble) / matchesAsDouble)
    ) / 3.0

    MatrixMetricKernelHelper.clampToUnitInterval(rawScore)
  }
}
