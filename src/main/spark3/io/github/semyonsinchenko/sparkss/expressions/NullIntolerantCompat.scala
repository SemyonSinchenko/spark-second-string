package io.github.semyonsinchenko.sparkss.expressions

import org.apache.spark.sql.catalyst.expressions.Expression

trait NullIntolerantCompat extends Expression {

  def nullIntolerant: Boolean = true
}
