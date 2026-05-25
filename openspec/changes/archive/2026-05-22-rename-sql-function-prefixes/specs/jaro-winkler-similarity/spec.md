## ADDED Requirements

### Requirement: Canonical SQL name for Jaro-Winkler
The system SHALL register Jaro-Winkler for SQL invocation under the canonical name `ss_jaro_winkler`.

#### Scenario: SQL invocation name
- **WHEN** Jaro-Winkler is exposed through Spark SQL function registration
- **THEN** the available SQL function name SHALL be `ss_jaro_winkler`
- **THEN** the unprefixed SQL function name `jaro_winkler` SHALL NOT be registered
