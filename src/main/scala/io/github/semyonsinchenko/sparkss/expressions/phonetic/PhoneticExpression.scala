package io.github.semyonsinchenko.sparkss.expressions.phonetic

import io.github.semyonsinchenko.sparkss.expressions.NullIntolerantCompat
import org.apache.spark.sql.catalyst.expressions.codegen.{CodegenContext, ExprCode}
import org.apache.spark.sql.catalyst.expressions.{ImplicitCastInputTypes, UnaryExpression}
import org.apache.spark.sql.types.{DataType, StringType}
import org.apache.spark.unsafe.types.UTF8String

abstract class PhoneticExpression
    extends UnaryExpression
    with ImplicitCastInputTypes
    with NullIntolerantCompat
    with Serializable {

  override def dataType: DataType = StringType

  override def inputTypes: Seq[DataType] = Seq(StringType)

  protected def encode(input: UTF8String): UTF8String

  protected def genEncodeCode(ctx: CodegenContext, inputValue: String): String

  final override protected def nullSafeEval(input: Any): Any = {
    encode(input.asInstanceOf[UTF8String])
  }

  final override protected def doGenCode(ctx: CodegenContext, ev: ExprCode): ExprCode = {
    nullSafeCodeGen(
      ctx,
      ev,
      inputValue => {
        val encodeExpr = genEncodeCode(ctx, inputValue)
        s"${ev.value} = $encodeExpr;"
      }
    )
  }
}
