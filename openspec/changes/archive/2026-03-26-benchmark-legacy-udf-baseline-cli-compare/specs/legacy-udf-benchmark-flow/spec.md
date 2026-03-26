## ADDED Requirements

### Requirement: Legacy UDF benchmark execution flow
The benchmark suite SHALL provide a legacy execution flow that runs selected algorithms through legacy Java SecondString wrapped as Spark UDF calls.

#### Scenario: Legacy flow runs selected algorithms through UDF wrappers
- **WHEN** the legacy benchmark flow is executed
- **THEN** each supported algorithm SHALL be evaluated via Spark UDF invocation of legacy Java SecondString logic
- **THEN** the benchmark SHALL emit JMH JSON output for the legacy flow

### Requirement: Warmup and trial parity with native flow
The legacy UDF benchmark flow SHALL use the same JMH warmup, measurement, and fork settings as the Spark-native benchmark flow.

#### Scenario: Configuration parity is enforced
- **WHEN** native and legacy benchmark flows are configured for a suite run
- **THEN** warmup iterations, measurement iterations, fork count, and timing mode MUST be identical across both flows

### Requirement: No timed-method input I/O in legacy flow
Legacy UDF benchmark methods SHALL avoid file and network input I/O during timed measurements.

#### Scenario: Input data prepared before measurement
- **WHEN** legacy benchmark setup is initialized
- **THEN** test DataFrames MUST be created during setup, cached in memory, and materialized before timed benchmark methods begin
- **THEN** timed benchmark methods MUST read from prepared in-memory data only
