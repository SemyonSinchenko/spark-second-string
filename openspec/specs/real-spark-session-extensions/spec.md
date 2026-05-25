# Real Spark Session Extensions

## Purpose

Define and document Spark `SparkSessionExtensions` integration that auto-registers project SQL functions for Spark SQL and PySpark workloads while preserving the legacy JVM registration path.

## Requirements

### Requirement: Spark session extension registers SQL functions automatically
The system SHALL provide a Spark `SparkSessionExtensions` implementation that registers all supported SQL functions when the extension is configured through Spark session or cluster configuration.

#### Scenario: Functions are registered when extension is configured in Spark
- **WHEN** a Spark session starts with the project extension class configured in `spark.sql.extensions`
- **THEN** all project SQL functions are available for SQL queries without manual registration calls

### Requirement: Extension supports PySpark and Spark SQL entry points
The extension integration MUST work for users who create sessions from PySpark and for users who run Spark SQL workloads that rely on `spark.sql.extensions`.

#### Scenario: PySpark session resolves project SQL functions
- **WHEN** a PySpark user starts a session with the extension enabled and executes SQL using a project function
- **THEN** the query resolves and executes without requiring explicit JVM-side registration code

#### Scenario: Spark SQL workload resolves project SQL functions
- **WHEN** a Spark SQL workload runs with the extension enabled through cluster or Spark configuration
- **THEN** project SQL functions are resolved by the analyzer in that workload

### Requirement: Legacy extension path remains available
The system SHALL keep the existing extension/registration path available for advanced JVM users while adding the new Spark `SparkSessionExtensions` subclass.

#### Scenario: Existing JVM registration integration remains usable
- **WHEN** an advanced JVM user invokes the existing registration mechanism directly
- **THEN** function registration continues to work as before with no required migration
