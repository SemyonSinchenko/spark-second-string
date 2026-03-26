## 1. Affine-gap metric kernel

- [x] 1.1 Implement `affine_gap` scoring in the matrix metric kernel with fixed affine-gap constants and deterministic behavior
- [x] 1.2 Reuse shared normalization to guarantee bounded similarity output in `[0.0, 1.0]`
- [x] 1.3 Add/extend kernel-level tests for affine-gap boundaries: both-empty (`1.0`), one-empty (`0.0`), identical non-empty (`1.0`), and normalization guards

## 2. Expression, codegen, and registry integration

- [x] 2.1 Register `affine_gap` in the unified binary string-similarity expression family with exact two-argument arity
- [x] 2.2 Implement complete codegen support for `affine_gap` (no placeholders/null code objects) and ensure interpreted/codegen path parity
- [x] 2.3 Add parity tests for direct and nested expression evaluation, including mixed null/non-null inputs and null propagation equivalence

## 3. DSL and optional SQL exposure

- [x] 3.1 Add Scala/Java DSL constructor/helper for `affine_gap(left, right)` following existing naming and signature conventions
- [x] 3.2 Extend optional SparkSession SQL registration to include `affine_gap` via the thin registration layer
- [x] 3.3 Add DSL/SQL coverage tests to verify canonical metric name, two-argument arity, and delegation to existing expression implementations

## 4. Correctness and regression coverage

- [x] 4.1 Add affine-gap correctness fixtures for representative content classes (whitespace-only, punctuation, repeated chars, asymmetric lengths)
- [x] 4.2 Expand matrix-family regression tests to ensure existing metrics have no boundary or normalization regressions after adding `affine_gap`
- [x] 4.3 Verify all affine-gap outputs remain deterministic and normalized across repeated evaluations

## 5. Benchmarking and rollout checks

- [x] 5.1 Add benchmark cohorts for short/medium/long strings with low/medium/high overlap targeting `affine_gap`
- [x] 5.2 Compare benchmark results against existing matrix metrics and capture baseline deltas for performance review
- [x] 5.3 Update user/developer docs to describe fixed affine-gap semantics and run full test + benchmark suites before merge
