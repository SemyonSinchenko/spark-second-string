package io.github.semyonsinchenko.sparkss.expressions.matrix

import org.apache.spark.unsafe.types.UTF8String

object MatrixMetricKernelHelper {

  private final val NoBoundaryResult = Double.NaN

  /** Resolved string representation for matrix metrics. When both inputs are ASCII-only, stores raw byte arrays to
    * avoid String allocation. Otherwise falls back to Java Strings via UTF8String.toString.
    */
  private[matrix] final class ResolvedStrings(left: UTF8String, right: UTF8String) {
    private val leftBytes = left.getBytes
    private val rightBytes = right.getBytes
    private val isAscii = isAllAscii(leftBytes) && isAllAscii(rightBytes)
    private val leftString = if (isAscii) null else left.toString
    private val rightString = if (isAscii) null else right.toString

    def leftLength: Int = if (isAscii) leftBytes.length else leftString.length
    def rightLength: Int = if (isAscii) rightBytes.length else rightString.length
    def leftCharAt(i: Int): Int = if (isAscii) leftBytes(i) & 0xff else leftString.charAt(i).toInt
    def rightCharAt(i: Int): Int = if (isAscii) rightBytes(i) & 0xff else rightString.charAt(i).toInt
  }

  private def isAllAscii(bytes: Array[Byte]): Boolean = {
    var i = 0
    while (i < bytes.length) {
      if (bytes(i) < 0) return false
      i += 1
    }
    true
  }

  private[sparkss] def boundarySimilarity(leftLength: Int, rightLength: Int): Double = {
    if (leftLength == 0 && rightLength == 0) {
      1.0
    } else if (leftLength == 0 || rightLength == 0) {
      0.0
    } else {
      NoBoundaryResult
    }
  }

  private[sparkss] def hasBoundaryResult(value: Double): Boolean = {
    !java.lang.Double.isNaN(value)
  }

  private[sparkss] def createWorkspaceRow(size: Int): Array[Int] = {
    new Array[Int](size)
  }

  private[sparkss] def createInitializedDistanceRow(size: Int): Array[Int] = {
    val row = createWorkspaceRow(size)
    var index = 0
    while (index < size) {
      row(index) = index
      index += 1
    }
    row
  }

  private[sparkss] def normalizeDistance(distance: Int, leftLength: Int, rightLength: Int): Double = {
    val maxLength = Math.max(leftLength, rightLength)
    if (maxLength == 0) {
      return 1.0
    }

    val rawScore = 1.0 - (distance.toDouble / maxLength.toDouble)
    clampToUnitInterval(rawScore)
  }

  private[sparkss] def clampToUnitInterval(score: Double): Double = {
    if (score < 0.0) {
      0.0
    } else if (score > 1.0) {
      1.0
    } else {
      score
    }
  }
}
