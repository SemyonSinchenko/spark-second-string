## 1. Test Scaffolding and Shared Fixtures

- [x] 1.1 Add shared Unicode fixture data covering diacritics, composed/decomposed forms, CJK, emoji, mixed-script, zero-width, and no-whitespace inputs
- [x] 1.2 Add reusable score assertion helpers to enforce determinism and `[0.0, 1.0]` bounds across metric suites

## 2. Unicode Coverage Across Metric Families

- [x] 2.1 Add matrix metric tests for non-ASCII inputs (diacritics, CJK, emoji, mixed-script) with deterministic bounded-score assertions
- [x] 2.2 Add token metric tests for Unicode boundary edge cases, including non-ASCII separators, zero-width characters, and CJK no-whitespace text
- [x] 2.3 Add phonetic metric tests that exercise non-ASCII inputs and assert deterministic bounded behavior without runtime errors

## 3. Matrix Internals and Missing Suite Coverage

- [x] 3.1 Add targeted `ResolvedStrings` tests to verify non-ASCII fallback selection, character-length semantics, and deterministic `charAt` behavior
- [x] 3.2 Create a dedicated `SmithWatermanSuite` matching existing matrix suite depth and style

## 4. AffineGap Sign Harmonization

- [x] 4.1 Update `AffineGap` validation to require negative mismatch/open/extend penalties and adjust defaults to `-1`, `-2`, `-1`
- [x] 4.2 Update `AffineGap` dynamic-programming recurrence to convert penalty inputs into cost-space while preserving algorithm behavior
- [x] 4.3 Update Scala API docs and migration notes with old-vs-new examples and fail-fast expectations for legacy positive penalties

## 5. Property and Integration Validation

- [x] 5.1 Add ScalaCheck property tests for identity, bounds, empty handling, and family-appropriate symmetry invariants
- [x] 5.2 Add non-ASCII expression-level integration cases to confirm interpreted/codegen parity for Unicode rows
- [x] 5.3 Update affected tests and fuzzy baselines for the new `AffineGap` convention and tune property sample sizes for stable CI runtime
