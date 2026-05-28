# SQL Flow Testing Coverage

## Purpose

Define end-to-end SQL test coverage requirements for registered extension functions so SQL API behavior stays stable during refactoring.

## Requirements

### Requirement: SQL function registry has end-to-end coverage
The test suite SHALL include end-to-end coverage for every SQL function registered by the Spark second string SQL extension so that public SQL behavior remains protected during refactoring.

#### Scenario: All registered SQL functions are covered
- **WHEN** the SQL extension registers its SQL functions
- **THEN** the extension suite contains at least one end-to-end test case for each registered function

### Requirement: End-to-end tests validate SQL-level execution
Each SQL function end-to-end test MUST execute through SQL query parsing and evaluation rather than direct expression unit invocation.

#### Scenario: Test runs via SQL query
- **WHEN** an end-to-end test is executed for a registered SQL function
- **THEN** the function is invoked by a SQL query and the observed result is asserted from query output

### Requirement: End-to-end tests focus on API stability baseline
End-to-end SQL coverage SHALL validate baseline successful behavior for each registered function, while corner-case and algorithmic details remain validated by expression-level tests.

#### Scenario: Baseline happy-path behavior is asserted
- **WHEN** end-to-end coverage is added for a registered SQL function
- **THEN** at least one happy-path SQL usage and expected result is asserted without duplicating expression-level corner-case suites
