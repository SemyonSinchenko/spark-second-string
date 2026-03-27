# Jaccard

## Purpose

Provide a token-based Jaccard similarity metric implemented on top of the unified string-similarity expression contract.

## Requirements

### Requirement: Jaccard similarity implementation
The system SHALL provide a `Jaccard` expression for token-set Jaccard similarity between two strings.

#### Scenario: Class location and naming
- **WHEN** the Jaccard class is created
- **THEN** it SHALL be located in package `io.github.semyonsinchenko.sparkss.expressions.token`
- **THEN** it SHALL be named `Jaccard`

#### Scenario: Jaccard similarity calculation
- **WHEN** Jaccard is evaluated with two non-empty strings
- **THEN** both inputs SHALL be tokenized into token sets using the metric tokenizer
- **THEN** it SHALL compute: `|intersection| / |union|`
- **THEN** it SHALL return a value between 0.0 and 1.0

#### Scenario: Edge cases for Jaccard similarity
- **WHEN** both input strings are empty
- **THEN** the result SHALL be 1.0 (identical empty sets)
- **WHEN** one string is empty and the other is not
- **THEN** the result SHALL be 0.0 (no overlap)

#### Scenario: Duplicate token handling
- **WHEN** an input contains duplicate tokens
- **THEN** duplicates SHALL NOT increase set cardinality
- **THEN** the score SHALL be computed from unique tokens only

#### Scenario: Whitespace normalization
- **WHEN** inputs include repeated or mixed whitespace separators
- **THEN** token boundaries SHALL be treated consistently
- **THEN** semantically equivalent whitespace tokenization SHALL produce identical scores

#### Scenario: Code generation implementation
- **WHEN** `doGenCode` is called
- **THEN** it SHALL generate Java bytecode for the Jaccard similarity computation
- **THEN** the generated code SHALL be executable and non-placeholder
- **THEN** the generated path SHALL match interpreted results for the same inputs

#### Scenario: Performance requirements for Jaccard
- **WHEN** Jaccard is called in a hot loop
- **THEN** it SHALL minimize GC pressure
- **THEN** it SHALL use while/for loops instead of Scala collections where possible
- **THEN** it SHALL avoid `Option`, pattern matching, and other allocation-heavy constructs

### Requirement: Test suite for Jaccard similarity
The system SHALL provide a test suite for the Jaccard similarity implementation using ScalaTest.

#### Scenario: Algorithm-level coverage
- **WHEN** tests are written for Jaccard similarity
- **THEN** tests SHALL cover: identical strings, completely different strings, partial overlap
- **THEN** tests SHALL cover edge cases: empty strings, duplicate tokens, whitespace variations

#### Scenario: Catalyst-level integration coverage
- **WHEN** expression-level tests are written
- **THEN** tests SHALL verify null propagation at expression evaluation level
- **THEN** tests SHALL verify interpreted/codegen parity for representative inputs
