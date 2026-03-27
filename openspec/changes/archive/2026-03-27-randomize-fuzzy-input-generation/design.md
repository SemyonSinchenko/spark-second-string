## Context

The current fuzzy-test pair generator constructs each pair with a fixed shared 4-character prefix derived from the same hash source. This creates a consistent structural correlation across nearly all generated pairs, which narrows diversity and can mask how similarity metrics behave across broader input relationships.

This change keeps deterministic reproducibility and Spark DataFrame output semantics, but replaces the pair-construction logic with a seed-driven randomized case generator. The proposal defines required cohorts: exact match, high/medium/low overlap, disjoint, asymmetric length, whitespace/punctuation heavy, repeated-character, and empty-string cases.

Constraints:
- The same `(seed, rows)` must always produce identical output.
- Row count and downstream Spark-based processing contracts must remain stable.
- The generator should remove any globally forced shared-prefix rule.

## Goals / Non-Goals

**Goals:**
- Produce deterministic random input cohorts with explicitly diverse pair relationships.
- Eliminate global shared-prefix bias while preserving seed reproducibility.
- Keep the generator interface compatible with existing fuzzy-testing pipelines.
- Make cohort composition intentional and inspectable, so parity outputs are less shaped by generator artifacts.

**Non-Goals:**
- Changing metric algorithms or scoring logic.
- Changing report schema, pass/fail policy, or scaling behavior.
- Introducing runtime nondeterminism.
- Backfilling historical reports to match old and new distributions.

## Decisions

1. Implement a case-type-driven generation pipeline

Decision: Generate each row by first selecting a case type from a deterministic weighted distribution, then producing `(lhs, rhs)` with case-specific construction rules.

Rationale:
- Ensures every required relationship class is represented by design.
- Avoids accidental concentration in a narrow structure.
- Keeps generation logic modular and testable per case type.

Alternatives considered:
- Pure unconstrained random pair generation. Rejected because it cannot guarantee coverage for edge cohorts like empty-string or repeated-character cases.
- Keep prefix-based construction and add random suffixes. Rejected because global prefix coupling remains and still biases similarity.

2. Use deterministic pseudo-random draws keyed by global seed and row index

Decision: Derive per-row random state from `(seed, row_index[, stream_id])` so each row is reproducible independent of partitioning/order differences.

Rationale:
- Preserves reproducibility for regression and parity checks.
- Reduces sensitivity to distributed execution details.
- Supports stable regeneration for debugging specific rows.

Alternatives considered:
- Single mutable PRNG advanced sequentially. Rejected because it is fragile under parallelized generation and can drift when row scheduling changes.

3. Define overlap/relationship semantics explicitly per case type

Decision: Encode explicit construction constraints for each case category (for example, exact-match is identical strings; high-overlap shares most characters/tokens with controlled edits; disjoint intentionally avoids common character/token sets).

Rationale:
- Makes expected relationship semantics clear and testable.
- Prevents implicit behavior from leaking across cases.

Alternatives considered:
- Infer overlap classes from post-hoc similarity thresholds. Rejected because it introduces circular dependence on metrics and can make cohorts unstable.

4. Keep DataFrame schema and row-count contract unchanged

Decision: Preserve existing output columns and row cardinality while changing only how values are generated.

Rationale:
- Minimizes downstream migration effort.
- Enables drop-in replacement for existing test/report jobs.

Alternatives considered:
- Add explicit case metadata column immediately. Deferred to a later enhancement to avoid contract changes in this iteration.

## Risks / Trade-offs

- [Distribution drift from intended cohort weights] -> Add deterministic weight validation checks and snapshot tests of case counts for fixed seeds.
- [Hidden overlap in supposedly disjoint cases due to small alphabet] -> Use larger configurable alphabet pools and add invariants that assert required disjointness properties.
- [Edge-case explosion increases maintenance burden] -> Centralize case builders behind shared helpers and keep case-specific tests focused on invariants.
- [Historical report trend discontinuity] -> Document cohort model change and treat old/new runs as different baselines.

## Migration Plan

1. Implement new generator internals behind the existing public generator contract.
2. Add deterministic tests that verify:
   - same `(seed, rows)` yields identical output,
   - row count and schema stability,
   - cohort-specific invariants.
3. Replace old prefix-based path in the fuzzy-testing subproject and run parity/report smoke checks.
4. Roll out as the default path once validation passes.

Rollback strategy:
- Keep the prior generator implementation available behind an internal feature flag or temporary fallback path until confidence is established.
- If regression appears, revert to old path while retaining new tests for iterative fixes.

## Open Questions

- Should case-type weights be fixed constants or configurable per run while remaining deterministic?
- Do we need to persist case labels in output for easier debugging, or is internal-only labeling sufficient for this phase?
- What minimum per-case sample guarantees are required for small row counts?
