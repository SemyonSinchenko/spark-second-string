package io.github.semyonsinchenko.sparkss.expressions.matrix

import io.github.semyonsinchenko.sparkss.expressions.support.ScoreAssertions
import io.github.semyonsinchenko.sparkss.expressions.support.UnicodeFixtures
import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.funsuite.AnyFunSuite

class UnicodeMatrixMetricsSuite extends AnyFunSuite with ScoreAssertions {

  private val metrics: Seq[(String, (UTF8String, UTF8String) => Double)] = Seq(
    "levenshtein" -> ((left, right) => Levenshtein.similarity(left, right)),
    "lcs_similarity" -> ((left, right) => LcsSimilarity.similarity(left, right)),
    "jaro" -> ((left, right) => Jaro.similarity(left, right)),
    "jaro_winkler" -> ((left, right) => JaroWinkler.similarity(left, right)),
    "needleman_wunsch" -> ((left, right) => NeedlemanWunsch.similarity(left, right)),
    "smith_waterman" -> ((left, right) => SmithWaterman.similarity(left, right)),
    "affine_gap" -> ((left, right) => AffineGap.similarity(left, right))
  )

  test("unicode matrix fixtures are deterministic and bounded") {
    UnicodeFixtures.AllPairs.foreach { pair =>
      metrics.foreach { case (metricName, metric) =>
        val left = UTF8String.fromString(pair.left)
        val right = UTF8String.fromString(pair.right)
        withClue(s"fixture=${pair.name}, metric=$metricName") {
          assertDeterministicAndBounded(metric(left, right))
        }
      }
    }
  }

  test("identical cjk and emoji inputs return 1.0 across matrix metrics") {
    val identicalCases = Seq("\u6771\u4EAC", "\uD83D\uDE00", "hello \u4E16\u754C")
    identicalCases.foreach { value =>
      metrics.foreach { case (metricName, metric) =>
        val score = metric(UTF8String.fromString(value), UTF8String.fromString(value))
        withClue(s"value=$value, metric=$metricName") {
          assert(score === 1.0)
        }
      }
    }
  }
}
