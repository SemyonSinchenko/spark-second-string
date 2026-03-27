## ADDED Requirements

### Requirement: Deterministic randomized case generation
The system SHALL generate fuzzy-test input pairs through deterministic pseudo-random case selection and case-specific builders so that identical `(seed, rows)` always produce identical pair values and case composition.

#### Scenario: Repeated run yields identical pairs and case mix
- **WHEN** input generation is executed twice with the same `seed` and `rows`
- **THEN** both runs produce identical `(input_left, input_right)` rows and identical per-case counts

#### Scenario: Seed change alters generated cohorts
- **WHEN** input generation is executed with different `seed` values and the same `rows`
- **THEN** generated pairs and case-type distribution differ between runs

### Requirement: Required relationship cohorts are representable
The system SHALL include deterministic generation rules for exact-match, high-overlap, medium-overlap, low-overlap, disjoint, asymmetric-length, whitespace/punctuation-heavy, repeated-character, and empty-string pair relationships.

#### Scenario: Generator supports all required case types
- **WHEN** generation rules are evaluated for supported case builders
- **THEN** each required relationship cohort has an explicit construction path

#### Scenario: Small-row runs remain valid
- **WHEN** `rows` is smaller than the number of relationship cohorts
- **THEN** generation still succeeds deterministically without violating output schema or row-count contracts

### Requirement: Global shared-prefix coupling is eliminated
The system SHALL NOT enforce any always-on shared-prefix rule across all generated pairs.

#### Scenario: No fixed prefix is globally injected
- **WHEN** generated pairs are inspected for construction policy
- **THEN** there is no mandatory fixed-length shared prefix applied to every row

#### Scenario: Overlap is case-specific only
- **WHEN** overlap exists in generated pairs
- **THEN** that overlap is introduced only by the selected case builder semantics rather than by a universal prefix mechanism
