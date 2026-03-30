package io.github.semyonsinchenko.sparkss.expressions.matrix

import org.apache.spark.unsafe.types.UTF8String
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

  test("resolved strings use character lengths for non-ascii fallback path") {
    val resolved = new MatrixMetricKernelHelper.ResolvedStrings(
      UTF8String.fromString("caf\u00E9"),
      UTF8String.fromString("\u6771\u4EAC")
    )

    val isAsciiField = resolved.getClass.getDeclaredField("isAscii")
    isAsciiField.setAccessible(true)
    assert(!isAsciiField.getBoolean(resolved))

    assert(resolved.leftLength === 4)
    assert(resolved.rightLength === 2)
    assert(resolved.leftCharAt(3) === '\u00E9'.toInt)
    assert(resolved.rightCharAt(0) === '\u6771'.toInt)
    assert(resolved.rightCharAt(1) === '\u4EAC'.toInt)
  }

  test("resolved strings remain deterministic for surrogate pairs") {
    val emoji = "\uD83D\uDE00"
    val resolved = new MatrixMetricKernelHelper.ResolvedStrings(
      UTF8String.fromString(emoji),
      UTF8String.fromString(emoji)
    )

    assert(resolved.leftLength === 2)
    assert(resolved.rightLength === 2)
    assert(resolved.leftCharAt(0) === emoji.charAt(0).toInt)
    assert(resolved.leftCharAt(1) === emoji.charAt(1).toInt)
    assert(resolved.leftCharAt(0) === resolved.leftCharAt(0))
    assert(resolved.leftCharAt(1) === resolved.leftCharAt(1))
  }

  test("resolved strings stay on ascii fast path for ascii-only inputs") {
    val resolved = new MatrixMetricKernelHelper.ResolvedStrings(
      UTF8String.fromString("spark"),
      UTF8String.fromString("spork")
    )
    val isAsciiField = resolved.getClass.getDeclaredField("isAscii")
    isAsciiField.setAccessible(true)
    assert(isAsciiField.getBoolean(resolved))
    assert(resolved.leftLength === 5)
    assert(resolved.rightLength === 5)
  }
}
