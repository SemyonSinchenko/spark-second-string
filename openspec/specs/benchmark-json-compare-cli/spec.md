# Benchmark JSON Compare CLI

## Purpose

Define requirements for a CLI that compares Spark-native and legacy-UDF JMH JSON benchmark outputs and reports deterministic algorithm-level deltas.

## Requirements

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

### Requirement: Primary output table schema
The comparison CLI SHALL output a primary table with fixed columns `algorithm | spark-native | UDF | diff`.

#### Scenario: Output columns and value formatting are deterministic
- **WHEN** the comparison table is rendered
- **THEN** each `spark-native` and `UDF` cell MUST include central value and error term
- **THEN** `diff` MUST be rendered as percent delta between UDF and spark-native central values

### Requirement: Native-flow mapping mode
The comparison CLI SHALL support explicit native-flow selection to choose the class-name mapping used for native benchmark rows.

#### Scenario: Native-flow mapping is selected deterministically
- **WHEN** the compare CLI is invoked with `--native-flow direct` or `--native-flow spark`
- **THEN** the CLI MUST select the corresponding hardcoded native class-name mapping for pair selection
- **THEN** unsupported native-flow values MUST fail fast with a validation error

### Requirement: Runtime-setting parity validation
The comparison CLI SHALL validate benchmark runtime settings for native and legacy rows before emitting paired comparisons.

#### Scenario: Settings mismatch fails comparison
- **WHEN** parsed rows contain warmup, measurement, fork, or mode values that differ from suite settings
- **THEN** the CLI MUST fail with an explicit settings-mismatch error

### Requirement: Supplemental deterministic reporting
The comparison CLI SHALL support deterministic supplemental reporting in addition to the primary algorithm table.

#### Scenario: Scenario-level and diagnostic sections are emitted deterministically
- **WHEN** comparable rows and diagnostics are available
- **THEN** the CLI MAY emit a deterministic scenario-level comparison table
- **THEN** unmapped benchmark classes and unsupported legacy mappings MUST be listed in deterministic diagnostic sections when present
