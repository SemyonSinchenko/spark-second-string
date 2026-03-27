## 1. Legacy scaling policy foundation

- [x] 1.1 Implement a metric-to-scaler registry that maps each supported SecondString metric to a deterministic scaling formula.
- [x] 1.2 Implement shared scaling helpers that convert `NaN`/`Infinity`/missing raw values to `NULL` and clamp numeric outputs to `[0,1]`.
- [x] 1.3 Implement per-metric empty-input handling so `""/""` cases return the documented numeric value or `NULL` consistently.

## 2. Baseline pipeline integration

- [x] 2.1 Update the fuzz-testing baseline pipeline to compute and retain both `second_string_raw` and `second_string_scaled` columns for each metric run.
- [x] 2.2 Wire length-aware scalers to use `input_left` and `input_right` lengths from the same scored row.
- [x] 2.3 Exclude rows with `NULL` scaled baselines from parity analytics while tracking excluded-row counts per metric.

## 3. Parity analytics updates

- [x] 3.1 Update relative-delta computation to `abs(native-scaled_baseline)/max((abs(native)+abs(scaled_baseline))/2, 1e-9)`.
- [x] 3.2 Compute and report `+-5%`, `+-10%`, `+-30%`, and `>30%` delta buckets with percentages normalized by compared-row totals.
- [x] 3.3 Compute Pearson and Spearman correlations from native-vs-scaled baseline pairs using Spark ML correlation APIs.

## 4. Report and export behavior

- [x] 4.1 Update markdown reporting to include per-metric sections, one cross-metric aggregate summary table, and per-metric `NULL` exclusion counts.
- [x] 4.2 Add optional `--save-output <dir>` behavior that remains disabled by default when the flag is omitted.
- [x] 4.3 When `--save-output` is enabled, write one per-metric CSV output (coalesced to one file) with `input_left`, `input_right`, `native`, `second_string_raw`, `second_string_scaled`, and `relative_diff`.

## 5. Verification and migration safety

- [x] 5.1 Add metric-level tests covering deterministic scaling, length-aware formulas, invalid numeric handling, clamp bounds, and empty-input policies.
- [x] 5.2 Add integration coverage for seeded reproducibility and parity analytics computed from scaled baselines only.
- [x] 5.3 Update change-facing documentation to clarify raw-vs-scaled baseline semantics and rollout expectations for report consumers.
