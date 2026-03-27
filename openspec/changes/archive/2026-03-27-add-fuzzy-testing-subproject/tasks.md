## 1. Build and project wiring

- [x] 1.1 Register the `fuzzy-testing` sbt subproject and ensure `sbt projects` lists root, `benchmarks`, and `fuzzy-testing`.
- [x] 1.2 Configure Spark dependency scope for `fuzzy-testing` so the CLI can run from that subproject.
- [x] 1.3 Verify existing root and `benchmarks` dependency scopes and behavior remain unchanged.

## 2. CLI and deterministic input generation

- [x] 2.1 Add a fuzzy-testing CLI entry point that accepts `--seed`, `--rows`, and required `--out`, with defaults `42` and `10000` for `--seed`/`--rows`.
- [x] 2.2 Implement argument validation/parsing so explicit `--seed` and `--rows` values are honored exactly and `--out` must be provided.
- [x] 2.3 Implement deterministic seeded string-pair generation that reproduces identical data for identical `(seed, rows)`.
- [x] 2.4 Construct the generated dataset as a Spark DataFrame schema used by downstream metric evaluation.

## 3. Metric evaluation pipeline

- [x] 3.1 Implement native metric computation fully as Spark DataFrame transformations.
- [x] 3.2 Implement legacy SecondString baseline scoring via Spark UDF wrappers over DataFrame columns.
- [x] 3.3 Assemble a unified per-row comparison DataFrame with native score, baseline score, and relative-delta inputs.
- [x] 3.4 Compute relative delta using symmetric denominator `abs(native-baseline)/max((abs(native)+abs(baseline))/2, 1e-9)`.

## 4. Correlation and delta analytics

- [x] 4.1 Add Pearson and Spearman correlation computation using Spark ML correlation APIs.
- [x] 4.2 Implement absolute relative-delta band aggregation for `+-5%`, `+-10%`, `+-30%`, and overflow `>30%` as counts.
- [x] 4.3 Compute and format percentages normalized to total row count for each required delta band.

## 5. Deterministic markdown reporting

- [x] 5.1 Implement a deterministic markdown renderer with fixed section ordering and stable table headers.
- [x] 5.2 Include correlation results, required delta-band count/percentage columns, and a cross-metric aggregate summary table in report output.
- [x] 5.4 Write markdown report to `--out` while limiting stdout to top-level execution logs.
- [x] 5.3 Ensure the CLI emits reports without any pass/fail gating or threshold-based termination behavior.
- [x] 5.5 Add optional `--save-output <dir>` to write per-metric Spark CSV outputs with stable column schema.

## 6. Validation and regression safety

- [x] 6.1 Add tests for CLI defaults and explicit argument handling (`--seed`, `--rows`, required `--out`).
- [x] 6.2 Add determinism tests confirming identical outputs for repeated runs with the same `(seed, rows)` and different outputs for changed seed.
- [x] 6.3 Add tests verifying DataFrame-only execution path and UDF-based baseline scoring usage.
- [x] 6.4 Add tests for markdown report structure stability and required correlation/delta sections.
- [x] 6.5 Run verification commands to confirm existing root and benchmark workflows still execute unchanged.
- [x] 6.6 Add tests validating `--save-output` writes one csv part per metric and required output columns.
