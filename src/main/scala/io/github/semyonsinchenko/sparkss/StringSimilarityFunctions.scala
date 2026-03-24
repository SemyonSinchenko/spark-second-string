package io.github.semyonsinchenko.sparkss

import io.github.semyonsinchenko.sparkss.expressions.token.Jaccard
import org.apache.spark.sql.Column
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.functions.col

object StringSimilarityFunctions {

  def jaccard(left: Column, right: Column): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(Jaccard(leftExpr, rightExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def jaccard(left: String, right: String): Column = {
    jaccard(col(left), col(right))
  }

  private def convertColumnNodeToExpression(node: AnyRef): Expression = {
    val moduleClass = Class.forName("org.apache.spark.sql.classic.ColumnNodeToExpressionConverter$")
    val module = moduleClass.getField("MODULE$").get(null)
    val columnNodeClass = Class.forName("org.apache.spark.sql.internal.ColumnNode")
    val apply = moduleClass.getMethod("apply", columnNodeClass)
    apply.invoke(module, node).asInstanceOf[Expression]
  }

  private def convertExpressionToColumnNode(expression: Expression): AnyRef = {
    val moduleClass = Class.forName("org.apache.spark.sql.classic.ExpressionColumnNode$")
    val module = moduleClass.getField("MODULE$").get(null)
    val apply = moduleClass.getMethod("apply", classOf[Expression])
    apply.invoke(module, expression).asInstanceOf[AnyRef]
  }

  private def convertColumnNodeToColumn(node: AnyRef): Column = {
    val columnNodeClass = Class.forName("org.apache.spark.sql.internal.ColumnNode")
    val constructor = classOf[Column].getConstructor(columnNodeClass)
    constructor.newInstance(node).asInstanceOf[Column]
  }
}
