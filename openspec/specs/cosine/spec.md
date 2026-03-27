# Cosine

## Purpose

Define token-set Cosine similarity expression behavior, correctness constraints, and validation requirements for tests and benchmarks.

## Requirements

### Requirement: Cosine similarity implementation
The system SHALL provide a `Cosine` expression for token-set Cosine similarity between two strings.

#### Scenario: Class location and naming
- **WHEN** the Cosine class is created
- **THEN** it SHALL be located in package `io.github.semyonsinchenko.sparkss.expressions.token`
- **THEN** it SHALL be named `Cosine`

#### Scenario: Cosine similarity calculation
- **WHEN** Cosine is evaluated with two non-empty strings
- **THEN** both inputs SHALL be tokenized into token sets using the metric tokenizer
- **THEN** it SHALL compute: `|intersection| / sqrt(|A| * |B|)`
- **THEN** it SHALL return a value between 0.0 and 1.0

#### Scenario: Edge cases for Cosine similarity
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
- **THEN** it SHALL generate executable bytecode for Cosine computation
- **THEN** the generated path SHALL match interpreted results for the same inputs

### Requirement: Test suite for Cosine similarity
The system SHALL provide a test suite for the Cosine similarity implementation using ScalaTest.

#### Scenario: Algorithm-level coverage
- **WHEN** tests are written for Cosine similarity
- **THEN** tests SHALL cover: identical strings, completely different strings, partial overlap
- **THEN** tests SHALL cover edge cases: empty strings, duplicate tokens, whitespace variations

#### Scenario: Catalyst-level integration coverage
- **WHEN** expression-level tests are written
- **THEN** tests SHALL verify null propagation at expression evaluation level
- **THEN** tests SHALL verify interpreted/codegen parity for representative inputs

### Requirement: Cosine benchmark coverage
The system SHALL provide benchmark coverage for Cosine in the benchmark module.

#### Scenario: Relative benchmark baseline
- **WHEN** benchmark scenarios are executed
- **THEN** Cosine SHALL be measured against the existing Jaccard baseline
- **THEN** scenarios SHALL include high-overlap, low-overlap, and subset-like token distributions
