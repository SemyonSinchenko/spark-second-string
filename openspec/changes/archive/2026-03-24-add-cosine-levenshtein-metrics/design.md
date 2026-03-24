## Context

The codebase already provides a unified Catalyst expression contract (`StringSimExpression`) with family-specific hooks for token metrics (`TokenMetricExpression`) and matrix metrics (`MatrixMetricExpression`). Three token-set metrics are implemented (`Jaccard`, `SorensenDice`, `OverlapCoefficient`) and exposed through DSL helpers plus optional SQL registration. Matrix-family support exists only as an abstract extension point and has no concrete metric implementation yet.

This change introduces two metrics with different algorithm families:
- `cosine` as token-set similarity aligned with existing tokenization semantics
- `levenshtein` as normalized matrix-based similarity aligned with null/codegen parity expectations

The proposal also requires a reusable matrix helper abstraction to reduce duplication when adding future matrix metrics.

## Goals / Non-Goals

**Goals:**
- Add a token-set Cosine metric that follows current token-set boundary and deduplication rules.
- Add a normalized Levenshtein matrix metric as the first concrete `MatrixMetricExpression` implementation.
- Introduce a minimal matrix helper layer for shared matrix boundary logic and parity-safe reuse.
- Preserve DSL-first usage and optional SQL registration by adding new functions without changing existing ones.
- Expand tests and benchmarks to cover algorithm correctness, Catalyst integration, and interpreted/codegen parity for both new metrics.

**Non-Goals:**
- Adding configurable tokenization, weighted token frequency, locale-aware normalization, or case-folding policy changes.
- Changing behavior of existing metrics (`jaccard`, `sorensen_dice`, `overlap_coefficient`).
- Introducing SQL-first APIs, SQL-only features, or planner-level optimizations.
- Implementing additional matrix metrics (for example Damerau-Levenshtein, Jaro, Jaro-Winkler) in this change.

## Decisions

1. Implement Cosine as token-set Cosine, not token-frequency vector Cosine
   - Decision: compute `|A∩B| / sqrt(|A| * |B|)` over deduplicated token sets using the same whitespace token boundaries as existing token metrics.
   - Rationale: keeps cross-token-metric semantics consistent (set-based, not bag-based), minimizes behavioral surprises, and reuses current contract assumptions and test patterns.
   - Alternatives considered:
     - Token-frequency Cosine (bag semantics): rejected because it changes semantic model relative to existing token metrics and requires new tokenizer/counting rules.
     - Character n-gram Cosine: rejected for this phase due to tokenizer scope expansion.

2. Define normalized Levenshtein similarity with explicit denominator policy
   - Decision: compute distance with classic dynamic programming and return normalized similarity as `1.0 - (distance / max(len(left), len(right)))`, with explicit empty-string handling (`"", "" -> 1.0`; one empty -> 0.0).
   - Rationale: normalization is bounded [0,1], deterministic, and comparable to existing metric outputs.
   - Alternatives considered:
     - Return raw distance: rejected because the expression contract and existing metrics are similarity-oriented in [0,1].
     - Normalize by average or sum of lengths: rejected to avoid less intuitive behavior and cross-input inconsistency.

3. Add a shared matrix helper abstraction for boundary and kernel reuse
   - Decision: introduce a small matrix helper component that centralizes repeated matrix concerns (string-length boundary checks, row-buffer setup conventions, and shared normalization guard rails), while keeping metric-specific recurrence logic in the metric companion.
   - Rationale: prevents copy-paste when adding future matrix metrics and enforces consistent edge-case behavior.
   - Alternatives considered:
     - Keep all matrix logic inline in each metric: rejected due to duplication and drift risk.
     - Build a large generic matrix framework now: rejected as over-engineering before multiple matrix metrics are present.

4. Preserve interpreted/codegen parity through shared companion invocation pattern
   - Decision: use the established pattern where expression codegen calls companion-object static kernel methods for both Cosine and Levenshtein.
   - Rationale: proven approach in current token metrics, minimizes codegen complexity, and supports deterministic parity testing.
   - Alternatives considered:
     - Inline full algorithm logic in generated snippets: rejected due to maintenance cost and higher defect risk.

5. Keep additive API integration and naming stability
   - Decision: add `cosine` and `levenshtein` to `StringSimilarityFunctions` (Column and String overloads) and SQL registration extension (`cosine`, `levenshtein`) without altering existing function names or behavior.
   - Rationale: backward compatibility and predictable DSL/SQL expansion.
   - Alternatives considered:
     - Rename existing functions to unify naming style: rejected as unnecessary breaking churn.

6. Extend benchmark strategy to cover both token and matrix families
   - Decision: add cosine to existing token benchmark comparisons and add a matrix benchmark suite for normalized Levenshtein with representative short/medium/long and typo-heavy scenarios.
   - Rationale: captures family-specific runtime characteristics and validates no major regressions.
   - Alternatives considered:
     - Benchmark token metrics only: rejected because matrix implementation is new and needs baseline visibility.

## Risks / Trade-offs

- [Matrix normalization ambiguity can cause inconsistent expectations] -> Mitigation: codify formula and edge-case tables in specs and algorithm tests.
- [Levenshtein runtime cost grows with string lengths] -> Mitigation: use rolling-row DP buffers and include long-input benchmark scenarios.
- [Codegen/interpreted divergence for matrix paths] -> Mitigation: add parity tests with whole-stage codegen enabled/disabled for Levenshtein.
- [Helper abstraction may be too narrow or too broad] -> Mitigation: keep helper minimal and boundary-focused; defer generalization until a second matrix metric needs it.
- [Token Cosine semantics may be confused with frequency-based cosine] -> Mitigation: explicitly specify token-set semantics and non-goals in specs.

## Migration Plan

1. Add shared matrix helper abstraction with explicit boundary conventions.
2. Implement `Cosine` token metric using existing token-set semantics.
3. Implement normalized `Levenshtein` matrix metric using matrix helper primitives.
4. Extend DSL and optional SQL registration with additive function entries.
5. Add algorithm-level tests for both metrics plus matrix helper edge behavior.
6. Extend Catalyst-level parity and null-propagation integration tests.
7. Extend/add benchmark suites for token and matrix metric scenarios.

Rollback strategy:
- Revert additive files and registrations for `cosine`, `levenshtein`, and matrix helper components while leaving existing metrics unchanged.

## Open Questions

- Should SQL exposure include aliases (for example `levenshtein_similarity`) now, or remain one canonical name per metric in this change?
- Do we need explicit length guardrails in specs for benchmark coverage classes, or keep benchmark datasets implementation-defined?
