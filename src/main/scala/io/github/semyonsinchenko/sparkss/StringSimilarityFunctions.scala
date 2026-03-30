package io.github.semyonsinchenko.sparkss

import io.github.semyonsinchenko.sparkss.expressions.token.Jaccard
import io.github.semyonsinchenko.sparkss.expressions.token.OverlapCoefficient
import io.github.semyonsinchenko.sparkss.expressions.token.SorensenDice
import io.github.semyonsinchenko.sparkss.expressions.token.Cosine
import io.github.semyonsinchenko.sparkss.expressions.token.BraunBlanquet
import io.github.semyonsinchenko.sparkss.expressions.token.MongeElkan
import io.github.semyonsinchenko.sparkss.expressions.matrix.Levenshtein
import io.github.semyonsinchenko.sparkss.expressions.matrix.LcsSimilarity
import io.github.semyonsinchenko.sparkss.expressions.matrix.Jaro
import io.github.semyonsinchenko.sparkss.expressions.matrix.JaroWinkler
import io.github.semyonsinchenko.sparkss.expressions.matrix.NeedlemanWunsch
import io.github.semyonsinchenko.sparkss.expressions.matrix.SmithWaterman
import io.github.semyonsinchenko.sparkss.expressions.matrix.AffineGap
import io.github.semyonsinchenko.sparkss.expressions.phonetic.{DoubleMetaphone, RefinedSoundex, Soundex}
import org.apache.spark.sql.Column
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.functions.col

object StringSimilarityFunctions {

  def jaccard(left: Column, right: Column): Column = {
    jaccard(left, right, 0)
  }

