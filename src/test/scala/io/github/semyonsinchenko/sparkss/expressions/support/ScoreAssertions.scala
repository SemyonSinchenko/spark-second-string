package io.github.semyonsinchenko.sparkss.expressions.support

trait ScoreAssertions {

  protected def assertDeterministicAndBounded(compute: => Double): Unit = {
    val first = compute
    val second = compute
    assert(first == second)
    assert(first >= 0.0)
    assert(first <= 1.0)
  }

  protected def assertBounded(score: Double): Unit = {
    assert(score >= 0.0)
    assert(score <= 1.0)
  }
}
