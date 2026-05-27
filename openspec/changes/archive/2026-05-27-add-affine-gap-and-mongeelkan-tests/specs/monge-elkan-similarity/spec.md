## MODIFIED Requirements

### Requirement: Correctness and benchmark coverage for monge_elkan
The system SHALL include explicit correctness tests and benchmark coverage for `monge_elkan` to protect semantic stability and performance visibility.

#### Scenario: Required correctness edge cases
- **WHEN** correctness tests are executed for `monge_elkan`
- **THEN** the suite SHALL include both-empty, one-empty, whitespace-only inputs, repeated tokens, punctuation-bearing tokens, asymmetric token counts, token-order differences, and invalid `innerMetric` values that MUST be rejected during analysis

#### Scenario: Required benchmark matrix
- **WHEN** benchmark suites are executed
- **THEN** benchmark coverage SHALL include short, medium, and long inputs across low, medium, and high token-overlap cohorts
- **THEN** results SHALL include comparison baselines against existing token metrics and selected matrix metrics
