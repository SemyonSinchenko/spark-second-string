## 1. Legacy scorer thread confinement

- [x] 1.1 Update the legacy scorer wrapper in `FuzzyTestingPipeline.scala` to replace the shared lazy scorer instance with per-thread initialization using `ThreadLocal`.
- [x] 1.2 Preserve reflective class loading and score-method invocation contract while scoping scorer object graphs to each thread.
- [x] 1.3 Ensure failure handling and null/empty input behavior remain consistent with the current legacy scorer path.

## 2. Fuzzy flow integration and compatibility

- [x] 2.1 Keep the fix localized to the legacy fuzzy baseline path without changing algorithm selection, SQL `ss_` function naming, or DataFrame-only evaluation flow.
- [x] 2.2 Validate that baseline scoring outputs remain compatible with pre-fix behavior on representative fuzzy-testing datasets.

## 3. Verification and regression coverage

- [x] 3.1 Add or update automated tests to exercise concurrent invocation of legacy baseline UDFs and assert no `TreeMap.rotateLeft`/tokenizer race failure occurs.
- [x] 3.2 Run existing fuzzy flow regression tests plus the new concurrency-stress scenario and confirm stable execution with no score drift.
