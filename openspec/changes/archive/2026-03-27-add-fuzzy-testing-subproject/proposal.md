## Why

Need a deterministic, reproducible fuzz-testing flow for native metrics that computes similarity-to-legacy quality signals at scale without introducing pass/fail gating in this phase.

## What Changes

Add a new sbt subproject with a standalone CLI entry point that generates seeded random string pairs, evaluates native metrics and SecondString UDF baselines on a Spark DataFrame, computes correlation and delta-distribution statistics, and writes structured markdown tables.

## Capabilities

### New Capabilities

- `fuzzy-testing-subproject`: Add an independent sbt project and main entry point with CLI args `--seed`, `--rows`, and `--out` (defaults: `42`, `10000`, with `--out` required), plus optional `--save-output <dir>` (default off) that writes per-metric Spark CSV outputs using `coalesce(1)` with columns `input_left`, `input_right`, `native`, `second_string`, and `relative_diff`; deterministic generation for identical `(seed, rows)`, Spark DataFrame-based evaluation only, SecondString wrapped via Spark UDFs only, correlation outputs for both `pearson` and `spearman` via Spark ML correlation API, delta distribution outputs as row counts and percentages for `+-5%`, `+-10%`, `+-30%`, and overflow `>30%` buckets, relative-delta computation using symmetric denominator `abs(native-baseline)/max((abs(native)+abs(baseline))/2, 1e-9)`, markdown report output written to `--out` with stable table structure plus top-level progress logs to stdout, per-metric report sections plus one aggregated cross-metric summary table, and no row-count guardrail or cap; process must never fail due to metric deltas and must not include assertions or hard thresholds in this phase.

### Modified Capabilities

- `build-system`: Register the new fuzz-testing subproject in sbt with its own dependency set and runnable main while keeping existing root, benchmarks, and benchmark-tools behavior unchanged.

## Impact

- Adds a new offline analysis workflow for native-vs-legacy similarity quality visibility.
- Adds CI/runtime surface only if explicitly invoked; no default benchmark/test behavior changes.
- Out of scope: implementation-level optimizations, pass/fail policy, CI gate wiring, replacement of existing benchmark compare CLI, and introducing new metric formulas or scoring semantics.
