# Benchmark Execution

Use this command to run only the Needleman-Wunsch benchmark in a stable machine-readable format:

```bash
sbt "benchmarks/jmh:run -rf json -rff target/reports/needleman-wunsch.json NeedlemanWunschBenchmark"
```

The JSON output file is suitable for regression tracking across runs.

Use this command to run only the Smith-Waterman benchmark with matrix-metric baselines:

```bash
sbt "benchmarks/jmh:run -rf json -rff target/reports/smith-waterman.json SmithWatermanBenchmark"
```

Regression bound policy for `smith_waterman` benchmark comparisons:

- Compare `smithWaterman` throughput against `needlemanWunschBaseline`, `levenshteinBaseline`, `lcsSimilarityBaseline`, and `jaroBaseline` for each overlap/length scenario.
- Flag follow-up work if `smithWaterman` is slower than `needlemanWunschBaseline` by more than 30% in more than two scenarios.
- Flag follow-up work if `smithWaterman` is slower than all matrix baselines by more than 50% in any single scenario.
