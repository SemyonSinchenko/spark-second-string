## 1. Project Setup

- [x] 1.1 Create package structure: `sparkss.expressions` and `sparkss.expressions.token`
- [x] 1.2 Verify build.sbt includes ScalaTest dependency for tests

## 2. Base Class Implementation (StringSimExpression)

- [x] 2.1 Create abstract class `StringSimExpression` extending `BinaryExpression` with `Serializable` and `CodegenFallback`
- [x] 2.2 Implement abstract method `getSim(left: String, right: String): Double`
- [x] 2.3 Keep the abstract method `doGenCode(ctx: CodegenContext, ev: ExprCode): ExprCode` of the `BinaryExpression` unimplemented
- [x] 2.4 Override `nullSafeEval(left: Any, right: Any): Any` to call `getSim` with casted strings
- [x] 2.5 Override `eval(input: InternalRow): Any` with null validation and result wrapping: input null is always null output

## 3. Jaccard Implementation

- [x] 3.1 Create class `Jaccard` extending `StringSimExpression` in `sparkss.expressions.token`
- [x] 3.2 Implement `getSim` method: tokenize strings, compute |intersection| / |union|
- [x] 3.3 Handle edge cases: both empty → 1.0, one empty → 0.0
- [x] 3.4 Implement `doGenCode` with Java code as strings for Jaccard computation (deferred - out of scope per proposal, relies on CodegenFallback)
- [x] 3.5 Use mutable collections and while-loops to minimize GC pressure

## 4. Test Suite

- [x] 4.1 Create test class for `StringSimExpression` base functionality (tested indirectly via Jaccard)
- [x] 4.2 Create test class for `Jaccard.getSim` method
- [x] 4.3 Test identical strings (expect 1.0)
- [x] 4.4 Test completely different strings (expect 0.0)
- [x] 4.5 Test partial overlap (expect value between 0.0 and 1.0)
- [x] 4.6 Test edge cases: empty strings, null handling

## 5. Verification

- [x] 5.1 Run `sbt test` to verify all tests pass
- [x] 5.2 Verify no compilation errors in main and test sources
- [x] 5.3 Apply scalafmt by `sbt scalafmtAll`
