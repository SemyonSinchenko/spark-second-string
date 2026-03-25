## ADDED Requirements

### Requirement: Canonical monge_elkan metric
The system SHALL provide a binary string similarity metric named `monge_elkan` with deterministic canonical semantics and output normalized to the closed interval `[0.0, 1.0]`.

#### Scenario: Metric naming and invocation contract
- **WHEN** users invoke the metric through expression or DSL surfaces
- **THEN** the metric name SHALL be `monge_elkan`
- **THEN** the metric SHALL accept exactly two string-compatible inputs
- **THEN** the metric SHALL return a Double similarity score

#### Scenario: Empty input boundaries
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

### Requirement: Token-level soft matching semantics
The `monge_elkan` metric SHALL score token-level soft matches using deterministic tokenization and fixed canonical scoring behavior for this phase.

#### Scenario: Whitespace-only input handling
- **WHEN** either input contains only whitespace separators
- **THEN** tokenization SHALL treat it as an empty token sequence
- **THEN** boundary behavior SHALL follow the empty-input rules

#### Scenario: Repeated token handling
- **WHEN** either input contains repeated tokens
- **THEN** repeated tokens SHALL be preserved in the token sequence used for matching
- **THEN** repeated tokens SHALL influence the result consistently across repeated evaluations

#### Scenario: Punctuation-bearing token handling
- **WHEN** inputs contain punctuation-bearing tokens
- **THEN** punctuation SHALL be preserved as part of token text unless split by whitespace
- **THEN** matching SHALL operate on those resulting token strings deterministically

#### Scenario: Asymmetric token-count behavior
- **WHEN** the two inputs have different token counts
- **THEN** the score SHALL be computed without requiring equal token counts
- **THEN** the result SHALL remain within `[0.0, 1.0]`

#### Scenario: Token-order differences
- **WHEN** inputs contain the same or near-matching tokens in different order
- **THEN** evaluation SHALL follow monge_elkan token-level best-match semantics
- **THEN** score computation SHALL remain deterministic for the same inputs

#### Scenario: No runtime tunables in this phase
- **WHEN** users invoke `monge_elkan`
- **THEN** canonical scoring semantics SHALL be fixed
- **THEN** runtime configuration of weighting, affine variants, or alternative inner-metric knobs SHALL be out of scope

### Requirement: Correctness and benchmark coverage for monge_elkan
The system SHALL include explicit correctness tests and benchmark coverage for `monge_elkan` to protect semantic stability and performance visibility.

#### Scenario: Required correctness edge cases
- **WHEN** correctness tests are executed for `monge_elkan`
- **THEN** the suite SHALL include both-empty, one-empty, whitespace-only inputs, repeated tokens, punctuation-bearing tokens, asymmetric token counts, and token-order differences

#### Scenario: Required benchmark matrix
- **WHEN** benchmark suites are executed
- **THEN** benchmark coverage SHALL include short, medium, and long inputs across low, medium, and high token-overlap cohorts
- **THEN** results SHALL include comparison baselines against existing token metrics and selected matrix metrics
