## Context

The codebase already has a reusable Catalyst expression contract (`StringSimExpression` + `TokenMetricExpression`) and one token metric (`Jaccard`).
This change adds two token-set metrics in the same architecture:
- Sorensen-Dice: `2 * |intersection| / (|A| + |B|)`
- Overlap Coefficient: `|intersection| / min(|A|, |B|)`

Constraints from proposal:
- Keep DSL-first API and optional SQL registration.
- Preserve null propagation, whitespace token-boundary behavior, duplicate-token set semantics, and interpreted/codegen parity.
- Include algorithm tests, Catalyst integration tests, and benchmark coverage.

## Goals / Non-Goals

**Goals:**
- Add two token metrics under `expressions/token` using the existing token metric base class.
- Expose both metrics via `StringSimilarityFunctions` (`Column, Column` and `String, String` overloads).
- Register both SQL functions in SparkSession extension with the same validation behavior as `jaccard`.
- Add parity and correctness tests mirroring the existing Jaccard coverage style.
- Add JMH benchmarks for both metrics and compare with Jaccard baseline.

**Non-Goals:**
- Adding matrix/edit-distance metrics (Levenshtein, Damerau, Jaro, etc.).
- Changing tokenization policy beyond current whitespace boundary handling.
- Introducing weighted tokens, locale-aware normalization, or configurable tokenizers.
- Redesigning SQL ergonomics beyond adding two new thin registrations.

## Decisions

1. Reuse existing token expression architecture
   - Decision: Implement `SorensenDice` and `OverlapCoefficient` as `case class` expressions extending `TokenMetricExpression`.
   - Rationale: avoids duplicating Catalyst lifecycle logic and keeps new metrics consistent with Jaccard behavior.
   - Alternatives considered:
     - Add metric-specific base classes: rejected due to boilerplate and divergence risk.
     - Put all algorithms in one expression with mode flags: rejected because it weakens type-level clarity and complicates testing.

2. Keep shared tokenization semantics aligned with Jaccard
   - Decision: use the same whitespace token splitting and set deduplication pattern as Jaccard for both new metrics.
   - Rationale: maintains predictable cross-metric behavior and keeps edge-case expectations uniform.
   - Alternatives considered:
     - Metric-specific tokenization rules: rejected due to inconsistent user experience.
     - Frequency-based bag semantics: rejected because this change is explicitly token-set based.

3. Define explicit empty-input behavior to avoid divide-by-zero ambiguity
   - Decision: both empty -> `1.0`; one empty and one non-empty -> `0.0` for both metrics.
   - Rationale: matches proposal constraints and keeps behavior parallel to Jaccard.
   - Alternatives considered:
     - Return `0.0` for both empty: rejected because identical empty sets should score as full similarity in this library.
     - Return null for empty strings: rejected because null propagation is reserved for null inputs, not empty values.

4. Preserve codegen pathway via static module method invocation
   - Decision: implement metric kernels in companion objects and call them from generated code as module static invocations, same style as Jaccard.
   - Rationale: guarantees codegen support with minimal complexity and proven parity pattern.
   - Alternatives considered:
     - Inline full tokenization logic in generated Java snippets: rejected for readability and maintenance cost.
     - Fallback/interpreted only path: rejected by parity and performance expectations.

5. Extend DSL and SQL registration without API breakage
   - Decision: add new functions to `StringSimilarityFunctions` and new registration methods in `StringSimilaritySparkSessionExtensions`, leaving existing `jaccard` behavior untouched.
   - Rationale: additive, backward-compatible API evolution.
   - Alternatives considered:
     - Replace `registerStringSimilarityFunctions` with selective registration only: rejected to preserve existing convenience method behavior.

6. Expand tests by cloning proven structure from Jaccard suites
   - Decision: add per-metric algorithm suites and extend expression-level suite with new metric cases for null handling, nested expressions, and codegen parity.
   - Rationale: reuses established quality gates and reduces accidental gaps.
   - Alternatives considered:
     - Only unit-test kernel methods: rejected because Catalyst integration regressions would be missed.

7. Add benchmark scenarios for token metrics comparison
   - Decision: benchmark Jaccard, Sorensen-Dice, and Overlap Coefficient on representative token distributions (high overlap, low overlap, subset-like).
   - Rationale: validates no major regressions and highlights metric runtime profile differences.
   - Alternatives considered:
     - Benchmark only new metrics: rejected because baseline comparison is required for meaningful interpretation.

## Risks / Trade-offs

- [Code duplication between three token metrics] -> Mitigation: keep shared tokenization/intersection helpers small and local, and refactor only if duplication becomes error-prone.
- [Behavior drift between interpreted and generated paths] -> Mitigation: parity tests for each metric with whole-stage codegen on/off.
- [Performance regressions from extra object allocations] -> Mitigation: follow allocation-light loops/hash-set usage style from Jaccard and validate with JMH.
- [Ambiguous user expectations for empty string semantics] -> Mitigation: encode explicit empty-edge behavior in specs and tests.

## Migration Plan

1. Add new token metric expression classes and companion kernels.
2. Extend DSL constructors and SQL registration.
3. Add algorithm-level tests for both metrics.
4. Add/extend Catalyst-level integration and codegen parity tests.
5. Add benchmark coverage and run baseline comparisons.
6. Validate full test suite and benchmarks before implementation merge.

Rollback strategy:
- Revert additive metric files and registration entries while leaving existing Jaccard path unchanged.

## Open Questions

- Should SQL registration include aliases (for example `dice` for `sorensen_dice`) now, or remain one name per metric in this change?
- Should benchmark outputs be checked into version control, or only benchmark harness code and scenarios?
