## 1. Expression API Boundary Hardening

- [x] 1.1 Identify all string-similarity expression case classes currently publicly constructible and mark each as `private[sparkss]`.
- [x] 1.2 Remove default constructor arguments from the identified expression case classes, including configurable-parameter expressions.
- [x] 1.3 Update any internal instantiation sites that relied on constructor defaults to pass explicit parameter values.

## 2. DSL Helper Compatibility

- [x] 2.1 Update `StringSimilarityFunctions` helpers to be the only supported construction path for string-similarity expressions.
- [x] 2.2 Ensure existing two-argument/defaulted DSL entry points remain available via overloads that supply explicit legacy-default values.
- [x] 2.3 Verify configurable metrics (including `smithWaterman`, `needlemanWunsch`, `jaroWinkler`, `affineGap`, and `mongeElkan` where applicable) preserve current behavior for omitted tuning arguments.

## 3. Validation and Migration Notes

- [x] 3.1 Add or update compile-level and unit tests to confirm direct external expression constructor usage is no longer part of the public API contract.
- [x] 3.2 Add or update tests that confirm DSL helper outputs and metric scores remain unchanged for existing defaulted call shapes.
- [x] 3.3 Document migration guidance noting the breaking change: callers must use `StringSimilarityFunctions` instead of direct expression case-class instantiation.
