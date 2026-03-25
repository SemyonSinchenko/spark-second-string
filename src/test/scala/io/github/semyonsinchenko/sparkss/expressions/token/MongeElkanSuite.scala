package io.github.semyonsinchenko.sparkss.expressions.token

import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.funsuite.AnyFunSuite

class MongeElkanSuite extends AnyFunSuite {

  private def score(left: String, right: String): Double = {
    MongeElkan.similarity(UTF8String.fromString(left), UTF8String.fromString(right))
  }

  test("identical inputs should return 1.0") {
    assert(score("alpha beta", "alpha beta") === 1.0)
  }

  test("both-empty and one-empty boundaries are canonical") {
    assert(score("", "") === 1.0)
    assert(score("", "alpha") === 0.0)
    assert(score("alpha", "") === 0.0)
  }

  test("whitespace-only inputs are treated as empty") {
    assert(score("   \t", "\n ") === 1.0)
    assert(score("   ", "alpha") === 0.0)
    assert(score("alpha", " \n") === 0.0)
  }

  test("token-order variation remains deterministic") {
    assert(score("alpha beta gamma", "gamma beta alpha") === 1.0)
  }

  test("repeated tokens influence the score") {
    val withoutRepeat = score("alpha beta", "alpha gamma")
    val withRepeat = score("alpha alpha beta", "alpha gamma")

    assert(withRepeat > withoutRepeat)
  }

  test("punctuation is preserved inside tokens") {
    val punctuationToken = score("alpha,beta", "alpha beta")

    assert(punctuationToken >= 0.0)
    assert(punctuationToken <= 1.0)
    assert(punctuationToken < 1.0)
  }

  test("asymmetric token counts stay in unit interval") {
    val asymmetric = score("alpha beta gamma delta epsilon", "alpha beta")

    assert(asymmetric >= 0.0)
    assert(asymmetric <= 1.0)
  }
}