  def jaccard(left: Column, right: Column, ngramSize: Int): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(Jaccard(leftExpr, rightExpr, ngramSize))
    convertColumnNodeToColumn(expressionNode)
  }

  def jaccard(left: String, right: String): Column = {
    jaccard(col(left), col(right))
  }

  def jaccard(left: String, right: String, ngramSize: Int): Column = {
    jaccard(col(left), col(right), ngramSize)
  }

  def sorensenDice(left: Column, right: Column): Column = {
    sorensenDice(left, right, 0)
  }

  def sorensenDice(left: Column, right: Column, ngramSize: Int): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(SorensenDice(leftExpr, rightExpr, ngramSize))
    convertColumnNodeToColumn(expressionNode)
  }

  def sorensenDice(left: String, right: String): Column = {
    sorensenDice(col(left), col(right))
  }

  def sorensenDice(left: String, right: String, ngramSize: Int): Column = {
    sorensenDice(col(left), col(right), ngramSize)
  }

  def overlapCoefficient(left: Column, right: Column): Column = {
    overlapCoefficient(left, right, 0)
  }

  def overlapCoefficient(left: Column, right: Column, ngramSize: Int): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(OverlapCoefficient(leftExpr, rightExpr, ngramSize))
    convertColumnNodeToColumn(expressionNode)
  }

  def overlapCoefficient(left: String, right: String): Column = {
    overlapCoefficient(col(left), col(right))
  }

  def overlapCoefficient(left: String, right: String, ngramSize: Int): Column = {
    overlapCoefficient(col(left), col(right), ngramSize)
  }

  def cosine(left: Column, right: Column): Column = {
    cosine(left, right, 0)
  }

  def cosine(left: Column, right: Column, ngramSize: Int): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(Cosine(leftExpr, rightExpr, ngramSize))
    convertColumnNodeToColumn(expressionNode)
  }

  def cosine(left: String, right: String): Column = {
    cosine(col(left), col(right))
  }

  def cosine(left: String, right: String, ngramSize: Int): Column = {
    cosine(col(left), col(right), ngramSize)
  }

  def braunBlanquet(left: Column, right: Column): Column = {
    braunBlanquet(left, right, 0)
  }

  def braunBlanquet(left: Column, right: Column, ngramSize: Int): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(BraunBlanquet(leftExpr, rightExpr, ngramSize))
    convertColumnNodeToColumn(expressionNode)
  }

  def braunBlanquet(left: String, right: String): Column = {
    braunBlanquet(col(left), col(right))
  }

  def braunBlanquet(left: String, right: String, ngramSize: Int): Column = {
    braunBlanquet(col(left), col(right), ngramSize)
  }

  def monge_elkan(left: Column, right: Column): Column = {
    monge_elkan(left, right, MongeElkan.DefaultInnerMetric, 0)
  }

  def monge_elkan(left: Column, right: Column, ngramSize: Int): Column = {
    monge_elkan(left, right, MongeElkan.DefaultInnerMetric, ngramSize)
  }

  def monge_elkan(left: Column, right: Column, innerMetric: String): Column = {
    monge_elkan(left, right, innerMetric, 0)
  }

  def monge_elkan(left: Column, right: Column, innerMetric: String, ngramSize: Int): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(MongeElkan(leftExpr, rightExpr, innerMetric, ngramSize))
    convertColumnNodeToColumn(expressionNode)
  }

  def monge_elkan(left: String, right: String): Column = {
    monge_elkan(col(left), col(right))
  }

  def monge_elkan(left: String, right: String, ngramSize: Int): Column = {
    monge_elkan(col(left), col(right), ngramSize)
  }

  def monge_elkan(left: String, right: String, innerMetric: String): Column = {
    monge_elkan(col(left), col(right), innerMetric)
  }

  def monge_elkan(left: String, right: String, innerMetric: String, ngramSize: Int): Column = {
    monge_elkan(col(left), col(right), innerMetric, ngramSize)
  }

  def levenshtein(left: Column, right: Column): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(Levenshtein(leftExpr, rightExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def levenshtein(left: String, right: String): Column = {
    levenshtein(col(left), col(right))
  }

  def lcsSimilarity(left: Column, right: Column): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(LcsSimilarity(leftExpr, rightExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def lcsSimilarity(left: String, right: String): Column = {
    lcsSimilarity(col(left), col(right))
  }

  def jaro(left: Column, right: Column): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(Jaro(leftExpr, rightExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def jaro(left: String, right: String): Column = {
    jaro(col(left), col(right))
  }

  def jaroWinkler(left: Column, right: Column): Column = {
    jaroWinkler(left, right, JaroWinkler.DefaultPrefixScale, JaroWinkler.DefaultPrefixCap)
  }

  def jaroWinkler(left: Column, right: Column, prefixScale: Double, prefixCap: Int): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(JaroWinkler(leftExpr, rightExpr, prefixScale, prefixCap))
    convertColumnNodeToColumn(expressionNode)
  }

  def jaroWinkler(left: String, right: String): Column = {
    jaroWinkler(col(left), col(right))
  }

  def jaroWinkler(left: String, right: String, prefixScale: Double, prefixCap: Int): Column = {
    jaroWinkler(col(left), col(right), prefixScale, prefixCap)
  }

  def needlemanWunsch(left: Column, right: Column): Column = {
    needlemanWunsch(
      left,
      right,
      NeedlemanWunsch.DefaultMatchScore,
      NeedlemanWunsch.DefaultMismatchPenalty,
      NeedlemanWunsch.DefaultGapPenalty
    )
  }

  def needlemanWunsch(
      left: Column,
      right: Column,
      matchScore: Int,
      mismatchPenalty: Int,
      gapPenalty: Int
  ): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode =
      convertExpressionToColumnNode(NeedlemanWunsch(leftExpr, rightExpr, matchScore, mismatchPenalty, gapPenalty))
    convertColumnNodeToColumn(expressionNode)
  }

  def needlemanWunsch(left: String, right: String): Column = {
    needlemanWunsch(col(left), col(right))
  }

  def needlemanWunsch(left: String, right: String, matchScore: Int, mismatchPenalty: Int, gapPenalty: Int): Column = {
    needlemanWunsch(col(left), col(right), matchScore, mismatchPenalty, gapPenalty)
  }

  def smithWaterman(left: Column, right: Column): Column = {
    smithWaterman(
      left,
      right,
      SmithWaterman.DefaultMatchScore,
      SmithWaterman.DefaultMismatchPenalty,
      SmithWaterman.DefaultGapPenalty
    )
  }

  def smithWaterman(
      left: Column,
      right: Column,
      matchScore: Int,
      mismatchPenalty: Int,
      gapPenalty: Int
  ): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode =
      convertExpressionToColumnNode(SmithWaterman(leftExpr, rightExpr, matchScore, mismatchPenalty, gapPenalty))
    convertColumnNodeToColumn(expressionNode)
  }

  def smithWaterman(left: String, right: String): Column = {
    smithWaterman(col(left), col(right))
  }

  def smithWaterman(left: String, right: String, matchScore: Int, mismatchPenalty: Int, gapPenalty: Int): Column = {
    smithWaterman(col(left), col(right), matchScore, mismatchPenalty, gapPenalty)
  }

  def affine_gap(left: Column, right: Column): Column = {
    affine_gap(
      left,
      right,
      AffineGap.DefaultMismatchPenalty,
      AffineGap.DefaultGapOpenPenalty,
      AffineGap.DefaultGapExtendPenalty
    )
  }

  /** Affine-gap sequence alignment similarity.
    *
    * Penalty parameters use the same sign convention as Needleman-Wunsch and Smith-Waterman: mismatch/open/extend
    * penalties must be negative values.
    *
    * Migration note for pre-1.0 users:
    *   - old style: `affine_gap(left, right, mismatchPenalty = 1, gapOpenPenalty = 2, gapExtendPenalty = 1)`
    *   - new style: `affine_gap(left, right, mismatchPenalty = -1, gapOpenPenalty = -2, gapExtendPenalty = -1)`
    *
    * Positive penalty values are rejected at analysis time with a fail-fast type-check error.
    */
  def affine_gap(
      left: Column,
      right: Column,
      mismatchPenalty: Int,
      gapOpenPenalty: Int,
      gapExtendPenalty: Int
  ): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode =
      convertExpressionToColumnNode(AffineGap(leftExpr, rightExpr, mismatchPenalty, gapOpenPenalty, gapExtendPenalty))
    convertColumnNodeToColumn(expressionNode)
  }

  def affine_gap(left: String, right: String): Column = {
    affine_gap(col(left), col(right))
  }

  def affine_gap(
      left: String,
      right: String,
      mismatchPenalty: Int,
      gapOpenPenalty: Int,
      gapExtendPenalty: Int
  ): Column = {
    affine_gap(col(left), col(right), mismatchPenalty, gapOpenPenalty, gapExtendPenalty)
  }

  def soundex(input: Column): Column = {
    val inputExpr = convertColumnNodeToExpression(input.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(Soundex(inputExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def soundex(inputColName: String): Column = {
    soundex(col(inputColName))
  }

  def refinedSoundex(input: Column): Column = {
    val inputExpr = convertColumnNodeToExpression(input.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(RefinedSoundex(inputExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def refinedSoundex(inputColName: String): Column = {
    refinedSoundex(col(inputColName))
  }

  def doubleMetaphone(input: Column): Column = {
    val inputExpr = convertColumnNodeToExpression(input.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(DoubleMetaphone(inputExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def doubleMetaphone(inputColName: String): Column = {
    doubleMetaphone(col(inputColName))
  }

  private def convertColumnNodeToExpression(node: AnyRef): Expression = {
    val moduleClass = Class.forName("org.apache.spark.sql.classic.ColumnNodeToExpressionConverter$")
    val module = moduleClass.getField("MODULE$").get(null)
    val columnNodeClass = Class.forName("org.apache.spark.sql.internal.ColumnNode")
    val apply = moduleClass.getMethod("apply", columnNodeClass)
    apply.invoke(module, node).asInstanceOf[Expression]
  }

  private def convertExpressionToColumnNode(expression: Expression): AnyRef = {
    val moduleClass = Class.forName("org.apache.spark.sql.classic.ExpressionColumnNode$")
    val module = moduleClass.getField("MODULE$").get(null)
    val apply = moduleClass.getMethod("apply", classOf[Expression])
    apply.invoke(module, expression).asInstanceOf[AnyRef]
  }

  private def convertColumnNodeToColumn(node: AnyRef): Column = {
    val columnNodeClass = Class.forName("org.apache.spark.sql.internal.ColumnNode")
    val constructor = classOf[Column].getConstructor(columnNodeClass)
    constructor.newInstance(node).asInstanceOf[Column]
  }
}
