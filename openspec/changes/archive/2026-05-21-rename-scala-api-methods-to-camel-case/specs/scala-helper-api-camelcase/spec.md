## ADDED Requirements

### Requirement: CamelCase Scala helper naming for selected metrics
The system SHALL expose `mongeElkan` and `affineGap` as the Scala/Java DSL helper names for the Monge-Elkan and Affine Gap metrics.

#### Scenario: CamelCase helpers are available for Column inputs
- **WHEN** a Scala/Java caller constructs Monge-Elkan or Affine Gap expressions with `Column` arguments
- **THEN** the DSL SHALL provide `mongeElkan(left: Column, right: Column)` and `affineGap(left: Column, right: Column)` helper methods

#### Scenario: CamelCase helpers are available for String column names
- **WHEN** a Scala/Java caller constructs Monge-Elkan or Affine Gap expressions with string column-name arguments
- **THEN** the DSL SHALL provide `mongeElkan(left: String, right: String)` and `affineGap(left: String, right: String)` helper methods

### Requirement: SQL names remain snake_case
The system SHALL keep SQL-facing function names unchanged as `monge_elkan` and `affine_gap` while applying camelCase only to Scala/Java helper method names.

#### Scenario: SQL registration contract is preserved
- **WHEN** users invoke SQL string similarity functions
- **THEN** SQL function names for Monge-Elkan and Affine Gap SHALL remain `monge_elkan` and `affine_gap`
- **THEN** no new SQL function names `mongeElkan` or `affineGap` SHALL be introduced
