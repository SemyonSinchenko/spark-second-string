package io.github.semyonsinchenko.sparkss.expressions

import org.apache.spark.sql.catalyst.expressions.Expression

private[sparkss] trait NullIntolerantCompat extends Expression with org.apache.spark.sql.catalyst.expressions.NullIntolerant {

  def nullIntolerant: Boolean = true
}
