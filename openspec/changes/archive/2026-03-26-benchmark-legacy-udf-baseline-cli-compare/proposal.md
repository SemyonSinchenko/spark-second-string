## Why

Current benchmark reports only quantify this library's Spark-native codegen path and do not provide a stable baseline against legacy Java SecondString executed through Spark UDF calls. This blocks apples-to-apples runtime visibility for migration decisions and regression reviews. A single, repeatable suite is required to run both flows under identical benchmark settings and produce a lightweight comparison table from JMH JSON outputs.

## What Changes

- Add a separate benchmark flow for legacy Java SecondString wrapped as Spark UDF calls.
- Keep the existing benchmark flow for this library's native Catalyst/codegen expressions.
- Standardize warmup/trial configuration so both flows use the same JMH settings.
- Enforce no benchmark-time input I/O: test DataFrames are created in setup, cached in memory, and materialized before timed measurements.
- Add a lightweight Scala CLI helper subproject (non-published) that reads two JMH JSON files, matches algorithms by hardcoded JMH benchmark class mapping, and emits a comparison table.
- Add an orchestration shell flow (`/dev/benchmarks_suite.sh`) that runs native flow first, legacy UDF flow second, then invokes the CLI helper.

## Capabilities

### New Capabilities

- `legacy-udf-benchmark-flow`: Runs benchmark scenarios for selected algorithms via legacy Java SecondString wrapped in Spark UDF calls, using the same JMH warmup/measurement/fork parameters as the native flow; excludes file/network I/O during timed benchmark methods; uses setup-time DataFrame creation + cache + warmup-time materialization.
- `benchmark-json-compare-cli`: Reads native and legacy JMH JSON results, matches algorithms by hardcoded JMH class names, and outputs a table with fixed columns `algorithm | spark-native | UDF | diff` where value cells include central value and error term and `diff` is percent delta.
- `benchmark-suite-orchestrator`: Provides `/dev/benchmarks_suite.sh` to run benchmark flows in strict order (native -> UDF -> compare CLI) with configurable verbosity and deterministic output file locations.

### Modified Capabilities

- `matrix-metric-kernel`: Benchmark obligations are extended to include dual-flow comparability (native Catalyst/codegen vs legacy UDF baseline) under equal JMH settings and aligned algorithm/scenario naming for report matching.
- `token-metric-kernel`: Benchmark obligations are extended to include dual-flow comparability where legacy equivalents exist, with explicit handling for unsupported legacy mappings to avoid implicit or approximate substitutions.

## Impact

- Adds a migration-quality performance baseline between Spark-native and legacy-UDF execution paths.
- Increases benchmark runtime because each suite executes twice by design.
- Improves reproducibility by fixing warmup/trial parity and banning timed-method I/O.
- Introduces explicit non-goals for this change: no algorithm semantic rewrites, no production SQL/DSL behavior changes, no auto-tuning of warmup/trial per algorithm, no distributed cluster benchmarking scope, no replacement of JMH JSON as the source format, and no publication of the helper subproject.
