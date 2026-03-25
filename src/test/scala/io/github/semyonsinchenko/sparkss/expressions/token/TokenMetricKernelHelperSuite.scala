package io.github.semyonsinchenko.sparkss.expressions.token

import org.scalatest.funsuite.AnyFunSuite

class TokenMetricKernelHelperSuite extends AnyFunSuite {

  test("tokenization treats mixed and repeated whitespace consistently") {
    val spaces = TokenMetricKernelHelper.tokenizeToSet("alpha  beta   gamma")
    val mixed = TokenMetricKernelHelper.tokenizeToSet("alpha\tbeta\ngamma")

    assert(spaces == mixed)
    assert(spaces.size === 3)
  }

  test("tokenization collapses duplicate tokens") {
    val tokens = TokenMetricKernelHelper.tokenizeToSet("alpha alpha alpha beta")

    assert(tokens.size === 2)
    assert(tokens.contains("alpha"))
    assert(tokens.contains("beta"))
  }

  test("intersection size uses set overlap cardinality") {
    val leftTokens = TokenMetricKernelHelper.tokenizeToSet("alpha beta gamma")
    val rightTokens = TokenMetricKernelHelper.tokenizeToSet("alpha gamma delta")

    assert(TokenMetricKernelHelper.intersectionSize(leftTokens, rightTokens) === 2)
    assert(TokenMetricKernelHelper.intersectionSize(rightTokens, leftTokens) === 2)
  }

  test("token sequence preserves order and duplicate tokens") {
    val sequence = TokenMetricKernelHelper.tokenizeToSequence("alpha beta alpha gamma")

    assert(sequence.size() === 4)
    assert(sequence.get(0) === "alpha")
    assert(sequence.get(1) === "beta")
    assert(sequence.get(2) === "alpha")
    assert(sequence.get(3) === "gamma")
  }

  test("whitespace-only input tokenizes to empty sequence") {
    val sequence = TokenMetricKernelHelper.tokenizeToSequence(" \t\n ")

    assert(sequence.isEmpty)
  }
}
