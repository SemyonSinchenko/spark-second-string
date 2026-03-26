## MODIFIED Requirements

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
