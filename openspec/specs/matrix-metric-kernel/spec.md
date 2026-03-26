# Matrix Metric Kernel

## Purpose

Define a shared helper abstraction for matrix-based similarity metrics to centralize boundary behavior, workspace conventions, and parity-safe execution semantics.

## Requirements

### Requirement: Shared matrix metric helper abstraction
The system SHALL provide a reusable helper abstraction for matrix-based similarity metrics.

#### Scenario: Reusable matrix boundary behavior
- **WHEN** matrix-based metrics are implemented
- **THEN** shared boundary behavior for empty inputs and normalization guards SHALL be provided by the helper abstraction
- **THEN** matrix metrics SHALL be able to reuse these behaviors without duplicating boundary logic

#### Scenario: Matrix workspace conventions
- **WHEN** a matrix metric computes dynamic programming state
- **THEN** the helper abstraction SHALL define reusable workspace conventions compatible with metric-specific recurrence logic
- **THEN** the helper abstraction SHALL NOT require metric-specific recurrence formulas to be embedded in shared code

#### Scenario: Deterministic parity support
- **WHEN** matrix metrics implement both interpreted and codegen paths
- **THEN** helper semantics SHALL support deterministic parity between interpreted and generated execution

### Requirement: Matrix helper validation coverage
The system SHALL provide test coverage for matrix helper behavior used by matrix metrics.

#### Scenario: Boundary contract validation
- **WHEN** helper behavior is tested
- **THEN** tests SHALL cover boundary outcomes for both-empty and one-empty inputs
- **THEN** tests SHALL cover normalization guard behavior that prevents out-of-range similarity scores

#### Scenario: Affine-gap validation obligations
- **WHEN** matrix-family validation is expanded for `affine_gap`
- **THEN** tests SHALL verify deterministic normalized scores bounded to `[0.0, 1.0]`
- **THEN** tests SHALL verify affine-gap boundary outcomes for both-empty (`1.0`) and one-empty (`0.0`) inputs
- **THEN** tests SHALL verify no boundary or normalization regressions in existing matrix metrics
