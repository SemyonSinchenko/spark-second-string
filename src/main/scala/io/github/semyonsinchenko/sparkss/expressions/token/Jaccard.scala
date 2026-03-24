package io.github.semyonsinchenko.sparkss.expressions.token

import io.github.semyonsinchenko.sparkss.expressions.StringSimExpression
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.{CodegenContext, ExprCode}

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
case class Jaccard(left: Expression, right: Expression) extends StringSimExpression {

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
  override protected[sparkss] def getSim(left: String, right: String): Double = {
    // Handle empty string edge cases
    val leftEmpty = left.isEmpty
    val rightEmpty = right.isEmpty

    if (leftEmpty && rightEmpty) {
      return 1.0
    }
    if (leftEmpty || rightEmpty) {
      return 0.0
    }

    // Tokenize strings using mutable collection to minimize GC pressure
    val leftTokens = tokenize(left)
    val rightTokens = tokenize(right)

    // Compute intersection and union sizes using mutable sets
    val intersectionSize = computeIntersectionSize(leftTokens, rightTokens)
    val unionSize = leftTokens.size + rightTokens.size - intersectionSize

    if (unionSize == 0) {
      1.0
    } else {
      intersectionSize.toDouble / unionSize.toDouble
    }
  }

  /** Tokenize a string on whitespace using mutable collection.
    *
    * @param str
    *   the string to tokenize
    * @return
    *   mutable set of tokens
    */
  private def tokenize(str: String): scala.collection.mutable.HashSet[String] = {
    val tokens = new scala.collection.mutable.HashSet[String]()
    var i = 0
    val len = str.length
    var start = 0
    var inToken = false

    while (i < len) {
      val c = str.charAt(i)
      if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
        if (inToken) {
          tokens.add(str.substring(start, i))
          inToken = false
        }
      } else {
        if (!inToken) {
          start = i
          inToken = true
        }
      }
      i += 1
    }

    // Add the last token if any
    if (inToken) {
      tokens.add(str.substring(start, len))
    }

    tokens
  }

  /** Compute the size of intersection between two token sets.
    *
    * @param leftTokens
    *   first set of tokens
    * @param rightTokens
    *   second set of tokens
    * @return
    *   number of tokens in the intersection
    */
  private def computeIntersectionSize(
      leftTokens: scala.collection.mutable.HashSet[String],
      rightTokens: scala.collection.mutable.HashSet[String]
  ): Int = {
    var count = 0
    val iter = leftTokens.iterator
    while (iter.hasNext) {
      if (rightTokens.contains(iter.next())) {
        count += 1
      }
    }
    count
  }

  override protected def doGenCode(ctx: CodegenContext, ev: ExprCode): ExprCode = {
    // TODO: Implement code generation for Jaccard similarity
    // For now, rely on interpreted execution via CodegenFallback
    null
  }
}
