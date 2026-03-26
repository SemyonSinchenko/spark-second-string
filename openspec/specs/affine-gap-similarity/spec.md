# Affine Gap Similarity

## Purpose

Define the canonical `affine_gap` binary string similarity metric contract, including deterministic normalization, edge-case behavior, and null propagation.

## Requirements

### Requirement: Canonical affine-gap similarity metric
The system SHALL provide a binary string similarity metric named `affine_gap` with fixed affine-gap semantics and no runtime tuning in this phase.

#### Scenario: Metric identity and arity
- **WHEN** users construct or invoke the metric
- **THEN** the metric name SHALL be `affine_gap`
- **THEN** the metric SHALL require exactly two string-compatible inputs

#### Scenario: Deterministic bounded score
- **WHEN** `affine_gap` is evaluated for the same non-null inputs
- **THEN** it SHALL return the same deterministic score on every evaluation
- **THEN** the score SHALL be normalized to the inclusive range `[0.0, 1.0]`

### Requirement: Affine-gap edge-case contract
The system SHALL enforce explicit affine-gap outcomes for boundary and representative input classes.

#### Scenario: Empty-input boundaries
- **WHEN** both inputs are empty strings
- **THEN** `affine_gap` SHALL return `1.0`
- **WHEN** exactly one input is an empty string
- **THEN** `affine_gap` SHALL return `0.0`

#### Scenario: Identity boundary
- **WHEN** both inputs are identical non-empty strings
- **THEN** `affine_gap` SHALL return `1.0`

#### Scenario: Representative content classes
- **WHEN** `affine_gap` is evaluated on whitespace-only strings, punctuation-bearing strings, repeated-character strings, and asymmetric-length strings
- **THEN** it SHALL produce deterministic normalized scores within `[0.0, 1.0]`

### Requirement: Null propagation for affine-gap
The metric SHALL preserve existing expression null propagation behavior.

#### Scenario: Null input propagation
- **WHEN** either input to `affine_gap` is NULL
- **THEN** the result SHALL be NULL
