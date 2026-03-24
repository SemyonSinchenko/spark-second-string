package io.github.semyonsinchenko.sparkss.expressions.matrix

import org.scalatest.funsuite.AnyFunSuite

class MatrixMetricKernelHelperSuite extends AnyFunSuite {

  test("boundary behavior returns 1.0 for both empty inputs") {
    val boundary = MatrixMetricKernelHelper.boundarySimilarity(0, 0)
    assert(MatrixMetricKernelHelper.hasBoundaryResult(boundary))
    assert(boundary === 1.0)
  }

  test("boundary behavior returns 0.0 for one empty input") {
    val leftEmpty = MatrixMetricKernelHelper.boundarySimilarity(0, 3)
    val rightEmpty = MatrixMetricKernelHelper.boundarySimilarity(3, 0)

    assert(MatrixMetricKernelHelper.hasBoundaryResult(leftEmpty))
    assert(MatrixMetricKernelHelper.hasBoundaryResult(rightEmpty))
    assert(leftEmpty === 0.0)
    assert(rightEmpty === 0.0)
  }

  test("normalization is clamped to [0.0, 1.0]") {
    assert(MatrixMetricKernelHelper.normalizeDistance(0, 4, 4) === 1.0)
    assert(MatrixMetricKernelHelper.normalizeDistance(10, 4, 4) === 0.0)
  }
}
