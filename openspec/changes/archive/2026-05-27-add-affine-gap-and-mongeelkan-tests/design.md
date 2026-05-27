## Context

Current test coverage does not include two important validation-path failures in string similarity expressions:
- `AffineGap` should reject positive penalties (must be `< 0`) with the same failure class and message style already used in analyzer/type-check tests.
- `MongeElkan` should reject unsupported `innerMetric` values using the established supported-values/rejection pattern.

The proposal limits scope to tests only, with no production behavior changes unless tests expose existing defects.

## Goals / Non-Goals

**Goals:**
- Add negative tests in `AffineGapSuite.scala` proving positive penalties are rejected during analysis/type-check.
- Add negative tests in `MongeElkanSuite.scala` proving invalid `innerMetric` values are rejected with clear supported-values guidance.
- Keep assertions aligned with existing rejection patterns (`TypeCheckFailure` / `AnalysisException` and message matching style).
- Identify whether failures indicate a real bug vs. missing test coverage, and document findings in the change artifacts.

**Non-Goals:**
- No algorithmic or runtime changes to `AffineGap` or `MongeElkan` implementations.
- No broad refactor of test utilities or unrelated suite cleanup.
- No expansion into additional metrics or parameter-validation scenarios beyond the two proposal items.

## Decisions

1. Add tests directly in existing suites instead of creating new suite files.
   - Rationale: these are feature-local validation behaviors; colocating tests preserves discoverability and existing fixture usage.
   - Alternative considered: creating a shared "validation errors" suite; rejected because it fragments ownership and adds indirection for two narrow checks.

2. Reuse established rejection assertion helpers/patterns in each suite.
   - Rationale: keeps failure semantics consistent with current analyzer/type-check expectations and reduces flaky message matching.
   - Alternative considered: custom ad-hoc exception assertions; rejected due to inconsistency and weaker regression signal.

3. Treat failing new tests as signal to investigate, not immediate production-code change in this artifact.
   - Rationale: OpenSpec artifact phase should first capture intended behavior and validation coverage; implementation decisions belong to apply/tasks flow.
   - Alternative considered: patching implementation immediately while writing tests; rejected to preserve artifact sequencing and scope control.

## Risks / Trade-offs

- [Error message assertions become too strict] -> Mitigation: match stable fragments/classification patterns already used in the suites, not brittle full-string snapshots.
- [Suite-specific helper mismatch for negative-path assertions] -> Mitigation: mirror the closest existing rejection test shape in each target suite.
- [New tests fail due to real behavior gap] -> Mitigation: record findings and create implementation tasks in `tasks.md` during next artifact step.

## Migration Plan

1. Add the two new negative tests in their existing suites.
2. Run targeted suite tests, then broader relevant test scope.
3. If tests pass, merge as test-only coverage improvement.
4. If tests fail, proceed with `/opsx-apply` implementation tasks to fix behavior before merge.

Rollback strategy: revert only the added test cases if temporary unblock is required, since this change has no data or runtime migration impact.

## Open Questions

- Should assertions pin exact exception subclasses per runtime/version, or allow a small compatible set already accepted in nearby tests?
- Do current `MongeElkan` supported-values messages have a canonical ordering that tests should enforce or ignore?
