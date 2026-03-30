## Context

The current library exposes Spark Catalyst expressions for string similarity, but many algorithms still rely on hard-coded constants and fixed tokenization behavior. The proposal for `n-gram-and-configs` introduces three capabilities that broaden practical matching workflows: configurable metric parameters, character n-gram tokenization for token metrics, and phonetic preprocessing expressions.

This change is cross-cutting because it touches expression constructors, validation pathways (`checkInputDataTypes`), code generation, DSL helpers, SQL registration, test suites, and benchmarks. Backward compatibility is required for existing two-argument DSL and SQL calls, and interpreted/codegen parity must be preserved.

Constraints from the proposal:
- Existing behavior must remain the default when users do not pass optional tuning arguments.
- Parameter validation must fail at analysis time with clear errors.
- SQL should avoid collisions with Spark built-ins (`soundex`) while keeping DSL names ergonomic.
- Changes should be shippable incrementally and tested independently.

## Goals / Non-Goals

**Goals:**
- Introduce non-child expression parameters for configurable metrics without breaking existing call sites.
- Add a robust tokenization mode (`ngramSize`) for token metrics while preserving whitespace behavior as default.
- Add unary phonetic expressions (`Soundex`, `RefinedSoundex`, `DoubleMetaphone`) that compose cleanly with existing similarity expressions.
- Keep Catalyst compatibility: deterministic expression identity, `withNewChildrenInternal` correctness, and codegen/interpreted parity.
- Provide clear migration and operational guidance for users of DSL and SQL APIs.

**Non-Goals:**
- Parameterized SQL variants for all configurable metrics and n-gram token metrics.
- Locale-specific tokenization/normalization beyond current ASCII-oriented behavior.
- New weighted token metrics or TF-IDF style scoring.
- Non-English phonetic algorithm families.
- Fused single-expression phonetic+similarity operators.

## Decisions

### 1) Represent tunable settings as constructor fields, not child expressions

**Decision:** Keep metric tuning knobs (e.g., `matchScore`, `prefixScale`, `ngramSize`) as Scala constructor fields in case classes with defaults.

**Rationale:**
- These values are configuration constants, not row-varying data.
- Constructor fields naturally participate in case-class `equals`/`hashCode`, preserving Catalyst dedup correctness.
- This keeps child expression trees stable (`left`, `right` for binary metrics), simplifying optimizer interactions.
- Codegen can inline literal values directly into generated Java.

**Alternatives considered:**
- **Child `Expression` parameters:** rejected due to added evaluation overhead, broader type handling, and unnecessary complexity.
- **Global runtime config:** rejected because per-call tuning is required and global state would make behavior less explicit.

### 2) Validate all new parameters in `checkInputDataTypes`

**Decision:** Enforce constraints for each metric at analysis time (e.g., `prefixScale` bounds, sign constraints for penalties, valid `innerMetric`, `ngramSize >= 0`).

**Rationale:**
- Failing before execution gives users immediate actionable feedback.
- Prevents undefined/degenerate execution paths in generated code.
- Matches Spark expression contract for early type/argument safety.

**Alternatives considered:**
- **Runtime row-level validation:** rejected due to repeated overhead and delayed failures.
- **Silent coercion/clamping:** rejected because implicit behavior masks user mistakes.

### 3) Use tokenization mode flag for n-grams inside existing token metrics

**Decision:** Extend existing token metrics with `ngramSize` where `0` means whitespace mode and `> 0` means character n-gram mode.

**Rationale:**
- Jaccard/Dice/Overlap/Cosine/BraunBlanquet semantics are unchanged; only token extraction changes.
- Avoids class explosion (`NgramJaccard`, etc.) and keeps API discoverable.
- Default `0` preserves full backward compatibility.

**Alternatives considered:**
- **New parallel n-gram metric classes:** rejected due to duplicate logic and maintenance burden.
- **Tokenizer as pluggable strategy object:** rejected for this scope; over-engineered relative to one new mode.

### 4) Support Monge-Elkan configurability in two dimensions

