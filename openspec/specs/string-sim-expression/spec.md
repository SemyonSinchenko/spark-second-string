# String Similarity Expression

## Purpose

TBD - Provides an abstract base class for all string similarity Catalyst expressions.

## Requirements

### Requirement: Base class for string similarity expressions
The system SHALL provide an abstract base class `StringSimExpression` for all string similarity Catalyst expressions. The class SHALL be serializable, extend Spark's Catalyst BinaryExpression, and support codeGenFallback.

#### Scenario: Class structure and inheritance
- **WHEN** the base class is defined
- **THEN** it SHALL extend `org.apache.spark.sql.catalyst.expressions.BinaryExpression`
- **THEN** it SHALL implement `Serializable`
- **THEN** it SHALL support `CodegenFallback` trait

#### Scenario: Abstract methods required from implementations
- **WHEN** a developer extends `StringSimExpression`
- **THEN** they MUST implement `doGenCode(ctx: CodegenContext, ev: ExprCode): ExprCode`
- **THEN** they MUST implement `getSim(left: String, right: String): Double`

#### Scenario: Null handling in eval
- **WHEN** `nullSafeEval` is called with a NULL left input
- **THEN** the result SHALL be NULL
- **WHEN** `nullSafeEval` is called with a NULL right input
- **THEN** the result SHALL be NULL

#### Scenario: Input validation and type casting
- **WHEN** `eval(input: InternalRow): Any` is called with non-NULL inputs
- **THEN** the inputs SHALL be validated and cast to strings
- **THEN** `getSim` SHALL be called with the casted string values
- **THEN** the result SHALL be wrapped as a Catalyst `Any` type (Double)

#### Scenario: Code generation fallback
- **WHEN** code generation is not available
- **THEN** the expression SHALL fall back to interpreted execution via `eval`

### Requirement: Performance constraints for base class
The system SHALL minimize GC pressure in the base class implementation.

#### Scenario: Memory allocation awareness
- **WHEN** implementing the base class
- **THEN** allocations SHALL be avoided where possible
- **THEN** while/for loops SHALL be preferred over Scala syntactic sugar (pattern matching, etc.)
- **THEN** `Option` and similar constructs SHALL be avoided in hot paths
