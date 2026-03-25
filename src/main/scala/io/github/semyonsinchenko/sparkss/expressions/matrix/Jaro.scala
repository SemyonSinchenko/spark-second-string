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

  private[sparkss] def similarity(left: UTF8String, right: UTF8String): Double = {
    val leftString = left.toString
    val rightString = right.toString
    val leftLength = leftString.length
    val rightLength = rightString.length

    val boundary = MatrixMetricKernelHelper.boundarySimilarity(leftLength, rightLength)
    if (MatrixMetricKernelHelper.hasBoundaryResult(boundary)) {
      return boundary
    }

    val matchDistance = Math.max(0, (Math.max(leftLength, rightLength) / 2) - 1)
    val leftMatches = new Array[Boolean](leftLength)
    val rightMatches = new Array[Boolean](rightLength)

    var matches = 0
    var leftIndex = 0
    while (leftIndex < leftLength) {
      val windowStart = Math.max(0, leftIndex - matchDistance)
      val windowEnd = Math.min(leftIndex + matchDistance + 1, rightLength)

      var rightIndex = windowStart
      var found = false
      while (rightIndex < windowEnd && !found) {
        if (!rightMatches(rightIndex) && leftString.charAt(leftIndex) == rightString.charAt(rightIndex)) {
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
        if (leftString.charAt(leftIndex) != rightString.charAt(rightCursor)) {
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
