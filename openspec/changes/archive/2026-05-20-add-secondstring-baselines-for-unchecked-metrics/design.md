## Context

The current fuzzy test harness compares only four metrics against SecondString, even though more metrics are implemented and exercised internally. This creates uneven confidence: regressions in unchecked metrics can pass CI without detection. The change is test-harness focused, with no intended public API changes, and should remain incremental so existing validated metrics continue to behave exactly as before.

## Goals / Non-Goals

**Goals:**
- Extend baseline parity checks to additional metrics that have direct or well-defined equivalence with SecondString.
- Centralize metric mapping/adapter logic so adding future baseline checks is low-risk and mechanical.
- Preserve deterministic, debuggable failure output per metric and per test case.
- Keep existing four baseline-checked metrics stable while expanding coverage.

**Non-Goals:**
- Re-implementing metric algorithms themselves as part of this change.
- Introducing new production-facing similarity APIs or changing score semantics intentionally.
- Forcing parity for metrics with no reasonable SecondString equivalent.

## Decisions

1. Define an explicit baseline-eligible metric registry.
   - Rationale: A single registry of metric identifiers, input shaping rules, and expected score normalization makes coverage auditable and avoids ad hoc per-test branching.
   - Alternatives considered:
     - Infer coverage from all implemented metrics dynamically: rejected because unsupported metrics become ambiguous and failures are harder to interpret.
     - Keep one-off tests per metric: rejected due to duplication and drift risk.

2. Add normalization adapters at comparison boundaries.
   - Rationale: Some metrics differ in naming, tokenization behavior, or score scaling conventions between implementations. Adapters keep core implementation untouched while making comparisons apples-to-apples.
   - Alternatives considered:
     - Alter internal metric outputs to match SecondString globally: rejected because it risks changing production semantics.
     - Loosen assertions with wide tolerances: rejected because it hides real divergences.

3. Use table-driven baseline tests with per-metric fixtures.
   - Rationale: A structured matrix (metric x cases) makes added metrics mostly data-entry work, consistent with the proposal's "mechanical" expansion intent.
   - Alternatives considered:
     - One test file per metric: rejected because maintenance overhead grows linearly and reporting becomes fragmented.

4. Retain strict checks with metric-specific tolerances only where mathematically justified.
   - Rationale: Floating-point heavy metrics may require tiny epsilon handling, but defaults should remain strict to detect subtle regressions.
   - Alternatives considered:
     - Uniform global epsilon: rejected because it is either too strict for some metrics or too loose for others.

## Risks / Trade-offs

- [False mismatches from semantic differences] -> Mitigation: document per-metric adapters and unsupported edge cases in test metadata.
- [Increased CI runtime from expanded matrix] -> Mitigation: reuse shared fixture setup and keep case sets targeted but representative.
- [Fragile mapping if SecondString behavior changes] -> Mitigation: isolate baseline invocation behind a thin wrapper and keep metric registry declarative.
- [Confusion about uncovered metrics] -> Mitigation: explicitly mark non-baselined metrics with rationale in the registry.

## Migration Plan

1. Introduce metric registry and adapter layer in the harness while keeping existing four checks green.
2. Add newly targeted metrics (token metrics, `jaro`, `levenshtein`, `lcs`, `affine_gap`) to the registry with fixtures.
3. Run full harness in CI and verify deterministic failures/messages.
4. If issues arise, rollback by disabling only newly added registry entries while preserving prior baseline checks.

## Open Questions

- Are any token metric variants implemented internally but mapped to differently named SecondString functions that need explicit aliasing rules?
- For `affine_gap`, do both implementations expose identical gap-penalty defaults, or should the harness pin explicit parameters?
