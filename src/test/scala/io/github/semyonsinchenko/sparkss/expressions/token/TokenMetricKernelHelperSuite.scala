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

  test("unicode tokenization covers non-ascii separators and zero-width boundaries") {
    val nbsp = TokenMetricKernelHelper.tokenizeToSet("alpha\u00A0beta gamma")
    assert(nbsp.size() === 2)
    assert(nbsp.contains("alpha\u00A0beta"))
    assert(nbsp.contains("gamma"))

    val zeroWidth = TokenMetricKernelHelper.tokenizeToSet("alpha\u200Bbeta")
    assert(zeroWidth.size() === 1)
    assert(zeroWidth.contains("alpha\u200Bbeta"))

    val cjkNoWhitespace = TokenMetricKernelHelper.tokenizeToSet("\u5317\u4EAC\u5927\u5B66")
    assert(cjkNoWhitespace.size() === 1)
    assert(cjkNoWhitespace.contains("\u5317\u4EAC\u5927\u5B66"))
  }
}
