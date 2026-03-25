## ADDED Requirements

### Requirement: Canonical smith_waterman metric
The system SHALL provide a binary string similarity metric named `smith_waterman` with deterministic canonical semantics and output normalized to the closed interval `[0.0, 1.0]`.

#### Scenario: Metric naming and invocation contract
- **WHEN** users invoke the metric through expression or DSL surfaces
- **THEN** the metric name SHALL be `smith_waterman`
- **THEN** the metric SHALL accept exactly two string-compatible inputs
- **THEN** the metric SHALL return a Double similarity score

#### Scenario: Boundary normalization guarantees
- **WHEN** both inputs are empty strings
- **THEN** the metric SHALL return `1.0`
- **THEN** the returned value SHALL remain within `[0.0, 1.0]`

#### Scenario: One-sided empty input handling
- **WHEN** exactly one input is an empty string and the other is non-empty
- **THEN** the metric SHALL return `0.0`
- **THEN** the returned value SHALL remain within `[0.0, 1.0]`

#### Scenario: Null propagation
- **WHEN** either input evaluates to NULL
- **THEN** the similarity result SHALL be NULL

### Requirement: Interpreted and codegen equivalence for smith_waterman
The `smith_waterman` metric SHALL produce identical outputs across interpreted and generated-code execution paths for the same inputs.

#### Scenario: Parity across execution modes
- **WHEN** interpreted and code-generated evaluation are both available for `smith_waterman`
- **THEN** both evaluation paths SHALL return identical similarity values for identical inputs

#### Scenario: No placeholder codegen implementation
- **WHEN** `smith_waterman` participates in code generation
- **THEN** the codegen implementation SHALL be complete and executable
- **THEN** it SHALL NOT return placeholder or null code fragments

### Requirement: Correctness and benchmark coverage for smith_waterman
The system SHALL include explicit correctness tests and benchmark coverage for `smith_waterman` to protect semantic stability and performance visibility.

#### Scenario: Required correctness edge cases
- **WHEN** correctness tests are executed for `smith_waterman`
- **THEN** the suite SHALL include both-empty, one-empty, identical strings, no-overlap strings, repeated characters, asymmetric lengths, whitespace-only strings, and punctuation-bearing strings

#### Scenario: Required benchmark matrix
- **WHEN** benchmark suites are executed
- **THEN** benchmark coverage SHALL include short, medium, and long input lengths across low, medium, and high overlap patterns
- **THEN** results SHALL include comparison baselines against existing matrix metrics

### Requirement: Smith-waterman scope boundaries
The system SHALL keep non-canonical variants and learner-style APIs out of scope for this phase.

#### Scenario: Out-of-scope enforcement
- **WHEN** planning or implementing `smith_waterman` in this phase
- **THEN** affine-gap variants, configurable scoring parameters, and any learner/training APIs SHALL be out of scope
- **THEN** token/statistical metrics such as `tfidf` and `soft_tfidf` SHALL remain unchanged
