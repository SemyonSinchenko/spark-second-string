## ADDED Requirements

### Requirement: Build configuration with dynamic Scala version
The system SHALL support building with different Spark versions, automatically deriving the correct Scala version based on the Spark version provided.

#### Scenario: Build with Spark 4.x
- **WHEN** user sets `sparkVersion` to `4.0.2`
- **THEN** `scalaVersion` is automatically set to `2.13`

#### Scenario: Build with Spark 3.5.x
- **WHEN** user sets `sparkVersion` to `3.5.x`
- **THEN** `scalaVersion` is automatically set to `2.12`

### Requirement: Multi-project structure
The system SHALL define two sbt subprojects: root project (`.`) and `benchmarks`.

#### Scenario: Root project exists
- **WHEN** user runs `sbt projects`
- **THEN** the root project (`*`) is listed as the default project

#### Scenario: Benchmarks subproject exists
- **WHEN** user runs `sbt projects`
- **THEN** the `benchmarks` subproject is listed

### Requirement: Apache Spark dependency configuration
The system SHALL include Apache Spark as a dependency with appropriate scoping for each subproject.

#### Scenario: Spark dependency in root project
- **WHEN** user inspects root project dependencies
- **THEN** Apache Spark is listed with `provided` scope

#### Scenario: Spark dependency in benchmarks project
- **WHEN** user inspects benchmarks project dependencies
- **THEN** Apache Spark is listed with `runtime` scope

### Requirement: sbt version specification
The system SHALL specify sbt version in `project/build.properties` file.

#### Scenario: sbt version is set
- **WHEN** user reads `project/build.properties`
- **THEN** `sbt.version` is `1.11.0` or higher

### Requirement: sbt plugins configuration
The system SHALL include the following sbt plugins: `sbt-scalafmt`, `sbt-scalafix`, `sbt-ci-release`, and `sbt-jmh`.

#### Scenario: Formatting plugin available
- **WHEN** user runs `sbt scalafmtCheck`
- **THEN** the command executes without "plugin not found" error

#### Scenario: Scalafix plugin available
- **WHEN** user runs `sbt scalafix --help`
- **THEN** the command executes without "plugin not found" error

#### Scenario: CI release plugin available
- **WHEN** user runs `sbt ci-release` (dry-run or help)
- **THEN** the command is recognized by sbt

#### Scenario: JMH plugin available
- **WHEN** user runs `sbt jmh:run`
- **THEN** the command executes without "plugin not found" error

### Requirement: Testing dependencies
The system SHALL include ScalaTest as a testing dependency for the root project.

#### Scenario: ScalaTest available for testing
- **WHEN** user adds a test using ScalaTest DSL
- **THEN** the test compiles and runs successfully

### Requirement: Project metadata for publishing
The system SHALL configure project metadata including homepage, licenses, version scheme, SCM info, and developer information.

#### Scenario: Homepage is set
- **WHEN** user runs `sbt show homepage`
- **THEN** the output shows `https://github.com/SemyonSinchenko/spark-second-string`

#### Scenario: License is configured
- **WHEN** user runs `sbt show licenses`
- **THEN** the output shows Apache-2.0 license with URL

#### Scenario: Version scheme is set
- **WHEN** user runs `sbt show versionScheme`
- **THEN** the output shows `semver-spec`

#### Scenario: Developer info is configured
- **WHEN** user runs `sbt show developers`
- **THEN** the output includes developer with id `SemyonSinchenko`

### Requirement: Dynamic artifact naming
The system SHALL produce artifacts named with the pattern `spark-second-string-$sparkMinorVersion`.

#### Scenario: Artifact name for Spark 4.0
- **WHEN** `sparkVersion` is `4.0.2`
- **THEN** the artifact name includes `spark-second-string-4.0`

#### Scenario: Artifact name for Spark 3.5
- **WHEN** `sparkVersion` is `3.5.x`
- **THEN** the artifact name includes `spark-second-string-3.5`

### Requirement: Dynamic versioning from git tags
The system SHALL derive the project version from git tags using the `sbt-ci-release` plugin.

#### Scenario: Version from git tag
- **WHEN** a git tag exists (e.g., `v0.1.0`)
- **THEN** `sbt show version` reflects the tagged version

### Requirement: Hello-world source structure
The system SHALL include a minimal hello-world source file in the correct package structure.

#### Scenario: Source directory exists
- **WHEN** user checks `src/main/scala/io/github/semyonsinchenko/sparkss`
- **THEN** at least one Scala source file exists

#### Scenario: Hello-world compiles
- **WHEN** user runs `sbt compile`
- **THEN** compilation succeeds without errors

### Requirement: Hello-world benchmark structure
The system SHALL include a minimal hello-world benchmark in the benchmarks subproject.

#### Scenario: Benchmark source directory exists
- **WHEN** user checks `benchmarks/src/main/scala`
- **THEN** at least one benchmark Scala file exists

#### Scenario: Benchmark compiles
- **WHEN** user runs `sbt benchmarks/compile`
- **THEN** compilation succeeds without errors
