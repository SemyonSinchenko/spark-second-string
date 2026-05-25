package io.github.semyonsinchenko.sparkss.sql

import io.github.semyonsinchenko.sparkss.expressions.matrix.Levenshtein
import io.github.semyonsinchenko.sparkss.expressions.matrix.LcsSimilarity
import io.github.semyonsinchenko.sparkss.expressions.matrix.Jaro
import io.github.semyonsinchenko.sparkss.expressions.matrix.JaroWinkler
import io.github.semyonsinchenko.sparkss.expressions.matrix.NeedlemanWunsch
import io.github.semyonsinchenko.sparkss.expressions.matrix.SmithWaterman
import io.github.semyonsinchenko.sparkss.expressions.matrix.AffineGap
import io.github.semyonsinchenko.sparkss.expressions.token.BraunBlanquet
import io.github.semyonsinchenko.sparkss.expressions.token.Cosine
import io.github.semyonsinchenko.sparkss.expressions.token.Jaccard
import io.github.semyonsinchenko.sparkss.expressions.token.MongeElkan
import io.github.semyonsinchenko.sparkss.expressions.token.OverlapCoefficient
import io.github.semyonsinchenko.sparkss.expressions.token.SorensenDice
import io.github.semyonsinchenko.sparkss.expressions.phonetic.{DoubleMetaphone, RefinedSoundex, Soundex}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.FunctionIdentifier
import org.apache.spark.sql.catalyst.expressions.{Expression, ExpressionInfo}

object StringSimilaritySparkSessionExtensions {

  implicit class StringSimilaritySparkSessionOps(private val spark: SparkSession) extends AnyVal {

    def registerStringSimilarityFunctions(): Unit = {
      registerJaccard(spark)
      registerSorensenDice(spark)
      registerOverlapCoefficient(spark)
      registerCosine(spark)
      registerBraunBlanquet(spark)
      registerMongeElkan(spark)
      registerLevenshtein(spark)
      registerLcsSimilarity(spark)
      registerJaro(spark)
      registerJaroWinkler(spark)
      registerNeedlemanWunsch(spark)
      registerSmithWaterman(spark)
      registerAffineGap(spark)
      registerSoundex(spark)
      registerRefinedSoundex(spark)
      registerDoubleMetaphone(spark)
    }
  }

  private def registerJaccard(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[Jaccard].getName, "ss_jaccard")
    val builder: Seq[Expression] => Expression = {
      case Seq(left, right) => Jaccard(left, right, 0)
      case args =>
        throw new IllegalArgumentException(
          s"Function ss_jaccard expects 2 arguments, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("ss_jaccard"),
      expressionInfo,
      builder
    )
  }

  private def registerSorensenDice(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[SorensenDice].getName, "ss_sorensen_dice")
    val builder: Seq[Expression] => Expression = {
      case Seq(left, right) => SorensenDice(left, right, 0)
      case args =>
        throw new IllegalArgumentException(
          s"Function ss_sorensen_dice expects 2 arguments, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("ss_sorensen_dice"),
      expressionInfo,
      builder
    )
  }

  private def registerOverlapCoefficient(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[OverlapCoefficient].getName, "ss_overlap_coefficient")
    val builder: Seq[Expression] => Expression = {
      case Seq(left, right) => OverlapCoefficient(left, right, 0)
      case args =>
        throw new IllegalArgumentException(
          s"Function ss_overlap_coefficient expects 2 arguments, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("ss_overlap_coefficient"),
      expressionInfo,
      builder
    )
  }

  private def registerCosine(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[Cosine].getName, "ss_cosine")
    val builder: Seq[Expression] => Expression = {
      case Seq(left, right) => Cosine(left, right, 0)
      case args =>
        throw new IllegalArgumentException(
          s"Function ss_cosine expects 2 arguments, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("ss_cosine"),
      expressionInfo,
      builder
    )
  }

  private def registerBraunBlanquet(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[BraunBlanquet].getName, "ss_braun_blanquet")
    val builder: Seq[Expression] => Expression = {
      case Seq(left, right) => BraunBlanquet(left, right, 0)
      case args =>
        throw new IllegalArgumentException(
          s"Function ss_braun_blanquet expects 2 arguments, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("ss_braun_blanquet"),
      expressionInfo,
      builder
    )
  }

  private def registerMongeElkan(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[MongeElkan].getName, "ss_monge_elkan")
    val builder: Seq[Expression] => Expression = {
      case Seq(left, right) => MongeElkan(left, right, MongeElkan.DefaultInnerMetric, 0)
      case args =>
        throw new IllegalArgumentException(
          s"Function ss_monge_elkan expects 2 arguments, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("ss_monge_elkan"),
      expressionInfo,
      builder
    )
  }

