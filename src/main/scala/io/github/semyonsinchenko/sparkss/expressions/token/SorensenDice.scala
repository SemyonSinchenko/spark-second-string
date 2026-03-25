package io.github.semyonsinchenko.sparkss.expressions.token

import io.github.semyonsinchenko.sparkss.expressions.TokenMetricExpression
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class SorensenDice(left: Expression, right: Expression) extends TokenMetricExpression {

  private final val SorensenDiceModule =
    "io.github.semyonsinchenko.sparkss.expressions.token.SorensenDice$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  override protected def evalTokenMetric(left: UTF8String, right: UTF8String): Double = {
    SorensenDice.similarity(left, right)
  }

  override protected def genTokenMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$SorensenDiceModule.similarity($leftValue, $rightValue)"
  }
}

object SorensenDice {

  private[sparkss] def similarity(left: UTF8String, right: UTF8String): Double = {
    val leftString = left.toString
    val rightString = right.toString

    if (leftString.isEmpty && rightString.isEmpty) {
      return 1.0
    }
    if (leftString.isEmpty || rightString.isEmpty) {
      return 0.0
    }

    val leftTokens = TokenMetricKernelHelper.tokenizeToSet(leftString)
    val rightTokens = TokenMetricKernelHelper.tokenizeToSet(rightString)
    val interSize = TokenMetricKernelHelper.intersectionSize(leftTokens, rightTokens)
    val denominator = leftTokens.size + rightTokens.size

    if (denominator == 0) 1.0 else (2.0 * interSize.toDouble) / denominator.toDouble
  }
}
