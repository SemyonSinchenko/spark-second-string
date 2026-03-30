package io.github.semyonsinchenko.sparkss.expressions.token

import io.github.semyonsinchenko.sparkss.expressions.TokenMetricExpression
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult.{TypeCheckFailure, TypeCheckSuccess}
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

/** Jaccard similarity between two strings based on token sets.
  *
  * Computes the Jaccard similarity coefficient: |intersection| / |union| where intersection and union are computed over
  * the set of tokens (whitespace-separated).
  *
  * @param left
  *   first string expression
  * @param right
  *   second string expression
  *
  * @example
  *   {{{
  *   val jaccard = Jaccard(col("a"), col("b"))
  *   }}}
  */
case class Jaccard(left: Expression, right: Expression, ngramSize: Int = 0) extends TokenMetricExpression {

  private final val JaccardModule = "io.github.semyonsinchenko.sparkss.expressions.token.Jaccard$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  /** Compute Jaccard similarity between two strings.
    *
    * Tokenizes both strings on whitespace, then computes:
    * - |intersection| / |union|
    *
    * Edge cases:
    * - Both empty strings: returns 1.0 (identical empty sets)
    * - One empty string: returns 0.0 (no overlap)
    *
    * @param left first string
    * @param right second string
    * @return Jaccard similarity score between 0.0 and 1.0
    */
  override protected def evalTokenMetric(left: UTF8String, right: UTF8String): Double = {
    Jaccard.similarity(left, right, ngramSize)
  }

  override protected def genTokenMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$JaccardModule.similarity($leftValue, $rightValue, $ngramSize)"
  }

  override def checkInputDataTypes(): TypeCheckResult = {
    super.checkInputDataTypes() match {
      case TypeCheckSuccess =>
        if (ngramSize < 0) {
          TypeCheckFailure(s"ngramSize must be >= 0, but got $ngramSize")
        } else {
          TypeCheckSuccess
        }
      case failure => failure
    }
  }
}

object Jaccard {

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
    val unionSize = leftTokens.size + rightTokens.size - interSize

    if (unionSize == 0) 1.0 else interSize.toDouble / unionSize.toDouble
  }
}
