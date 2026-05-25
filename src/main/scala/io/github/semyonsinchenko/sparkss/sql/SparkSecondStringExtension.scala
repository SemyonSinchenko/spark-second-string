package io.github.semyonsinchenko.sparkss.sql

import org.apache.spark.sql.SparkSessionExtensions

class SparkSecondStringExtension extends (SparkSessionExtensions => Unit) {

  override def apply(extensions: SparkSessionExtensions): Unit = {
    StringSimilaritySparkSessionExtensions.registerAllFunctions { (identifier, expressionInfo, builder) =>
      extensions.injectFunction((identifier, expressionInfo, builder))
    }
  }
}
