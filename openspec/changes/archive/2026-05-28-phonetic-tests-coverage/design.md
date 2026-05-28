## Context

Phonetic expressions currently lack dedicated null-propagation unit tests.
`Soundex`, `RefinedSoundex`, and `DoubleMetaphone` are covered indirectly through
`StringSimExpressionSuite`, but there are no explicit tests that lock down their
`NullIntolerant` behavior. This is important because Catalyst optimization relies
on the null-intolerance contract to reason about expression nullability and
execution planning.

The proposal scope is to add explicit tests for phonetic expressions and only
perform targeted fixes if those tests uncover an existing behavior mismatch.

## Goals / Non-Goals

**Goals:**
- Add explicit null-propagation tests for phonetic expressions.
- Validate that each phonetic expression continues to satisfy
  `NullIntolerant` expectations.
- Keep fixes minimal and localized if tests expose behavior regressions.

**Non-Goals:**
- Refactor phonetic expression implementations beyond what is needed to satisfy
  null-propagation correctness.
- Broaden this effort to non-phonetic string similarity expressions.
- Introduce API or behavior changes unrelated to null handling.

## Decisions

1. Add dedicated null-focused tests in the phonetic test suite rather than
   relying on cross-suite implicit coverage.
   - Rationale: Explicit tests prevent accidental regressions and make the
     contract visible at the exact feature boundary.
   - Alternative considered: Keep relying on `StringSimExpressionSuite`.
     Rejected because failures become less actionable and contract coverage is
     indirect.

2. Use a table-driven pattern to cover `Soundex`, `RefinedSoundex`, and
   `DoubleMetaphone` consistently.
   - Rationale: This keeps assertions uniform and makes future extensions cheap.
   - Alternative considered: Fully separate tests per expression.
     Rejected due to duplicated setup and weaker consistency.

3. Apply code changes only when tests reveal concrete contract violations.
   - Rationale: The change is primarily a coverage hardening effort, so behavior
     should remain stable unless a bug is demonstrated.
   - Alternative considered: Preemptive implementation cleanup.
     Rejected to avoid scope creep and unintended semantic changes.

## Risks / Trade-offs

- [Risk] Tests may expose existing null-handling defects that require
  implementation changes. -> Mitigation: Limit fixes to the failing paths and
  preserve existing non-null semantics with focused assertions.
- [Risk] Test placement might drift from existing suite conventions.
  -> Mitigation: Follow current phonetic/string-expression test organization and
  naming patterns.
- [Trade-off] Narrow scope improves safety but may leave adjacent coverage gaps
  untouched. -> Mitigation: Document follow-up opportunities separately instead
  of expanding this change.

## Migration Plan

1. Add explicit null-propagation tests for each phonetic expression.
2. Run the relevant test suites to detect regressions.
3. If failures occur, implement targeted fixes and rerun tests.
4. Land the change once null-propagation coverage passes consistently.

Rollback strategy: revert the test additions and any associated targeted fixes
as a single change if unexpected instability appears.

## Open Questions

- Should null-propagation checks be centralized in a reusable helper for all
  null-intolerant expressions, or remain local to each feature suite?
- Is there any optimizer-facing behavior around `NullIntolerant` for these
  expressions that also warrants plan-level regression tests?
