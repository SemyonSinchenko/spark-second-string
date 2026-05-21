## 1. Rename Scala helper API surface

- [x] 1.1 Rename `StringSimilarityFunctions` helper definitions from `monge_elkan` to `mongeElkan` for all public overloads.
- [x] 1.2 Rename `StringSimilarityFunctions` helper definitions from `affine_gap` to `affineGap` for all public overloads.
- [x] 1.3 Verify helper arity and accepted argument forms remain unchanged (Column and String-based overloads).

## 2. Migrate internal Scala usage

- [x] 2.1 Update all internal Scala call sites that reference `monge_elkan` to use `mongeElkan`.
- [x] 2.2 Update all internal Scala call sites that reference `affine_gap` to use `affineGap`.
- [x] 2.3 Run repository-wide search to confirm no stale Scala helper references to `monge_elkan` or `affine_gap` remain in Scala DSL usages.

## 3. Update tests for renamed helpers

- [x] 3.1 Update Scala test suites to call `mongeElkan` and `affineGap` helper names.
- [x] 3.2 Add or update tests that assert the renamed helpers still produce expected expressions/results for both overload families.
- [x] 3.3 Run relevant test suites and fix any failures caused by helper renaming.

## 4. Preserve SQL and metric identifier contracts

- [x] 4.1 Verify SQL function registration names remain `monge_elkan` and `affine_gap`.
- [x] 4.2 Verify metric/report identifiers and any SQL-facing references remain snake_case and unchanged.
- [x] 4.3 Add or update coverage that proves no SQL aliases `mongeElkan` or `affineGap` are introduced.

## 5. Refresh documentation and migration messaging

- [x] 5.1 Update Scala DSL docs/examples to use `mongeElkan` and `affineGap`.
- [x] 5.2 Ensure SQL docs/examples continue to use `monge_elkan` and `affine_gap`.
- [x] 5.3 Add explicit breaking-change migration note with before/after usage for Scala/Java callers.
