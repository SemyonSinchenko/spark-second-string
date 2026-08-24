package io.github.semyonsinchenko.sparkss.sql

import org.apache.spark.sql.SparkSessionExtensions
import org.apache.spark.sql.catalyst.FunctionIdentifier
import org.apache.spark.sql.catalyst.expressions.ExpressionInfo

class SparkSecondStringExtension extends (SparkSessionExtensions => Unit) {

  override def apply(extensions: SparkSessionExtensions): Unit = {
    StringSimilaritySparkSessionExtensions.registerAllFunctions { (name, builder, className) =>
      extensions.injectFunction((FunctionIdentifier(name), new ExpressionInfo(className, name), builder))
    }
  }
}
