## Context

Spark-native similarity metrics in this repository already emit normalized values in `[0,1]`. Legacy SecondString baselines do not share a common range (`affine_gap`, `needleman_wunsch`, and `smith_waterman` each use different raw domains), which distorts delta and correlation analytics during parity runs. The proposal requires a deterministic baseline-scaling layer that runs before analytics and preserves seeded reproducibility and existing report shape.

Constraints:
- No changes to native Spark metric implementations.
- No changes to legacy SecondString UDF algorithms.
- No pass/fail parity gates introduced.
- Undefined numeric outputs (`NaN`, `Infinity`) and empty-input behavior must be explicit.

Primary stakeholders are maintainers of fuzzy-testing parity reports and downstream consumers who currently interpret baseline-related report columns.

## Goals / Non-Goals

**Goals:**
- Introduce one canonical scaling policy that maps all legacy baseline outputs to `[0,1]`.
- Keep scaling deterministic and metric-aware, including any length-aware normalization where required by metric behavior.
- Ensure parity analytics (delta distribution and correlation) compare native scores against scaled legacy baselines.
- Preserve current seeded test determinism and non-gating parity workflow.
- Make output semantics clear enough for report consumers to migrate safely.

**Non-Goals:**
- Rewriting or retuning native similarity metrics.
- Replacing legacy baseline implementations.
- Defining strict acceptance thresholds for parity quality.
- Expanding fuzzy-testing scope beyond baseline scaling and analytics alignment.

## Decisions

1. Add a dedicated legacy-scaling stage between raw baseline computation and analytics.
   - Decision: Compute raw legacy baseline first, then pass through a metric-specific scaler that returns a normalized value in `[0,1]`.
   - Rationale: Keeps legacy computation and normalization concerns separated, easier to test independently, and avoids changing baseline generators.
   - Alternatives considered:
     - Inline scaling inside each legacy UDF wrapper: rejected because it spreads policy logic across wrappers and complicates reuse.
     - Post-hoc scaling only in report rendering: rejected because intermediate analytics would still run on mixed scales.

2. Use a registry-driven scaling policy keyed by metric identifier.
   - Decision: Maintain a central mapping from metric name to scaling function and required auxiliary inputs (for example, string lengths for length-aware metrics).
   - Rationale: Enforces explicit policy for every supported metric and prevents silent fallback to raw values.
   - Alternatives considered:
     - Generic min-max normalization across observed dataset: rejected because it is data-dependent, non-deterministic across datasets, and can hide outliers.
     - Hard-coded branching in analytics code: rejected because it couples analytics with metric policy and makes extension error-prone.

3. Standardize exceptional-value handling before clamp.
   - Decision: Treat `NaN`, `+/-Infinity`, or missing numeric outputs as explicit fallback values (policy-defined per metric or global default), then clamp final scaled values into `[0,1]`.
   - Rationale: Avoids Spark implicit casting/default behaviors and yields deterministic output across runtimes.
   - Alternatives considered:
     - Dropping rows with invalid values: rejected because it biases correlations and removes visibility into problematic cases.
     - Leaving invalid values untouched: rejected because downstream analytics break or become inconsistent.

4. Preserve backward interpretability by surfacing scaled semantics in outputs.
   - Decision: Keep current report structure but ensure fields used in parity calculations reference scaled baselines and are clearly named/documented to distinguish from any raw baseline fields.
   - Rationale: Reduces consumer confusion while minimizing disruption to existing pipeline shape.
   - Alternatives considered:
     - Full schema redesign: rejected for scope and migration cost.
     - Silent replacement of raw columns without naming/documentation updates: rejected due to high interpretation risk.

## Risks / Trade-offs

- [Metric scaling formula is incorrectly specified for one algorithm] → Mitigation: add metric-level golden tests with representative samples (including edge lengths and known raw outputs) and cross-check against documented policy.
- [Fallback behavior for invalid numeric values masks upstream defects] → Mitigation: emit counters/log fields for fallback usage and include fallback frequency in parity summary output.
- [Report consumers misread changed baseline semantics] → Mitigation: document raw-vs-scaled meaning in change notes and keep explicit column naming.
- [Length-aware scaling adds compute overhead] → Mitigation: reuse already-available input lengths where possible and keep scaling functions pure/simple to minimize per-row cost.

## Migration Plan

1. Implement the scaling registry and metric-specific scaler functions in the fuzzy-testing baseline pipeline.
2. Wire parity analytics to consume scaled legacy values by default.
3. Update report metadata/column naming documentation to clarify scaled semantics.
4. Validate with deterministic seeded runs across all supported metrics.
5. Rollout strategy: ship as non-gating change, compare historical and new reports for sanity, and monitor fallback counters.
6. Rollback strategy: toggle analytics back to raw baseline input path (or revert change) while leaving native/legacy algorithm code untouched.

## Open Questions (Resolved)

- Should we retain both raw and scaled baseline columns long-term, or only during migration? <- yes, add a new columns to the DF, save only if CSV save is requested
- For each metric, what exact fallback constant should be used for invalid numeric outputs when policy is not inherently defined by formula? <- it should be a NULL: we need a formula that works on all the cases; NULL should be an error in formula;; NULL-rows are excluded from comparison (but reported in MD-report as total amount of NULL-rows per metric for further analysis)
- Do downstream dashboards require explicit version tagging to indicate the switch from raw to scaled baseline semantics? <- NO, it is a new feature, so no versions for now
