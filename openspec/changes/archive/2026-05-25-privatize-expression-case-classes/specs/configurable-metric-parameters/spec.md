## MODIFIED Requirements

### Requirement: DSL supports parameterized overloads
The system SHALL expose parameterized DSL overloads for configurable metrics while retaining existing overloads that delegate to default values, and SHALL keep those defaults defined at the DSL helper layer rather than as public expression-constructor defaults.

#### Scenario: Legacy DSL call remains valid
- **WHEN** a user calls `smithWaterman(left, right)` without tuning arguments
- **THEN** the call compiles and executes by delegating to the parameterized form with default settings

#### Scenario: Explicit-parameter construction is preserved internally
- **WHEN** configurable expressions are instantiated by DSL helper implementations
- **THEN** helper implementations SHALL provide explicit parameter values for tunable settings
- **THEN** behavior for omitted tuning arguments SHALL remain backward compatible with legacy defaults
