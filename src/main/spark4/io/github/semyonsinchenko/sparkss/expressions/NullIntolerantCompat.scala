package io.github.semyonsinchenko.sparkss.expressions

import org.apache.spark.sql.catalyst.expressions.Expression

trait NullIntolerantCompat extends Expression {

  override def nullIntolerant: Boolean = true
}
