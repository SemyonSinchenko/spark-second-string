## Context

SparkSecondString registers 16 public SQL functions, but only 2 are currently covered by end-to-end SQL flow tests in `SparkSecondStringExtensionSuite`. The existing gap increases the risk of unintentionally breaking the extension's public SQL API during refactors, especially when function registration, parser behavior, or expression wiring changes.

The proposal defines a focused goal: add one minimal end-to-end test per registered SQL function while keeping deep semantic and edge-case validation in expression-level suites.

## Goals / Non-Goals

**Goals:**
- Add end-to-end SQL flow coverage for every registered SQL function in `SparkSecondStringExtensionSuite`.
- Verify public API stability by checking each function can be resolved and executed through Spark SQL.
- Keep tests simple, deterministic, and fast to run in normal CI.

**Non-Goals:**
- Re-implement expression-level correctness testing already covered in lower-level suites.
- Add exhaustive corner-case matrices for each SQL function in this suite.
- Change function semantics or registration behavior as part of this change.

## Decisions

1. Add exactly one baseline end-to-end test per registered SQL function.
   - Rationale: This gives broad public API coverage with minimal maintenance overhead.
   - Alternative considered: multiple scenario tests per function in this suite. Rejected because it duplicates lower-level tests and slows CI.

2. Keep test inputs small and canonical.
   - Rationale: Simple literals and minimal DataFrame setup reduce flakiness and make failures easy to diagnose.
   - Alternative considered: randomized/property-like SQL input sets. Rejected due to lower debuggability and weaker signal for API break detection.

3. Centralize the function inventory in the suite to ensure all 16 functions are represented.
   - Rationale: A visible inventory allows reviewers to quickly confirm full registration coverage.
   - Alternative considered: ad hoc test additions without an explicit inventory. Rejected because omissions are hard to detect.

4. Assert function execution through Spark SQL entry points (not direct expression invocation).
   - Rationale: The risk being addressed is API wiring/regression in SQL flow, so assertions should exercise registration, parsing, and execution path.
   - Alternative considered: direct expression unit assertions only. Rejected because they do not validate SQL registration and parser integration.

## Risks / Trade-offs

- [Risk] SQL function list drifts as new functions are added later, leaving this suite incomplete again. -> Mitigation: keep a single explicit inventory in the suite and update it whenever registrations change.
- [Risk] Broad coverage increases test count and suite runtime. -> Mitigation: enforce one minimal scenario per function and avoid redundant edge-case permutations.
- [Risk] Tests may pass while missing deep correctness defects. -> Mitigation: explicitly rely on expression-level suites for semantic depth; this suite guards API wiring and end-to-end execution.

## Migration Plan

1. Add/expand `SparkSecondStringExtensionSuite` with one end-to-end SQL test per function.
2. Validate local test execution for this suite and full test run in CI.
3. Monitor CI for stability and iterate on brittle fixtures if needed.

Rollback strategy: revert the added tests if they introduce instability, then reintroduce incrementally with simpler fixtures.

## Open Questions

- Should the inventory of registered functions be generated from registration code at runtime, or kept as a static reviewed list in tests?
- Are there environment-specific SQL parsing differences in CI that require normalizing session configuration for deterministic results?
