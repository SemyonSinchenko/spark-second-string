## Validation Run Notes

- Command: `sbt "testOnly io.github.semyonsinchenko.sparkss.expressions.StringSimExpressionSuite"`
  - Result: pass (46/46 tests)
- Command: `sbt "benchmarks/jmh:run -rf json -rff target/reports/smith-waterman.json SmithWatermanBenchmark"`
  - Result: completed, JSON report at `benchmarks/target/reports/smith-waterman.json`

## Benchmark Comparison Summary

Comparison policy from `benchmarks/README.md`:

- Flag follow-up if `smithWaterman` throughput is more than 30% slower than `needlemanWunschBaseline` in more than two scenarios.
- Flag follow-up if `smithWaterman` throughput is more than 50% slower than every matrix baseline in any scenario.

Observed from this run:

- `smithWaterman` is more than 30% slower than `needlemanWunschBaseline` in 6/9 scenarios (all medium/long scenarios).
- `smithWaterman` is not more than 50% slower than all baselines in any scenario.

## Follow-up Needed Before Merge

- Investigate medium/long-string performance for `smith_waterman` and reduce regression versus `needleman_wunsch`.
- Re-run the Smith-Waterman benchmark after optimization and update this note with a new comparison snapshot.
