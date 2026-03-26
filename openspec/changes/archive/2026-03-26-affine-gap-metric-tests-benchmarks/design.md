## Context

The change adds a new binary string similarity metric, `affine_gap`, to close a functional gap in the matrix-alignment family while preserving all existing metric semantics and APIs. The implementation must remain deterministic, return normalized scores in `[0.0, 1.0]`, and propagate nulls through existing expression contracts.

Current proposal constraints:
- No runtime scoring knobs in this phase (fixed scoring profile only).
- Additive change only; no behavior or signature regressions for existing metrics.
- End-to-end parity is required across interpreted execution, codegen execution, DSL constructors, and optional SQL registration.
- Validation must cover boundary/edge behavior and benchmark cohorts (short/medium/long, low/medium/high overlap) against existing matrix metrics.

Stakeholders are users relying on stable metric contracts, maintainers of expression/DSL surfaces, and performance owners who need benchmark visibility before and after the metric is introduced.

## Goals / Non-Goals

**Goals:**
- Introduce `affine_gap(left, right)` as a canonical binary metric with fixed semantics and deterministic normalized output.
- Preserve consistency with existing binary metric surfaces (expression, Scala/Java DSL, and optional SQL function naming/arity).
- Guarantee interpreted-vs-codegen equivalence for direct and nested expressions under mixed null/non-null input shapes.
- Expand correctness testing to include affine-gap-specific edge cases and regression protection for matrix-family contracts.
- Add benchmark coverage that isolates `affine_gap` behavior and compares it with existing matrix metrics across representative workload cohorts.

**Non-Goals:**
- User-configurable gap-open/gap-extend parameters or alternate affine variants.
- Local-alignment substitutions, token/statistical similarity additions, or locale normalization policy changes.
- Any redesign of broader APIs or behavior changes to existing metric names/signatures.

## Decisions

1. Add `affine_gap` as a first-class matrix-family metric with fixed internal scoring constants.
   - Rationale: Keeps the initial contract simple and deterministic, minimizing API surface growth while enabling future tunable variants in separate changes.
   - Alternatives considered:
     - Expose scoring knobs immediately: rejected due to higher API and compatibility complexity in first rollout.
     - Implement as a private helper only: rejected because the objective is public parity and benchmark visibility.

2. Reuse existing matrix metric normalization contract and null semantics.
   - Rationale: Users already depend on bounded `[0.0, 1.0]` outputs and null propagation behavior; reusing the same contract reduces surprise and regression risk.
   - Alternatives considered:
     - Raw alignment score output: rejected because scores become length-dependent and non-comparable across pairs.
     - Special null handling for this metric: rejected to avoid one-off semantics in a shared expression family.

3. Integrate through the standard binary expression registry and DSL/SQL naming pipeline.
   - Rationale: Ensures consistent two-argument signatures and discoverability where users already consume other metrics.
   - Alternatives considered:
     - Separate API entry point for affine metrics: rejected because it fragments call patterns and increases maintenance overhead.

4. Validate parity and correctness with focused tests plus matrix-family regression coverage.
   - Rationale: Affine-gap behavior has edge-sensitive outcomes (empty strings, repeated chars, asymmetric lengths), so explicit test vectors are needed in addition to shared contract tests.
   - Alternatives considered:
     - Rely only on golden examples: rejected because it under-covers null/codegen parity and broad contract invariants.

5. Add dedicated benchmark scenarios and side-by-side baselines with existing matrix metrics.
   - Rationale: Affine penalties can shift runtime profile versus other edit-distance-style metrics; cohort-based benchmarks make trade-offs observable and trackable over time.
   - Alternatives considered:
     - Reuse a single aggregate benchmark: rejected because it hides cohort-specific performance characteristics.

## Risks / Trade-offs

- [Normalization drift at edge cases] -> Mitigation: enforce explicit assertions for both-empty (`1.0`), one-empty (`0.0`), identical strings (`1.0`), and out-of-range guard checks.
- [Interpreted/codegen divergence] -> Mitigation: parity tests for direct, nested, and mixed null expression graphs with shared expected fixtures.
- [Performance regression in long/asymmetric inputs] -> Mitigation: benchmark cohorts for long/high-overlap and long/low-overlap inputs; compare against existing matrix baselines in CI/perf runs.
- [Contract confusion from fixed scoring constants] -> Mitigation: document fixed semantics clearly and defer configurability to a follow-up proposal.
- [Maintenance complexity from adding another matrix metric path] -> Mitigation: route through shared registry and kernel abstractions instead of introducing bespoke execution branches.

## Migration Plan

1. Implement `affine_gap` in the matrix metric kernel with normalized output guarantees.
2. Register metric in expression and codegen paths using existing binary metric plumbing.
3. Add Scala/Java DSL constructor and optional SQL function registration with exact two-arg parity.
4. Land tests covering edge behavior, null propagation, and interpreted-vs-codegen parity.
5. Land benchmarks for short/medium/long and overlap cohorts with matrix-metric baselines.
6. Roll out as additive feature (no migration steps required for existing users).

Rollback strategy:
- If issues are found pre-release, remove/disable registration of `affine_gap` while preserving underlying scaffolding.
- If issues are found post-release, gate or temporarily unregister the metric in SQL/DSL exposure paths while a fix is prepared, without altering existing metric contracts.

## Open Questions

- Should fixed affine scoring constants be explicitly documented in user-facing docs now, or kept implementation-defined until configurability is introduced?
- Do benchmark acceptance thresholds need to be hard-gated in CI for this change, or reported initially without failing builds?
- Is optional SQL registration enabled by default in all distributions, or should this follow existing feature-flag conventions per deployment profile?
