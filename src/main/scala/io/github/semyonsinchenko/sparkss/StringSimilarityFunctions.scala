package io.github.semyonsinchenko.sparkss

import io.github.semyonsinchenko.sparkss.expressions.token.Jaccard
import io.github.semyonsinchenko.sparkss.expressions.token.OverlapCoefficient
import io.github.semyonsinchenko.sparkss.expressions.token.SorensenDice
import io.github.semyonsinchenko.sparkss.expressions.token.Cosine
import io.github.semyonsinchenko.sparkss.expressions.token.BraunBlanquet
import io.github.semyonsinchenko.sparkss.expressions.token.MongeElkan
import io.github.semyonsinchenko.sparkss.expressions.matrix.Levenshtein
import io.github.semyonsinchenko.sparkss.expressions.matrix.LcsSimilarity
import io.github.semyonsinchenko.sparkss.expressions.matrix.Jaro
import io.github.semyonsinchenko.sparkss.expressions.matrix.JaroWinkler
import io.github.semyonsinchenko.sparkss.expressions.matrix.NeedlemanWunsch
import io.github.semyonsinchenko.sparkss.expressions.matrix.SmithWaterman
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

  def sorensenDice(left: Column, right: Column): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(SorensenDice(leftExpr, rightExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def sorensenDice(left: String, right: String): Column = {
    sorensenDice(col(left), col(right))
  }

  def overlapCoefficient(left: Column, right: Column): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(OverlapCoefficient(leftExpr, rightExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def overlapCoefficient(left: String, right: String): Column = {
    overlapCoefficient(col(left), col(right))
  }

  def cosine(left: Column, right: Column): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(Cosine(leftExpr, rightExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def cosine(left: String, right: String): Column = {
    cosine(col(left), col(right))
  }

  def braunBlanquet(left: Column, right: Column): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(BraunBlanquet(leftExpr, rightExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def braunBlanquet(left: String, right: String): Column = {
    braunBlanquet(col(left), col(right))
  }

  def monge_elkan(left: Column, right: Column): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(MongeElkan(leftExpr, rightExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def monge_elkan(left: String, right: String): Column = {
    monge_elkan(col(left), col(right))
  }

  def levenshtein(left: Column, right: Column): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(Levenshtein(leftExpr, rightExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def levenshtein(left: String, right: String): Column = {
    levenshtein(col(left), col(right))
  }

  def lcsSimilarity(left: Column, right: Column): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(LcsSimilarity(leftExpr, rightExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def lcsSimilarity(left: String, right: String): Column = {
    lcsSimilarity(col(left), col(right))
  }

  def jaro(left: Column, right: Column): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(Jaro(leftExpr, rightExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def jaro(left: String, right: String): Column = {
    jaro(col(left), col(right))
  }

  def jaroWinkler(left: Column, right: Column): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(JaroWinkler(leftExpr, rightExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def jaroWinkler(left: String, right: String): Column = {
    jaroWinkler(col(left), col(right))
  }

  def needlemanWunsch(left: Column, right: Column): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(NeedlemanWunsch(leftExpr, rightExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def needlemanWunsch(left: String, right: String): Column = {
    needlemanWunsch(col(left), col(right))
  }

  def smithWaterman(left: Column, right: Column): Column = {
    val leftExpr = convertColumnNodeToExpression(left.node.asInstanceOf[AnyRef])
    val rightExpr = convertColumnNodeToExpression(right.node.asInstanceOf[AnyRef])
    val expressionNode = convertExpressionToColumnNode(SmithWaterman(leftExpr, rightExpr))
    convertColumnNodeToColumn(expressionNode)
  }

  def smithWaterman(left: String, right: String): Column = {
    smithWaterman(col(left), col(right))
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
