package io.github.semyonsinchenko.sparkss.expressions.token

import io.github.semyonsinchenko.sparkss.expressions.TokenMetricExpression
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult.{TypeCheckFailure, TypeCheckSuccess}
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class BraunBlanquet(left: Expression, right: Expression, ngramSize: Int = 0) extends TokenMetricExpression {

  private final val BraunBlanquetModule =
    "io.github.semyonsinchenko.sparkss.expressions.token.BraunBlanquet$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  override protected def evalTokenMetric(left: UTF8String, right: UTF8String): Double = {
    BraunBlanquet.similarity(left, right, ngramSize)
  }

  override protected def genTokenMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$BraunBlanquetModule.similarity($leftValue, $rightValue, $ngramSize)"
  }

  override def checkInputDataTypes(): TypeCheckResult = {
    super.checkInputDataTypes() match {
      case TypeCheckSuccess =>
        if (ngramSize < 0) TypeCheckFailure(s"ngramSize must be >= 0, but got $ngramSize")
        else TypeCheckSuccess
      case failure => failure
    }
  }
}

object BraunBlanquet {

  private[sparkss] def similarity(left: UTF8String, right: UTF8String): Double = {
    similarity(left, right, 0)
  }

  private[sparkss] def similarity(left: UTF8String, right: UTF8String, ngramSize: Int): Double = {
    val leftString = left.toString
    val rightString = right.toString

    if (leftString.isEmpty && rightString.isEmpty) {
      return 1.0
    }
    if (leftString.isEmpty || rightString.isEmpty) {
      return 0.0
    }

    val leftTokens =
      if (ngramSize > 0) TokenMetricKernelHelper.tokenizeToCharNgramSet(leftString, ngramSize)
      else TokenMetricKernelHelper.tokenizeToSet(leftString)
    val rightTokens =
      if (ngramSize > 0) TokenMetricKernelHelper.tokenizeToCharNgramSet(rightString, ngramSize)
      else TokenMetricKernelHelper.tokenizeToSet(rightString)
    val interSize = TokenMetricKernelHelper.intersectionSize(leftTokens, rightTokens)
    val maxSetSize = Math.max(leftTokens.size, rightTokens.size)

    if (maxSetSize == 0) 1.0 else interSize.toDouble / maxSetSize.toDouble
  }
}
