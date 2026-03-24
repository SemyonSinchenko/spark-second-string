## 1. Token Metric Expressions

- [x] 1.1 Add `SorensenDice` token expression and companion kernel in `src/main/scala/io/github/semyonsinchenko/sparkss/expressions/token/` with token-set semantics aligned to Jaccard.
- [x] 1.2 Add `OverlapCoefficient` token expression and companion kernel in `src/main/scala/io/github/semyonsinchenko/sparkss/expressions/token/` with token-set semantics aligned to Jaccard.
- [x] 1.3 Implement explicit empty-input behavior for both metrics (`both empty -> 1.0`, `one empty -> 0.0`) and null propagation through the base expression contract.
- [x] 1.4 Implement generated-code hooks for both metrics and ensure generated path delegates to executable metric kernels.

## 2. DSL and SQL Surface

- [x] 2.1 Extend `StringSimilarityFunctions` with `sorensenDice(Column, Column)` and `sorensenDice(String, String)` helpers.
- [x] 2.2 Extend `StringSimilarityFunctions` with `overlapCoefficient(Column, Column)` and `overlapCoefficient(String, String)` helpers.
- [x] 2.3 Update `StringSimilaritySparkSessionExtensions` to register `sorensen_dice` and `overlap_coefficient` as thin adapters over expression constructors.
- [x] 2.4 Keep existing `jaccard` DSL and SQL registration behavior unchanged.

## 3. Test Coverage

- [x] 3.1 Add `SorensenDiceSuite` covering identical strings, disjoint strings, partial overlap, empty-input edges, duplicate tokens, and mixed-whitespace normalization.
- [x] 3.2 Add `OverlapCoefficientSuite` covering identical strings, disjoint strings, partial overlap, empty-input edges, duplicate tokens, and mixed-whitespace normalization.
- [x] 3.3 Extend Catalyst-level expression tests to verify null propagation and nested-expression correctness for both new metrics.
- [x] 3.4 Extend parity tests to assert interpreted and whole-stage codegen outputs are identical for both new metrics.
- [x] 3.5 Add DSL-vs-SQL consistency tests for `sorensen_dice` and `overlap_coefficient` after registration.

## 4. Benchmarks

- [x] 4.1 Add benchmark scenarios for Sorensen-Dice and Overlap Coefficient in the JMH module.
- [x] 4.2 Include Jaccard baseline runs in the same benchmark suite for relative comparison.
- [x] 4.3 Cover representative datasets for high-overlap, low-overlap, and subset-like token distributions.

## 5. Validation

- [x] 5.1 Run root test suite and resolve any failures.
- [x] 5.2 Run benchmark module checks to ensure new benchmark code compiles and executes.
- [x] 5.3 Verify final OpenSpec status reports all artifacts complete for `add-dice-overlap-token-metrics`.
