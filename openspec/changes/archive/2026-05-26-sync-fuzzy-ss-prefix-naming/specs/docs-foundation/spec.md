## MODIFIED Requirements

### Requirement: Existing Metrics page provides comprehensive reference
The Existing Metrics page SHALL describe every supported metric with its formula or algorithm summary, DSL and SQL function names, configurable parameters with defaults and valid ranges, and return type. It SHALL also document tokenization modes and the phonetic encoder family, and SHALL list SQL function names using the current `ss_` prefix convention.

#### Scenario: User can find metric parameters and API names
- **WHEN** a user reads the Existing Metrics page
- **THEN** every metric lists its DSL name, SQL name, parameters with defaults, and output range

#### Scenario: SQL names reflect prefix convention
- **WHEN** a user reads SQL function names in metric documentation
- **THEN** each documented SQL function name starts with `ss_`
