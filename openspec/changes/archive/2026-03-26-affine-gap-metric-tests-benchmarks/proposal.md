## Why

The metric set still lacks a canonical affine-gap alignment similarity even though affine penalties are a common baseline for sequence matching. Adding `affine_gap` closes this parity gap and creates a clear, testable contract for gap-open vs gap-extend behavior without changing existing metric semantics.

## What Changes

- Add one new binary string similarity metric named `affine_gap` with deterministic, normalized output in `[0.0, 1.0]`.
- Extend existing DSL and optional SQL registration surfaces so `affine_gap` has the same naming and arity contract as other binary metrics.
- Expand validation scope to include correctness, interpreted-vs-codegen parity, null propagation, and boundary behavior for `affine_gap`.
- Add dedicated benchmark coverage for `affine_gap` plus side-by-side baselines against existing matrix metrics across short/medium/long and low/medium/high overlap cohorts.
- Freeze scope for this change by explicitly defining non-goals and edge-case boundaries.

## Capabilities

### New Capabilities

- `affine-gap-similarity`: Provide `affine_gap` as a canonical binary string similarity metric with fixed semantics (no runtime tuning in this phase), deterministic output bounded to `[0.0, 1.0]`, explicit edge-case outcomes for both-empty (`1.0`), one-empty (`0.0`), identical strings (`1.0`), whitespace-only inputs, punctuation-bearing inputs, repeated characters, asymmetric lengths, and null propagation through existing expression contracts.

### Modified Capabilities

- `string-sim-expression`: Extend the unified binary expression roster to include `affine_gap` with required interpreted/codegen equivalence under direct calls, nested expressions, and mixed null/non-null inputs.
- `string-sim-dsl`: Add Scala/Java DSL constructors and optional SQL function registration for `affine_gap` with exact two-argument parity and naming consistency with existing metrics.
- `matrix-metric-kernel`: Expand matrix-family validation obligations to include affine-gap-specific boundary and normalization expectations, ensuring no out-of-range scores and no contract regressions in existing matrix metrics.

## Impact

- Improves functional parity by adding a missing affine-gap alignment metric to the matrix family.
- Increases correctness and regression confidence through broader tests and interpreted-vs-codegen parity checks.
- Increases performance visibility via dedicated `affine_gap` benchmarks and matrix-metric baselines.
- Preserves backward compatibility because this is additive and does not alter existing metric names, signatures, or scoring behavior.
- Out of scope: configurable scoring knobs, alternate affine variants, local-alignment substitutions, token/statistical metrics (`tfidf`, `soft_tfidf`), locale-aware normalization policy changes, learner/training APIs, and non-DSL-first API redesign.
