## Context

The project currently exposes SQL-callable functions with mixed naming, including unprefixed names that can collide in shared Spark sessions. This change standardizes SQL-visible names by enforcing an `ss_` prefix while keeping scope limited to SQL function identifiers (not internal algorithm/module names).

This is a pre-publishing cleanup change in an early-stage project, so compatibility migration documentation and gradual deprecation phases are intentionally out of scope.

## Goals / Non-Goals

**Goals:**
- Ensure every SQL-exposed function is registered under an `ss_`-prefixed name.
- Rename explicit collisions: `levenshtein` -> `ss_levenshtein`, `jaro_winkler` -> `ss_jaro_winkler`.
- Keep non-SQL internals stable where possible (Scala/Python APIs, algorithm identifiers, package/class names).
- Provide deterministic naming rules that tests can enforce.

**Non-Goals:**
- Preserving backward compatibility for unprefixed SQL names.
- Introducing aliases, deprecation windows, or migration tooling.
- Changing similarity algorithm behavior, scoring semantics, or parameter defaults.
- Refactoring unrelated function registration infrastructure.

## Decisions

1. Use SQL-name-only prefixing
   - Decision: Apply `ss_` exclusively to SQL function registration names.
   - Rationale: Solves collision risk while minimizing implementation impact and avoiding broad API churn.
   - Alternative considered: Renaming internal APIs and symbols to `ss_` as well. Rejected due to unnecessary surface-area changes unrelated to SQL collision prevention.

2. Enforce full registration consistency
   - Decision: No unprefixed SQL names remain after this change.
   - Rationale: Partial rollout leaves collision vectors and ambiguous conventions.
   - Alternative considered: Keep compatibility aliases. Rejected because project is pre-publishing and does not require migration overhead.

3. Centralize naming rule validation in tests
   - Decision: Add/adjust tests that verify SQL function registry names all begin with `ss_` and include explicit checks for `ss_levenshtein` and `ss_jaro_winkler`.
   - Rationale: Prevents regressions as new SQL functions are added.
   - Alternative considered: Rely on manual review. Rejected due to high drift risk.

4. Update user-facing SQL examples with canonical names
   - Decision: Any SQL snippets and docs in-repo should reference only prefixed names.
   - Rationale: Keeps discoverability aligned with runtime behavior and prevents stale examples.
   - Alternative considered: Mention both old and new names. Rejected to avoid normalizing deprecated identifiers.

## Risks / Trade-offs

- Existing SQL queries break immediately if still using unprefixed names -> Mitigation: Update tests and examples atomically and call out breaking change in proposal/specs.
- Missed function during rename leads to inconsistent namespace -> Mitigation: Add registry-wide assertion that all SQL names match `^ss_`.
- Confusion between SQL names and internal API names -> Mitigation: Scope language explicitly in specs/design and avoid touching non-SQL identifiers.

## Migration Plan

1. Update SQL registration name mappings to `ss_`-prefixed forms.
2. Add/adjust tests for explicit renamed functions and global prefix invariants.
3. Update in-repo SQL examples to prefixed names.
4. Validate all SQL function invocation tests pass with prefixed names only.

Rollback strategy: revert this change set if immediate downstream breakage is discovered before release packaging.

## Open Questions

- Should the SQL prefix policy also reserve a future namespace for experimental functions (for example, `ss_exp_`), or keep a single namespace now?
