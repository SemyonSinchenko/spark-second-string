# Benchmark Execution

Use this command to run only the Needleman-Wunsch benchmark in a stable machine-readable format:

```bash
sbt "benchmarks/jmh:run -rf json -rff benchmarks/target/reports/needleman-wunsch.json NeedlemanWunschBenchmark"
```

The JSON output file is suitable for regression tracking across runs.
