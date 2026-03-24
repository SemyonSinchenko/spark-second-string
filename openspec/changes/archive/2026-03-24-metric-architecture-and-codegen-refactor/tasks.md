## 1. Refactor expression contract

- [x] 1.1 Replace the current `StringSimExpression` base abstraction with a unified binary Catalyst expression contract that returns `Double` and validates string-compatible children.
- [x] 1.2 Remove ordinal-based `InternalRow` assumptions and route evaluation through Catalyst child-expression semantics (`nullSafeEval` and generated child code).
- [x] 1.3 Enforce null propagation in the base contract so any null input yields a null similarity result.
- [x] 1.4 Introduce minimal metric-family extension points so token-based and matrix-based metrics can plug into the same expression lifecycle without duplicating Catalyst boilerplate.

## 2. Rework Jaccard on the new contract

- [x] 2.1 Move or update `Jaccard` to conform to the new token-metric contract shape and package requirements.
- [x] 2.2 Implement token-set Jaccard semantics (`|intersection| / |union|`) with duplicate-token deduplication and consistent whitespace tokenization.
- [x] 2.3 Preserve required edge-case behavior: both empty strings => `1.0`; one empty string => `0.0`.
- [x] 2.4 Ensure the interpreted path avoids allocation-heavy constructs in hot loops where practical.

## 3. Implement codegen parity

- [x] 3.1 Implement executable `doGenCode` for `Jaccard` (no placeholder/null code objects).
- [x] 3.2 Align generated logic with interpreted tokenization and scoring so representative and edge inputs produce identical outputs.
- [x] 3.3 Remove or isolate `CodegenFallback` usage where it conflicts with declared codegen support.

## 4. Add DSL and optional SQL registration surface

- [x] 4.1 Confirm or add DSL-first constructors/functions for similarity expressions as the primary integration path.
- [x] 4.2 Implement a thin SparkSession extension that optionally registers SQL functions by delegating to existing DSL expression implementations.
- [x] 4.3 Keep SQL-first ergonomics and SQL-only features out of scope for this phase.

## 5. Expand verification coverage

- [x] 5.1 Update/extend Jaccard algorithm tests for identical, disjoint, partial-overlap, empty-input, duplicate-token, and whitespace-normalization scenarios.
- [x] 5.2 Add expression-level tests for null propagation and child-evaluation correctness under Catalyst execution.
- [x] 5.3 Add interpreted-vs-codegen parity tests for representative and edge-case inputs.
- [x] 5.4 Run the relevant test suites and fix regressions introduced by the refactor.
