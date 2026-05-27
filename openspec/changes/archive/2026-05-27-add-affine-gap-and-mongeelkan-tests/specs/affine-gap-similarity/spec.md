## ADDED Requirements

### Requirement: Affine-gap parameter validation coverage
The system SHALL include correctness tests that verify `affine_gap` rejects invalid affine-penalty parameters at analysis time.

#### Scenario: Reject non-negative affine penalties
- **WHEN** `affine_gap` is constructed with a penalty parameter that is `0` or positive
- **THEN** analysis SHALL fail with a type-check or analysis exception indicating affine penalties MUST be negative
