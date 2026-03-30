# Benchmarks

Performance comparison of Spark-native Catalyst expressions vs. equivalent UDF wrappers around the Java SecondString library. Higher ops/s is better; the diff column shows the relative throughput change (negative means the native implementation is faster).

## Summary

Algorithms compared: ${benchmarks.algorithm_count}

Best relative delta (closest to parity): ${benchmarks.best_algorithm} (${benchmarks.best_diff_percent})

| Algorithm | spark-native | UDF | diff |
|---|---|---|---|
| affine_gap | ${benchmarks.affine_gap.native} | ${benchmarks.affine_gap.udf} | ${benchmarks.affine_gap.diff} |
| jaro_winkler | ${benchmarks.jaro_winkler.native} | ${benchmarks.jaro_winkler.udf} | ${benchmarks.jaro_winkler.diff} |
| monge_elkan | ${benchmarks.monge_elkan.native} | ${benchmarks.monge_elkan.udf} | ${benchmarks.monge_elkan.diff} |
| needleman_wunsch | ${benchmarks.needleman_wunsch.native} | ${benchmarks.needleman_wunsch.udf} | ${benchmarks.needleman_wunsch.diff} |
| smith_waterman | ${benchmarks.smith_waterman.native} | ${benchmarks.smith_waterman.udf} | ${benchmarks.smith_waterman.diff} |

## How to read the table

- **spark-native**: throughput of the Catalyst code-generated expression (ops/s with standard deviation).
- **UDF**: throughput of a Spark UDF calling the equivalent Java SecondString method.
- **diff**: relative throughput change. A negative value means the native path is faster by that percentage.

## Reproducing

Run the benchmark suite and regenerate the comparison table:

```bash
./dev/benchmarks_suite.sh --mode compare-only
```

Artifact source: ${benchmarks.source_path}
