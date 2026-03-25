## 1. Shared Token Kernel Refactor

- [x] 1.1 Add a shared token metric helper abstraction for whitespace tokenization into unique token sets and set-overlap primitives.
- [x] 1.2 Migrate existing token metrics (`jaccard`, `sorensen_dice`, `overlap_coefficient`, `cosine`) to use the shared helper without changing outputs.
- [x] 1.3 Add helper-focused unit tests for tokenization equivalence, duplicate collapse, and overlap primitive correctness.

## 2. Braun-Blanquet Token Metric

- [x] 2.1 Add `BraunBlanquet` token expression and companion kernel under `expressions/token` using shared token helper semantics.
- [x] 2.2 Implement formula and boundaries: `|A∩B| / max(|A|,|B|)`, both-empty `1.0`, one-empty `0.0`, bounded `[0.0, 1.0]`.
- [x] 2.3 Add `BraunBlanquetSuite` covering identical, disjoint, partial overlap, subset-like overlap, empty-input, duplicate-token, and mixed-whitespace scenarios.

## 3. LCS Matrix Metric

- [x] 3.1 Add `LcsSimilarity` matrix expression and companion kernel under `expressions/matrix`.
- [x] 3.2 Implement normalized LCS similarity `lcsLen / max(len(left), len(right))` with explicit boundaries for both-empty and one-empty inputs.
- [x] 3.3 Add `LcsSimilaritySuite` covering identical, low-overlap, order-sensitive, empty-input, and different-length scenarios.

## 4. DSL/SQL and Catalyst Integration

- [x] 4.1 Extend `StringSimilarityFunctions` with `braun_blanquet` and `lcs_similarity` overloads for `(Column, Column)` and `(String, String)`.
- [x] 4.2 Extend `StringSimilaritySparkSessionExtensions` to register SQL functions `braun_blanquet` and `lcs_similarity` with existing argument validation behavior.
- [x] 4.3 Extend expression integration tests for null propagation, nested-child evaluation, DSL-vs-SQL consistency, and interpreted/codegen parity for both new metrics.
- [x] 4.4 Update family-level parity assertions so token and matrix parity coverage includes `braun_blanquet` and `lcs_similarity`.

## 5. Benchmarks and Final Verification

- [x] 5.1 Extend token benchmarks to include Braun-Blanquet across high-overlap, low-overlap, and subset-like scenarios.
- [x] 5.2 Add LCS benchmark coverage across short, medium, and long inputs with both low-overlap and high-overlap sequence pairs.
- [x] 5.3 Run full test suite and benchmark-module checks, then fix any regressions while preserving existing metric behavior.
