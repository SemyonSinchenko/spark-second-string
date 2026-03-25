## ADDED Requirements

### Requirement: Jaro similarity metric semantics
The system SHALL provide a binary string similarity metric named `jaro` that follows standard Jaro semantics with deterministic behavior.

#### Scenario: Empty input boundaries
- **WHEN** both input strings are empty
- **THEN** the `jaro` score SHALL be `1.0`

#### Scenario: One side empty
- **WHEN** exactly one input string is empty
- **THEN** the `jaro` score SHALL be `0.0`

#### Scenario: No matching characters
- **WHEN** input strings share no matching characters within the Jaro matching window
- **THEN** the `jaro` score SHALL be `0.0`

#### Scenario: Deterministic normalization bounds
- **WHEN** `jaro` is evaluated for any valid non-null string inputs
- **THEN** the resulting score SHALL be clamped to the closed interval `[0.0, 1.0]`
- **THEN** repeated evaluation over identical inputs SHALL return identical results

#### Scenario: Matching window definition
- **WHEN** `jaro` computes candidate matches
- **THEN** the matching window SHALL be `max(0, floor(max(len(left), len(right)) / 2) - 1)`

#### Scenario: Transposition accounting
- **WHEN** matching characters are identified in different relative order
- **THEN** transpositions SHALL be counted per the Jaro definition and reflected in the final score

### Requirement: Jaro validation and benchmark coverage
The system SHALL include validation and benchmark coverage for `jaro` across representative overlap patterns and input lengths.

#### Scenario: Correctness coverage matrix
- **WHEN** the `jaro` metric is validated
- **THEN** tests SHALL cover identical strings, single transposition, partial overlap, disjoint strings, repeated characters, asymmetric lengths, and empty-input boundaries

#### Scenario: Benchmark dimension coverage
- **WHEN** benchmark suites are defined for `jaro`
- **THEN** benchmarks SHALL include short, medium, and long string lengths with both high-overlap and low-overlap cases
