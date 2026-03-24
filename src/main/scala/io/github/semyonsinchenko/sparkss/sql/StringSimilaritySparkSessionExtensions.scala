package io.github.semyonsinchenko.sparkss.sql

import io.github.semyonsinchenko.sparkss.expressions.token.Jaccard
import io.github.semyonsinchenko.sparkss.expressions.token.OverlapCoefficient
import io.github.semyonsinchenko.sparkss.expressions.token.SorensenDice
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.FunctionIdentifier
import org.apache.spark.sql.catalyst.expressions.{Expression, ExpressionInfo}

object StringSimilaritySparkSessionExtensions {

  implicit class StringSimilaritySparkSessionOps(private val spark: SparkSession) extends AnyVal {

    def registerStringSimilarityFunctions(): Unit = {
      registerJaccard(spark)
      registerSorensenDice(spark)
      registerOverlapCoefficient(spark)
    }
  }

  private def registerJaccard(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[Jaccard].getName, "jaccard")
    val builder: Seq[Expression] => Expression = {
      case Seq(left, right) => Jaccard(left, right)
      case args =>
        throw new IllegalArgumentException(
          s"Function jaccard expects 2 arguments, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("jaccard"),
      expressionInfo,
      builder
    )
  }

  private def registerSorensenDice(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[SorensenDice].getName, "sorensen_dice")
    val builder: Seq[Expression] => Expression = {
      case Seq(left, right) => SorensenDice(left, right)
      case args =>
        throw new IllegalArgumentException(
          s"Function sorensen_dice expects 2 arguments, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("sorensen_dice"),
      expressionInfo,
      builder
    )
  }

  private def registerOverlapCoefficient(spark: SparkSession): Unit = {
    val expressionInfo = new ExpressionInfo(classOf[OverlapCoefficient].getName, "overlap_coefficient")
    val builder: Seq[Expression] => Expression = {
      case Seq(left, right) => OverlapCoefficient(left, right)
      case args =>
        throw new IllegalArgumentException(
          s"Function overlap_coefficient expects 2 arguments, found ${args.size}"
        )
    }

    spark.sessionState.functionRegistry.registerFunction(
      FunctionIdentifier("overlap_coefficient"),
      expressionInfo,
      builder
    )
  }
}
