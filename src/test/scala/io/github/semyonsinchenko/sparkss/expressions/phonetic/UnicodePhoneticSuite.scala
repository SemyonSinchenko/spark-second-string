package io.github.semyonsinchenko.sparkss.expressions.phonetic

import io.github.semyonsinchenko.sparkss.expressions.support.UnicodeFixtures
import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.funsuite.AnyFunSuite

class UnicodePhoneticSuite extends AnyFunSuite {

  private def assertDeterministic(encode: UTF8String => UTF8String, value: String): Unit = {
    val utf8 = UTF8String.fromString(value)
    val first = encode(utf8)
    val second = encode(utf8)
    assert(first === second)
    assert(first != null)
  }

  test("phonetic encoders are deterministic and stable on unicode inputs") {
    val samples = UnicodeFixtures.AllPairs.flatMap(pair => Seq(pair.left, pair.right)).distinct
    samples.foreach { value =>
      withClue(s"soundex input=$value") {
        assertDeterministic(Soundex.encode, value)
      }
      withClue(s"refined_soundex input=$value") {
        assertDeterministic(RefinedSoundex.encode, value)
      }
      withClue(s"double_metaphone input=$value") {
        assertDeterministic(DoubleMetaphone.encode, value)
      }
    }
  }
}
