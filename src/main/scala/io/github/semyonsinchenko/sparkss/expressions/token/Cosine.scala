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

    val leftTokens = tokenizeToSet(leftString)
    val rightTokens = tokenizeToSet(rightString)
    val interSize = intersectionSize(leftTokens, rightTokens)
    val denominator = Math.sqrt(leftTokens.size.toDouble * rightTokens.size.toDouble)

    if (denominator == 0.0) 1.0 else interSize.toDouble / denominator
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
