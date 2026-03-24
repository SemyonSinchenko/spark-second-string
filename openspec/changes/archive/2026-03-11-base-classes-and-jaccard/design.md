## Context

The project skeleton and build system are complete. This is the first implementation phase introducing string similarity expressions to Spark. The design focuses on:

- **Package structure**: `sparkss.expressions` for the base class, `sparkss.expressions.token` for token-based implementations (Jaccard)
- **Spark integration**: Extending Catalyst's `BinaryExpression` for SQL function integration
- **Performance-first**: All implementations must minimize GC pressure for hot-loop usage

**Constraints:**
- Must work with Spark's Catalyst expression framework
- Must support both interpreted (`eval`) and codegen (`doGenCode`) execution paths
- Null handling must follow Spark conventions (NULL input → NULL output)

## Goals / Non-Goals

**Goals:**
- Create a reusable base class (`StringSimExpression`) for all future string similarity functions
- Implement Jaccard similarity as the first concrete implementation
- Provide a clean abstraction that makes adding new similarity algorithms trivial
- Ensure performance characteristics suitable for large-scale data processing
- Basic ScalaTest coverage via the `getSim` method

**Non-Goals:**
- Benchmarks (deferred to a future change)
- Codegen testing (deferred; only regular `eval` path will be tested initially)
- Other similarity algorithms (Levenshtein, Cosine, etc. - future changes)

## Decisions

### 1. Base class extends Catalyst BinaryExpression

**Decision:** `StringSimExpression` extends `org.apache.spark.sql.catalyst.expressions.BinaryExpression`

**Rationale:**
- BinaryExpression provides the standard pattern for two-argument expressions in Spark
- Built-in null handling via `nullSafeEval`
- Integrates seamlessly with Spark SQL's query optimizer
- Standard approach used by Spark's built-in functions

**Alternatives considered:**
- Extending `UnaryExpression`: Rejected – we need two string inputs
- Extending `Expression` directly: Rejected – would require reimplementing binary input handling

### 2. Abstract method split: `doGenCode` + `getSim`

**Decision:** Subclasses must implement:
- `doGenCode(ctx: CodegenContext, ev: ExprCode): ExprCode` (Catalyst requirement)
- `getSim(left: String, right: String): Double` (our abstraction)

**Rationale:**
- `getSim` isolates the similarity algorithm from Spark boilerplate
- Makes unit testing simple – no need to mock CodegenContext
- `doGenCode` handles code generation, delegating similarity logic to generated code
- Clear separation: `getSim` = what, `doGenCode` = how to generate

### 3. Performance: avoid Scala collections in hot paths

**Decision:** Use `while`/`for` loops and mutable collections instead of Scala's functional collections

**Rationale:**
- Scala collections (List, Set, Map) create significant GC pressure
- Token-based similarity algorithms iterate over strings repeatedly
- Mutable `ArrayBuffer` or primitive arrays reduce allocations
- Pattern matching on strings creates intermediate objects

**Implementation guidance:**
- Tokenization: use `java.util.StringTokenizer` or manual parsing with indices
- Set operations: use mutable `HashSet` or primitive bit sets if applicable
- Avoid `Option`, `match`, `map`, `flatMap` in `getSim` and codegen paths

### 4. Null handling: NULL inputs produce NULL output

**Decision:** Follow Spark's null propagation semantics

**Rationale:**
- Consistent with all Spark SQL functions
- `nullSafeEval` in BinaryExpression handles this automatically
- Simplifies downstream logic – no need to check for null in `getSim`

### 5. Jaccard tokenization strategy

**Decision:** Split on whitespace for initial implementation

**Rationale:**
- Simplest tokenization approach
- Adequate for proof-of-concept
- Can be enhanced later (punctuation handling, n-grams, etc.)

**Implementation:**
- `String.split("\\s+")` for simple cases, or manual tokenization for performance
- Empty string → empty token set
- Both empty → similarity = 1.0 (identical empty sets)

### 6. Test strategy: test `getSim` directly

**Decision:** Unit tests call `getSim` method, not full expression evaluation

**Rationale:**
- Simpler test setup – no need for InternalRow, CodegenContext
- Tests the core algorithm, not Spark integration
- Codegen path tested separately in future change

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| **GC pressure from tokenization** | Use mutable collections, avoid intermediate strings, consider character-level tokenization without splitting |
| **Codegen complexity** | Start with simple Java code generation; can optimize later |
| **Base class may need refactoring** | Keep base class minimal; only add abstractions when 2nd implementation proves the pattern |
| **Whitespace-only tokenization is limiting** | Document as initial approach; design `getSim` to allow future tokenization strategies |
| **Serializable requirement** | Ensure all fields are Serializable; avoid transient Spark internals |

## Migration Plan

Not applicable – this is a new feature with no existing functionality to migrate.

## Open Questions

1. **Tokenization performance**: Should we use `String.split()` or manual index-based parsing? (Decision: start with `split`, profile, optimize if needed) <- confirmed
2. **Case sensitivity**: Should Jaccard be case-sensitive by default? (Decision: yes, keep it simple; can add normalization later) <- confirmed, users can always use Spark' `lower` Built-in
3. **Empty set semantics**: Is similarity of two empty strings = 1.0 correct? (Decision: yes, mathematically sound – identical sets) <- confirmed
