## Context

Current string-similarity Catalyst expressions are case classes that can be constructed directly by external callers, and several rely on default constructor arguments. This leaks internal expression construction details into the public surface and makes refactoring risky because constructor signatures become de facto API. The proposal defines a DSL-first boundary: `StringSimilarityFunctions` remains the public entry point, while expression classes become internal to `sparkss`.

Constraints:
- Existing DSL behavior must remain source-compatible for common calls.
- Tunable metrics must still offer defaulted behavior, but defaults should live in DSL helper overloads rather than expression constructors.
- SQL integration remains a thin layer that delegates to DSL/expression wiring.

## Goals / Non-Goals

**Goals:**
- Restrict expression case-class construction to package scope (`private[sparkss]`).
- Remove default constructor arguments from expression case classes.
- Preserve ergonomic and backward-compatible DSL entry points via `StringSimilarityFunctions` overloads.
- Keep metric semantics unchanged when callers use existing two-argument helper methods.

**Non-Goals:**
- Introducing new similarity metrics or changing scoring algorithms.
- Expanding SQL function signatures or SQL-first API ergonomics.
- Reworking Catalyst evaluation logic beyond visibility/constructor boundary updates.

## Decisions

- Make all user-addressable expression case classes `private[sparkss]`.
  - Rationale: prevents external direct instantiation and formalizes DSL helpers as the stable API boundary.
  - Alternative considered: keep public classes and rely on documentation discouraging constructor use. Rejected because it does not enforce the boundary and still blocks safe signature evolution.

- Remove default constructor args from expression classes and pass all parameters explicitly from helper methods.
  - Rationale: avoids implicit API commitments at constructor level and makes defaults centrally managed in one public layer.
  - Alternative considered: keep defaults in constructors and wrap with helpers. Rejected because constructor defaults remain externally visible and callable where visibility permits.

- Preserve compatibility by keeping/adding `StringSimilarityFunctions` overloads that encode existing defaults.
  - Rationale: external callers continue to use prior call shapes without behavioral drift, while internals become explicit.
  - Alternative considered: require callers to always provide full parameter sets. Rejected because it creates unnecessary migration friction and breaks existing DSL usage.

- Keep SQL registration unchanged, delegating to updated helper wiring.
  - Rationale: SQL stability is already required by existing capabilities and avoids widening scope.
  - Alternative considered: expose parameterized SQL variants in this change. Rejected as out-of-scope and unrelated to visibility hardening.

## Risks / Trade-offs

- [Risk] External code that directly instantiates expression case classes breaks at compile time. -> Mitigation: document as breaking change and provide equivalent helper-based call patterns.
- [Risk] Missing helper overload coverage could accidentally remove prior ergonomic defaults. -> Mitigation: audit each constructor that previously had defaults and ensure matching helper overloads exist.
- [Trade-off] More overloads in `StringSimilarityFunctions` increase API surface there. -> Mitigation: keep overloads minimal and aligned to existing defaults only.
- [Risk] Partial visibility updates leave some expressions publicly constructible. -> Mitigation: apply a consistent visibility rule across all string-sim expression case classes and verify through compile checks.

## Migration Plan

- Update expression case-class visibility to `private[sparkss]` and remove constructor defaults.
- Update `StringSimilarityFunctions` to pass explicit parameters and preserve prior two-argument/defaulted call forms via overloads.
- Run compile/tests to catch any internal sites that still rely on removed constructor defaults.
- Publish release notes/migration note: direct expression constructor usage is unsupported; use `StringSimilarityFunctions`.
- Rollback strategy: temporarily restore public visibility/defaulted constructors in a patch release only if critical downstream breakage is discovered.

## Open Questions

- Should migration documentation include concrete before/after Scala examples for each configurable metric, or only a generic guidance section?
- Are there any non-DSL internal modules outside `sparkss` package that currently instantiate these expressions and need package boundary adjustments?
