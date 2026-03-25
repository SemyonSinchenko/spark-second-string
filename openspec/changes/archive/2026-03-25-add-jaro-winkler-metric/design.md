## Context

The project already supports `jaro` similarity in the string similarity matrix, expression evaluation path, and DSL surface. The proposal adds canonical `jaro_winkler` to cover short identifiers and name-like strings where common leading prefixes should increase similarity.

This change must preserve deterministic behavior, null propagation, and interpreted/codegen parity guarantees that existing metrics provide. It also needs explicit edge-case behavior (`""`/`""`, one-empty, no-match, identical inputs) and bounded output in `[0.0, 1.0]`.

## Goals / Non-Goals

**Goals:**
- Add a fixed-parameter `jaro_winkler` metric to the existing metric registry and expression evaluation flow.
- Keep semantics canonical and non-configurable (`p = 0.1`, `l = 4`) across all APIs and execution modes.
- Extend validation and benchmarks so correctness boundaries and runtime parity are covered for Jaro-Winkler.

**Non-Goals:**
- Expose tunable prefix length or scaling factor.
- Introduce weighted, locale-aware, or normalization/case-folding variants.
- Change existing metric signatures or behavior outside adding `jaro_winkler` support.

## Decisions

1. Implement Jaro-Winkler as a wrapper over the existing Jaro core.
   - Rationale: Reusing Jaro matching/transposition logic minimizes risk of divergence and keeps complexity contained.
   - Alternative considered: Independent Jaro-Winkler implementation; rejected because duplicated matching logic increases maintenance cost and parity risk.

2. Enforce canonical fixed parameters in code and API surface.
   - Decision: Prefix scaling factor is hard-coded to `0.1`; common-prefix contribution uses `min(prefixLen, 4)`; final score uses `jaro + (l * p * (1 - jaro))` and is clamped to `[0.0, 1.0]`.
   - Rationale: Matches proposal requirements and avoids API expansion.
   - Alternative considered: Optional parameters in DSL/expression; rejected as out of scope and would broaden compatibility/testing burden.

3. Extend metric enumeration/validation centrally before execution.
   - Decision: Register `jaro_winkler` in the same metric resolution layer used by matrix expressions and DSL builders.
   - Rationale: A single validation path guarantees consistent error behavior and avoids ad hoc checks.
   - Alternative considered: Local handling in each entry point; rejected due to inconsistent diagnostics risk.

4. Guarantee interpreted/codegen parity via shared computation path.
   - Decision: Both interpreted and generated execution call the same metric function or equivalent shared helper.
   - Rationale: Reduces semantic drift and makes correctness easier to test once.
   - Alternative considered: Separate codegen math expansion; rejected because subtle numeric differences are harder to reason about.

5. Expand tests and benchmarks with explicit boundary cases.
   - Decision: Add correctness tests for empty/no-match/identical/repeated-character cases and representative short/medium/long string pairs; add benchmark entries for `jaro_winkler` alongside existing metrics.
   - Rationale: Ensures functional correctness and validates expected runtime characteristics without changing benchmark methodology.

## Risks / Trade-offs

- [Numeric boundary drift near 1.0 due to floating-point operations] -> Clamp final score to `[0.0, 1.0]` and include boundary assertions.
- [Codegen and interpreted implementations diverge over time] -> Route both through shared helper and keep parity tests mandatory.
- [Prefix handling mistakes on repeated characters or partial matches] -> Add targeted tests with repeated-prefix and transposition-heavy cases.
- [Performance regression from additional post-processing] -> Benchmark against existing Jaro path and track overhead on short and long strings.

## Migration Plan

1. Add `jaro_winkler` to metric registration/validation.
2. Implement canonical Jaro-Winkler computation using existing Jaro base score.
3. Wire support through expression evaluation and DSL/API builders.
4. Add correctness tests and interpreted/codegen parity tests.
5. Add benchmark coverage and compare to current baseline.
6. Release as additive change with no breaking API updates.

Rollback strategy: remove `jaro_winkler` registration and wiring if regressions are detected; existing metrics remain unaffected because the change is additive.

## Open Questions

- Should SQL registration expose `jaro_winkler` by default in all environments, or remain opt-in where SQL function registration is optional?
- Are there existing benchmark datasets for name-like short strings that should be reused for consistency, or should new fixtures be introduced?
