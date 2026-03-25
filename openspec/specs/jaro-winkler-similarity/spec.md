# Jaro-Winkler Similarity

## Purpose

Define canonical behavior, quality expectations, and parity guarantees for the `jaro_winkler` string similarity metric.

## Requirements

### Requirement: Canonical Jaro-Winkler metric semantics
The system SHALL provide a binary string similarity metric named `jaro_winkler` that uses canonical fixed parameters and deterministic behavior.

#### Scenario: Canonical formula and fixed parameters
- **WHEN** `jaro_winkler` is evaluated for non-null string inputs
- **THEN** it SHALL compute `jaro + (l * p * (1 - jaro))` where `p = 0.1` and `l = min(commonPrefixLength, 4)`
- **THEN** it SHALL NOT expose tunable scaling factor or prefix-length parameters

#### Scenario: Empty input boundaries
- **WHEN** both input strings are empty
- **THEN** the `jaro_winkler` score SHALL be `1.0`

#### Scenario: One side empty
- **WHEN** exactly one input string is empty
- **THEN** the `jaro_winkler` score SHALL be `0.0`

#### Scenario: No matching characters
- **WHEN** input strings share no matching characters within the matching window
- **THEN** the `jaro_winkler` score SHALL be `0.0`

#### Scenario: Identical strings
- **WHEN** both input strings are identical
- **THEN** the `jaro_winkler` score SHALL be `1.0`

#### Scenario: Deterministic bounds and repeated characters
- **WHEN** `jaro_winkler` is evaluated on any valid non-null inputs, including repeated-character cases
- **THEN** the resulting score SHALL be clamped to the closed interval `[0.0, 1.0]`
- **THEN** repeated evaluation over identical inputs SHALL return identical results

### Requirement: Jaro-Winkler parity and coverage
The system SHALL validate `jaro_winkler` correctness and execution parity across interpreted and generated paths.

#### Scenario: Interpreted and codegen parity
- **WHEN** interpreted and code-generated execution paths are both available for `jaro_winkler`
- **THEN** both paths SHALL return identical results for identical inputs, including edge cases

#### Scenario: Correctness and benchmark coverage
- **WHEN** validation and benchmark suites are defined for `jaro_winkler`
- **THEN** correctness tests SHALL cover empty-input boundaries, no-match inputs, identical strings, repeated characters, and asymmetric lengths
- **THEN** benchmarks SHALL include short, medium, and long string pairs with both high-overlap and low-overlap cases
