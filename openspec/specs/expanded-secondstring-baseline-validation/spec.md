## Purpose

Define the expected baseline validation behavior for comparing all eligible internal metrics against SecondString, including deterministic mapping, actionable reporting, and explicit exclusions.

## Requirements

### Requirement: Harness compares all baseline-eligible metrics against SecondString
The fuzzy harness SHALL execute baseline comparisons for every metric declared baseline-eligible in the project metric registry, including token metrics, `jaro`, `levenshtein`, `lcs`, and `affine_gap`.

#### Scenario: Run baseline suite with expanded coverage
- **WHEN** the baseline comparison suite is executed
- **THEN** it evaluates all baseline-eligible metrics instead of only `needleman_wunsch`, `smith_waterman`, `jaro_winkler`, and `monge_elkan`

### Requirement: Harness applies deterministic metric mapping and normalization
The harness MUST apply explicit per-metric mapping and normalization rules before comparing internal scores to SecondString outputs so that equivalent metrics are compared under consistent semantics.

#### Scenario: Compare metric with adapter requirements
- **WHEN** a metric requires aliasing, token pre-processing, or score normalization to match SecondString semantics
- **THEN** the harness applies the configured adapter rules before asserting parity

### Requirement: Baseline failures are metric-specific and actionable
The baseline suite SHALL report failures with metric identifier and failing scenario details so regressions can be diagnosed without additional instrumentation.

#### Scenario: Baseline mismatch occurs
- **WHEN** internal and SecondString outputs differ beyond configured tolerance for a metric case
- **THEN** test output identifies the metric, case, expected value, and actual value

### Requirement: Unsupported metrics are explicitly excluded with rationale
The harness MUST maintain an explicit record of metrics not compared to SecondString, including a reason for exclusion.

#### Scenario: Metric has no valid SecondString equivalent
- **WHEN** a metric cannot be compared to SecondString due to missing or incompatible reference behavior
- **THEN** the metric is marked excluded with a documented rationale and is not silently skipped
