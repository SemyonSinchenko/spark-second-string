package io.github.semyonsinchenko.sparkss.expressions.token

import io.github.semyonsinchenko.sparkss.expressions.support.ScoreAssertions
import io.github.semyonsinchenko.sparkss.expressions.support.UnicodeFixtures
import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.funsuite.AnyFunSuite

class UnicodeTokenMetricsSuite extends AnyFunSuite with ScoreAssertions {

  private val setMetrics: Seq[(String, (UTF8String, UTF8String) => Double)] = Seq(
    "jaccard" -> ((left, right) => Jaccard.similarity(left, right)),
    "sorensen_dice" -> ((left, right) => SorensenDice.similarity(left, right)),
    "overlap_coefficient" -> ((left, right) => OverlapCoefficient.similarity(left, right)),
    "cosine" -> ((left, right) => Cosine.similarity(left, right)),
    "braun_blanquet" -> ((left, right) => BraunBlanquet.similarity(left, right))
  )

  test("unicode token metric fixtures stay deterministic and bounded") {
    UnicodeFixtures.AllPairs.foreach { pair =>
      setMetrics.foreach { case (metricName, metric) =>
        val left = UTF8String.fromString(pair.left)
        val right = UTF8String.fromString(pair.right)
        withClue(s"fixture=${pair.name}, metric=$metricName") {
          assertDeterministicAndBounded(metric(left, right))
        }
      }

      val left = UTF8String.fromString(pair.left)
      val right = UTF8String.fromString(pair.right)
      withClue(s"fixture=${pair.name}, metric=monge_elkan") {
        assertDeterministicAndBounded(MongeElkan.similarity(left, right))
      }
    }
  }

  test("token boundaries for unicode separators and no-whitespace scripts are explicit") {
    assert(Jaccard.similarity(UTF8String.fromString("alpha\u00A0beta"), UTF8String.fromString("alpha beta")) === 0.0)
    assert(Jaccard.similarity(UTF8String.fromString("alpha\u200Bbeta"), UTF8String.fromString("alpha beta")) === 0.0)
    assert(
      Jaccard.similarity(
        UTF8String.fromString("\u5317\u4EAC\u5927\u5B66"),
        UTF8String.fromString("\u5317\u4EAC\u5927\u5B66")
      ) === 1.0
    )
  }
}
