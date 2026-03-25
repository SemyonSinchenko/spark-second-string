# Braun-Blanquet

## Purpose

Define token-set Braun-Blanquet similarity behavior and validation coverage.

## Requirements

### Requirement: Braun-Blanquet similarity implementation
The system SHALL provide a `BraunBlanquet` expression for token-set Braun-Blanquet similarity between two strings.

#### Scenario: Class location and naming
- **WHEN** the BraunBlanquet class is created
- **THEN** it SHALL be located in package `sparkss.expressions.token`
- **THEN** it SHALL be named `BraunBlanquet`

#### Scenario: Braun-Blanquet similarity calculation
- **WHEN** Braun-Blanquet is evaluated with two non-empty strings
- **THEN** both inputs SHALL be tokenized into token sets using the token metric kernel
- **THEN** it SHALL compute: `|intersection| / max(|A|, |B|)`
- **THEN** it SHALL return a value between 0.0 and 1.0

#### Scenario: Edge cases for Braun-Blanquet similarity
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
- **THEN** it SHALL generate executable bytecode for Braun-Blanquet computation
- **THEN** the generated path SHALL match interpreted results for the same inputs

### Requirement: Test suite for Braun-Blanquet similarity
The system SHALL provide a test suite for the Braun-Blanquet similarity implementation using ScalaTest.

#### Scenario: Algorithm-level coverage
- **WHEN** tests are written for Braun-Blanquet similarity
- **THEN** tests SHALL cover: identical strings, completely different strings, partial overlap, and subset-like overlap
- **THEN** tests SHALL cover edge cases: empty strings, duplicate tokens, and whitespace variations

#### Scenario: Catalyst-level integration coverage
- **WHEN** expression-level tests are written
- **THEN** tests SHALL verify null propagation at expression evaluation level
- **THEN** tests SHALL verify interpreted/codegen parity for representative inputs

### Requirement: Braun-Blanquet benchmark coverage
The system SHALL provide benchmark coverage for Braun-Blanquet in the benchmark module.

#### Scenario: Relative benchmark baseline
- **WHEN** benchmark scenarios are executed
- **THEN** Braun-Blanquet SHALL be measured against existing token metrics
- **THEN** scenarios SHALL include high-overlap, low-overlap, and subset-like token distributions
