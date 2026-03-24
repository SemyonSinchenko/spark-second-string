# String Similarity Expression

## Purpose

Define a stable Catalyst expression contract for binary string similarity metrics that supports both token-based and matrix-based metric families.

## Requirements

### Requirement: Unified binary expression contract
The system SHALL provide a unified binary expression contract for string similarity metrics with output type Double.

#### Scenario: Expression shape and output
- **WHEN** a string similarity expression is defined
- **THEN** it SHALL be a binary Catalyst expression
- **THEN** it SHALL accept two string-compatible inputs
- **THEN** it SHALL produce a Double score

#### Scenario: Child expression evaluation semantics
- **WHEN** the expression is evaluated
- **THEN** it SHALL evaluate child expressions via Catalyst child-evaluation flow
- **THEN** it SHALL NOT assume fixed ordinal positions in `InternalRow` for metric inputs

#### Scenario: Null propagation
- **WHEN** either input evaluates to NULL
- **THEN** the similarity result SHALL be NULL

### Requirement: Metric family support
The unified contract SHALL support both token-based and matrix-based metric implementations.

#### Scenario: Token metric compatibility
- **WHEN** a token-based metric is implemented
- **THEN** it SHALL conform to the unified binary expression contract
- **THEN** the contract SHALL support multiple token metrics including `jaccard`, `sorensen_dice`, `overlap_coefficient`, and `cosine`

#### Scenario: Matrix metric compatibility
- **WHEN** a matrix-based metric is implemented
- **THEN** it SHALL conform to the unified binary expression contract
- **THEN** the contract SHALL support normalized matrix metrics including `levenshtein`

### Requirement: Interpreted and codegen parity
String similarity expressions SHALL provide consistent behavior between interpreted evaluation and generated code.

#### Scenario: Deterministic parity
- **WHEN** interpreted and code-generated paths are both available
- **THEN** both paths SHALL return identical results for identical inputs, including edge cases

#### Scenario: Incomplete codegen is forbidden
- **WHEN** a metric declares code-generation support
- **THEN** its codegen implementation SHALL be complete and executable
- **THEN** it SHALL NOT return placeholder or null code objects

#### Scenario: Per-metric parity enforcement
- **WHEN** token and matrix metrics are expanded
- **THEN** parity validation SHALL include `jaccard`, `sorensen_dice`, `overlap_coefficient`, `cosine`, and `levenshtein`

### Requirement: DSL-first API direction
The system SHALL prioritize Scala/Java DSL usage for string similarity expressions.

#### Scenario: Primary usage mode
- **WHEN** developers consume string similarity expressions
- **THEN** DSL usage SHALL be the primary and first-class integration path

#### Scenario: Optional SQL exposure
- **WHEN** SQL function exposure is needed
- **THEN** registration SHALL be available through a thin SparkSession extension
- **THEN** SQL ergonomics SHALL remain secondary to DSL ergonomics in this phase
