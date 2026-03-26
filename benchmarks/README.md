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

Use this command to run only the Affine-Gap benchmark with matrix-metric baselines:

```bash
sbt "benchmarks/jmh:run -rf json -rff target/reports/affine-gap.json AffineGapBenchmark"
```

Fixed affine-gap scoring profile in this phase:

- mismatch penalty: `1`
- gap open penalty: `2`
- gap extend penalty: `1`

Regression bound policy for `affine_gap` benchmark comparisons:

- Compare `affineGap` throughput against `needlemanWunschBaseline`, `smithWatermanBaseline`, `levenshteinBaseline`, `lcsSimilarityBaseline`, and `jaroBaseline` for each short/medium/long and low/medium/high overlap cohort.
- Capture per-cohort baseline deltas from the JSON report in change-review notes before merge.
- Flag follow-up work if `affineGap` is slower than both `needlemanWunschBaseline` and `smithWatermanBaseline` by more than 35% in more than two cohorts.

Use this command to run only the Monge-Elkan benchmark with token and matrix baselines:

```bash
sbt "benchmarks/jmh:run -rf json -rff target/reports/monge-elkan.json MongeElkanBenchmark"
```

Regression bound policy for `monge_elkan` benchmark comparisons:

- Compare `mongeElkan` throughput against `jaccardBaseline`, `sorensenDiceBaseline`, `cosineBaseline`, `braunBlanquetBaseline`, `jaroBaseline`, and `levenshteinBaseline` for each short/medium/long and low/medium/high overlap cohort.
- Flag follow-up work if `mongeElkan` is slower than all token baselines by more than 50% in any single cohort.
- Keep benchmark inputs and scoring semantics fixed for release review; do not add weighted or affine variants to this benchmark in this phase.
