package io.github.semyonsinchenko.sparkss.benchmarks

object BenchmarkSuiteConfig {
  val WarmupIterations: Int = 6
  val WarmupSeconds: Int = 1
  val MeasurementIterations: Int = 6
  val MeasurementSeconds: Int = 1
  val Forks: Int = 1
  val Mode: String = "thrpt"
  val RowCount: Int = 10000

  val NativeJsonFileName: String = "native-jmh.json"
  val NativeSparkJsonFileName: String = "native-spark-jmh.json"
  val LegacyJsonFileName: String = "legacy-udf-jmh.json"
  val CompareFileName: String = "compare-table.txt"
}
