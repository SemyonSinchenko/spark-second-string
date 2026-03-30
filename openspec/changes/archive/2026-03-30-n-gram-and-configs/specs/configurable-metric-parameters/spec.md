## ADDED Requirements

### Requirement: Configurable matrix metric parameters
The system SHALL allow callers to configure non-child constructor parameters for `jaro_winkler`, `needleman_wunsch`, `smith_waterman`, and `affine_gap` while preserving current behavior when omitted by using defaults that match legacy constants.

#### Scenario: Defaults preserve legacy behavior
- **WHEN** a user calls each configurable matrix metric using the existing two-argument API shape
- **THEN** the computed score matches the legacy implementation for the same input pairs

### Requirement: Configurable Monge-Elkan inner metric
The system SHALL allow Monge-Elkan to select its token-pair inner metric through an `innerMetric` parameter constrained to `jaro_winkler`, `jaro`, `levenshtein`, `needleman_wunsch`, or `smith_waterman`.

#### Scenario: Supported inner metric is applied
- **WHEN** a user evaluates Monge-Elkan with a supported `innerMetric` value
- **THEN** token-pair scoring uses the selected metric and returns a deterministic similarity score

### Requirement: Analysis-time validation for tunable parameters
The system SHALL validate configurable metric parameters during analysis using `checkInputDataTypes` and reject invalid values with descriptive type-check failures.

#### Scenario: Invalid numeric bounds are rejected
- **WHEN** a user provides an out-of-range parameter such as `prefixScale = 0.3` or `matchScore = -1`
- **THEN** query analysis fails before execution with an error that identifies the violated constraint

### Requirement: DSL supports parameterized overloads
The system SHALL expose parameterized DSL overloads for configurable metrics while retaining existing overloads that delegate to default values.

#### Scenario: Legacy DSL call remains valid
- **WHEN** a user calls `smithWaterman(left, right)` without tuning arguments
- **THEN** the call compiles and executes by delegating to the parameterized form with default settings

### Requirement: SQL compatibility remains stable for matrix and token metrics
The system SHALL keep existing SQL function signatures unchanged for existing similarity metrics and SHALL not require extra SQL arguments for parameterized behavior.

#### Scenario: Existing SQL query remains unchanged
- **WHEN** a user runs an existing two-argument SQL similarity call
- **THEN** query behavior and output remain backward compatible
