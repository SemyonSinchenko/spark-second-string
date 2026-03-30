package io.github.semyonsinchenko.sparkss.expressions.phonetic

import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.funsuite.AnyFunSuite

class PhoneticExpressionsSuite extends AnyFunSuite {

  private def utf8(value: String): UTF8String = UTF8String.fromString(value)

  test("soundex matches canonical examples") {
    assert(Soundex.encode(utf8("Robert")).toString === "R163")
    assert(Soundex.encode(utf8("Rupert")).toString === "R163")
    assert(Soundex.encode(utf8("Ashcraft")).toString === "A261")
    assert(Soundex.encode(utf8("Tymczak")).toString === "T522")
    assert(Soundex.encode(utf8("Pfister")).toString === "P236")
    assert(Soundex.encode(utf8("A")).toString === "A000")
    assert(Soundex.encode(utf8("123")).toString === "")
  }

  test("refined soundex is deterministic and case-insensitive") {
    val robert = RefinedSoundex.encode(utf8("Robert")).toString
    val rupert = RefinedSoundex.encode(utf8("Rupert")).toString
    assert(robert === rupert)
    assert(RefinedSoundex.encode(utf8("SMITH")).toString === RefinedSoundex.encode(utf8("smith")).toString)
  }

  test("double metaphone primary code is deterministic and strips non alphabetics") {
    val stephen = DoubleMetaphone.encode(utf8("Stephen")).toString
    val steven = DoubleMetaphone.encode(utf8("Steven")).toString
    assert(stephen === steven)
    assert(DoubleMetaphone.encode(utf8("O'Brien")).toString === DoubleMetaphone.encode(utf8("OBRIEN")).toString)
    assert(DoubleMetaphone.encode(utf8("123")).toString === "")
  }
}
