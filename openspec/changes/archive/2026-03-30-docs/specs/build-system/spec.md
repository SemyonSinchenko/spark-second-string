## MODIFIED Requirements

### Requirement: Multi-project structure
The system SHALL define four sbt subprojects: root project (`.`), `benchmarks`, `fuzzy-testing`, and `docs`.

#### Scenario: Root project exists
- **WHEN** user runs `sbt projects`
- **THEN** the root project (`*`) is listed as the default project

#### Scenario: Benchmarks subproject exists
- **WHEN** user runs `sbt projects`
- **THEN** the `benchmarks` subproject is listed

#### Scenario: Fuzzy-testing subproject exists
- **WHEN** user runs `sbt projects`
- **THEN** the `fuzzy-testing` subproject is listed

#### Scenario: Docs subproject exists
- **WHEN** user runs `sbt projects`
- **THEN** the `docs` subproject is listed

## ADDED Requirements

### Requirement: Docs build enforces prerequisite data
The docs build workflow MUST require benchmark and fuzzy-testing outputs to exist before generating documentation pages that consume parsed variables.

#### Scenario: Docs build fails when benchmark outputs are missing
- **WHEN** user runs docs generation without required benchmark outputs
- **THEN** the build fails with an explicit message describing missing benchmark prerequisites

#### Scenario: Docs build fails when fuzzy-testing outputs are missing
- **WHEN** user runs docs generation without required fuzzy-testing outputs
- **THEN** the build fails with an explicit message describing missing fuzzy-testing prerequisites

#### Scenario: Docs build succeeds when prerequisites are present
- **WHEN** user runs benchmark and fuzzy-testing flows and then runs docs generation
- **THEN** docs generation succeeds and renders pages that depend on parsed variables

### Requirement: Docs prerequisite behavior is documented
The project MUST document docs build prerequisites and expected failure behavior in user-facing documentation.

#### Scenario: Users can find prerequisite guidance
- **WHEN** a user reads setup guidance for documentation generation
- **THEN** required pre-run commands or artifacts are explicitly listed with failure expectations
