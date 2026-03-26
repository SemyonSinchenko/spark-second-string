## ADDED Requirements

### Requirement: Compare two JMH JSON result sets
The comparison CLI SHALL ingest one Spark-native JMH JSON file and one legacy-UDF JMH JSON file to generate an algorithm-level comparison report.

#### Scenario: CLI loads both benchmark result inputs
- **WHEN** the compare CLI is invoked with native and legacy result file paths
- **THEN** it MUST parse both JMH JSON documents and load benchmark entries required for comparison

### Requirement: Hardcoded algorithm mapping by benchmark class
The comparison CLI SHALL match native and legacy results using a hardcoded mapping of JMH benchmark class names to shared algorithm identifiers.

#### Scenario: Class-name mapping drives pair selection
- **WHEN** entries from both JMH JSON files are processed
- **THEN** the CLI MUST pair rows only through the configured benchmark-class mapping
- **THEN** unmapped class names MUST be excluded from paired comparisons

### Requirement: Fixed output table schema
The comparison CLI SHALL output a table with fixed columns `algorithm | spark-native | UDF | diff`.

#### Scenario: Output columns and value formatting are deterministic
- **WHEN** the comparison table is rendered
- **THEN** each `spark-native` and `UDF` cell MUST include central value and error term
- **THEN** `diff` MUST be rendered as percent delta between UDF and spark-native central values
