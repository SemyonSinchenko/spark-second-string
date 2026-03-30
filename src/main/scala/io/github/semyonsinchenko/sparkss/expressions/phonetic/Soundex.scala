package io.github.semyonsinchenko.sparkss.expressions.phonetic

import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class Soundex(child: Expression) extends PhoneticExpression {

  private final val SoundexModule = "io.github.semyonsinchenko.sparkss.expressions.phonetic.Soundex$.MODULE$"

  override protected def withNewChildInternal(newChild: Expression): Expression = {
    copy(child = newChild)
  }

  override protected def encode(input: UTF8String): UTF8String = {
    Soundex.encode(input)
  }

  override protected def genEncodeCode(ctx: CodegenContext, inputValue: String): String = {
    s"$SoundexModule.encode($inputValue)"
  }
}

object Soundex {

  private val CodeMap: Array[Int] = {
    val map = Array.fill(26)(0)
    setCodes(map, "BFPV", 1)
    setCodes(map, "CGJKQSXZ", 2)
    setCodes(map, "DT", 3)
    setCodes(map, "L", 4)
    setCodes(map, "MN", 5)
    setCodes(map, "R", 6)
    map
  }

  private def setCodes(map: Array[Int], chars: String, code: Int): Unit = {
    var i = 0
    while (i < chars.length) {
      map(chars.charAt(i) - 'A') = code
      i += 1
    }
  }

  private def normalizeAlphabeticUpper(input: String): String = {
    val builder = new StringBuilder(input.length)
    var i = 0
    while (i < input.length) {
      val c = Character.toUpperCase(input.charAt(i))
      if (c >= 'A' && c <= 'Z') {
        builder.append(c)
      }
      i += 1
    }
    builder.toString()
  }

  private def codeFor(c: Char): Int = {
    CodeMap(c - 'A')
  }

  private def resetsDuplicateSuppression(c: Char): Boolean = {
    c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U' || c == 'Y'
  }

  private[sparkss] def encode(input: UTF8String): UTF8String = {
    val normalized = normalizeAlphabeticUpper(input.toString)
    if (normalized.isEmpty) {
      return UTF8String.fromString("")
    }

    val output = new StringBuilder(4)
    val firstLetter = normalized.charAt(0)
    output.append(firstLetter)

    var previousCode = codeFor(firstLetter)
    var i = 1
    while (i < normalized.length && output.length < 4) {
      val c = normalized.charAt(i)
      val code = codeFor(c)
      if (code != 0 && code != previousCode) {
        output.append((code + '0').toChar)
      }
      if (code != 0) {
        previousCode = code
      } else if (resetsDuplicateSuppression(c)) {
        previousCode = 0
      }
      i += 1
    }

    while (output.length < 4) {
      output.append('0')
    }

    UTF8String.fromString(output.toString)
  }
}
