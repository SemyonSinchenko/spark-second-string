package io.github.semyonsinchenko.sparkss.expressions.phonetic

import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class RefinedSoundex(child: Expression) extends PhoneticExpression {

  private final val RefinedSoundexModule =
    "io.github.semyonsinchenko.sparkss.expressions.phonetic.RefinedSoundex$.MODULE$"

  override protected def withNewChildInternal(newChild: Expression): Expression = {
    copy(child = newChild)
  }

  override protected def encode(input: UTF8String): UTF8String = {
    RefinedSoundex.encode(input)
  }

  override protected def genEncodeCode(ctx: CodegenContext, inputValue: String): String = {
    s"$RefinedSoundexModule.encode($inputValue)"
  }
}

object RefinedSoundex {

  private val Mapping: Array[Int] = Array(
    0, 1, 2, 3, 0, 5, 6, 0, 0, 6, 2, 7, 8, 8, 0, 1, 2, 9, 2, 3, 0, 5, 0, 2, 0, 2
  )

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

  private def mapCode(c: Char): Int = {
    Mapping(c - 'A')
  }

  private[sparkss] def encode(input: UTF8String): UTF8String = {
    val normalized = normalizeAlphabeticUpper(input.toString)
    if (normalized.isEmpty) {
      return UTF8String.fromString("")
    }

    val output = new StringBuilder(normalized.length + 1)
    output.append(normalized.charAt(0))

    var previousCode = mapCode(normalized.charAt(0))
    var i = 1
    while (i < normalized.length) {
      val code = mapCode(normalized.charAt(i))
      if (code != previousCode) {
        output.append((code + '0').toChar)
      }
      previousCode = code
      i += 1
    }

    UTF8String.fromString(output.toString)
  }
}
