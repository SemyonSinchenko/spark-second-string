# Overview

`spark-second-string` provides Spark-native similarity metrics for string-heavy workloads such as identity resolution, entity matching, and duplicate detection.

The core idea is to keep similarity scoring inside Spark execution, so teams can use broad and explainable string heuristics as an inexpensive stage before expensive model-based matching.

## Where it fits?

Most large-scale entity-resolution / identity-resolution pipelines have the same four stages. The hard scale work is in stages 1–3; stage 4 stops being a scale problem because each cluster is independent and small.

### 1. Blocking — narrow the candidate space

You don't want all-vs-all comparison. Use cheap, exact keys to bucket records and only score within the bucket. Spark built-ins are enough:

```sql
-- Block by digits-only key (phone numbers, IDs with separators)
SELECT *, regexp_replace(phone, '[^0-9]', '') AS block_key FROM customers

-- Block by first 4 letters of normalized last name
SELECT *, substring(lower(last_name), 1, 4) AS block_key FROM customers

-- Block by leading digit (account numbers, postal codes)
SELECT *, substring(account, 1, 1) AS block_key
FROM accounts
WHERE account RLIKE '^[0-9]'
```

Then `JOIN ... ON l.block_key = r.block_key` produces candidate pairs without the quadratic blow-up.

### 2. Scoring — fuzzy similarity inside the block

This is where `spark-second-string` lives. For each candidate pair, compute one or more similarity scores and keep pairs above a threshold:

```sql
SELECT l.id, r.id, ss_jaro_winkler(l.name, r.name) AS name_sim
FROM pairs
WHERE ss_jaro_winkler(l.name, r.name) > 0.85
```

Multiple metrics are cheap — every metric is a single Catalyst expression that fuses into the same whole-stage codegen block as the surrounding filter.

### 3. Clustering — connect pairs into entities

