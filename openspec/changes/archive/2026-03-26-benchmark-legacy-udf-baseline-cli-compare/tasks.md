## 1. Benchmark flow foundations

- [x] 1.1 Extract and centralize shared JMH settings so native and legacy suites use identical warmup, measurement, fork, and timing mode values
- [x] 1.2 Define stable algorithm and scenario identifiers used by both flows to support deterministic row matching
- [x] 1.3 Add validation that flags unsupported legacy mappings explicitly instead of substituting alternate algorithms

## 2. Legacy UDF benchmark flow

- [x] 2.1 Implement legacy benchmark entrypoints that execute supported algorithms through Spark UDF wrappers over Java SecondString
- [x] 2.2 Emit legacy-flow JMH JSON output to a deterministic path that is distinct from native output
- [x] 2.3 Move legacy benchmark input preparation into setup, cache and materialize DataFrames before timed methods, and remove timed-method input I/O

## 3. JSON comparison CLI

- [x] 3.1 Create a local Scala compare CLI command that accepts native and legacy JMH JSON file paths as inputs
- [x] 3.2 Implement hardcoded benchmark-class mapping to shared algorithm identifiers and pair rows only through this mapping
- [x] 3.3 Render deterministic table output with columns `algorithm | spark-native | UDF | diff` including central value, error term, and percent delta
- [x] 3.4 Report unmapped classes and unsupported algorithms clearly without heuristic matching

## 4. Suite orchestration

- [x] 4.1 Add `/dev/benchmarks_suite.sh` to run phases in strict order: native benchmarks, legacy-UDF benchmarks, then compare CLI
- [x] 4.2 Add suite options for verbosity and output location while preserving deterministic artifact formats and filenames
- [x] 4.3 Ensure orchestration exits with clear status when any phase fails and prints generated artifact locations on success

## 5. Verification and usage

- [x] 5.1 Execute the suite with default settings and confirm native JSON, legacy JSON, and comparison output are generated at expected stable paths
- [x] 5.2 Verify both flows use identical runtime settings and that compare output aligns supported algorithms without heuristic pairing
- [x] 5.3 Document local run and troubleshooting steps for baseline comparison workflow, including handling of unsupported legacy mappings
