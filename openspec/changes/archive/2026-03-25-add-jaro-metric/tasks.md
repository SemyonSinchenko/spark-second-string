## 1. Core Jaro Metric Implementation

- [x] 1.1 Add a reusable core `jaro` similarity function implementing standard matching-window and transposition semantics
- [x] 1.2 Implement explicit boundary guards for both-empty, one-empty, and no-match cases
- [x] 1.3 Clamp final `jaro` output to `[0.0, 1.0]` and ensure deterministic numeric behavior

## 2. Expression Engine Integration

- [x] 2.1 Register `jaro` in the string-sim metric selector used by interpreted evaluation
- [x] 2.2 Wire `jaro` into generated execution so codegen uses complete executable logic
- [x] 2.3 Preserve existing binary-expression contract semantics, including null propagation and argument validation

## 3. DSL and SQL Surface Wiring

- [x] 3.1 Expose `jaro` in Scala/Java DSL helpers alongside existing metrics
- [x] 3.2 Add optional SparkSession SQL registration entry for `jaro` using the existing thin registration pattern
- [x] 3.3 Add integration checks confirming DSL construction and SQL registration route to the same expression implementation

## 4. Correctness and Parity Test Coverage

- [x] 4.1 Add core correctness tests for identical, transposition, partial-overlap, disjoint, repeated-character, asymmetric-length, and empty-input scenarios
- [x] 4.2 Add interpreted-vs-codegen parity tests for `jaro` (and updated metric roster including `jaro`) using deterministic assertions
- [x] 4.3 Verify codegen path is fully implemented (no placeholder/null code objects) through execution-focused tests

## 5. Benchmark Coverage and Validation

- [x] 5.1 Extend benchmark suites with `jaro` short/medium/long cases for high-overlap and low-overlap inputs
- [x] 5.2 Capture and review benchmark deltas against existing metric baselines to detect regressions
