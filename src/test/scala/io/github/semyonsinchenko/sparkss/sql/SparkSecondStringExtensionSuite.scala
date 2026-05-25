package io.github.semyonsinchenko.sparkss.sql

import io.github.semyonsinchenko.sparkss.sql.StringSimilaritySparkSessionExtensions._
import org.apache.spark.sql.SparkSession
import org.scalatest.funsuite.AnyFunSuite

class SparkSecondStringExtensionSuite extends AnyFunSuite {

  private val extensionClassName = classOf[SparkSecondStringExtension].getName
  private val expectedFunctionNames = Set(
    "ss_jaccard",
    "ss_sorensen_dice",
    "ss_overlap_coefficient",
    "ss_cosine",
    "ss_braun_blanquet",
    "ss_monge_elkan",
    "ss_levenshtein",
    "ss_lcs_similarity",
    "ss_jaro",
    "ss_jaro_winkler",
    "ss_needleman_wunsch",
    "ss_smith_waterman",
    "ss_affine_gap",
    "ss_soundex",
    "ss_refined_soundex",
    "ss_double_metaphone"
  )

  private def withSparkSession(configureExtension: Boolean)(f: SparkSession => Unit): Unit = {
    val builder = SparkSession
      .builder()
      .master("local[1]")
      .appName("SparkSecondStringExtensionSuite")
      .config("spark.ui.showConsoleProgress", "false")

    val spark = if (configureExtension) {
      builder.config("spark.sql.extensions", extensionClassName).getOrCreate()
    } else {
      builder.getOrCreate()
    }

    spark.sparkContext.setLogLevel("ERROR")
    try {
      f(spark)
    } finally {
      spark.stop()
      SparkSession.clearActiveSession()
      SparkSession.clearDefaultSession()
    }
  }

  private def registeredFunctionNames(spark: SparkSession): Set[String] = {
    spark.sessionState.functionRegistry.listFunction().map(_.funcName).toSet.intersect(expectedFunctionNames)
  }

  test("spark.sql.extensions auto-registers SQL functions") {
    withSparkSession(configureExtension = true) { spark =>
      val row = spark
        .sql(
          """
            |SELECT
            |  ss_jaccard('a b', 'a c') AS jaccard,
            |  ss_jaro_winkler('martha', 'marhta') AS jaro_winkler,
            |  ss_soundex('Robert') AS soundex
            |""".stripMargin
        )
        .head()

      assert(row.getDouble(0) === (1.0 / 3.0))
      assert(row.getDouble(1) > 0.9)
      assert(row.getString(2).nonEmpty)
      assert(registeredFunctionNames(spark) === expectedFunctionNames)
    }
  }

  test("configured extension and legacy registration expose identical SQL function set") {
    withSparkSession(configureExtension = true) { configuredSpark =>
      val configuredNames = registeredFunctionNames(configuredSpark)

      withSparkSession(configureExtension = false) { legacySpark =>
        legacySpark.registerStringSimilarityFunctions()
        val legacyNames = registeredFunctionNames(legacySpark)

        assert(configuredNames === expectedFunctionNames)
        assert(legacyNames === expectedFunctionNames)
      }
    }
  }

  test("legacy registration path remains usable") {
    withSparkSession(configureExtension = false) { spark =>
      spark.registerStringSimilarityFunctions()
      val score = spark.sql("SELECT ss_levenshtein('kitten', 'sitting')").head().getDouble(0)
      assert(score > 0.0)
    }
  }
}
