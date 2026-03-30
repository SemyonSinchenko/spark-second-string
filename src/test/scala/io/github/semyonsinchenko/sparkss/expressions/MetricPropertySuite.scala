package io.github.semyonsinchenko.sparkss.expressions

import io.github.semyonsinchenko.sparkss.expressions.matrix._
import io.github.semyonsinchenko.sparkss.expressions.token._
import org.apache.spark.unsafe.types.UTF8String
import org.scalacheck.Gen
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class MetricPropertySuite extends AnyFunSuite with ScalaCheckPropertyChecks {

  private val emptyUtf8 = UTF8String.fromString("")

  override implicit val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 60, sizeRange = 30)

  private val asciiChar: Gen[Char] = Gen.oneOf(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9'))
  private val unicodeChar: Gen[Char] = Gen.oneOf(
    '\u00E9', '\u00FC', '\u6771', '\u4EAC', '\u4E16', '\u754C', '\u0421', '\u043C', '\u0438', '\u0442', '\u00A0',
    '\u200B'
  )
  private val punctuationChar: Gen[Char] = Gen.oneOf(' ', '\t', ',', '.', '-', '_', '!', '?')
  private val anyChar: Gen[Char] = Gen.frequency(
    7 -> asciiChar,
    2 -> unicodeChar,
    1 -> punctuationChar
  )

  private val anyString: Gen[String] =
    Gen.chooseNum(0, 64).flatMap(length => Gen.listOfN(length, anyChar).map(_.mkString))
  private val nonEmptyString: Gen[String] =
    Gen.chooseNum(1, 64).flatMap(length => Gen.listOfN(length, anyChar).map(_.mkString))

  private val allMetrics: Seq[(String, (UTF8String, UTF8String) => Double)] = Seq(
    "jaccard" -> ((l, r) => Jaccard.similarity(l, r)),
    "sorensen_dice" -> ((l, r) => SorensenDice.similarity(l, r)),
    "overlap_coefficient" -> ((l, r) => OverlapCoefficient.similarity(l, r)),
    "cosine" -> ((l, r) => Cosine.similarity(l, r)),
    "braun_blanquet" -> ((l, r) => BraunBlanquet.similarity(l, r)),
    "monge_elkan" -> ((l, r) => MongeElkan.similarity(l, r)),
    "levenshtein" -> ((l, r) => Levenshtein.similarity(l, r)),
    "lcs_similarity" -> ((l, r) => LcsSimilarity.similarity(l, r)),
    "jaro" -> ((l, r) => Jaro.similarity(l, r)),
    "jaro_winkler" -> ((l, r) => JaroWinkler.similarity(l, r)),
    "needleman_wunsch" -> ((l, r) => NeedlemanWunsch.similarity(l, r)),
    "smith_waterman" -> ((l, r) => SmithWaterman.similarity(l, r)),
    "affine_gap" -> ((l, r) => AffineGap.similarity(l, r))
  )

  private def isWhitespaceOnly(value: String): Boolean = {
    value.nonEmpty && value.forall(Character.isWhitespace)
  }

  test("all metrics satisfy identity for non-empty strings") {
    forAll(nonEmptyString) { value =>
      val utf8 = UTF8String.fromString(value)
      allMetrics.foreach { case (name, metric) =>
        assert(metric(utf8, utf8) === 1.0, s"identity failed for $name with input '$value'")
      }
    }
  }

  test("all metrics are bounded in [0.0, 1.0]") {
    forAll(anyString, anyString) { (left, right) =>
      val l = UTF8String.fromString(left)
      val r = UTF8String.fromString(right)
      allMetrics.foreach { case (name, metric) =>
        val score = metric(l, r)
        assert(score >= 0.0 && score <= 1.0, s"bounds failed for $name with left='$left' right='$right': $score")
      }
    }
  }

  test("all metrics satisfy empty-input behavior") {
    forAll(anyString) { value =>
      val utf8 = UTF8String.fromString(value)
      allMetrics.foreach { case (name, metric) =>
        if (value.isEmpty || (name == "monge_elkan" && isWhitespaceOnly(value))) {
          assert(metric(emptyUtf8, utf8) === 1.0, s"empty-left failed for $name with input '$value'")
          assert(metric(utf8, emptyUtf8) === 1.0, s"empty-right failed for $name with input '$value'")
        } else {
          assert(metric(emptyUtf8, utf8) === 0.0, s"empty-left failed for $name with input '$value'")
          assert(metric(utf8, emptyUtf8) === 0.0, s"empty-right failed for $name with input '$value'")
        }
      }
    }

    allMetrics.foreach { case (name, metric) =>
      assert(metric(emptyUtf8, emptyUtf8) === 1.0, s"empty-empty failed for $name")
    }
  }

  test("symmetric metrics remain symmetric") {
    forAll(anyString, anyString) { (left, right) =>
      val l = UTF8String.fromString(left)
      val r = UTF8String.fromString(right)
      allMetrics.foreach { case (name, metric) =>
        assert(metric(l, r) === metric(r, l), s"symmetry failed for $name with left='$left' right='$right'")
      }
    }
  }
}
