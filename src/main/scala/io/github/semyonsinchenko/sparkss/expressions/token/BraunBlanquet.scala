package io.github.semyonsinchenko.sparkss.expressions.token

import io.github.semyonsinchenko.sparkss.expressions.TokenMetricExpression
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class BraunBlanquet(left: Expression, right: Expression) extends TokenMetricExpression {

  private final val BraunBlanquetModule =
    "io.github.semyonsinchenko.sparkss.expressions.token.BraunBlanquet$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  override protected def evalTokenMetric(left: UTF8String, right: UTF8String): Double = {
    BraunBlanquet.similarity(left, right)
  }

  override protected def genTokenMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$BraunBlanquetModule.similarity($leftValue, $rightValue)"
  }
}

object BraunBlanquet {

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
    val maxSetSize = Math.max(leftTokens.size, rightTokens.size)

    if (maxSetSize == 0) 1.0 else interSize.toDouble / maxSetSize.toDouble
  }
}
