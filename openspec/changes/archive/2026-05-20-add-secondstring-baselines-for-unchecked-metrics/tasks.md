## 1. Baseline Registry and Mapping

- [x] 1.1 Identify all currently implemented fuzzy metrics and classify each as baseline-eligible or explicitly excluded with rationale.
- [x] 1.2 Implement a declarative baseline metric registry that includes metric ids, SecondString mapping names, and exclusion reasons where applicable.
- [x] 1.3 Add adapter hooks in the registry for per-metric input shaping (including tokenization variants) and score normalization.

## 2. Expand Baseline Comparison Coverage

- [x] 2.1 Refactor baseline harness execution to iterate over the baseline-eligible registry instead of a hardcoded four-metric list.
- [x] 2.2 Add baseline comparisons for unvalidated metrics listed in scope, including token metrics, `jaro`, `levenshtein`, `lcs`, and `affine_gap`.
- [x] 2.3 Configure metric-specific tolerances only where mathematically required and keep default assertions strict for all other metrics.

## 3. Failure Reporting and Diagnostics

- [x] 3.1 Ensure mismatch output includes metric id, fixture/case id, expected value, and actual value.
- [x] 3.2 Add coverage for excluded metrics in reporting so skipped baseline checks are explicit and justified.

## 4. Validation and Stability

- [x] 4.1 Run the full fuzzy baseline suite and verify deterministic pass/fail behavior across repeated runs.
- [x] 4.2 Confirm existing baseline checks for `needleman_wunsch`, `smith_waterman`, `jaro_winkler`, and `monge_elkan` remain unchanged.
- [x] 4.3 Update any harness test data/fixtures needed to keep runtime reasonable while preserving representative cases.
