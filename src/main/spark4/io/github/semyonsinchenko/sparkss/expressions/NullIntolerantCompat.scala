package io.github.semyonsinchenko.sparkss.expressions

import org.apache.spark.sql.catalyst.expressions.Expression

private[sparkss] trait NullIntolerantCompat extends Expression {

  override def nullIntolerant: Boolean = true
}
