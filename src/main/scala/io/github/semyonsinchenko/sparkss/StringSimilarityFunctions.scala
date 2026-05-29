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

  /** Jaccard similarity between two strings.
    *
    * Compares token overlap divided by token union size. The result is in `[0.0, 1.0]`, where `1.0` means identical
    * token sets and `0.0` means no shared tokens.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
  def jaccard(left: Column, right: Column): Column = {
    jaccard(left, right, 0)
  }

  /** Jaccard similarity between two strings using custom tokenization n-gram size.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @param ngramSize
    *   token n-gram size (`0` keeps default tokenization)
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
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

  /** Sorensen-Dice similarity between two strings.
    *
    * Measures doubled token intersection over total token counts. The result is in `[0.0, 1.0]`, where `1.0` means
    * perfect overlap.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
  def sorensenDice(left: Column, right: Column): Column = {
    sorensenDice(left, right, 0)
  }

  /** Sorensen-Dice similarity between two strings using custom tokenization n-gram size.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @param ngramSize
    *   token n-gram size (`0` keeps default tokenization)
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
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

  /** Overlap coefficient similarity between two strings.
    *
    * Computes token intersection relative to the smaller token set. The result is in `[0.0, 1.0]`, where `1.0` means
    * one token set is fully contained in the other.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
  def overlapCoefficient(left: Column, right: Column): Column = {
    overlapCoefficient(left, right, 0)
  }

  /** Overlap coefficient similarity between two strings using custom tokenization n-gram size.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @param ngramSize
    *   token n-gram size (`0` keeps default tokenization)
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
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

  /** Cosine similarity between two strings.
    *
    * Compares token vectors by angle. The result is in `[0.0, 1.0]`, where higher values indicate more similar token
    * distributions.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
  def cosine(left: Column, right: Column): Column = {
    cosine(left, right, 0)
  }

  /** Cosine similarity between two strings using custom tokenization n-gram size.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @param ngramSize
    *   token n-gram size (`0` keeps default tokenization)
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
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

  /** Braun-Blanquet similarity between two strings.
    *
    * Computes token intersection relative to the larger token set. The result is in `[0.0, 1.0]`, where `1.0` means
    * identical token sets.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
  def braunBlanquet(left: Column, right: Column): Column = {
    braunBlanquet(left, right, 0)
  }

  /** Braun-Blanquet similarity between two strings using custom tokenization n-gram size.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @param ngramSize
    *   token n-gram size (`0` keeps default tokenization)
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
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

  /** Monge-Elkan similarity between two strings.
    *
    * Tokenizes both inputs and compares tokens via an inner similarity metric, then aggregates the best token matches.
    * The result is in `[0.0, 1.0]`, where higher values indicate stronger similarity.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
  def mongeElkan(left: Column, right: Column): Column = {
    mongeElkan(left, right, MongeElkan.DefaultInnerMetric, 0)
  }

  /** Monge-Elkan similarity between two strings using custom tokenization n-gram size.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @param ngramSize
    *   token n-gram size (`0` keeps default tokenization)
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
  def mongeElkan(left: Column, right: Column, ngramSize: Int): Column = {
    mongeElkan(left, right, MongeElkan.DefaultInnerMetric, ngramSize)
  }

  /** Monge-Elkan similarity between two strings with a custom inner metric.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @param innerMetric
    *   inner token-level similarity metric name used by Monge-Elkan
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
  def mongeElkan(left: Column, right: Column, innerMetric: String): Column = {
    mongeElkan(left, right, innerMetric, 0)
  }

  /** Monge-Elkan similarity between two strings with custom inner metric and tokenization n-gram size.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @param innerMetric
    *   inner token-level similarity metric name used by Monge-Elkan
    * @param ngramSize
    *   token n-gram size (`0` keeps default tokenization)
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
  def mongeElkan(left: Column, right: Column, innerMetric: String, ngramSize: Int): Column = {
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(MongeElkan(leftExpr, rightExpr, innerMetric, ngramSize))
  }

  def mongeElkan(left: String, right: String): Column = {
    mongeElkan(col(left), col(right))
  }

  def mongeElkan(left: String, right: String, ngramSize: Int): Column = {
    mongeElkan(col(left), col(right), ngramSize)
  }

  def mongeElkan(left: String, right: String, innerMetric: String): Column = {
    mongeElkan(col(left), col(right), innerMetric)
  }

  def mongeElkan(left: String, right: String, innerMetric: String, ngramSize: Int): Column = {
    mongeElkan(col(left), col(right), innerMetric, ngramSize)
  }

  /** Levenshtein similarity between two strings.
    *
    * Converts edit distance to a normalized similarity score in `[0.0, 1.0]`, where `1.0` means exact match and lower
    * values indicate more edits are required.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
  def levenshtein(left: Column, right: Column): Column = {
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(Levenshtein(leftExpr, rightExpr))
  }

  def levenshtein(left: String, right: String): Column = {
    levenshtein(col(left), col(right))
  }

  /** Longest-common-subsequence (LCS) similarity between two strings.
    *
    * Normalizes common subsequence length into a score in `[0.0, 1.0]`, where `1.0` means both strings share all
    * characters in order.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
  def lcsSimilarity(left: Column, right: Column): Column = {
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(LcsSimilarity(leftExpr, rightExpr))
  }

  def lcsSimilarity(left: String, right: String): Column = {
    lcsSimilarity(col(left), col(right))
  }

  /** Jaro similarity between two strings.
    *
    * Scores agreement in matching characters and transpositions. The result is in `[0.0, 1.0]`, where `1.0` means an
    * exact match.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
  def jaro(left: Column, right: Column): Column = {
    val leftExpr = SparkColumnInterop.toExpression(left)
    val rightExpr = SparkColumnInterop.toExpression(right)
    SparkColumnInterop.fromExpression(Jaro(leftExpr, rightExpr))
  }

  def jaro(left: String, right: String): Column = {
    jaro(col(left), col(right))
  }

  /** Jaro-Winkler similarity between two strings.
    *
    * Extends Jaro with a prefix bonus so early-character agreement increases similarity. The result is in `[0.0, 1.0]`,
    * where `1.0` means an exact match.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
  def jaroWinkler(left: Column, right: Column): Column = {
    jaroWinkler(left, right, JaroWinkler.DefaultPrefixScale, JaroWinkler.DefaultPrefixCap)
  }

  /** Jaro-Winkler similarity between two strings with custom prefix tuning.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @param prefixScale
    *   weight of the common-prefix bonus
    * @param prefixCap
    *   maximum prefix length eligible for the bonus
    * @return
    *   similarity score in `[0.0, 1.0]`
    */
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

  /** Needleman-Wunsch global alignment similarity between two strings.
    *
    * Scores an end-to-end alignment across full strings. Higher values indicate better global alignment under the
    * configured scoring scheme.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @return
    *   alignment-based similarity score where higher is more similar
    */
  def needlemanWunsch(left: Column, right: Column): Column = {
    needlemanWunsch(
      left,
      right,
      NeedlemanWunsch.DefaultMatchScore,
      NeedlemanWunsch.DefaultMismatchPenalty,
      NeedlemanWunsch.DefaultGapPenalty
    )
  }

  /** Needleman-Wunsch global alignment similarity between two strings with custom scoring.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @param matchScore
    *   score added for aligned matching characters
    * @param mismatchPenalty
    *   penalty applied to aligned non-matching characters
    * @param gapPenalty
    *   penalty applied to insertion/deletion gaps
    * @return
    *   alignment-based similarity score where higher is more similar
    */
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

  /** Smith-Waterman local alignment similarity between two strings.
    *
    * Scores the best matching local subsequences rather than full-string alignment. Higher values indicate stronger
    * local similarity under the configured scoring scheme.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @return
    *   alignment-based similarity score where higher is more similar
    */
  def smithWaterman(left: Column, right: Column): Column = {
    smithWaterman(
      left,
      right,
      SmithWaterman.DefaultMatchScore,
      SmithWaterman.DefaultMismatchPenalty,
      SmithWaterman.DefaultGapPenalty
    )
  }

  /** Smith-Waterman local alignment similarity between two strings with custom scoring.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @param matchScore
    *   score added for aligned matching characters
    * @param mismatchPenalty
    *   penalty applied to aligned non-matching characters
    * @param gapPenalty
    *   penalty applied to insertion/deletion gaps
    * @return
    *   alignment-based similarity score where higher is more similar
    */
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

  def affineGap(left: Column, right: Column): Column = {
    affineGap(
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
    *   - old style: `affineGap(left, right, mismatchPenalty = 1, gapOpenPenalty = 2, gapExtendPenalty = 1)`
    *   - new style: `affineGap(left, right, mismatchPenalty = -1, gapOpenPenalty = -2, gapExtendPenalty = -1)`
    *
    * Positive penalty values are rejected at analysis time with a fail-fast type-check error.
    *
    * @param left
    *   left input string column
    * @param right
    *   right input string column
    * @param mismatchPenalty
    *   penalty applied to aligned non-matching characters (must be negative)
    * @param gapOpenPenalty
    *   penalty applied when opening a gap (must be negative)
    * @param gapExtendPenalty
    *   penalty applied when extending an existing gap (must be negative)
    * @return
    *   alignment-based similarity score where higher is more similar
    */
  def affineGap(
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

  def affineGap(left: String, right: String): Column = {
    affineGap(col(left), col(right))
  }

  def affineGap(
      left: String,
      right: String,
      mismatchPenalty: Int,
      gapOpenPenalty: Int,
      gapExtendPenalty: Int
  ): Column = {
    affineGap(col(left), col(right), mismatchPenalty, gapOpenPenalty, gapExtendPenalty)
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
