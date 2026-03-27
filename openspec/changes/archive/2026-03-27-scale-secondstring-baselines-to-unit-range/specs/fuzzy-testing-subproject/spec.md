## MODIFIED Requirements

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
