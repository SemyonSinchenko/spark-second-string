package io.github.semyonsinchenko.sparkss

import io.github.semyonsinchenko.sparkss.expressions.matrix.{AffineGap, JaroWinkler, NeedlemanWunsch, SmithWaterman}
import io.github.semyonsinchenko.sparkss.expressions.token.{Jaccard, MongeElkan}
import io.github.semyonsinchenko.sparkss.internal.SparkColumnInterop
import org.apache.spark.sql.functions.col
import org.scalatest.funsuite.AnyFunSuite

class StringSimilarityFunctionsApiSuite extends AnyFunSuite {

  test("defaulted helpers pass explicit legacy defaults to expressions") {
    val jaccard = SparkColumnInterop
      .toExpression(StringSimilarityFunctions.jaccard(col("left"), col("right")))
      .asInstanceOf[Jaccard]
    assert(jaccard.ngramSize === 0)

    val mongeElkan = SparkColumnInterop
      .toExpression(StringSimilarityFunctions.mongeElkan(col("left"), col("right")))
      .asInstanceOf[MongeElkan]
    assert(mongeElkan.innerMetric === MongeElkan.DefaultInnerMetric)
    assert(mongeElkan.ngramSize === 0)

    val jaroWinkler = SparkColumnInterop
      .toExpression(StringSimilarityFunctions.jaroWinkler(col("left"), col("right")))
      .asInstanceOf[JaroWinkler]
    assert(jaroWinkler.prefixScale === JaroWinkler.DefaultPrefixScale)
    assert(jaroWinkler.prefixCap === JaroWinkler.DefaultPrefixCap)

    val needlemanWunsch = SparkColumnInterop
      .toExpression(StringSimilarityFunctions.needlemanWunsch(col("left"), col("right")))
      .asInstanceOf[NeedlemanWunsch]
    assert(needlemanWunsch.matchScore === NeedlemanWunsch.DefaultMatchScore)
    assert(needlemanWunsch.mismatchPenalty === NeedlemanWunsch.DefaultMismatchPenalty)
    assert(needlemanWunsch.gapPenalty === NeedlemanWunsch.DefaultGapPenalty)

    val smithWaterman = SparkColumnInterop
      .toExpression(StringSimilarityFunctions.smithWaterman(col("left"), col("right")))
      .asInstanceOf[SmithWaterman]
    assert(smithWaterman.matchScore === SmithWaterman.DefaultMatchScore)
    assert(smithWaterman.mismatchPenalty === SmithWaterman.DefaultMismatchPenalty)
    assert(smithWaterman.gapPenalty === SmithWaterman.DefaultGapPenalty)

    val affineGap = SparkColumnInterop
      .toExpression(StringSimilarityFunctions.affineGap(col("left"), col("right")))
      .asInstanceOf[AffineGap]
    assert(affineGap.mismatchPenalty === AffineGap.DefaultMismatchPenalty)
    assert(affineGap.gapOpenPenalty === AffineGap.DefaultGapOpenPenalty)
    assert(affineGap.gapExtendPenalty === AffineGap.DefaultGapExtendPenalty)
  }

  test("expression companions do not expose constructor default methods") {
    val companions = Seq(
      Jaccard,
      MongeElkan,
      JaroWinkler,
      NeedlemanWunsch,
      SmithWaterman,
      AffineGap
    )

    companions.foreach { companion =>
      val methods = companion.getClass.getMethods.map(_.getName).toSet
      assert(!methods.exists(_.startsWith("apply$default$")))
    }
  }
}
