## ADDED Requirements

### Requirement: Token benchmark dual-flow comparability with explicit legacy support boundaries
The token metric kernel capability SHALL define dual-flow benchmark obligations for Spark-native and legacy-UDF baselines where legacy equivalents exist, and SHALL require explicit handling for unsupported legacy mappings.

#### Scenario: Supported token metrics are compared across equivalent flows
- **WHEN** token metric benchmark suites include algorithms with defined legacy equivalents
- **THEN** native and legacy flows MUST run under identical JMH settings with aligned algorithm and scenario identifiers for deterministic matching

#### Scenario: Unsupported legacy mappings are handled explicitly
- **WHEN** a token metric has no supported legacy equivalent
- **THEN** the benchmark and comparison workflow MUST report the metric as unsupported for legacy matching
- **THEN** the workflow MUST NOT substitute approximate or alternate algorithms implicitly
