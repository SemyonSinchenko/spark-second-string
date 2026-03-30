package io.github.semyonsinchenko.sparkss.expressions.token

import org.apache.spark.sql.catalyst.analysis.TypeCheckResult.TypeCheckFailure
import org.apache.spark.sql.catalyst.expressions.Literal
import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.funsuite.AnyFunSuite

class NgramTokenMetricsSuite extends AnyFunSuite {

  private def utf8(value: String): UTF8String = UTF8String.fromString(value)

  private def assertClose(actual: Double, expected: Double): Unit = {
    assert(Math.abs(actual - expected) <= 1e-12)
  }

  test("char ngram tokenization handles empty, short, and deduplication semantics") {
    assert(TokenMetricKernelHelper.tokenizeToCharNgramSet("", 2).isEmpty)

    val short = TokenMetricKernelHelper.tokenizeToCharNgramSet("ab", 3)
    assert(short.size() === 1)
    assert(short.contains("ab"))

    val repeated = TokenMetricKernelHelper.tokenizeToCharNgramSet("aaaa", 2)
    assert(repeated.size() === 1)
    assert(repeated.contains("aa"))
  }

  test("ngram mode matches hand-computed jaccard values") {
    assertClose(Jaccard.similarity(utf8("abcd"), utf8("abce"), 2), 0.5)
    assertClose(Jaccard.similarity(utf8("abc"), utf8("abc"), 2), 1.0)
    assertClose(Jaccard.similarity(utf8("abc"), utf8("xyz"), 2), 0.0)
    assertClose(Jaccard.similarity(utf8("ab"), utf8("abc"), 2), 0.5)
    assertClose(Jaccard.similarity(utf8("ab"), utf8("ab"), 3), 1.0)
    assertClose(Jaccard.similarity(utf8("a b"), utf8("a c"), 2), 1.0 / 3.0)
  }

  test("ngram mode works across token metrics and preserves whitespace mode parity") {
    val left = utf8("alpha beta")
    val right = utf8("alpha gamma")

    assertClose(SorensenDice.similarity(left, right), SorensenDice.similarity(left, right, 0))
    assertClose(OverlapCoefficient.similarity(left, right), OverlapCoefficient.similarity(left, right, 0))
    assertClose(Cosine.similarity(left, right), Cosine.similarity(left, right, 0))
    assertClose(BraunBlanquet.similarity(left, right), BraunBlanquet.similarity(left, right, 0))

    assert(SorensenDice.similarity(left, right, 2) >= 0.0)
    assert(OverlapCoefficient.similarity(left, right, 2) >= 0.0)
    assert(Cosine.similarity(left, right, 2) >= 0.0)
    assert(BraunBlanquet.similarity(left, right, 2) >= 0.0)
  }

  test("monge elkan supports configurable inner metric and ngram mode") {
    val left = utf8("stephen smyth")
    val right = utf8("steven smith")

    val jaroWinklerScore = MongeElkan.similarity(left, right, "jaro_winkler", 0)
    val jaroScore = MongeElkan.similarity(left, right, "jaro", 0)
    val ngramScore = MongeElkan.similarity(left, right, "jaro_winkler", 2)

    assert(jaroWinklerScore !== jaroScore)
    assert(ngramScore >= 0.0)
    assert(ngramScore <= 1.0)
  }

  test("analysis-time validation rejects negative ngram and invalid inner metric") {
    assert(Jaccard(Literal("a"), Literal("b"), -1).checkInputDataTypes().isInstanceOf[TypeCheckFailure])
    assert(MongeElkan(Literal("a"), Literal("b"), "invalid", 0).checkInputDataTypes().isInstanceOf[TypeCheckFailure])
    assert(MongeElkan(Literal("a"), Literal("b"), "jaro", -1).checkInputDataTypes().isInstanceOf[TypeCheckFailure])
  }
}