**Decision:**
- Add `innerMetric` selector for token-pair scoring (`jaro_winkler`, `jaro`, `levenshtein`, `needleman_wunsch`, `smith_waterman`).
- Add `ngramSize` tokenization mode for outer token splitting, with fallback to disable if benchmark/testing shows degenerate behavior.

**Rationale:**
- Keeps Monge-Elkan useful for domain-specific tuning.
- Aligns with proposal intent to expose both scoring and tokenization flexibility.
- Retains proposal safety valve if n-gram outer tokenization produces poor discrimination in practice.

**Alternatives considered:**
- **Keep Monge-Elkan fixed:** rejected because it would be inconsistent with the rest of the feature set.
- **Expose fully arbitrary inner metrics:** rejected for now to avoid broad validation and combinatorial testing complexity.

### 5) Introduce a dedicated phonetic expression family

**Decision:** Add `expressions.phonetic` package with a shared `PhoneticExpression` unary base class and three concrete implementations.

**Rationale:**
- Phonetic encoding is a transformation (string -> string), not a similarity score.
- Unary expressions compose naturally in Spark plans (`metric(phonetic(a), phonetic(b))`).
- A shared base centralizes null handling, input type constraints, and codegen scaffolding.

**Alternatives considered:**
- **Implement phonetic logic as helper methods only:** rejected; would bypass Catalyst expression composability.
- **Fused phonetic similarity expressions:** rejected as less flexible and outside scope.

### 6) Avoid SQL naming collisions via namespaced function names

**Decision:** Register SQL phonetic functions as `ss_soundex`, `ss_refined_soundex`, and `ss_double_metaphone`, while preserving ergonomic short names in Scala DSL.

**Rationale:**
- Spark already provides `soundex`; collision avoidance is mandatory.
- Namespaced SQL keeps behavior explicit and predictable.
- DSL stays concise for programmatic consumers.

**Alternatives considered:**
- **Override built-in `soundex`:** rejected due to ambiguity and operational risk.
- **Use long prefixed names in DSL too:** rejected to avoid unnecessary API friction.

## Risks / Trade-offs

- [Constructor signature expansion may break call sites or serialization assumptions] -> Keep defaults matching old constants; preserve two-argument DSL overloads; add targeted compatibility tests.
- [N-gram mode increases CPU/memory pressure due to larger token sets] -> Benchmark each token metric for `ngramSize=2`; document expected overhead and keep whitespace default.
- [Monge-Elkan with n-grams may over-score short near-matching tokens] -> Add dedicated quality tests; if results are consistently degenerate, disable n-gram support for Monge-Elkan and document rationale.
- [Double Metaphone implementation complexity can introduce subtle correctness bugs] -> Validate against known corpus and Apache Commons Codec behavior as oracle; prioritize exhaustive edge-case tests.
- [Validation rule drift across expressions] -> Centralize constraint assertions near expression definitions and test each invalid boundary explicitly.

## Migration Plan

1. Land expression constructor and companion updates with default-value parity tests.
2. Add DSL overloads that delegate old signatures to defaults; keep SQL two-arg behavior unchanged for existing metrics.
3. Introduce n-gram helper and token metric wiring, then add correctness and parity tests.
4. Introduce phonetic package/classes and SQL registration with namespaced identifiers.
5. Run benchmark suite and compare default-path regressions plus new non-default scenarios.
6. Update docs/examples to show:
   - default backward-compatible usage,
   - parameterized DSL usage,
   - namespaced SQL phonetic usage,
   - composed patterns like `jaccard(soundex(a), soundex(b))`.

Rollback strategy:
- If regressions appear, disable newly added overload entry points and SQL registrations first (surface rollback) while preserving internal code for quick re-enable.
- For severe performance regressions in n-gram mode, keep feature guarded by validation/feature flag semantics (e.g., restrict to `ngramSize=0` temporarily) until optimizations land.

## Open Questions

- Should Monge-Elkan n-gram support be shipped by default, or guarded pending benchmark/quality thresholds?
- Do we need explicit docs on expected ranges for tunable parameters to prevent misleading score interpretations?
- Is a dedicated migration note needed for users who relied on unqualified SQL `soundex` semantics in mixed environments?
