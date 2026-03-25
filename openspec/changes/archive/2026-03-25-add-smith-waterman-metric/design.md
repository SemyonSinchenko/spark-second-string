## Context

Spark SecondString currently ships matrix-based global alignment (`needleman_wunsch`) but lacks a local-alignment counterpart. This change adds canonical `smith_waterman` behavior to close parity with the legacy Java library for partial-substring matching use cases. The implementation must preserve existing expression contracts across interpreted and codegen paths, support DSL and optional SQL registration surfaces, and keep outputs deterministic in `[0.0, 1.0]`.

The proposal also constrains scope: no affine-gap variants, no configurable scoring knobs, and no token/statistical or training-based extensions.

## Goals / Non-Goals

**Goals:**
- Add a canonical binary `smith_waterman` similarity metric with deterministic normalized output.
- Preserve boundary and null semantics expected by current string-sim expression contracts.
- Extend user entry points (Scala/Java DSL and optional SQL registration) with naming/arity parity.
- Add correctness, parity, and benchmark coverage tailored to local-alignment behavior.

**Non-Goals:**
- Affine-gap Smith-Waterman or alternative scoring schemes.
- User-configurable scoring parameters in this change.
- New Monge-Elkan, TF-IDF, Soft-TFIDF, or learner/training APIs.
- Broad refactors of unrelated metric infrastructure.

## Decisions

1. Implement a fixed-semantics `smith_waterman` matrix metric in the same metric family as existing matrix similarities.
   - Rationale: keeps API and execution model consistent with `needleman_wunsch`, minimizing integration risk.
   - Alternatives considered: introducing a standalone execution path specific to local alignment. Rejected because it would duplicate plumbing for null handling, expression evaluation, and codegen hooks.

2. Normalize output to `[0.0, 1.0]` with explicit boundary rules (`""`/`""` => `1.0`, one-empty => `0.0`).
   - Rationale: canonicalized boundaries and bounded outputs are required for deterministic semantics and predictable downstream usage.
   - Alternatives considered: returning raw alignment score or metric-specific unbounded scaling. Rejected due to poorer comparability with existing similarity metrics and increased user surprise.

3. Reuse existing binary string-sim expression contracts for null propagation and evaluation parity.
   - Rationale: guarantees consistent behavior between interpreted and generated code and avoids contract drift.
   - Alternatives considered: metric-specific null shortcuts. Rejected because it would violate current expression-family behavior.

4. Add first-class DSL/SQL surface support with the same two-argument arity as existing metrics.
   - Rationale: preserves discoverability and API consistency across registration and expression construction paths.
   - Alternatives considered: exposing only expression-level support and deferring DSL/SQL wiring. Rejected due to parity gap and avoidable adoption friction.

5. Add targeted tests and benchmarks for local-alignment characteristics.
   - Rationale: semantic correctness and performance both matter for matrix metrics; dedicated overlap-length cohorts reduce blind spots.
   - Alternatives considered: relying only on generic matrix metric tests. Rejected because local alignment has distinct edge and overlap behaviors.

## Risks / Trade-offs

- [Normalization mismatch with legacy expectations] -> Mitigate by codifying canonical boundaries and validating against representative legacy-aligned fixtures.
- [Codegen/interpreted divergence] -> Mitigate with parity tests over nested expressions, null inputs, and edge cases.
- [Performance regressions on long strings] -> Mitigate with dedicated short/medium/long benchmark cohorts and baseline comparisons with existing matrix metrics.
- [API drift across DSL/SQL surfaces] -> Mitigate with coverage that asserts name/arity parity and end-to-end registration execution.

## Migration Plan

1. Introduce `smith_waterman` implementation in matrix metric internals and wire it into expression dispatch.
2. Add DSL and SQL registration hooks with two-argument signatures matching existing conventions.
3. Add/expand correctness and parity tests, including boundary and nested-expression scenarios.
4. Add benchmark scenarios for overlap cohorts and publish baseline comparisons.
5. Rollout as additive functionality; no existing metric behavior changes expected.

Rollback strategy: remove `smith_waterman` registration/wiring and metric dispatch entry if regressions are detected; existing metrics remain unaffected due to additive scope.

## Open Questions

- Should benchmark acceptance criteria be defined as absolute runtime ceilings, relative slowdown bounds versus `needleman_wunsch`, or both?
- Do we need fixture-based parity checks against a specific legacy Java SecondString version in CI, or are curated expected outputs sufficient?
