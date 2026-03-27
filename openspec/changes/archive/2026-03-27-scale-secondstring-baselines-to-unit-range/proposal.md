## Why

Native Spark metrics are normalized to `[0,1]`, while legacy SecondString baselines mix raw and normalized scales (`affine_gap` large positive values, `needleman_wunsch` negative values, `smith_waterman` positive unbounded values, `jaro_winkler` already normalized). This makes relative-delta bands and correlations reflect scale mismatch instead of behavior mismatch.

## What Changes

Define and adopt a deterministic baseline scaling policy that maps every legacy SecondString score to `[0,1]` before parity analytics are computed.

The policy is metric-specific and length-aware where required, and MUST explicitly define behavior for empty inputs, `NaN`/`Infinity`, and out-of-range values.

Reports MUST compare native scores against scaled legacy scores for correlation and delta distributions so numbers are interpretable across metrics.

Out of scope: changing native metric algorithms, changing legacy UDF implementations, introducing pass/fail gating, or asserting strict parity thresholds.

## Capabilities

### New Capabilities

- `legacy-baseline-unit-scaling`: Fuzzy-testing can normalize legacy SecondString outputs to `[0,1]` per metric with deterministic, documented rules; every transformed value is clamped to `[0,1]`; undefined numeric outputs are handled by explicit fallback behavior instead of implicit Spark defaults.

### Modified Capabilities

- `fuzzy-testing-subproject`: Metric parity analytics are computed on native-vs-scaled-baseline values rather than native-vs-raw-baseline values, while preserving seeded determinism, report structure, and non-gating behavior.

## Impact

Parity analysis becomes scale-consistent and comparable across all supported metrics, reducing false large deltas caused only by incompatible score domains.

Existing CSV/report consumers that assumed raw legacy scales may require a documented migration path or explicit raw-vs-scaled field naming to avoid interpretation errors.

## Report Consumer Rollout Notes

- Per-row comparison exports now carry both `second_string_raw` and `second_string_scaled`; parity analytics are computed from `second_string_scaled` only.
- Rows with `NULL` scaled baselines are excluded from delta/correlation calculations and reported as per-metric exclusion counts in markdown output.
- Existing consumers should migrate any interpretation logic that previously treated legacy baseline columns as raw-only values.
