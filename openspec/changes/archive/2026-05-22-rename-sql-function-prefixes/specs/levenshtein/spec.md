## ADDED Requirements

### Requirement: Canonical SQL name for Levenshtein
The system SHALL register the Levenshtein metric for SQL invocation under the canonical name `ss_levenshtein`.

#### Scenario: SQL invocation name
- **WHEN** Levenshtein is exposed through Spark SQL function registration
- **THEN** the available SQL function name SHALL be `ss_levenshtein`
- **THEN** the unprefixed SQL function name `levenshtein` SHALL NOT be registered
