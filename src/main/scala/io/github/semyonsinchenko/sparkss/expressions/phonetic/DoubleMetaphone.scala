package io.github.semyonsinchenko.sparkss.expressions.phonetic

import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.unsafe.types.UTF8String

case class DoubleMetaphone(child: Expression) extends PhoneticExpression {

  private final val DoubleMetaphoneModule =
    "io.github.semyonsinchenko.sparkss.expressions.phonetic.DoubleMetaphone$.MODULE$"

  override protected def withNewChildInternal(newChild: Expression): Expression = {
    copy(child = newChild)
  }

  override protected def encode(input: UTF8String): UTF8String = {
    DoubleMetaphone.encode(input)
  }

  override protected def genEncodeCode(ctx: CodegenContext, inputValue: String): String = {
    s"$DoubleMetaphoneModule.encode($inputValue)"
  }
}

object DoubleMetaphone {

  private final val MaxCodeLength = 4

  private val encoder = new ThreadLocal[org.apache.commons.codec.language.DoubleMetaphone] {
    override def initialValue(): org.apache.commons.codec.language.DoubleMetaphone = {
      val instance = new org.apache.commons.codec.language.DoubleMetaphone()
      instance.setMaxCodeLen(MaxCodeLength)
      instance
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

  private[sparkss] def encode(input: UTF8String): UTF8String = {
    val normalized = normalizeAlphabeticUpper(input.toString)
    if (normalized.isEmpty) {
      return UTF8String.fromString("")
    }

    UTF8String.fromString(encoder.get().doubleMetaphone(normalized, false))
  }
}
