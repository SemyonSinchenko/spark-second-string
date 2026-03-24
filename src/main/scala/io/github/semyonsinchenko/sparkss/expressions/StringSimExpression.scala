package io.github.semyonsinchenko.sparkss.expressions

import java.io.Serializable
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult.{TypeCheckFailure, TypeCheckSuccess}
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext
import org.apache.spark.sql.catalyst.expressions.{BinaryExpression, Expression, ImplicitCastInputTypes}
import org.apache.spark.sql.types.{DataType, DoubleType, StringType}
import org.apache.spark.unsafe.types.UTF8String

/** Abstract base class for string similarity Catalyst expressions.
  *
  * Provides common boilerplate for binary string expressions that return a Double similarity score. Subclasses must
  * implement the similarity algorithm via getSim and code generation via doGenCode.
  *
  * @example
  *   {{{
  *   class Jaccard extends StringSimExpression {
  *     override def getSim(left: String, right: String): Double = {
  *       // implementation
  *     }
  *
  *     override def doGenCode(ctx: CodegenContext, ev: ExprCode): ExprCode = {
  *       // code generation
  *     }
  *   }
  *   }}}
  */
abstract class StringSimExpression extends BinaryExpression with ImplicitCastInputTypes with Serializable {

  override def dataType: DoubleType = DoubleType

  final override def inputTypes: Seq[DataType] = Seq(StringType, StringType)

  override def withNewChildrenInternal(newLeft: Expression, newRight: Expression): Expression = {
    throw new UnsupportedOperationException("withNewChildrenInternal should be implemented by concrete subclasses")
  }

  /** Check that both input data types are explicitly StringType.
    *
    * @return
    *   TypeCheckResult indicating success or failure with error message
    */
  override def checkInputDataTypes(): TypeCheckResult = {
    if (left.dataType != StringType) {
      TypeCheckFailure(s"Input 0 must be a string type, but got ${left.dataType.catalogString}")
    } else if (right.dataType != StringType) {
      TypeCheckFailure(s"Input 1 must be a string type, but got ${right.dataType.catalogString}")
    } else {
      TypeCheckSuccess
    }
  }

  /** Indicates that this expression returns null if any input is null.
    */
  override def nullIntolerant: Boolean = true

  /** Compute similarity between two strings.
    *
    * @param left
    *   first string (never null)
    * @param right
    *   second string (never null)
    * @return
    *   similarity score between 0.0 and 1.0
    */
  protected def evalSimilarity(left: UTF8String, right: UTF8String): Double

  protected def genSimilarityCode(ctx: CodegenContext, leftValue: String, rightValue: String): String

  /** Null-safe evaluation of the similarity function.
    *
    * If either input is null, returns null. Otherwise, casts inputs to strings and delegates to getSim.
    *
    * @param left
    *   left input value
    * @param right
    *   right input value
    * @return
    *   similarity score as Double, or null if either input is null
    */
  final override protected def nullSafeEval(left: Any, right: Any): Any = {
    val leftUtf8 = left.asInstanceOf[UTF8String]
    val rightUtf8 = right.asInstanceOf[UTF8String]
    java.lang.Double.valueOf(evalSimilarity(leftUtf8, rightUtf8))
  }

  final override protected def doGenCode(
      ctx: org.apache.spark.sql.catalyst.expressions.codegen.CodegenContext,
      ev: org.apache.spark.sql.catalyst.expressions.codegen.ExprCode
  ): org.apache.spark.sql.catalyst.expressions.codegen.ExprCode = {
    nullSafeCodeGen(
      ctx,
      ev,
      (leftValue, rightValue) => {
        val scoreExpr = genSimilarityCode(ctx, leftValue, rightValue)
        s"${ev.value} = $scoreExpr;"
      }
    )
  }
}

abstract class TokenMetricExpression extends StringSimExpression {

  protected def evalTokenMetric(left: UTF8String, right: UTF8String): Double

  protected def genTokenMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String

  final override protected def evalSimilarity(left: UTF8String, right: UTF8String): Double = {
    evalTokenMetric(left, right)
  }

  final override protected def genSimilarityCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    genTokenMetricCode(ctx, leftValue, rightValue)
  }
}

abstract class MatrixMetricExpression extends StringSimExpression {

  protected def evalMatrixMetric(left: UTF8String, right: UTF8String): Double

  protected def genMatrixMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String

  final override protected def evalSimilarity(left: UTF8String, right: UTF8String): Double = {
    evalMatrixMetric(left, right)
  }

  final override protected def genSimilarityCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
    genMatrixMetricCode(ctx, leftValue, rightValue)
  }
}
