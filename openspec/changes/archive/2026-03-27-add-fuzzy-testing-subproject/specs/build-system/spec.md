## MODIFIED Requirements

### Requirement: Multi-project structure
The system SHALL define three sbt subprojects: root project (`.`), `benchmarks`, and `fuzzy-testing`.

#### Scenario: Root project exists
- **WHEN** user runs `sbt projects`
- **THEN** the root project (`*`) is listed as the default project

#### Scenario: Benchmarks subproject exists
- **WHEN** user runs `sbt projects`
- **THEN** the `benchmarks` subproject is listed

#### Scenario: Fuzzy-testing subproject exists
- **WHEN** user runs `sbt projects`
- **THEN** the `fuzzy-testing` subproject is listed

### Requirement: Apache Spark dependency configuration
The system SHALL include Apache Spark as a dependency with appropriate scoping for each subproject, while preserving existing dependency scopes and runtime behavior for `.` and `benchmarks`.

#### Scenario: Existing root dependency behavior is preserved
- **WHEN** user inspects root project dependencies before and after enabling the new subproject
- **THEN** Apache Spark scope and dependency behavior for the root project remain unchanged

#### Scenario: Existing benchmarks dependency behavior is preserved
- **WHEN** user inspects benchmarks project dependencies before and after enabling the new subproject
- **THEN** Apache Spark scope and dependency behavior for the `benchmarks` project remain unchanged

#### Scenario: Spark dependency in fuzzy-testing project
- **WHEN** user inspects fuzzy-testing project dependencies
- **THEN** Apache Spark is listed with a scope that supports running the fuzzy-testing CLI
