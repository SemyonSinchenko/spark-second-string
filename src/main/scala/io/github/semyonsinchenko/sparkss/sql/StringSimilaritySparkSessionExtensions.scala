package io.github.semyonsinchenko.sparkss.sql

import io.github.semyonsinchenko.sparkss.expressions.token.Jaccard
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.FunctionIdentifier
import org.apache.spark.sql.catalyst.expressions.{Expression, ExpressionInfo}

object StringSimilaritySparkSessionExtensions {

  implicit class StringSimilaritySparkSessionOps(private val spark: SparkSession) extends AnyVal {

    def registerStringSimilarityFunctions(): Unit = {
      registerJaccard(spark)
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
}
