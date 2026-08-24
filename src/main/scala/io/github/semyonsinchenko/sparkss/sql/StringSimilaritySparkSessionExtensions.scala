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
import org.apache.spark.sql.catalyst.expressions.Expression

/** Helper object that registers functions without needs to change spark-fonfigs.
  *
  * Consider this object as a workaround / alternative way to register functions.
  *
  * If you can change the spark-fonfs
  * `--conf spark.sql.extensions=io.github.semyonsinchenko.sparkss.sql.SparkSecondStringExtension` use it.
  *
  * If you want to have a programmatic access to the DSL I would point you to the
  * [[io.github.semyonsinchenko.sparkss.StringSimilarityFunctions]]
  *
  * This object is kind of workaround when you want to register functions as SQL but for some reason you cannot change
  * spark's configurations.
  */
object StringSimilaritySparkSessionExtensions {

  private type FunctionBuilder = Seq[Expression] => Expression

  private final case class RegisteredFunction(name: String, expressionClassName: String, builder: FunctionBuilder)

  private val allFunctions: Seq[RegisteredFunction] = Seq(
    RegisteredFunction(
      "ss_jaccard",
      classOf[Jaccard].getName,
      {
        case Seq(left, right) => Jaccard(left, right, 0)
        case args =>
          throw new IllegalArgumentException(
            s"Function ss_jaccard expects 2 arguments, found ${args.size}"
          )
      }
    ),
    RegisteredFunction(
      "ss_sorensen_dice",
      classOf[SorensenDice].getName,
      {
        case Seq(left, right) => SorensenDice(left, right, 0)
        case args =>
          throw new IllegalArgumentException(
            s"Function ss_sorensen_dice expects 2 arguments, found ${args.size}"
          )
      }
    ),
    RegisteredFunction(
      "ss_overlap_coefficient",
      classOf[OverlapCoefficient].getName,
      {
        case Seq(left, right) => OverlapCoefficient(left, right, 0)
        case args =>
          throw new IllegalArgumentException(
            s"Function ss_overlap_coefficient expects 2 arguments, found ${args.size}"
          )
      }
    ),
    RegisteredFunction(
      "ss_cosine",
      classOf[Cosine].getName,
      {
        case Seq(left, right) => Cosine(left, right, 0)
        case args =>
          throw new IllegalArgumentException(
            s"Function ss_cosine expects 2 arguments, found ${args.size}"
          )
      }
    ),
    RegisteredFunction(
      "ss_braun_blanquet",
      classOf[BraunBlanquet].getName,
      {
        case Seq(left, right) => BraunBlanquet(left, right, 0)
        case args =>
          throw new IllegalArgumentException(
            s"Function ss_braun_blanquet expects 2 arguments, found ${args.size}"
          )
      }
    ),
    RegisteredFunction(
      "ss_monge_elkan",
      classOf[MongeElkan].getName,
      {
        case Seq(left, right) => MongeElkan(left, right, MongeElkan.DefaultInnerMetric, 0)
        case args =>
          throw new IllegalArgumentException(
            s"Function ss_monge_elkan expects 2 arguments, found ${args.size}"
          )
      }
    ),
    RegisteredFunction(
      "ss_levenshtein",
      classOf[Levenshtein].getName,
      {
        case Seq(left, right) => Levenshtein(left, right)
        case args =>
          throw new IllegalArgumentException(
            s"Function ss_levenshtein expects 2 arguments, found ${args.size}"
          )
      }
    ),
    RegisteredFunction(
      "ss_lcs_similarity",
      classOf[LcsSimilarity].getName,
      {
        case Seq(left, right) => LcsSimilarity(left, right)
        case args =>
          throw new IllegalArgumentException(
            s"Function ss_lcs_similarity expects 2 arguments, found ${args.size}"
          )
      }
    ),
    RegisteredFunction(
      "ss_jaro",
      classOf[Jaro].getName,
      {
        case Seq(left, right) => Jaro(left, right)
        case args =>
          throw new IllegalArgumentException(
            s"Function ss_jaro expects 2 arguments, found ${args.size}"
          )
      }
    ),
    RegisteredFunction(
      "ss_jaro_winkler",
      classOf[JaroWinkler].getName,
      {
        case Seq(left, right) => JaroWinkler(left, right, JaroWinkler.DefaultPrefixScale, JaroWinkler.DefaultPrefixCap)
        case args =>
          throw new IllegalArgumentException(
            s"Function ss_jaro_winkler expects 2 arguments, found ${args.size}"
          )
      }
    ),
    RegisteredFunction(
      "ss_needleman_wunsch",
      classOf[NeedlemanWunsch].getName,
      {
        case Seq(left, right) =>
          NeedlemanWunsch(
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
    ),
    RegisteredFunction(
      "ss_smith_waterman",
      classOf[SmithWaterman].getName,
      {
        case Seq(left, right) =>
          SmithWaterman(
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
    ),
    RegisteredFunction(
      "ss_affine_gap",
      classOf[AffineGap].getName,
      {
        case Seq(left, right) =>
          AffineGap(
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
    ),
    RegisteredFunction(
      "ss_soundex",
      classOf[Soundex].getName,
      {
        case Seq(input) => Soundex(input)
        case args =>
          throw new IllegalArgumentException(
            s"Function ss_soundex expects 1 argument, found ${args.size}"
          )
      }
    ),
    RegisteredFunction(
      "ss_refined_soundex",
      classOf[RefinedSoundex].getName,
      {
        case Seq(input) => RefinedSoundex(input)
        case args =>
          throw new IllegalArgumentException(
            s"Function ss_refined_soundex expects 1 argument, found ${args.size}"
          )
      }
    ),
    RegisteredFunction(
      "ss_double_metaphone",
      classOf[DoubleMetaphone].getName,
      {
        case Seq(input) => DoubleMetaphone(input)
        case args =>
          throw new IllegalArgumentException(
            s"Function ss_double_metaphone expects 1 argument, found ${args.size}"
          )
      }
    )
  )

  private[sql] def registerAllFunctions(
      register: (String, FunctionBuilder, String) => Unit
  ): Unit = {
    allFunctions.foreach { fn =>
      register(fn.name, fn.builder, fn.expressionClassName)
    }
  }

  /** Unsafe method made for workaround usage (like via py4j).
    *
    * It is assumed that SparkSession exists! It modifies the FunctionRegistry of the existing SparkSession!
    * SparkSession is called without checking! Not thead safe (and not safe at all)!
    *
    * Do not use it directly until you have reasons: read the object's scaladoc first.
    */
  def registerAllFunctionsPy4j(): Unit = {
    val spark = SparkSession.getActiveSession.get
    val functionRegistry = spark.sessionState.functionRegistry
    registerAllFunctions { (name, builder, _) =>
      functionRegistry.createOrReplaceTempFunction(name, builder, "scala_udf")
    }
  }

  implicit class StringSimilaritySparkSessionOps(private val spark: SparkSession) extends AnyVal {

    /** Registers the SQL functions via Spark's temp-function API (the same path `spark.udf.register` uses). Spark
      * qualifies the plain function name per version (session namespace on 4.2+), so no internal namespace (e.g.
      * `system.builtin`) is hardcoded here.
      */
    def registerStringSimilarityFunctions(): Unit = {
      val functionRegistry = spark.sessionState.functionRegistry
      registerAllFunctions { (name, builder, _) =>
        functionRegistry.createOrReplaceTempFunction(name, builder, "scala_udf")
      }
    }
  }
}
