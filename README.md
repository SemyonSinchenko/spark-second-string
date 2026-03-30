# spark-second-string

`spark-second-string` is a Spark-native string-similarity library designed for identity-resolution and record-linkage
pipelines.

It helps you add a fast, low-cost similarity stage before heavier model-based ranking or LLM-based matching. In
practical pipelines, this supports cheap blocking, candidate pruning, and explainable score-based filtering before
expensive downstream inference.

## Why use it

- Native Spark SQL expressions for string metrics (no Python UDF overhead)
- Works in both DataFrame API flows and SQL-extension flows
- Includes benchmark and fuzzy-testing tooling for regression visibility

## Metrics

Current metrics include:

- Token-based: `jaccard`, `sorensen_dice`, `overlap_coefficient`, `cosine`, `braun_blanquet`, `monge_elkan`
- Matrix/edit-distance family: `levenshtein`, `lcs_similarity`, `jaro`, `jaro_winkler`, `needleman_wunsch`,
  `smith_waterman`, `affine_gap`

## Quick Start

See docs pages in `docs/` for full usage and compatibility details. The short version is:

1. Add the artifact matching your Spark minor line.
2. Use direct API helpers from `StringSimilarityFunctions`.
3. Or register SQL functions through Spark session extensions.

Example (direct API):

```scala
import io.github.semyonsinchenko.sparkss.StringSimilarityFunctions

df.select(StringSimilarityFunctions.jaroWinkler("left_name", "right_name").as("score"))
```

Example (Spark SQL extension registration):

```scala
import io.github.semyonsinchenko.sparkss.sql.StringSimilaritySparkSessionExtensions._

spark.registerStringSimilarityFunctions()
spark.sql("SELECT jaro_winkler(left_name, right_name) AS score FROM pairs")
```

## Documentation Build Prerequisites

Documentation pages for benchmarks and fuzzy-testing consume generated variables from precomputed reports.

Before building docs, run:

```bash
./dev/benchmarks_suite.sh --mode compare-only
sbt "fuzzy-testing/runMain io.github.semyonsinchenko.sparkss.fuzzy.FuzzyTestingCli --seed 42 --rows 100000 --out fuzzy-testing/target/reports/fuzzy-report.md --save-output fuzzy-testing/target/reports/fuzzy-csv"
```

Then build docs:

```bash
sbt docs/laikaSite
```

If prerequisite artifacts are missing, docs build fails with explicit instructions that point to the required command(
s).
