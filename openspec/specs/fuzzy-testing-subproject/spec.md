## Purpose

This specification defines the dedicated fuzzy-testing subproject and its deterministic CLI-driven parity analysis workflow.

## Requirements

### Requirement: Deterministic fuzzy-testing CLI entry point
The system SHALL provide a standalone fuzz-testing CLI entry point in the dedicated fuzzy-testing subproject, accepting `--seed`, `--rows`, and required `--out` arguments with defaults `42` and `10000` for `--seed` and `--rows`.

#### Scenario: CLI uses default arguments
- **WHEN** the fuzz-testing CLI is invoked with `--out` and without explicit `--seed` or `--rows`
- **THEN** the run uses `seed=42` and `rows=10000`

#### Scenario: CLI accepts explicit arguments
- **WHEN** the fuzz-testing CLI is invoked with `--seed`, `--rows`, and `--out`
- **THEN** the run uses exactly the provided values for data generation and reporting

#### Scenario: CLI requires output path
- **WHEN** the fuzz-testing CLI is invoked without `--out`
- **THEN** the CLI rejects invocation with argument validation failure

### Requirement: Reproducible synthetic dataset generation
The system SHALL generate seeded deterministic string pairs using a case-driven randomized construction model so that runs with identical `(seed, rows)` produce identical generated datasets and report values, while avoiding globally forced shared-prefix structure.

#### Scenario: Repeated run yields identical output
- **WHEN** the CLI is executed twice with the same `--seed` and `--rows`
- **THEN** both runs produce identical generated pairs and identical markdown report tables

#### Scenario: Seed change alters generated data
- **WHEN** the CLI is executed with different `--seed` values and the same `--rows`
- **THEN** the generated string pairs differ between runs

#### Scenario: Generation uses diverse case cohorts without universal prefixing
- **WHEN** synthetic pairs are generated for parity testing
- **THEN** pair relationships are produced by deterministic case builders for required cohorts and no fixed shared-prefix rule is applied to every pair

### Requirement: DataFrame-only evaluation pipeline
The system SHALL evaluate native metrics and legacy baseline metrics exclusively through Spark DataFrame transformations.

#### Scenario: No local collection scoring path
- **WHEN** the fuzz-testing workflow computes metric values
- **THEN** native and baseline scores are produced from Spark DataFrame operations rather than local row-by-row loops

#### Scenario: Legacy baseline uses Spark UDF wrappers
- **WHEN** baseline SecondString metrics are computed
- **THEN** each baseline score is produced through Spark UDF wrappers over DataFrame columns

### Requirement: Correlation reporting for parity analysis
The system SHALL compute and report both Pearson and Spearman correlations between native metric outputs and scaled legacy baseline outputs using Spark ML correlation APIs.

#### Scenario: Correlation table includes both methods
- **WHEN** a fuzz-testing report is generated
- **THEN** the markdown output includes Pearson and Spearman correlation values for each evaluated metric pair computed from native and scaled legacy baseline values

### Requirement: Delta band distribution reporting
The system SHALL compute absolute relative-delta band distributions from native-vs-scaled-legacy comparisons and report row counts and percentages for `+-5%`, `+-10%`, `+-30%`, and overflow `>30%` buckets.

#### Scenario: Delta band table includes required bands
- **WHEN** a fuzz-testing report is generated
- **THEN** the markdown output includes count and percentage columns for `+-5%`, `+-10%`, `+-30%`, and overflow `>30%` buckets

#### Scenario: Delta percentages are normalized to compared row count
- **WHEN** delta band statistics are computed for a run
- **THEN** each reported percentage is derived from the run's total number of rows included in native-vs-scaled-baseline comparison

### Requirement: Relative delta computation policy
The system SHALL compute relative delta using the symmetric denominator formula `abs(native-scaled_baseline)/max((abs(native)+abs(scaled_baseline))/2, 1e-9)`.

#### Scenario: Near-zero denominator handling is stable
- **WHEN** native and scaled baseline scores are both near zero
- **THEN** denominator flooring at `1e-9` prevents undefined or unstable relative-delta results

### Requirement: Report destination and top-level logs
The system SHALL write markdown report content to the `--out` file path and SHALL limit stdout to top-level execution logs.

#### Scenario: Markdown content is emitted to output file
- **WHEN** a fuzz-testing run completes
- **THEN** the markdown report is written to the path provided by `--out`

#### Scenario: Stdout remains log-oriented
- **WHEN** a fuzz-testing run is executing
- **THEN** stdout contains top-level run/log messages rather than full markdown report content

### Requirement: Optional per-metric CSV output export
The system SHALL support optional `--save-output <dir>` for exporting per-row comparison tables, SHALL keep this export disabled by default when the option is omitted, and SHALL include explicitly named raw and scaled legacy baseline columns when exports are written.

#### Scenario: Save-output omitted keeps csv export disabled
- **WHEN** the fuzz-testing CLI is invoked without `--save-output`
- **THEN** no per-metric CSV outputs are written and markdown output behavior remains unchanged

#### Scenario: Save-output writes one csv output per metric with explicit baseline semantics
- **WHEN** the fuzz-testing CLI is invoked with `--save-output <dir>`
- **THEN** it writes one per-metric Spark CSV output (using `coalesce(1)`) under the target directory with columns `input_left`, `input_right`, `native`, `second_string_raw`, `second_string_scaled`, and `relative_diff`

### Requirement: Aggregated summary table
The system SHALL include both per-metric sections and one aggregated cross-metric summary table in the markdown report, and SHALL report the count of rows excluded from parity analytics due to `NULL` scaled baselines.

#### Scenario: Report contains per-metric and aggregate views
- **WHEN** a report is generated
- **THEN** the output includes detailed per-metric sections and a single cross-metric aggregate summary table

#### Scenario: Null-excluded rows are reported
- **WHEN** parity analytics exclude rows with `NULL` scaled baseline values
- **THEN** the markdown report includes per-metric counts of excluded `NULL` rows

### Requirement: Unbounded row count input
The system SHALL NOT impose a soft or hard guardrail on `--rows` and SHALL accept any provided row count value.

#### Scenario: Large row counts are accepted
- **WHEN** the CLI is invoked with very large `--rows`
- **THEN** argument validation accepts the value without row-count guardrail rejection

### Requirement: Stable markdown report output without gating
The system SHALL output markdown reports with a stable table structure and SHALL NOT enforce pass/fail assertions or thresholds based on metric deltas.

#### Scenario: Report structure remains stable
- **WHEN** reports are generated for different inputs
- **THEN** section ordering and table header structure remain consistent across runs

#### Scenario: Delta variance does not fail execution
- **WHEN** computed delta or correlation values indicate low similarity
- **THEN** the CLI still completes normally and emits the report without threshold-based failure
