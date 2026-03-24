# Jaccard

## Purpose

TBD - Provides a Jaccard similarity implementation for string token sets.

## Requirements

### Requirement: Jaccard similarity implementation
The system SHALL provide a `Jaccard` class that extends `StringSimExpression` to compute Jaccard similarity between two strings based on token sets.

#### Scenario: Class location and naming
- **WHEN** the Jaccard class is created
- **THEN** it SHALL be located in package `sparkss.expressions.token`
- **THEN** it SHALL be named `Jaccard`

#### Scenario: Jaccard similarity calculation
- **WHEN** `getSim(left: String, right: String)` is called with two non-empty strings
- **THEN** the method SHALL tokenize both strings into sets of tokens
- **THEN** it SHALL compute: `|intersection| / |union|`
- **THEN** it SHALL return a value between 0.0 and 1.0

#### Scenario: Edge cases for Jaccard similarity
- **WHEN** both input strings are empty
- **THEN** the result SHALL be 1.0 (identical empty sets)
- **WHEN** one string is empty and the other is not
- **THEN** the result SHALL be 0.0 (no overlap)

#### Scenario: Code generation implementation
- **WHEN** `doGenCode` is called
- **THEN** it SHALL generate Java bytecode for the Jaccard similarity computation
- **THEN** the generated code SHALL avoid allocations in hot loops

#### Scenario: Performance requirements for Jaccard
- **WHEN** Jaccard is called in a hot loop
- **THEN** it SHALL minimize GC pressure
- **THEN** it SHALL use while/for loops instead of Scala collections where possible
- **THEN** it SHALL avoid `Option`, pattern matching, and other allocation-heavy constructs

### Requirement: Test suite for Jaccard similarity
The system SHALL provide a test suite for the Jaccard similarity implementation using ScalaTest.

#### Scenario: Testing via getSim method
- **WHEN** tests are written for Jaccard similarity
- **THEN** tests SHALL use the `getSim` method directly for simplicity
- **THEN** tests SHALL cover: identical strings, completely different strings, partial overlap
- **THEN** tests SHALL cover edge cases: empty strings, null handling
