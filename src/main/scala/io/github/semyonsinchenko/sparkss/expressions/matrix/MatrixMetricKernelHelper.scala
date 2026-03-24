package io.github.semyonsinchenko.sparkss.expressions.matrix

object MatrixMetricKernelHelper {

  private final val NoBoundaryResult = Double.NaN

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
