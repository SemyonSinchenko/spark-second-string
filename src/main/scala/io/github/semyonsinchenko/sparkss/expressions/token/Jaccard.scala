package io.github.semyonsinchenko.sparkss.expressions.token

import io.github.semyonsinchenko.sparkss.expressions.TokenMetricExpression
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
case class Jaccard(left: Expression, right: Expression) extends TokenMetricExpression {

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
    Jaccard.similarity(left, right)
  }

  override protected def genTokenMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$JaccardModule.similarity($leftValue, $rightValue)"
  }
}

object Jaccard {

  private[token] def similarity(left: UTF8String, right: UTF8String): Double = {
    val leftString = left.toString
    val rightString = right.toString

    if (leftString.isEmpty && rightString.isEmpty) {
      return 1.0
    }
    if (leftString.isEmpty || rightString.isEmpty) {
      return 0.0
    }

    val leftTokens = tokenizeToSet(leftString)
    val rightTokens = tokenizeToSet(rightString)
    val interSize = intersectionSize(leftTokens, rightTokens)
    val unionSize = leftTokens.size + rightTokens.size - interSize

    if (unionSize == 0) 1.0 else interSize.toDouble / unionSize.toDouble
  }

  private def tokenizeToSet(value: String): java.util.HashSet[String] = {
    val tokens = new java.util.HashSet[String]()
    val length = value.length
    var inToken = false
    var tokenStart = 0
    var index = 0

    while (index < length) {
      if (Character.isWhitespace(value.charAt(index))) {
        if (inToken) {
          tokens.add(value.substring(tokenStart, index))
          inToken = false
        }
      } else if (!inToken) {
        tokenStart = index
        inToken = true
      }
      index += 1
    }

    if (inToken) {
      tokens.add(value.substring(tokenStart, length))
    }

    tokens
  }

  private def intersectionSize(leftTokens: java.util.HashSet[String], rightTokens: java.util.HashSet[String]): Int = {
    var smaller = leftTokens
    var larger = rightTokens
    if (leftTokens.size > rightTokens.size) {
      smaller = rightTokens
      larger = leftTokens
    }

    var count = 0
    val iterator = smaller.iterator()
    while (iterator.hasNext) {
      if (larger.contains(iterator.next())) {
        count += 1
      }
    }
    count
  }
}