The pairs above your threshold form a graph; connected components groups them into entities. [GraphFrames `connectedComponents`](https://graphframes.io/04-user-guide/05-traversals.html#connected-components) implements WCC with algorithms tuned for the long transitive chains typical of ER (`A↔B`, `B↔C`, but no direct `A↔C` edge — you still want them in one cluster). Same author maintains GraphFrames and this library; the two are designed to compose.

### 4. Within-cluster pruning — do whatever

Once you have clusters, they're small and independent. Apply business rules, manual review, per-cluster ranking, an LLM call — whatever you want. No longer a scale problem.

### Why a dedicated library for step 2?

| Approach                                                            | Pros                                                                                                                                                                 | Cons                                                                                                                                           |
| ------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| Custom Scala UDFs / wrapped Java libs (e.g., SecondString in a UDF) | Easy to write; reuse existing implementations                                                                                                                        | Crosses the Tungsten boundary every row; per-row scratch allocation; no codegen fusion with surrounding filters                                |
| Python NLP libs (rapidfuzz, jellyfish) in vectorized UDFs           | Rich algorithm catalogue; familiar to DS teams                                                                                                                       | Off-heap memory pressure; row → Arrow columnar → row marshalling around every call; harder to size executors                                   |
| Spark built-ins only                                                | Native, fast, no dependencies                                                                                                                                        | Only `levenshtein` (raw `Int` distance, no normalization) and `soundex`; everything else has to be assembled from array primitives — see below |
| `spark-second-string`                                               | Native Catalyst expressions with codegen and ThreadLocal buffer reuse; 13 similarity metrics + phonetic codecs; fuzz-tested against SecondString as reference oracle | Slower than C-backed Python libs in pure micro-benchmarks; smaller algorithm catalogue than full NLP stacks                                    |

#### What "built-ins only" actually looks like

In theory you can express Jaccard with array primitives. In practice, even character-trigram Jaccard — a one-line definition mathematically — becomes:

```sql
SELECT
  size(array_intersect(
    transform(sequence(1, length(a) - 2), i -> substring(a, i, 3)),
    transform(sequence(1, length(b) - 2), i -> substring(b, i, 3))
  ))
  /
  size(array_union(
    transform(sequence(1, length(a) - 2), i -> substring(a, i, 3)),
    transform(sequence(1, length(b) - 2), i -> substring(b, i, 3))
  )) AS jaccard
FROM pairs
```

…which:

- breaks when `length(a) < 3` or `length(b) < 3` (you get an empty `sequence`, then division by zero),
- tokenizes each input twice because Catalyst can't always common-subexpression-eliminate `transform`,
- offers no parameter for n-gram size beyond rewriting the SQL,
- gives no control over case folding, whitespace normalization, or empty-input semantics.

Word-level Jaccard via `split` is slightly less ugly but still naive on whitespace and equally devoid of options.

The equivalent with this library:

```sql
-- SQL: whitespace-tokenized words, defaults
SELECT ss_jaccard(a, b) FROM pairs
```

```scala
// Scala DSL: character trigrams via the parametric overload
StringSimilarityFunctions.jaccard(col("a"), col("b"), 3)
```

For Jaro, Jaro-Winkler, Smith-Waterman, Needleman-Wunsch, Monge-Elkan, and the rest there is no built-in at all — a UDF is the only fallback, which puts you back in row 1 of the table.

## Design principles

The library exposes two intentionally different surfaces, tuned to two different audiences.

### Flow A — SQL / `expr(...)` for SQL and PySpark users

Two-argument SQL functions with sane defaults — `ss_jaccard(a, b)`, `ss_jaro_winkler(a, b)`, `ss_levenshtein(a, b)`, and the rest of the `ss_*` family. No knobs. The defaults are the common-case choices: Jaccard tokenizes on whitespace, Jaro-Winkler uses the canonical prefix scale and cap, Monge-Elkan uses Jaro-Winkler as the inner metric, alignment metrics use standard match / mismatch / gap costs.

Two ways to register:

- `--conf spark.sql.extensions=io.github.semyonsinchenko.sparkss.sql.SparkSecondStringExtension` at cluster or session bootstrap. Works for SQL-only and PySpark setups with no driver-side Scala code.
- `spark.registerStringSimilarityFunctions()` from Scala, after importing the implicit class from `StringSimilaritySparkSessionExtensions`.
- `io.github.semyonsinchenko.sparkss.sql.StringSimilaritySparkSessionExtensions.registerAllFunctionsPy4j()` from Py4j only if you cannot change spark's `conf` by any reason. Keep in mind that this method is unsafe and has side effects. It assumes that the `SparkSession` exists, that all the names are free and it modifies the existing `SparkSession` 's `functionRegistry` under the hood. I added this endpoint only to siplify the life of the PySpark users on some vendor-serverless-bla-bla deployment where the option of config's modification does not exists at all but user wants for any reason to use SQL-expressions instead of the DSL.


```python
spark._jvm.io.github.semyonsinchenko.sparkss.sql.StringSimilaritySparkSessionExtensions.registerAllFunctionsPy4j()
```

Once registered, PySpark users can call any function through `F.expr("ss_jaccard(a, b)")` like a native SQL function.

### Flow B — Scala DSL `StringSimilarityFunctions` for library developers

For JVM consumers embedding this library inside larger ER systems (Zingg-style, Splink-style, or custom in-house pipelines): every metric has parametric overloads — `ngramSize`, `prefixScale` / `prefixCap`, `matchScore` / `mismatchPenalty` / `gapPenalty`, Monge-Elkan `innerMetric`, affine-gap open / extend penalties — reachable from Scala or Java.

```scala
StringSimilarityFunctions.jaccard(col("a"), col("b"), 3)               // character trigrams
StringSimilarityFunctions.smithWaterman(col("a"), col("b"), 2, -1, -1) // tuned alignment
```

### A note on py4j

The Scala DSL was designed with `py4j` ergonomics in mind, even though a published PySpark wrapper is not part of this release. Concretely:

- Every public method parameter is a Java primitive (`int`, `double`), `String`, or `org.apache.spark.sql.Column`.
- No `Option`, `Seq`, `Map`, `Tuple`, type parameters, or implicit parameter lists.
- "Default" forms are explicit zero-arg overloads, not Scala default arguments — py4j picks the right overload by arity without `apply$default$N` synthetic gymnastics.

A thin in-house Python wrapper is roughly ten lines per metric:

```python
from pyspark.sql.column import Column, _to_java_column

ss = spark.sparkContext._jvm.io.github.semyonsinchenko.sparkss.StringSimilarityFunctions
score = Column(ss.jaccard(_to_java_column(left), _to_java_column(right), 3))
```

`py4j` compatibility is treated as a deliberate design constraint but is not part of the formal CI test matrix — treat as best-effort.
