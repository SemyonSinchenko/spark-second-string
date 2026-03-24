package io.github.semyonsinchenko.sparkss.expressions

import java.io.Serializable
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult.{TypeCheckFailure, TypeCheckSuccess}
import org.apache.spark.sql.catalyst.expressions.{BinaryExpression, Expression}
import org.apache.spark.sql.catalyst.expressions.codegen.{CodegenContext, ExprCode, CodegenFallback}
import org.apache.spark.sql.types.{DataType, DoubleType, StringType}

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
abstract class StringSimExpression extends BinaryExpression with CodegenFallback with Serializable {

  override def dataType: DoubleType = DoubleType

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
  protected[sparkss] def getSim(left: String, right: String): Double

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
    val leftStr = left.asInstanceOf[String]
    val rightStr = right.asInstanceOf[String]
    val result = getSim(leftStr, rightStr)
    java.lang.Double.valueOf(result)
  }

  /** Evaluate the expression on an input row.
    *
    * Handles null inputs by returning null. Validates and casts inputs to strings, then calls getSim and wraps the
    * result.
    *
    * @param input
    *   the input row
    * @return
    *   similarity score as Any (Double or null)
    */
  final override def eval(input: InternalRow): Any = {
    if (isNullAt(input, 0) || isNullAt(input, 1)) {
      null
    } else {
      val leftStr = input.getString(0)
      val rightStr = input.getString(1)
      val result = getSim(leftStr, rightStr)
      java.lang.Double.valueOf(result)
    }
  }

  /** Check if a value at the given ordinal is null.
    */
  private def isNullAt(input: InternalRow, ordinal: Int): Boolean = {
    input.isNullAt(ordinal)
  }
}
