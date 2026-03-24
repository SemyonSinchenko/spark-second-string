## 1. Matrix Helper Foundation

- [x] 1.1 Add shared matrix helper abstraction for boundary handling and normalization guards under `src/main/scala/io/github/semyonsinchenko/sparkss/expressions/`.
- [x] 1.2 Add unit tests validating helper behavior for both-empty and one-empty inputs and normalization range safety.

## 2. Cosine Token Metric

- [x] 2.1 Add `Cosine` token expression and companion kernel in `src/main/scala/io/github/semyonsinchenko/sparkss/expressions/token/` using token-set semantics.
- [x] 2.2 Implement Cosine formula `|A∩B| / sqrt(|A| * |B|)` with explicit empty-input behavior and duplicate-token set handling.
- [x] 2.3 Add `CosineSuite` covering identical, disjoint, partial overlap, empty-input, duplicate-token, and whitespace-normalization scenarios.

## 3. Levenshtein Matrix Metric

- [x] 3.1 Add `Levenshtein` matrix expression and companion kernel in `src/main/scala/io/github/semyonsinchenko/sparkss/expressions/matrix/` extending `MatrixMetricExpression`.
- [x] 3.2 Implement normalized similarity `1.0 - (distance / max(len(left), len(right)))` using insertion/deletion/substitution costs of 1 and no transposition.
- [x] 3.3 Reuse matrix helper abstraction for boundary behavior and row-workspace conventions.
- [x] 3.4 Add `LevenshteinSuite` covering identical, single-edit, multi-edit, different-length, both-empty, and one-empty scenarios.

## 4. DSL, SQL, and Catalyst Integration

- [x] 4.1 Extend `StringSimilarityFunctions` with `cosine` and `levenshtein` overloads for `(Column, Column)` and `(String, String)`.
- [x] 4.2 Extend `StringSimilaritySparkSessionExtensions` to register SQL functions `cosine` and `levenshtein` with existing validation behavior.
- [x] 4.3 Extend `StringSimExpressionSuite` with null propagation, nested-expression evaluation, DSL-vs-SQL consistency, and interpreted/codegen parity tests for both metrics.
- [x] 4.4 Update matrix and token family parity assertions to include `cosine` and `levenshtein` in expression-level coverage.

## 5. Benchmarks and Final Verification

- [x] 5.1 Extend token benchmarks to include Cosine alongside Jaccard, Sorensen-Dice, and Overlap Coefficient on high-overlap, low-overlap, and subset-like scenarios.
- [x] 5.2 Add matrix benchmarks for normalized Levenshtein across short/medium/long and low-edit/high-edit scenarios.
- [x] 5.3 Run full test suite and benchmark module checks, then resolve any parity or performance regressions found.
