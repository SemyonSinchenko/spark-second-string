## Context

The project currently exposes multiple string-similarity metrics through a shared expression path (interpreted evaluation and generated execution) and a DSL surface that can optionally register SQL functions. There is no character-alignment metric optimized for short strings with transpositions.

The proposal adds `jaro` as a new metric with strict boundary behavior (`both-empty => 1.0`, `one-empty => 0.0`, no matches => `0.0`, clamped output in `[0.0, 1.0]`) and requires deterministic parity between interpreted and generated execution.

Constraints:
- Preserve existing argument validation and null-propagation behavior in expression/DSL layers.
- Keep implementation scope to standard Jaro (no Jaro-Winkler or weighted variants).
- Add representative tests and benchmark cases without regressing current metric performance.

## Goals / Non-Goals

**Goals:**
- Implement standard Jaro similarity as a reusable core metric function with deterministic numeric behavior.
- Integrate `jaro` into metric selection for both interpreted and generated execution paths.
- Expose `jaro` through the existing DSL helper/registration pattern with the standard two-argument contract.
- Add correctness tests and parity tests that lock in edge-case behavior and transposition semantics.
- Extend benchmark coverage across short/medium/long inputs and high/low overlap scenarios.

**Non-Goals:**
- Implement Jaro-Winkler, prefix weighting, locale-aware normalization, or case-folding policy changes.
- Change argument-count semantics, null propagation, or existing metric behavior.
- Redesign SQL ergonomics beyond adding the optional `jaro` registration following current conventions.

## Decisions

1. Implement Jaro in a dedicated core metric function, then route both interpreted and generated execution to that same logic.
   - Rationale: one source of truth minimizes drift and enforces parity.
   - Alternative considered: separate implementations per execution mode. Rejected due to higher parity risk and duplicated bug surface.

2. Use the standard Jaro matching window `max(0, floor(max(len(left), len(right)) / 2) - 1)` and transposition count per canonical definition.
   - Rationale: aligns with common references and the proposal's required semantics.
   - Alternative considered: custom window heuristics for performance. Rejected because semantic differences would reduce predictability and compatibility.

3. Apply explicit boundary guards before general matching logic (`both-empty`, `one-empty`, and `no matches`) and clamp final score to `[0.0, 1.0]`.
   - Rationale: protects determinism and avoids floating-point edge drift.
   - Alternative considered: rely on formula output without clamping/guards. Rejected because edge handling becomes implicit and harder to verify.

4. Extend DSL and optional SQL registration through existing metric dispatch/registry extension points, without special-case APIs.
   - Rationale: preserves consistency with current user-facing patterns and validation behavior.
   - Alternative considered: add a bespoke Jaro API entry point. Rejected as unnecessary surface-area growth.

5. Add three test layers: core metric correctness tests, interpreted-vs-generated parity tests, and DSL integration checks.
   - Rationale: each layer catches a distinct failure mode (math correctness, engine parity, API wiring).
   - Alternative considered: only integration tests. Rejected because low-level semantic regressions become harder to diagnose.

6. Add targeted benchmarks grouped by overlap and length buckets, reusing existing benchmark harness conventions.
   - Rationale: validates performance profile without introducing benchmark framework churn.
   - Alternative considered: ad-hoc benchmark scripts. Rejected due to reduced comparability with existing metrics.

## Risks / Trade-offs

- [Floating-point precision differences between paths] -> Use shared computation path and assertion tolerances defined once in parity tests.
- [Repeated-character matching bugs] -> Add dedicated fixtures with repeated characters and asymmetric lengths.
- [Performance overhead on long strings] -> Measure with long-input benchmark bucket and compare against existing metric baselines.
- [Metric dispatch regression in DSL/SQL registration] -> Add integration tests for helper construction, argument validation, and registration naming.

## Migration Plan

1. Add core `jaro` metric implementation and unit tests.
2. Wire metric into expression metric selector used by interpreted and generated execution.
3. Extend DSL helper and optional SQL registration surfaces.
4. Add parity tests (interpreted vs generated) and integration tests.
5. Add benchmark scenarios and capture baseline deltas.
6. Rollback strategy: remove `jaro` from dispatch/DSL registration while retaining isolated metric implementation behind non-exported code if needed.

## Open Questions

- Should parity tests assert exact floating equality or tolerance-based equality for generated execution output?
- Are there existing benchmark threshold gates that `jaro` must satisfy before merge, or is relative trend reporting sufficient?
