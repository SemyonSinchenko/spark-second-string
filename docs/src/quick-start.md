# Quick Start

## Installation

Add the library artifact to your Spark application build. The artifact suffix follows your Spark minor line.

| Spark line | Scala binary | Artifact name |
|---|---|---|
| 3.5.x | 2.12 | `spark-second-string-3.5` |
| 4.0.x | 2.13 | `spark-second-string-4.0` |

## Usage Flow A: Direct DataFrame API

```scala
import io.github.semyonsinchenko.sparkss.StringSimilarityFunctions
import org.apache.spark.sql.functions.col

val scored = pairs
  .withColumn("jw", StringSimilarityFunctions.jaroWinkler(col("left_name"), col("right_name")))
  .withColumn("sw", StringSimilarityFunctions.smithWaterman("left_name", "right_name"))
```

## Usage Flow B: Spark SQL Extension Functions

```scala
import io.github.semyonsinchenko.sparkss.sql.StringSimilaritySparkSessionExtensions._

spark.registerStringSimilarityFunctions()

val scored = spark.sql(
  """
    |SELECT id,
    |       jaro_winkler(left_name, right_name) AS jw,
    |       smith_waterman(left_name, right_name) AS sw,
    |       ss_soundex(left_name) AS left_soundex
    |FROM candidate_pairs
    |""".stripMargin
)
```

Note: SQL similarity functions stay two-argument for compatibility; configurable metric parameters and `ngramSize` are available via `StringSimilarityFunctions` DSL overloads.

## Docs Build

Docs pages consume generated benchmark and fuzzy-testing variables.

Required pre-runs:

```bash
./dev/benchmarks_suite.sh --mode compare-only
sbt "fuzzy-testing/runMain io.github.semyonsinchenko.sparkss.fuzzy.FuzzyTestingCli --seed 42 --rows 100000 --out fuzzy-testing/target/reports/fuzzy-report.md --save-output fuzzy-testing/target/reports/fuzzy-csv"
```

Build docs:

```bash
sbt docs/laikaSite
```
