## Context

The codebase already supports a unified Catalyst expression contract for string similarity with two metric families: token-based (`TokenMetricExpression`) and matrix-based (`MatrixMetricExpression`). Existing token metrics (`jaccard`, `sorensen_dice`, `overlap_coefficient`, `cosine`) duplicate tokenization and set-intersection logic in each metric file. Existing matrix coverage includes normalized Levenshtein plus a shared matrix helper for boundary and normalization behavior.

This change introduces one token metric (`braun_blanquet`) and one matrix metric (`lcs_similarity`) while refactoring token-kernel duplication into a shared helper. The design must preserve DSL-first usage, optional SQL exposure, null propagation, deterministic empty-input behavior, and interpreted/codegen parity.

## Goals / Non-Goals

**Goals:**
- Add `braun_blanquet` with deterministic token-set semantics and output bounded to `[0.0, 1.0]`.
- Add `lcs_similarity` with normalized LCS score `LCS / max(len(left), len(right))` bounded to `[0.0, 1.0]`.
- Introduce shared token-kernel helpers to centralize tokenization/intersection/empty-input conventions across token metrics.
- Extend DSL and optional SQL registration without changing behavior or names of existing metrics.
- Enforce interpreted/codegen parity for both new metrics via the existing companion-method invocation pattern.

**Non-Goals:**
- No configurable tokenization, weighted token frequencies, or locale/case normalization changes.
- No SQL-first redesign, SQL aliases, or planner-level optimizations.
- No additional new metrics beyond `braun_blanquet` and `lcs_similarity`.
- No behavior changes for existing metric outputs.

## Decisions

1. Introduce `TokenMetricKernelHelper` for shared token primitives
   - Decision: add a token helper abstraction for whitespace tokenization to unique-token sets, intersection counting, and shared boundary checks used by token metrics.
   - Rationale: removes copy-paste logic from four existing token metrics and prevents semantic drift as metric count grows.
   - Alternatives considered:
     - Keep duplication in each metric file: rejected due to maintenance and drift risk.
     - Introduce a generic pluggable tokenizer framework: rejected as out of scope for this change.

2. Implement Braun-Blanquet with existing token-set policy
   - Decision: compute `|A∩B| / max(|A|, |B|)` over deduplicated whitespace tokens; preserve established empty-input semantics.
   - Rationale: aligns with current token metric family semantics and adds a subset-sensitive similarity signal with minimal conceptual overhead.
   - Alternatives considered:
     - Bag-of-words or weighted variants: rejected because current family is set-based.
     - Character n-gram variant: rejected because it changes tokenizer semantics.

3. Implement LCS as normalized matrix similarity
   - Decision: compute LCS length via dynamic programming and normalize as `lcsLen / max(len(left), len(right))`, with explicit boundary behavior (`"", "" -> 1.0`; one empty -> `0.0`).
   - Rationale: provides sequence-order awareness while retaining the common `[0,1]` similarity contract expected by the API.
   - Alternatives considered:
     - Raw LCS length return: rejected because it violates normalized similarity contract.
     - Normalize by min length: rejected because subset cases would saturate to `1.0` too easily and reduce comparability.

4. Keep interpreted/codegen parity through companion kernel calls
   - Decision: keep codegen path delegating to companion-object static similarity methods for both new metrics.
   - Rationale: matches current proven pattern and minimizes divergence risk between interpreted and generated execution.
   - Alternatives considered:
     - Inline algorithm logic in generated code: rejected due to complexity and higher defect surface.

5. Keep API additions additive and naming-stable
   - Decision: add new DSL and SQL registrations for `braun_blanquet` and `lcs_similarity` only; do not rename existing functions.
   - Rationale: avoids breaking consumers and keeps migration cost near zero.
   - Alternatives considered:
     - Introduce aliases in same change: rejected to keep scope small and unambiguous.

## Risks / Trade-offs

- [Token helper refactor can accidentally change existing metric behavior] -> Mitigation: run existing token algorithm suites unchanged and add invariance tests that compare pre-refactor semantics for representative cases.
- [LCS dynamic programming has O(n*m) runtime for long strings] -> Mitigation: use rolling-row workspace approach and add long-input benchmark scenarios to monitor throughput.
- [Codegen and interpreted paths may diverge on edge cases] -> Mitigation: enforce parity tests with whole-stage codegen enabled/disabled including empty, null, and mixed-overlap inputs.
- [SQL naming collisions or ambiguity] -> Mitigation: register only canonical new names (`braun_blanquet`, `lcs_similarity`) and avoid alias expansion in this change.

## Migration Plan

1. Add token helper abstraction and migrate existing token metrics to use it without behavior changes.
2. Add `BraunBlanquet` token expression + kernel and connect DSL/SQL registration.
3. Add `LcsSimilarity` matrix expression + kernel and connect DSL/SQL registration.
4. Extend algorithm-level tests, integration parity tests, and benchmark coverage for both new metrics and token-kernel consistency.
5. Validate full test suite and benchmark module checks.

Rollback strategy:
- Revert additive metric files and registrations.
- Revert token helper migration if any compatibility regression is detected.
- Preserve existing public metric set and behavior as the stable fallback.

## Open Questions

- Should LCS operate on UTF-16 code units (current string behavior) or code points for emoji-heavy inputs in a future scope?
- Should the project later expose SQL aliases (for discoverability) after canonical metric behavior is validated?