  private def registerLevenshtein(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[Levenshtein].getName, "ss_levenshtein")
    val builder: Seq[Expression] => Expression = {
      case Seq(left, right) => Levenshtein(left, right)
      case args =>
        throw new IllegalArgumentException(
          s"Function ss_levenshtein expects 2 arguments, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("ss_levenshtein"),
      expressionInfo,
      builder
    )
  }

  private def registerLcsSimilarity(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[LcsSimilarity].getName, "ss_lcs_similarity")
    val builder: Seq[Expression] => Expression = {
      case Seq(left, right) => LcsSimilarity(left, right)
      case args =>
        throw new IllegalArgumentException(
          s"Function ss_lcs_similarity expects 2 arguments, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("ss_lcs_similarity"),
      expressionInfo,
      builder
    )
  }

  private def registerJaro(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[Jaro].getName, "ss_jaro")
    val builder: Seq[Expression] => Expression = {
      case Seq(left, right) => Jaro(left, right)
      case args =>
        throw new IllegalArgumentException(
          s"Function ss_jaro expects 2 arguments, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("ss_jaro"),
      expressionInfo,
      builder
    )
  }

  private def registerJaroWinkler(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[JaroWinkler].getName, "ss_jaro_winkler")
    val builder: Seq[Expression] => Expression = {
      case Seq(left, right) => JaroWinkler(left, right, JaroWinkler.DefaultPrefixScale, JaroWinkler.DefaultPrefixCap)
      case args =>
        throw new IllegalArgumentException(
          s"Function ss_jaro_winkler expects 2 arguments, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("ss_jaro_winkler"),
      expressionInfo,
      builder
    )
  }

  private def registerNeedlemanWunsch(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[NeedlemanWunsch].getName, "ss_needleman_wunsch")
    val builder: Seq[Expression] => Expression = {
      case Seq(left, right) => NeedlemanWunsch(
          left,
          right,
          NeedlemanWunsch.DefaultMatchScore,
          NeedlemanWunsch.DefaultMismatchPenalty,
          NeedlemanWunsch.DefaultGapPenalty
        )
      case args =>
        throw new IllegalArgumentException(
          s"Function ss_needleman_wunsch expects 2 arguments, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("ss_needleman_wunsch"),
      expressionInfo,
      builder
    )
  }

  private def registerSmithWaterman(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[SmithWaterman].getName, "ss_smith_waterman")
    val builder: Seq[Expression] => Expression = {
      case Seq(left, right) => SmithWaterman(
          left,
          right,
          SmithWaterman.DefaultMatchScore,
          SmithWaterman.DefaultMismatchPenalty,
          SmithWaterman.DefaultGapPenalty
        )
      case args =>
        throw new IllegalArgumentException(
          s"Function ss_smith_waterman expects 2 arguments, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("ss_smith_waterman"),
      expressionInfo,
      builder
    )
  }

  private def registerAffineGap(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[AffineGap].getName, "ss_affine_gap")
    val builder: Seq[Expression] => Expression = {
      case Seq(left, right) => AffineGap(
          left,
          right,
          AffineGap.DefaultMismatchPenalty,
          AffineGap.DefaultGapOpenPenalty,
          AffineGap.DefaultGapExtendPenalty
        )
      case args =>
        throw new IllegalArgumentException(
          s"Function ss_affine_gap expects 2 arguments, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("ss_affine_gap"),
      expressionInfo,
      builder
    )
  }

  private def registerSoundex(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[Soundex].getName, "ss_soundex")
    val builder: Seq[Expression] => Expression = {
      case Seq(input) => Soundex(input)
      case args =>
        throw new IllegalArgumentException(
          s"Function ss_soundex expects 1 argument, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("ss_soundex"),
      expressionInfo,
      builder
    )
  }

  private def registerRefinedSoundex(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[RefinedSoundex].getName, "ss_refined_soundex")
    val builder: Seq[Expression] => Expression = {
      case Seq(input) => RefinedSoundex(input)
      case args =>
        throw new IllegalArgumentException(
          s"Function ss_refined_soundex expects 1 argument, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("ss_refined_soundex"),
      expressionInfo,
      builder
    )
  }

  private def registerDoubleMetaphone(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[DoubleMetaphone].getName, "ss_double_metaphone")
    val builder: Seq[Expression] => Expression = {
      case Seq(input) => DoubleMetaphone(input)
      case args =>
        throw new IllegalArgumentException(
          s"Function ss_double_metaphone expects 1 argument, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("ss_double_metaphone"),
      expressionInfo,
      builder
    )
  }
}
