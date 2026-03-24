# Proposal: Base classes and simplest Jaccard similarity

## Intent
At the moment the basic sceleton of the project including the build system is done. Now it's time to implement the base class for string-similarity Catalyst expressions and add one simplest implementation: Jaccard similarity.

## Scope
- Add a base class for all the future Catalyst expressions for string-similarity functions
- Implement a Jaccard similarity expression based on the base-class
- Add a basic testing suite
- the base class should be insude ...sparkss.expressions and being named StringSimExpression
- the Jaccard implementation should be inside ...sparkss.expressions.token with name Jaccard

## Out-of-scope
- Benchmarks are out-of-scope for now
- Testing codegen is out-of-scope for now

## What Changes: Base classes and simplest Jaccard similarity
Base class for the future implementations and the Jaccard. Inside the implementation avoid allocations ASAP, feel free to use while/for loops (instead of cool scala things like pattern matching, etc.), avoid Option and other things: code should create as less pressure on GC as possible.

## Approach
- The base class should be Serializable
- The base class should extend the spark' Catalyst binary expression
- The base class should support codeGenFallBack
- The base class should require two methods from the end user:
  - doGenCode(ctx: CodegenContext, ev: ExprCode): ExprCode (from the Catalyst BinaryExpression)
  - getSim(left: String, right: String): Double <- our own
- The base class should implement nullSafeEval as well as def eval(input: InternalRow): Any (from the Catalyst):
  - if any of inputs is NULL result is always NULL!
  - under the hood the input should be validated and casted to strings, after that the getSim should be called and wrapped to result
- The base class should do all the boilerplate for being a valid Spark Catalyst expression (String,String) -> Double
- Tests (scalatest) should rely on the getSim function for simplicity
- Jaccard similarty class should just be the first implementation
- Implement both, regular call and codegen, test only regular
- Jaccard will be called in the hot-loop, keep it in mind
- If any qeustion: ask, do not use imagination!


