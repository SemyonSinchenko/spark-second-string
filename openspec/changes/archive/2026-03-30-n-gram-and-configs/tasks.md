## 1. Configurable matrix and token metric parameters

- [x] 1.1 Add constructor parameters and defaults for `JaroWinkler`, `NeedlemanWunsch`, `SmithWaterman`, `AffineGap`, and `MongeElkan(innerMetric)` while preserving existing two-argument behavior.
- [x] 1.2 Update each metric companion `similarity` path and expression codegen to pass parameter literals from expression fields.
- [x] 1.3 Implement `checkInputDataTypes` validation for all new parameter constraints (numeric bounds, signs, and allowed `innerMetric` values).
- [x] 1.4 Ensure `withNewChildrenInternal` and expression copy/equality behavior preserve non-default parameter values.

## 2. Character n-gram tokenization support

- [x] 2.1 Implement `tokenizeToCharNgramSet` in `TokenMetricKernelHelper` with required semantics for empty input, short strings, deduplication, and raw-string sliding windows.
- [x] 2.2 Add `ngramSize` (default `0`) to token metrics (`Jaccard`, `SorensenDice`, `OverlapCoefficient`, `Cosine`, `BraunBlanquet`, `MongeElkan`) and switch tokenizer selection by mode.
- [x] 2.3 Add analysis-time validation for `ngramSize >= 0` and keep whitespace-token behavior unchanged when omitted or zero.
- [x] 2.4 Propagate `ngramSize` through companion `similarity` methods and generated code for interpreted/codegen parity.

## 3. Phonetic expression family

- [x] 3.1 Create `expressions.phonetic.PhoneticExpression` base class with string input typing, null-intolerant behavior, null-safe eval, and shared codegen scaffolding.
- [x] 3.2 Implement `Soundex` expression and companion encoding algorithm (canonical US Census behavior, four-character output, non-alphabetic stripping, case-insensitive).
- [x] 3.3 Implement `RefinedSoundex` expression and companion encoding algorithm (refined mapping, adjacent duplicate suppression, variable-length output).
- [x] 3.4 Implement `DoubleMetaphone` expression and primary-code encoder (deterministic, max length 4) consistent with published rules.

## 4. Public API and SQL registration updates

- [x] 4.1 Add parameterized DSL overloads for configurable matrix metrics and n-gram token metrics while retaining legacy overloads that delegate to defaults.
- [x] 4.2 Add unary DSL helpers for `soundex`, `refinedSoundex`, and `doubleMetaphone` accepting `Column` and column-name variants.
- [x] 4.3 Register namespaced SQL phonetic functions `ss_soundex`, `ss_refined_soundex`, and `ss_double_metaphone` without colliding with Spark built-ins.
- [x] 4.4 Document SQL compatibility limits (existing similarity SQL remains two-argument; parameterized behavior is DSL-first).

## 5. Verification, benchmarks, and rollout safety

- [x] 5.1 Add/extend tests for default-value parity, non-default correctness, and invalid-parameter analysis failures across configurable metrics.
- [x] 5.2 Add n-gram correctness tests (bigrams/trigrams, empty/short-string semantics, whitespace-in-ngram behavior) for all token metrics, including Monge-Elkan quality checks.
- [x] 5.3 Add phonetic tests for canonical examples, null propagation, case-insensitivity, non-alphabetic handling, and composition with similarity expressions.
- [x] 5.4 Add interpreted/codegen parity coverage for configurable metrics, n-gram token metrics, and phonetic expressions.
- [x] 5.5 Add benchmark scenarios for non-default configurable metrics, n-gram token metrics, phonetic encoders, and composed phonetic+similarity flows.
