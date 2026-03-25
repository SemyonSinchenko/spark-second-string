## Context

This change adds a new global-alignment similarity metric, `needleman_wunsch`, to close a parity gap with legacy Java SecondString while preserving existing behavior contracts in this Spark library.

Current matrix metrics (`levenshtein`, `lcs_similarity`, `jaro`, `jaro_winkler`) already share a consistent contract:
- null propagation through Catalyst expressions,
- deterministic bounded numeric output,
- parity between interpreted and codegen execution,
- DSL and SQL registration alignment.

The new metric must fit the same expression architecture and validation footprint. The proposal also requires stronger edge-case and benchmark coverage, including both-empty, one-empty, repeated characters, punctuation/whitespace, and overlap-shape performance cohorts.

## Goals / Non-Goals

**Goals:**
- Introduce a canonical `needleman_wunsch` binary similarity metric with deterministic semantics and output bounded to `[0.0, 1.0]`.
- Integrate the metric end-to-end: matrix expression, DSL constructors (Column and String overloads), and optional SQL registration naming/arity parity.
- Preserve existing cross-metric contracts: null behavior and interpreted/codegen equivalence.
- Add correctness and benchmark coverage aligned with existing metric quality bars.

**Non-Goals:**
- Configurable substitution/gap scoring parameters.
- Affine-gap or local-alignment variants.
- Locale/case-normalization policy changes.
- Any behavior changes to existing metrics.

## Decisions

### 1) Implement as a new matrix expression with canonical fixed scoring

`needleman_wunsch` is implemented as a dedicated matrix expression class under the existing matrix-metric package, using a fixed scoring policy (match reward and mismatch/gap penalties) defined in code constants.

Rationale:
- Keeps semantics stable and deterministic across runs and Spark execution modes.
- Matches the project pattern where each metric has one canonical implementation.
- Avoids API expansion and compatibility burden from tunable parameters.

Alternatives considered:
- Expose scoring parameters in DSL/SQL now: rejected to keep API surface minimal and consistent with proposal scope.
- Reuse edit-distance normalization directly: rejected because Needleman-Wunsch alignment semantics differ from Levenshtein in recurrence/scoring intent.

### 2) Normalize raw alignment score into bounded similarity

The algorithm computes a raw global-alignment score via dynamic programming, then maps it to `[0.0, 1.0]` with explicit boundary handling (both empty => `1.0`; one empty => deterministic low endpoint under canonical normalization).

Rationale:
- Preserves family-level similarity contract expected by expressions and downstream consumers.
- Provides stable comparisons across input lengths and overlap patterns.

Alternatives considered:
- Return unbounded raw alignment scores: rejected because it breaks cross-metric comparability and existing expression expectations.
- Length-only normalization borrowed from one existing metric: rejected as potentially misaligned with Needleman-Wunsch scoring range.

### 3) Mirror existing integration path for DSL, SQL, and execution parity

Integration follows current metric onboarding structure:
- add function constructors in `StringSimilarityFunctions`,
- add SQL registration in Spark session extensions with strict 2-argument validation,
- add expression-suite tests for null propagation, nested expressions, interpreted/codegen parity, and DSL-vs-SQL equivalence.

Rationale:
- Reduces risk by following proven wiring used by current matrix metrics.
- Ensures users can consume the new metric through all supported entry points.

Alternatives considered:
- DSL-only launch: rejected because roster parity and discoverability requirements include SQL naming/arity alignment.
- Codegen-only validation: rejected because interpreted/codegen equivalence is an explicit contract.

### 4) Add dedicated benchmarks using overlap and length cohorts

Add a `NeedlemanWunschBenchmark` using short/medium/long strings and low/medium/high overlap cohorts, and include baseline comparisons against existing matrix metrics.

Rationale:
- Provides performance visibility for the new dynamic-programming path.
- Supports regression detection and practical guidance for metric selection.

Alternatives considered:
- Only microbench on random strings: rejected because it misses overlap-structure effects that strongly influence alignment cost.

## Risks / Trade-offs

- [O(n*m) time and memory growth on long pairs] -> Mitigation: document expected complexity, keep benchmarks across length cohorts, and avoid hidden parameterization that could mask costs.
- [Normalization formula drift from expected semantics] -> Mitigation: codify boundary/canonical examples in matrix unit tests and expression-level contract tests.
- [Codegen vs interpreted divergence due implementation details] -> Mitigation: retain explicit parity tests in expression suite and run both modes in CI.
- [SQL/DSL naming mismatch or arity regression] -> Mitigation: add registration and constructor consistency tests matching current metric patterns.

## Migration Plan

1. Add matrix expression and similarity computation with canonical fixed scoring constants.
2. Wire public DSL functions and SQL registration (`needleman_wunsch`) with arity validation.
3. Add matrix correctness tests for canonical and edge-case scenarios.
4. Add expression integration tests for null handling, nested expressions, parity, and SQL/DSL equivalence.
5. Add benchmark suite and baseline comparisons against existing matrix metrics.
6. Release as additive change (no API removals). Rollback strategy is reverting the additive metric files and registration if regressions are detected.

## Open Questions

- Should SQL registration remain always-on or gated by the same extension toggle strategy used in deployment environments?
- Do we want to publish canonical example scores in user-facing docs now, or defer to a follow-up documentation-focused change?
- Is the current benchmark harness sufficient for memory profiling, or do we need additional instrumentation for long-string alignment workloads?
