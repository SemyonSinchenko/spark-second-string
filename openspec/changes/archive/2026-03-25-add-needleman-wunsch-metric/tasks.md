## 1. Needleman-Wunsch Matrix Metric Implementation

- [x] 1.1 Add a new `needleman_wunsch` matrix metric expression class with fixed canonical scoring constants and DP recurrence.
- [x] 1.2 Implement score normalization to `[0.0, 1.0]` with explicit boundaries (`"", ""` => `1.0`; one-empty => `0.0`).
- [x] 1.3 Ensure null propagation and input handling match existing binary string similarity expression contracts.

## 2. DSL and SQL Integration

- [x] 2.1 Add `needleman_wunsch` constructors/helpers in `StringSimilarityFunctions` for both `Column` and string overload patterns used by existing metrics.
- [x] 2.2 Register SQL function name `needleman_wunsch` in SparkSession extension with strict two-argument validation.
- [x] 2.3 Add or update wiring tests to verify DSL/SQL naming and arity alignment for the full metric roster including `needleman_wunsch`.

## 3. Expression Parity and Contract Tests

- [x] 3.1 Add expression-suite tests for `needleman_wunsch` null propagation and nested expression behavior.
- [x] 3.2 Add interpreted-vs-codegen parity tests that assert identical outputs for the same inputs.
- [x] 3.3 Ensure codegen path is fully executable (no placeholder/null code fragments) and covered by tests.

## 4. Metric Correctness Test Coverage

- [x] 4.1 Add matrix correctness tests for both-empty, one-empty, identical, and no-overlap input pairs.
- [x] 4.2 Add correctness tests for repeated characters and asymmetric-length string pairs.
- [x] 4.3 Add correctness tests for whitespace-only and punctuation-bearing strings, validating bounded outputs.

## 5. Benchmark Coverage and Regression Guardrails

- [x] 5.1 Add `NeedlemanWunschBenchmark` with short/medium/long length cohorts.
- [x] 5.2 Add low/medium/high overlap datasets and baseline comparisons against existing matrix metrics.
- [x] 5.3 Validate benchmark execution in the existing harness and capture stable output format for regression tracking.
