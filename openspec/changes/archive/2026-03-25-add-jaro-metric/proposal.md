## Why

The current metric set lacks a character-alignment metric that is more typo-tolerant than normalized edit distance and less tokenization-dependent than token-set metrics. Adding Jaro fills this gap for short identifiers, names, and transposition-heavy inputs while keeping implementation scope small.

## What Changes

- Add a new `jaro` similarity capability with deterministic boundaries and normalization guarantees.
- Extend DSL and optional SQL registration to expose `jaro` with the same two-argument contract as existing metrics.
- Add focused correctness tests, integration parity tests (interpreted vs codegen), and benchmark coverage across overlap patterns and input lengths.
- Keep Jaro-Winkler, weighted variants, locale/case normalization policies, and SQL-first ergonomics out of scope.

## Capabilities

### New Capabilities

- `jaro-similarity`: Provide a binary string similarity metric using standard Jaro semantics with explicit constraints: both-empty returns `1.0`, one-empty returns `0.0`, no matching characters returns `0.0`, score is clamped to `[0.0, 1.0]`, matching window is `max(0, floor(max(len(left), len(right)) / 2) - 1)`, transpositions are counted per Jaro definition, and behavior is deterministic and parity-safe between interpreted and generated execution. Validation must cover identical strings, single transposition, partial overlap, disjoint strings, repeated characters, asymmetric lengths, and empty-input boundaries. Benchmark coverage must include short/medium/long strings and high/low overlap scenarios.

### Modified Capabilities

- `string-sim-expression`: Expand supported metrics and parity enforcement to include `jaro`, including null propagation and nested child-expression evaluation invariants.
- `string-sim-dsl`: Add DSL helpers and optional SQL registration for `jaro` with existing argument-count validation behavior and naming consistency.

## Impact

Adds one new user-facing metric and associated validation/benchmark coverage without changing behavior of existing metrics. Increases maintenance surface minimally (one metric path plus tests/benchmarks) and preserves current DSL-first integration direction.
