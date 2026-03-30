package io.github.semyonsinchenko.sparkss.expressions.token

import io.github.semyonsinchenko.sparkss.expressions.TokenMetricExpression
import io.github.semyonsinchenko.sparkss.expressions.matrix.{
  Jaro,
  JaroWinkler,
  Levenshtein,
  NeedlemanWunsch,
  SmithWaterman
}
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult.{TypeCheckFailure, TypeCheckSuccess}
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class MongeElkan(
    left: Expression,
    right: Expression,
    innerMetric: String = MongeElkan.DefaultInnerMetric,
    ngramSize: Int = 0
) extends TokenMetricExpression {

  private final val MongeElkanModule = "io.github.semyonsinchenko.sparkss.expressions.token.MongeElkan$.MODULE$"

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    copy(left = newLeft, right = newRight)
  }

  override protected def evalTokenMetric(left: UTF8String, right: UTF8String): Double = {
    MongeElkan.similarity(left, right, innerMetric, ngramSize)
  }

  override protected def genTokenMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    s"$MongeElkanModule.similarity($leftValue, $rightValue, \"$innerMetric\", $ngramSize)"
  }

  override def checkInputDataTypes(): TypeCheckResult = {
    super.checkInputDataTypes() match {
      case TypeCheckSuccess =>
        if (!MongeElkan.SupportedInnerMetrics.contains(innerMetric)) {
          TypeCheckFailure(
            s"innerMetric must be one of ${MongeElkan.SupportedInnerMetrics.mkString(", ")}, but got '$innerMetric'"
          )
        } else if (ngramSize < 0) {
          TypeCheckFailure(s"ngramSize must be >= 0, but got $ngramSize")
        } else {
          TypeCheckSuccess
        }
      case failure => failure
    }
  }
}

object MongeElkan {

  private[sparkss] final val DefaultInnerMetric = "jaro_winkler"
  private[sparkss] final val SupportedInnerMetrics = Set(
    "jaro_winkler",
    "jaro",
    "levenshtein",
    "needleman_wunsch",
    "smith_waterman"
  )

  private[sparkss] def similarity(left: UTF8String, right: UTF8String): Double = {
    similarity(left, right, DefaultInnerMetric, 0)
  }

  private[sparkss] def similarity(left: UTF8String, right: UTF8String, innerMetric: String, ngramSize: Int): Double = {
    val leftTokens = tokenize(left.toString, ngramSize)
    val rightTokens = tokenize(right.toString, ngramSize)

    val leftSize = leftTokens.size()
    val rightSize = rightTokens.size()

    if (leftSize == 0 && rightSize == 0) {
      return 1.0
    }
    if (leftSize == 0 || rightSize == 0) {
      return 0.0
    }

    val rawScore =
      (directedSimilarity(leftTokens, rightTokens, innerMetric) + directedSimilarity(
        rightTokens,
        leftTokens,
        innerMetric
      )) / 2.0
    TokenMetricKernelHelper.clampToUnitInterval(rawScore)
  }

  private def tokenize(value: String, ngramSize: Int): java.util.ArrayList[String] = {
    if (ngramSize == 0) {
      return TokenMetricKernelHelper.tokenizeToSequence(value)
    }

    val set = TokenMetricKernelHelper.tokenizeToCharNgramSet(value, ngramSize)
    val sequence = new java.util.ArrayList[String](set.size())
    val iterator = set.iterator()
    while (iterator.hasNext) {
      sequence.add(iterator.next())
    }
    sequence
  }

  private def directedSimilarity(
      leftTokens: java.util.ArrayList[String],
      rightTokens: java.util.ArrayList[String],
      innerMetric: String
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
        val candidate = tokenPairScore(leftToken, UTF8String.fromString(rightTokens.get(rightIndex)), innerMetric)
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

  private def tokenPairScore(leftToken: UTF8String, rightToken: UTF8String, innerMetric: String): Double = {
    innerMetric match {
      case "jaro_winkler" => JaroWinkler.similarity(leftToken, rightToken)
      case "jaro"         => Jaro.similarity(leftToken, rightToken)
      case "levenshtein"  => Levenshtein.similarity(leftToken, rightToken)
      case "needleman_wunsch" =>
        NeedlemanWunsch.similarity(
          leftToken,
          rightToken,
          NeedlemanWunsch.DefaultMatchScore,
          NeedlemanWunsch.DefaultMismatchPenalty,
          NeedlemanWunsch.DefaultGapPenalty
        )
      case "smith_waterman" =>
        SmithWaterman.similarity(
          leftToken,
          rightToken,
          SmithWaterman.DefaultMatchScore,
          SmithWaterman.DefaultMismatchPenalty,
          SmithWaterman.DefaultGapPenalty
        )
    }
  }
}
