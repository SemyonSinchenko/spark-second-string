## 1. Smith-Waterman metric implementation

- [x] 1.1 Add canonical `smith_waterman` matrix-similarity implementation with fixed scoring semantics and deterministic normalization to `[0.0, 1.0]`
- [x] 1.2 Implement required boundary behavior in metric logic (`""`/`""` => `1.0`, one-empty => `0.0`) while preserving one-non-empty overlap behavior
- [x] 1.3 Wire `smith_waterman` into metric dispatch/enumeration so expression evaluation can resolve the new metric name

## 2. Expression and API surface integration

- [x] 2.1 Extend binary string-sim expression contracts to include `smith_waterman` in interpreted evaluation paths with existing null-propagation semantics
- [x] 2.2 Implement complete codegen support for `smith_waterman` and remove any placeholder/null code-generation branches
- [x] 2.3 Add Scala/Java DSL constructor/helper for `smith_waterman` with two-argument arity and Double result contract
- [x] 2.4 Add optional SQL registration hook for `smith_waterman` in SparkSession extension with name/arity parity to DSL

## 3. Correctness and parity tests

- [x] 3.1 Add/expand unit tests for `smith_waterman` edge cases: both-empty, one-empty, identical, no-overlap, repeated chars, asymmetric lengths, whitespace-only, punctuation
- [x] 3.2 Add null-handling tests asserting NULL propagation when either argument is NULL
- [x] 3.3 Add interpreted-vs-codegen parity tests for `smith_waterman`, including nested expressions and mixed edge-case inputs
- [x] 3.4 Add API-surface tests validating DSL and SQL registration naming/arity parity for all currently supported string-sim metrics including `smith_waterman`

## 4. Benchmark coverage and validation

- [x] 4.1 Extend benchmark suites with `smith_waterman` cohorts across short/medium/long lengths and low/medium/high overlap patterns
- [x] 4.2 Capture benchmark comparisons against existing matrix metrics (including `needleman_wunsch`) and record acceptable regression bounds
- [x] 4.3 Run targeted test and benchmark commands, then document any follow-up fixes needed before merge
