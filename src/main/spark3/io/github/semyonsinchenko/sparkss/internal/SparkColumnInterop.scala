package io.github.semyonsinchenko.sparkss.internal

import org.apache.spark.sql.Column
import org.apache.spark.sql.SparkColumnCompatBridge
import org.apache.spark.sql.catalyst.expressions.Expression

object SparkColumnInterop {

  def toExpression(column: Column): Expression = {
    SparkColumnCompatBridge.toExpression(column)
  }

  def fromExpression(expression: Expression): Column = {
    SparkColumnCompatBridge.fromExpression(expression)
  }
}
