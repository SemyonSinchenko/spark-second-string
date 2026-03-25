## Context

This change adds a missing token-level soft similarity metric, `monge_elkan`, to close parity gaps with legacy Java SecondString behavior while preserving this project's DSL-first integration model.

Current state:
- Existing binary string similarity metrics are exposed through a shared expression contract with interpreted and codegen execution paths.
- Token and matrix metrics already have benchmark and correctness infrastructure, but `monge_elkan` is not yet represented in expression, DSL, SQL registration, test parity, or benchmark suites.

Constraints:
- Output must be deterministic and normalized to `[0.0, 1.0]`.
- Arity and naming must match existing metric conventions (`monge_elkan` with exactly two inputs).
- Existing null-propagation and expression semantics must remain unchanged.
- This iteration excludes weighted/affine variants and runtime tunables.

## Goals / Non-Goals

**Goals:**
- Introduce a canonical `monge_elkan` binary similarity metric with explicit edge-case semantics (both empty, one empty, whitespace-only, repeated tokens, punctuation, asymmetric token counts, and reordering).
- Integrate `monge_elkan` across expression, DSL, and optional SQL registration layers with interpreted/codegen parity.
- Extend tests and benchmarks to validate correctness boundaries and performance against existing token and selected matrix baselines.

**Non-Goals:**
- Add tunable scoring parameters or weighted/affine Monge-Elkan variants.
- Introduce statistical/training-based similarity methods (for example, `tfidf`, `soft_tfidf`) or learner APIs.
- Redesign public API style beyond additive inclusion in current DSL-first surfaces.

## Decisions

1. Implement `monge_elkan` as a first-class member of the existing binary string similarity expression family.
   - Rationale: Reusing the existing expression contract ensures consistent null behavior, analyzer integration, and execution semantics without introducing a special-case operator path.
   - Alternative considered: Add a standalone token-metric API outside the expression family. Rejected because it would fragment usage patterns and increase parity burden between DSL and execution backends.

2. Keep scoring semantics fixed and deterministic in this phase.
   - Rationale: A fixed algorithm simplifies parity testing, reduces user confusion, and aligns with the proposal's anti-scope-creep boundary.
   - Alternative considered: Expose inner-metric/aggregation knobs at runtime. Rejected because it multiplies behavior combinations and test surface before baseline parity is established.

3. Define explicit edge-case contracts and codify them in both unit and parity tests.
   - Rationale: Token-based metrics are sensitive to tokenization and asymmetry details; explicit contracts prevent accidental drift between interpreted and codegen implementations.
   - Alternative considered: Depend only on broad range checks (`0..1`) and benchmark snapshots. Rejected because those checks cannot catch semantic mismatches on boundary inputs.

4. Add DSL method and optional SQL function registration using the canonical `monge_elkan` name.
   - Rationale: Naming/arity parity across API surfaces minimizes adoption friction and avoids hidden aliases.
   - Alternative considered: Alias-only support or DSL-only exposure. Rejected because inconsistent discoverability and surface mismatch would undermine contract clarity.

5. Extend benchmark suites with overlap and length cohorts used by existing metric comparisons.
   - Rationale: Reusing current cohort structure gives apples-to-apples performance context and preserves longitudinal benchmark comparability.
   - Alternative considered: Add only a minimal smoke benchmark. Rejected because it would not provide actionable performance guidance relative to token and matrix baselines.

## Risks / Trade-offs

- [Tokenization mismatch with user expectations] -> Mitigation: Document canonical token handling assumptions in tests/specs and add representative punctuation/whitespace cases.
- [Interpreted vs codegen divergence on asymmetric/reordered inputs] -> Mitigation: Add cross-engine parity tests for boundary and nested-expression scenarios.
- [Performance regression on long token sequences] -> Mitigation: Add benchmark cohorts for long inputs and track against existing token metrics before release.
- [Scope expansion pressure for tunables/variants] -> Mitigation: Explicitly mark variants and knobs as out-of-scope in specs and enforce additive-only implementation criteria.

## Migration Plan

1. Land `monge_elkan` kernel and expression integration behind existing metric registration pathways.
2. Add DSL exposure and optional SQL registration with canonical naming and arity checks.
3. Add correctness tests (edge cases, null handling, nested expressions) and interpreted/codegen parity tests.
4. Add benchmark coverage across overlap/length cohorts and compare with existing token/matrix baselines.
5. Roll forward as additive change only; no data migration required.

Rollback strategy:
- If regressions are detected, remove/disable `monge_elkan` registration while retaining unrelated metrics unchanged.
- Revert additive expression/DSL wiring in a focused rollback commit; existing metric behavior remains backward compatible.

## Open Questions

- Should SQL registration be enabled by default in all current integration contexts, or gated where SQL exposure is optional today?
- Should tokenization behavior be strictly shared with existing token metrics implementation, or documented as independently canonicalized if differences emerge?
- Do current benchmark acceptance thresholds need per-metric customization for `monge_elkan` before release gating?
