## 1. Add explicit null-propagation test coverage

- [x] 1.1 Locate the phonetic expression unit test suite and align new test placement with existing naming and structure conventions.
- [x] 1.2 Add table-driven null-input test cases for `soundex`, `refined_soundex`, and `double_metaphone` that assert null output for null row input.
- [x] 1.3 Ensure assertions explicitly validate each expression's `NullIntolerant` null-propagation behavior at the expression boundary.

## 2. Validate behavior and apply targeted fixes if needed

- [x] 2.1 Run the relevant phonetic/string expression test suites and capture any failures related to null propagation.
- [x] 2.2 If tests reveal a null-handling mismatch, implement a minimal, localized fix that preserves existing non-null semantics.
- [x] 2.3 Re-run the updated test scope to confirm all phonetic null-propagation coverage passes consistently.
