## Why

The library is still missing a key global-alignment metric that exists in legacy Java SecondString. Adding `needleman_wunsch` improves parity while expanding sequence-aware matching options beyond edit distance and LCS.

## What Changes

- Add a new matrix similarity metric named `needleman_wunsch` with a single canonical definition and deterministic output.
- Preserve existing cross-metric contract guarantees: null propagation, interpreted/codegen parity, and bounded similarity output.
- Add explicit correctness coverage for boundary and corner cases: both-empty, one-empty, identical strings, no-overlap strings, repeated characters, asymmetric lengths, whitespace-only strings, and punctuation-bearing strings.
- Add benchmark coverage for `needleman_wunsch` across short/medium/long inputs and low/medium/high overlap patterns, with baseline comparison against existing matrix metrics.
- Out of scope: configurable scoring parameters, affine-gap variants, local alignment variants, locale-aware normalization, case-folding policy changes, and any behavior changes to existing metrics.

## Capabilities

### New Capabilities

- `needleman-wunsch-similarity`: Provide a binary string similarity metric `needleman_wunsch` with canonical deterministic semantics, explicit boundary behavior, score bounded to `[0.0, 1.0]`, required interpreted/codegen equivalence, and mandatory correctness plus benchmark coverage.

### Modified Capabilities

- `string-sim-expression`: Extend matrix-metric roster and family parity requirements to include `needleman_wunsch` while preserving null and deterministic semantics for all metrics.
- `string-sim-dsl`: Expose `needleman_wunsch` through Scala/Java DSL and optional SQL registration with the same naming and arity conventions as existing binary metrics.

## Impact

Adds one new matrix metric and its validation footprint without breaking existing APIs.
Increases test and benchmark scope to protect semantic parity, regression detection, and performance visibility.
