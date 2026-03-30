package org.apache.spark.sql.classic

import org.apache.spark.sql.Column
import org.apache.spark.sql.catalyst.expressions.Expression

object SparkColumnCompatBridge {

  def toExpression(column: Column): Expression = {
    ColumnNodeToExpressionConverter(column.node)
  }

  def fromExpression(expression: Expression): Column = {
    new Column(ExpressionColumnNode(expression))
  }
}
