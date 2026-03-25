# LCS Similarity

## Purpose

Define normalized longest common subsequence similarity behavior for string pair comparison.

## Requirements

### Requirement: LCS similarity implementation
The system SHALL provide an `LcsSimilarity` expression for normalized longest common subsequence similarity between two strings.

#### Scenario: Class location and naming
- **WHEN** the LcsSimilarity class is created
- **THEN** it SHALL be located in package `sparkss.expressions.matrix`
- **THEN** it SHALL be named `LcsSimilarity`

#### Scenario: LCS similarity calculation
- **WHEN** LCS similarity is evaluated with two non-empty strings
- **THEN** it SHALL compute longest common subsequence length using matrix dynamic programming
- **THEN** it SHALL normalize similarity as `lcsLen / max(len(left), len(right))`
- **THEN** it SHALL return a value between 0.0 and 1.0

#### Scenario: Edge cases for LCS similarity
- **WHEN** both input strings are empty
- **THEN** the result SHALL be 1.0
- **WHEN** one string is empty and the other is not
- **THEN** the result SHALL be 0.0
- **WHEN** the input strings are identical
- **THEN** the result SHALL be 1.0

#### Scenario: Sequence-order sensitivity
- **WHEN** two strings contain similar characters in different orders
- **THEN** the score SHALL reflect subsequence order constraints rather than set overlap only

#### Scenario: Code generation implementation
- **WHEN** `doGenCode` is called
- **THEN** it SHALL generate executable bytecode for LCS similarity computation
- **THEN** the generated path SHALL match interpreted results for the same inputs

### Requirement: Test suite for LCS similarity
The system SHALL provide a test suite for the LCS similarity implementation using ScalaTest.

#### Scenario: Algorithm-level coverage
- **WHEN** tests are written for LCS similarity
- **THEN** tests SHALL cover: identical strings, low-overlap strings, and order-sensitive string pairs
- **THEN** tests SHALL cover edge cases: empty strings and different input lengths

#### Scenario: Catalyst-level integration coverage
- **WHEN** expression-level tests are written
- **THEN** tests SHALL verify null propagation at expression evaluation level
- **THEN** tests SHALL verify interpreted/codegen parity for representative inputs

### Requirement: LCS benchmark coverage
The system SHALL provide benchmark coverage for LCS similarity in the benchmark module.

#### Scenario: Matrix benchmark scenarios
- **WHEN** benchmark scenarios are executed
- **THEN** LCS similarity SHALL be measured across short, medium, and long inputs
- **THEN** scenarios SHALL include both low-overlap and high-overlap sequence pairs
