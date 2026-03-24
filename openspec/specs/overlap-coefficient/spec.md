# Overlap Coefficient

## Purpose

Define requirements for Overlap Coefficient token-set similarity expression behavior, tests, and benchmark coverage.

## Requirements

### Requirement: Overlap Coefficient similarity implementation
The system SHALL provide an `OverlapCoefficient` expression for token-set Overlap Coefficient similarity between two strings.

#### Scenario: Class location and naming
- **WHEN** the Overlap Coefficient class is created
- **THEN** it SHALL be located in package `sparkss.expressions.token`
- **THEN** it SHALL be named `OverlapCoefficient`

#### Scenario: Overlap Coefficient similarity calculation
- **WHEN** Overlap Coefficient is evaluated with two non-empty strings
- **THEN** both inputs SHALL be tokenized into token sets using the metric tokenizer
- **THEN** it SHALL compute: `|intersection| / min(|A|, |B|)`
- **THEN** it SHALL return a value between 0.0 and 1.0

#### Scenario: Edge cases for Overlap Coefficient similarity
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
- **THEN** it SHALL generate executable bytecode for Overlap Coefficient computation
- **THEN** the generated path SHALL match interpreted results for the same inputs

### Requirement: Test suite for Overlap Coefficient similarity
The system SHALL provide a test suite for the Overlap Coefficient similarity implementation using ScalaTest.

#### Scenario: Algorithm-level coverage
- **WHEN** tests are written for Overlap Coefficient similarity
- **THEN** tests SHALL cover: identical strings, completely different strings, partial overlap
- **THEN** tests SHALL cover edge cases: empty strings, duplicate tokens, whitespace variations

#### Scenario: Catalyst-level integration coverage
- **WHEN** expression-level tests are written
- **THEN** tests SHALL verify null propagation at expression evaluation level
- **THEN** tests SHALL verify interpreted/codegen parity for representative inputs

### Requirement: Overlap Coefficient benchmark coverage
The system SHALL provide benchmark coverage for Overlap Coefficient in the benchmark module.

#### Scenario: Relative benchmark baseline
- **WHEN** benchmark scenarios are executed
- **THEN** Overlap Coefficient SHALL be measured against the existing Jaccard baseline
- **THEN** scenarios SHALL include high-overlap, low-overlap, and subset-like token distributions
