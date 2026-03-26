## ADDED Requirements

### Requirement: Matrix benchmark dual-flow comparability obligations
The matrix metric kernel capability SHALL define benchmark obligations that enable direct comparability between Spark-native Catalyst/codegen execution and legacy-UDF baseline execution.

#### Scenario: Comparable matrix benchmark settings are enforced
- **WHEN** matrix metric benchmark suites are executed for native and legacy flows
- **THEN** both flows MUST use identical JMH warmup, measurement, and fork settings
- **THEN** benchmark scenarios MUST use aligned algorithm and scenario identifiers so result rows can be matched without heuristics
