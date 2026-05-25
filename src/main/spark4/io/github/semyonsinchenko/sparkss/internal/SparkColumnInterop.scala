package io.github.semyonsinchenko.sparkss.internal

import org.apache.spark.sql.Column
import org.apache.spark.sql.classic.SparkColumnCompatBridge
import org.apache.spark.sql.catalyst.expressions.Expression

private[sparkss] object SparkColumnInterop {

  def toExpression(column: Column): Expression = {
    SparkColumnCompatBridge.toExpression(column)
  }

  def fromExpression(expression: Expression): Column = {
    SparkColumnCompatBridge.fromExpression(expression)
  }
}
