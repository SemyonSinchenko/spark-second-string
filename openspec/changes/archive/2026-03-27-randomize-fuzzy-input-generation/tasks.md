## 1. Deterministic generation foundation

- [x] 1.1 Refactor fuzzy pair generation entrypoint to use a case-driven row generation flow while keeping the existing output schema and row-count contract unchanged.
- [x] 1.2 Implement per-row deterministic random-state derivation from `(seed, row_index[, stream_id])` so generated values are reproducible independent of execution order.
- [x] 1.3 Define deterministic weighted case-selection logic for each row and centralize constants/helpers needed by all case builders.

## 2. Case builders and relationship semantics

- [x] 2.1 Implement explicit builders for exact-match, high-overlap, medium-overlap, and low-overlap cases with deterministic edit/overlap behavior.
- [x] 2.2 Implement explicit builders for disjoint, asymmetric-length, whitespace/punctuation-heavy, repeated-character, and empty-string cases.
- [x] 2.3 Remove the globally enforced shared-prefix mechanism and ensure overlap is introduced only through the selected case builder semantics.

## 3. Validation and regression safety

- [x] 3.1 Add deterministic tests asserting identical `(seed, rows)` runs produce identical generated pairs and stable case composition counts.
- [x] 3.2 Add tests asserting different seeds with the same row count produce different generated pairs and cohort distributions.
- [x] 3.3 Add invariants and edge-case tests for required cohorts (including small-row runs) to verify schema stability, row-count stability, and disjoint-case guarantees.

## 4. Subproject integration and rollout checks

- [x] 4.1 Wire the new generator internals into the fuzzy-testing subproject CLI/report path without changing downstream DataFrame/report contracts.
- [x] 4.2 Run parity/report smoke checks using fixed seeds and document baseline expectations for post-change cohort distribution behavior.
- [x] 4.3 Keep a temporary fallback path or flag for the old generator until validation passes, then remove or retire it per rollout plan.
