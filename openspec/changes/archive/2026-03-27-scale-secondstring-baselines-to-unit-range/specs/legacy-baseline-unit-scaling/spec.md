## ADDED Requirements

### Requirement: Deterministic metric-specific legacy scaling
The system SHALL apply a deterministic, metric-specific scaling policy that transforms each supported raw legacy SecondString score into a normalized value in `[0,1]` before parity analytics are computed.

#### Scenario: Supported metric uses documented scaling rule
- **WHEN** a legacy score is produced for a supported metric
- **THEN** the system applies the documented scaling rule for that metric and emits a normalized baseline value

#### Scenario: Repeated runs produce identical scaled values
- **WHEN** fuzzy-testing is executed multiple times with identical inputs and seed
- **THEN** scaled baseline values are identical across runs for every metric row

### Requirement: Length-aware normalization inputs
The system SHALL support length-aware scaling formulas for metrics that require input-length context and SHALL derive required length values from the same input pair being scored.

#### Scenario: Length-aware metric receives input lengths
- **WHEN** a metric scaling policy requires string length information
- **THEN** the scaling function is evaluated with lengths derived from the corresponding `input_left` and `input_right` values

### Requirement: Bounded scaled baseline output
The system SHALL clamp every computed scaled legacy baseline value to the closed interval `[0,1]`.

#### Scenario: Above-range scaled value is clamped
- **WHEN** a metric scaling formula computes a value greater than `1`
- **THEN** the emitted scaled baseline value is `1`

#### Scenario: Below-range scaled value is clamped
- **WHEN** a metric scaling formula computes a value less than `0`
- **THEN** the emitted scaled baseline value is `0`

### Requirement: Explicit invalid numeric handling
The system SHALL treat invalid numeric legacy outputs (`NaN`, positive infinity, negative infinity, or missing values) as `NULL` scaled baselines and SHALL not rely on implicit Spark defaults.

#### Scenario: Invalid numeric raw baseline becomes null scaled value
- **WHEN** raw legacy output is `NaN`, `Infinity`, `-Infinity`, or missing
- **THEN** the corresponding scaled baseline value is set to `NULL`

### Requirement: Empty-input behavior is explicit per metric
The system SHALL define deterministic scaling behavior for empty-input pairs for each supported metric, including whether the resulting scaled value is numeric or `NULL`.

#### Scenario: Empty inputs follow documented metric policy
- **WHEN** both inputs are empty strings for a supported metric
- **THEN** the emitted scaled baseline value follows that metric's documented empty-input scaling policy
