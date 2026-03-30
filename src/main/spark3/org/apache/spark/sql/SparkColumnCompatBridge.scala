package org.apache.spark.sql

import org.apache.spark.sql.catalyst.expressions.Expression

object SparkColumnCompatBridge {

  def toExpression(column: Column): Expression = {
    column.expr
  }

  def fromExpression(expression: Expression): Column = {
    new Column(expression)
  }
}
