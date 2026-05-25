## ADDED Requirements

### Requirement: SQL function names SHALL use ss_ namespace
The system SHALL expose SQL-callable similarity functions only under names prefixed with `ss_`.

#### Scenario: Registering SQL functions
- **WHEN** SQL functions are registered for similarity metrics
- **THEN** every registered SQL function name SHALL start with `ss_`
- **THEN** no unprefixed SQL function variant SHALL be registered

#### Scenario: Adding new SQL-callable metrics
- **WHEN** a new SQL-callable metric is introduced
- **THEN** its SQL registration name SHALL follow the `ss_` prefix convention
