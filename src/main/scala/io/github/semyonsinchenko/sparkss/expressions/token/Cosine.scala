package io.github.semyonsinchenko.sparkss.expressions.token

import io.github.semyonsinchenko.sparkss.expressions.TokenMetricExpression
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class Cosine(left: Expression, right: Expression) extends TokenMetricExpression {

  private final val CosineModule = "io.github.semyonsinchenko.sparkss.expressions.token.Cosine$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  override protected def evalTokenMetric(left: UTF8String, right: UTF8String): Double = {
    Cosine.similarity(left, right)
  }

  override protected def genTokenMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$CosineModule.similarity($leftValue, $rightValue)"
  }
}

object Cosine {

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
    val denominator = Math.sqrt(leftTokens.size.toDouble * rightTokens.size.toDouble)

    if (denominator == 0.0) 1.0 else interSize.toDouble / denominator
  }
}
