## Context

The change targets a correctness and confidence gap in the current string similarity test strategy. Existing suites primarily exercise ASCII examples and example-based assertions, leaving multi-byte UTF-8 execution paths and broad invariant coverage under-tested.

Two specific risks drive this design:
- `MatrixMetricKernelHelper.ResolvedStrings` has an ASCII fast path and a non-ASCII fallback path, but only the fast path is effectively covered today.
- Penalty parameters are inconsistent across matrix alignment metrics: `NeedlemanWunsch`/`SmithWaterman` use negative penalties while `AffineGap` currently uses positive costs, increasing user error risk in mixed pipelines.

The proposal also calls out a dedicated `SmithWatermanSuite` gap and the need for property-based invariants that scale beyond hand-picked examples.

Constraints:
- Keep scoring contracts bounded in `[0.0, 1.0]` and deterministic.
- Preserve existing codegen/interpreted parity guarantees in expression-level integration tests.
- Minimize production-risk changes; most work should be additive tests, with one controlled API convention decision for `AffineGap`.

## Goals / Non-Goals

**Goals:**
- Add broad Unicode-focused test coverage for matrix, token, and phonetic metrics, including multi-byte fallback behavior in `ResolvedStrings`.
- Introduce a first-class `SmithWatermanSuite` matching depth and style of existing matrix metric suites.
- Add property-based tests for identity, bounds, empty handling, and symmetry (where applicable) across all metrics.
- Harmonize `AffineGap` penalty sign conventions with `NeedlemanWunsch` and `SmithWaterman` to reduce API confusion.
- Extend expression-level integration coverage with non-ASCII rows to validate interpreted/codegen parity for multi-byte data.

**Non-Goals:**
- Implement Unicode normalization (NFC/NFD), locale-specific transformations, or transliteration in production logic.
- Refactor `ResolvedStrings` to full Unicode code point indexing; current char-based behavior remains documented and tested.
- Redesign metric algorithms or introduce new similarity metrics.
- Expand benchmark suites as part of this change.

## Decisions

1. Add dedicated cross-cutting Unicode test suites plus targeted `ResolvedStrings` unit coverage.
- Why: Unicode behavior should be validated near both public metric APIs and internal kernel helpers to catch regressions in path selection, lengths, and character access.
- Alternatives considered:
  - Only add Unicode rows to existing suites. Rejected because coverage remains sparse and easy to miss per metric family.
  - Only add helper-level tests. Rejected because integration behavior (tokenization, normalization, expression execution) would still be under-tested.

2. Introduce `SmithWatermanSuite` as a standalone matrix metric suite.
- Why: SmithWaterman currently lacks direct unit tests, making correctness dependent on indirect integration checks.
- Alternatives considered:
  - Fold cases into existing mixed suites. Rejected due to discoverability and maintenance cost; dedicated suites are the existing project convention.

3. Harmonize `AffineGap` penalties to negative values (pre-1.0 breaking change).
- Decision details:
  - Update type checks to require negative penalties for mismatch/open/extend.
  - Update defaults to `-1`, `-2`, `-1`.
  - Internally convert to cost-space in DP recurrence (`cost = -penalty`) to preserve algorithmic intent.
- Why: Unified sign semantics across alignment metrics reduces configuration mistakes and documentation complexity.
- Alternatives considered:
  - Keep divergent signs and add docs only. Rejected as lower usability and continued user footguns.
  - Add dual-sign acceptance with deprecation warnings. Rejected due to ambiguous API behavior and longer migration tail.

4. Use ScalaCheck-based property tests integrated with ScalaTest.
- Why: Property frameworks provide shrinking and reproducible failures, improving debugging quality over ad-hoc random loops.
- Alternatives considered:
  - Hand-rolled random generators with fixed seeds. Rejected as weaker shrinking/debug ergonomics and duplicated infrastructure.

5. Scope symmetry and monotonicity properties by metric family.
- Why: Not all metrics satisfy all algebraic properties in identical form; explicit scoping prevents false failures.
- Alternatives considered:
  - Force universal symmetry/triangle checks. Rejected because some properties are undefined or not guaranteed for all similarity formulations.

6. Keep implementation mostly additive in tests, with limited production changes for `AffineGap` and API docs.
- Why: This reduces behavioral risk while still addressing the highest confusion and correctness gaps.
- Alternatives considered:
  - Larger production refactor (code-point aware indexing, tokenization overhaul). Rejected as out-of-scope for this change.

## Risks / Trade-offs

- [Breaking API behavior for `AffineGap` penalty signs] -> Mitigation: fail-fast analysis checks, explicit Scaladoc updates, release-note migration examples, and updated defaults.
- [Flaky property tests due to broad generators] -> Mitigation: deterministic seeds where needed, bounded sizes, and stable generator distributions for CI.
- [False assumptions about Unicode tokenization behavior] -> Mitigation: assert and document current behavior rather than encoding speculative expectations.
- [Increased test runtime from property and Unicode suites] -> Mitigation: tune sample counts, split heavy cases, and reserve larger inputs for nightly or tagged runs if needed.
- [Confusion around surrogate pair handling in char-based APIs] -> Mitigation: document exact char-indexed semantics and test current behavior explicitly.

## Migration Plan

1. Land additive test suites first (Unicode, SmithWaterman, property scaffolding) to establish baseline signal.
2. Implement `AffineGap` sign harmonization and update Scala API docs/defaults in the same commit set.
3. Update affected unit/integration tests and fuzzy baselines to the new sign convention.
4. Validate full test pass in CI and confirm no codegen/interpreted regressions with non-ASCII rows.
5. Publish migration guidance showing old vs new `affineGap` argument conventions and expected analysis-time failures for old inputs.

Rollback strategy:
- If adoption risk is higher than expected, revert `AffineGap` sign harmonization while retaining all additive test coverage and documentation clarifications.

## Open Questions

- Should property suites run at full sample counts in default CI or behind a slower test profile?
- Do we need a temporary compatibility shim for `AffineGap` sign migration, or is immediate fail-fast acceptable for all consumers?
- Should surrogate-pair behavior be elevated from "documented limitation" to a follow-up engineering change proposal?
