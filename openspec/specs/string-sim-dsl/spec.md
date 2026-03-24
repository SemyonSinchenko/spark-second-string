# String Similarity DSL

## Purpose

Define the primary developer-facing API for string similarity expressions through Scala/Java DSL, with optional SQL registration via thin SparkSession extension.

## Requirements

### Requirement: DSL-first expression access
The system SHALL expose string similarity expressions as first-class Scala/Java DSL constructs.

#### Scenario: Primary consumer mode
- **WHEN** library and platform developers integrate metrics
- **THEN** they SHALL be able to construct metrics directly from Scala/Java code without SQL registration

### Requirement: Optional SQL registration extension
The system SHALL provide an optional thin SparkSession extension for SQL registration of available metrics.

#### Scenario: SQL registration as bonus path
- **WHEN** users need SQL access to already-implemented expressions
- **THEN** they SHALL be able to register functions via SparkSession extension
- **THEN** this registration layer SHALL remain thin and defer to existing DSL expression implementations

### Requirement: Scope boundary for this phase
The system SHALL keep advanced SQL ergonomics out of scope in this phase.

#### Scenario: Non-goal enforcement
- **WHEN** planning this phase
- **THEN** SQL-first API design, SQL-specific optimizations, and SQL-only feature surface SHALL be out of scope
