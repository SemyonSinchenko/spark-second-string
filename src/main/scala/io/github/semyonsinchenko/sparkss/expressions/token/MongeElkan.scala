package io.github.semyonsinchenko.sparkss.expressions.token

import io.github.semyonsinchenko.sparkss.expressions.TokenMetricExpression
import io.github.semyonsinchenko.sparkss.expressions.matrix.JaroWinkler
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class MongeElkan(left: Expression, right: Expression) extends TokenMetricExpression {

  private final val MongeElkanModule = "io.github.semyonsinchenko.sparkss.expressions.token.MongeElkan$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  override protected def evalTokenMetric(left: UTF8String, right: UTF8String): Double = {
    MongeElkan.similarity(left, right)
  }

  override protected def genTokenMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$MongeElkanModule.similarity($leftValue, $rightValue)"
  }
}

object MongeElkan {

  private[sparkss] def similarity(left: UTF8String, right: UTF8String): Double = {
    val leftTokens = TokenMetricKernelHelper.tokenizeToSequence(left.toString)
    val rightTokens = TokenMetricKernelHelper.tokenizeToSequence(right.toString)

    val leftSize = leftTokens.size()
    val rightSize = rightTokens.size()

    if (leftSize == 0 && rightSize == 0) {
      return 1.0
    }
    if (leftSize == 0 || rightSize == 0) {
      return 0.0
    }

    val rawScore = (directedSimilarity(leftTokens, rightTokens) + directedSimilarity(rightTokens, leftTokens)) / 2.0
    TokenMetricKernelHelper.clampToUnitInterval(rawScore)
  }

  private def directedSimilarity(
      leftTokens: java.util.ArrayList[String],
      rightTokens: java.util.ArrayList[String]
  ): Double = {
    val leftSize = leftTokens.size()
    val rightSize = rightTokens.size()

    var sumBest = 0.0
    var leftIndex = 0
    while (leftIndex < leftSize) {
      val leftToken = UTF8String.fromString(leftTokens.get(leftIndex))
      var best = 0.0

      var rightIndex = 0
      while (rightIndex < rightSize) {
        val candidate = JaroWinkler.similarity(leftToken, UTF8String.fromString(rightTokens.get(rightIndex)))
        if (candidate > best) {
          best = candidate
        }
        rightIndex += 1
      }

      sumBest += best
      leftIndex += 1
    }

    sumBest / leftSize.toDouble
  }
}
