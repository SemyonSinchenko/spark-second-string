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
import io.github.semyonsinchenko.sparkss.internal.SparkColumnInterop
import org.apache.spark.sql.Column
import org.apache.spark.sql.functions.col

object StringSimilarityFunctions {

  def jaccard(left: Column, right: Column): Column = {
    jaccard(left, right, 0)
  }

  def jaccard(left: Column, right: Column, ngramSize: Int): Column = {
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(Jaccard(leftExpr, rightExpr, ngramSize))
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
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(SorensenDice(leftExpr, rightExpr, ngramSize))
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
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(OverlapCoefficient(leftExpr, rightExpr, ngramSize))
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
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(Cosine(leftExpr, rightExpr, ngramSize))
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
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(BraunBlanquet(leftExpr, rightExpr, ngramSize))
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
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(MongeElkan(leftExpr, rightExpr, innerMetric, ngramSize))
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
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(Levenshtein(leftExpr, rightExpr))
  }

  def levenshtein(left: String, right: String): Column = {
    levenshtein(col(left), col(right))
  }

  def lcsSimilarity(left: Column, right: Column): Column = {
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(LcsSimilarity(leftExpr, rightExpr))
  }

  def lcsSimilarity(left: String, right: String): Column = {
    lcsSimilarity(col(left), col(right))
  }

  def jaro(left: Column, right: Column): Column = {
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(Jaro(leftExpr, rightExpr))
  }

  def jaro(left: String, right: String): Column = {
    jaro(col(left), col(right))
  }

  def jaroWinkler(left: Column, right: Column): Column = {
    jaroWinkler(left, right, JaroWinkler.DefaultPrefixScale, JaroWinkler.DefaultPrefixCap)
  }

  def jaroWinkler(left: Column, right: Column, prefixScale: Double, prefixCap: Int): Column = {
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(JaroWinkler(leftExpr, rightExpr, prefixScale, prefixCap))
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
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(NeedlemanWunsch(leftExpr, rightExpr, matchScore, mismatchPenalty, gapPenalty))
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
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(SmithWaterman(leftExpr, rightExpr, matchScore, mismatchPenalty, gapPenalty))
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
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(AffineGap(leftExpr, rightExpr, mismatchPenalty, gapOpenPenalty, gapExtendPenalty))
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
    val inputExpr = SparkColumnInterop.toExpression(input)
    SparkColumnInterop.fromExpression(Soundex(inputExpr))
  }

  def soundex(inputColName: String): Column = {
    soundex(col(inputColName))
  }

  def refinedSoundex(input: Column): Column = {
    val inputExpr = SparkColumnInterop.toExpression(input)
    SparkColumnInterop.fromExpression(RefinedSoundex(inputExpr))
  }

  def refinedSoundex(inputColName: String): Column = {
    refinedSoundex(col(inputColName))
  }

  def doubleMetaphone(input: Column): Column = {
    val inputExpr = SparkColumnInterop.toExpression(input)
    SparkColumnInterop.fromExpression(DoubleMetaphone(inputExpr))
  }

  def doubleMetaphone(inputColName: String): Column = {
    doubleMetaphone(col(inputColName))
  }

}
