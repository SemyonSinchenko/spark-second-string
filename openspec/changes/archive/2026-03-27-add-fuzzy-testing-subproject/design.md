## Context

The repository currently includes benchmarking and compare utilities, but it does not provide a deterministic fuzz-testing workflow dedicated to native-metric parity analysis against the existing SecondString UDF baseline. The proposal introduces a new standalone sbt subproject and CLI that must generate reproducible random string-pair datasets, run all evaluations on Spark DataFrames, and emit markdown reports with stable structure. This phase is analysis-only: no pass/fail gating, assertions, or hard thresholds are allowed.

Primary constraints:
- Deterministic output for identical `--seed` and `--rows`
- Default args `--seed 42` and `--rows 10000`
- `--out` is required; markdown report is written to the provided file path while top-level progress logs are emitted to stdout
- Optional `--save-output <dir>` is disabled by default; when provided it writes one CSV output directory per metric via Spark `coalesce(1)` with columns `input_left`, `input_right`, `native`, `second_string`, and `relative_diff`
- Spark DataFrame-based evaluation path only
- SecondString metrics accessed through Spark UDF wrappers only
- Correlation outputs using Spark ML correlation APIs for Pearson and Spearman
- Delta band summaries for `+-5%`, `+-10%`, `+-30%`, and overflow `>30%` as counts and percentages
- Relative delta uses symmetric denominator `abs(native-baseline)/max((abs(native)+abs(baseline))/2, 1e-9)`
- Markdown report format must be stable for downstream consumption and review
- Report includes per-metric sections plus one aggregated cross-metric summary table
- No row-count guardrail or cap is enforced

## Goals / Non-Goals

**Goals:**
- Add an independent fuzz-testing sbt subproject with a runnable CLI entry point.
- Produce deterministic synthetic data and deterministic report layout for identical inputs.
- Compute native vs. legacy similarity comparisons at scale on Spark DataFrames.
- Report both correlation metrics and bounded delta-distribution statistics in markdown.
- Keep existing root, benchmark, and benchmark-tools behavior unchanged unless the new module is explicitly invoked.

**Non-Goals:**
- Enforcing quality gates, thresholds, or build failures from metric deltas.
- Replacing existing benchmark compare tooling.
- Introducing new metric formulas or changing score semantics.
- Performing optimization work beyond what is required for correctness and reproducibility.

## Decisions

1. Introduce a dedicated sbt subproject (for example, `fuzzy-testing`) rather than embedding logic into existing benchmark modules.
   - Rationale: keeps dependencies and runtime concerns isolated; avoids accidental behavior changes in existing workflows.
   - Alternatives considered:
     - Extend existing benchmark project: rejected due to coupling and increased risk of regressions.
     - Build as test-only suite: rejected because this is a standalone analysis CLI, not a test gate.

2. Use a seeded deterministic row generator that derives string pairs from a reproducible pseudo-random stream and row index.
   - Rationale: guarantees reproducibility for `(seed, rows)` and supports exact reruns when investigating regressions.
   - Alternatives considered:
     - Non-seeded randomness: rejected because outputs cannot be reproduced.
     - Precomputed fixture files: rejected due to maintenance burden and poor scalability for large row counts.

3. Keep all metric evaluation in Spark DataFrame pipelines, including baseline UDF execution.
   - Rationale: enforces parity with distributed execution behavior and satisfies requirement to avoid local collection-based scoring paths.
   - Alternatives considered:
     - Local Scala loops for baseline comparison: rejected for violating DataFrame-only requirement.
     - Mixed RDD/DataFrame approach: rejected to reduce execution-path variance and complexity.

4. Compute correlations with Spark ML correlation utilities and report both Pearson and Spearman values.
   - Rationale: uses standard Spark APIs and provides both linear and rank-based signal alignment.
   - Alternatives considered:
     - Custom correlation implementation: rejected due to correctness risk and unnecessary duplication.
     - Reporting only one correlation type: rejected because requirement explicitly calls for both.

5. Define delta bands as absolute relative-difference buckets (`<=5%`, `<=10%`, `<=30%`, and overflow `>30%`) and report count + percentage.
   - Rationale: provides simple quality visibility while preserving non-gating semantics.
   - Alternatives considered:
     - Raw percentile dumps only: rejected because less interpretable for quick review.
     - Pass/fail thresholding: rejected because explicitly out of scope for this phase.

6. Generate markdown through a small deterministic renderer with fixed section order, fixed table headers, and normalized numeric formatting.
   - Rationale: stable output supports diff-based review and automation that consumes report tables.
   - Alternatives considered:
     - Free-form logging: rejected due to unstable structure.
      - JSON-only output: rejected because proposal requires markdown tables.

7. Require `--out` for report output and emit only top-level execution logs to stdout.
   - Rationale: guarantees a durable artifact path while keeping stdout readable for run status.
   - Alternatives considered:
     - Stdout-only reports: rejected because it weakens reproducible artifact handling.
     - Optional `--out` with fallback stdout: rejected to avoid ambiguous output contracts.

8. Include both per-metric report sections and a cross-metric aggregate summary table.
   - Rationale: preserves metric-level detail and provides a compact roll-up view.
   - Alternatives considered:
     - Per-metric only: rejected because reviewers lose quick top-level summary.
     - Aggregate only: rejected because metric-level diagnostics are required.

9. Apply symmetric relative-delta denominator with epsilon floor (`1e-9`) and no explicit row-count guardrail.
   - Rationale: handles near-zero stability consistently and avoids imposing artificial execution caps.
   - Alternatives considered:
     - Baseline-only denominator: rejected due to asymmetry and instability around zero.
      - Hard/soft row caps: rejected per explicit requirement that `--rows` can be any value.

10. Support optional raw per-row CSV export through `--save-output <dir>` while keeping default behavior unchanged.
    - Rationale: allows deep diagnostics when needed without changing default report-oriented runs.
    - Alternatives considered:
      - Always-on CSV export: rejected due to unnecessary IO and artifact noise for standard runs.
      - Single combined CSV for all metrics: rejected because per-metric partitioning keeps outputs simpler and aligned with report sections.

## Risks / Trade-offs

- [Large row counts may increase runtime and memory pressure in CI or local machines] -> Mitigation: keep defaults reasonable, document tuning guidance, and avoid wide intermediate schemas.
- [UDF wrapping for legacy metrics can reduce Spark optimization opportunities] -> Mitigation: minimize repeated UDF calls, cache intermediate DataFrames only when beneficial, and keep projection columns narrow.
- [Relative-delta calculations can be unstable when denominator values are near zero] -> Mitigation: define explicit epsilon/zero-handling rules and document them in report notes.
- [Markdown stability can regress if formatting logic changes] -> Mitigation: centralize rendering and add output-structure checks in module tests.
- [Correlation computations may require vector assembly and additional transforms] -> Mitigation: isolate transformation helpers and validate schema contracts with focused tests.

## Migration Plan

1. Register the new subproject in sbt with required Spark and project dependencies.
2. Implement CLI argument parsing with defaults and validation.
3. Implement deterministic data generation and DataFrame construction.
4. Implement native and UDF baseline scoring pipeline.
5. Add correlation and delta-band aggregation logic.
6. Add deterministic markdown renderer and write-to-file/stdout behavior as required.
7. Add targeted tests for determinism, report schema stability, and stats correctness.
8. Validate that existing root and benchmark-related tasks run unchanged.

Rollback strategy: if integration causes instability, remove subproject registration and keep existing projects untouched; because the feature is additive and opt-in, rollback is low risk.

## Open Questions

- None.
