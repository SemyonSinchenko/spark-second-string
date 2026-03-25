package io.github.semyonsinchenko.sparkss.expressions.token

object TokenMetricKernelHelper {

  private[sparkss] def tokenizeToSet(value: String): java.util.HashSet[String] = {
    val tokens = new java.util.HashSet[String]()
    val length = value.length
    var inToken = false
    var tokenStart = 0
    var index = 0

    while (index < length) {
      if (Character.isWhitespace(value.charAt(index))) {
        if (inToken) {
          tokens.add(value.substring(tokenStart, index))
          inToken = false
        }
      } else if (!inToken) {
        tokenStart = index
        inToken = true
      }
      index += 1
    }

    if (inToken) {
      tokens.add(value.substring(tokenStart, length))
    }

    tokens
  }

  private[sparkss] def tokenizeToSequence(value: String): java.util.ArrayList[String] = {
    val tokens = new java.util.ArrayList[String]()
    val length = value.length
    var inToken = false
    var tokenStart = 0
    var index = 0

    while (index < length) {
      if (Character.isWhitespace(value.charAt(index))) {
        if (inToken) {
          tokens.add(value.substring(tokenStart, index))
          inToken = false
        }
      } else if (!inToken) {
        tokenStart = index
        inToken = true
      }
      index += 1
    }

    if (inToken) {
      tokens.add(value.substring(tokenStart, length))
    }

    tokens
  }

  private[sparkss] def intersectionSize(
      leftTokens: java.util.HashSet[String],
      rightTokens: java.util.HashSet[String]
  ): Int = {
    var smaller = leftTokens
    var larger = rightTokens
    if (leftTokens.size > rightTokens.size) {
      smaller = rightTokens
      larger = leftTokens
    }

    var count = 0
    val iterator = smaller.iterator()
    while (iterator.hasNext) {
      if (larger.contains(iterator.next())) {
        count += 1
      }
    }
    count
  }

  private[sparkss] def clampToUnitInterval(value: Double): Double = {
    if (value < 0.0) {
      0.0
    } else if (value > 1.0) {
      1.0
    } else {
      value
    }
  }
}
