## Context

Fuzzy testing currently fails because SQL routines are invoked using legacy names (for example, `needleman_wunsch`) while the implementation has been standardized to `ss_`-prefixed function names (for example, `ss_needleman_wunsch`). The project is pre-release (`0.0.0`), so compatibility and migration overhead can be minimized in favor of internal consistency.

The proposal scope is to fix fuzzy tests, scan the repository for additional naming mismatches, and update documentation where needed.

## Goals / Non-Goals

**Goals:**
- Ensure fuzzy tests call the current `ss_`-prefixed function names and pass.
- Eliminate old, unprefixed routine references across code, tests, and SQL snippets.
- Align documentation examples and references with the canonical `ss_` naming.

**Non-Goals:**
- Providing backward-compatible aliases for legacy routine names.
- Introducing deprecation workflows or migration guidance for external users.
- Changing function behavior, algorithm semantics, or performance characteristics.

## Decisions

1. **Canonical naming source is the current implementation (`ss_` prefix).**
   - Rationale: The implementation is already unified, and unresolved routine errors come from stale references.
   - Alternative considered: Keep both legacy and new names via aliases. Rejected because it adds maintenance complexity during pre-release.

2. **Apply repository-wide rename/update for known fuzzy-related routines and references.**
   - Rationale: Localized test-only fixes risk missing references in docs and other modules, causing recurring breakage.
   - Alternative considered: Fix only failing tests. Rejected because proposal explicitly includes mismatch scanning and docs sync.

3. **Treat docs as test-adjacent artifacts for naming correctness.**
   - Rationale: Outdated docs create confusion and often become copy/paste sources for failing examples.
   - Alternative considered: Postpone docs updates. Rejected because scope already includes docs checks and updates.

## Risks / Trade-offs

- **[Risk] Incomplete pattern matching misses uncommon legacy references** -> **Mitigation:** perform targeted grep scans for known old names and validate no remaining unresolved-name usage.
- **[Risk] Over-broad replacement touches unrelated identifiers** -> **Mitigation:** review each changed file context and prefer exact function-token replacements over loose substring swaps.
- **[Trade-off] No backward compatibility for old names** -> **Mitigation:** acceptable for pre-release; keep naming strict to reduce long-term ambiguity.

## Migration Plan

1. Enumerate legacy fuzzy routine names currently referenced in tests/docs/code.
2. Replace each legacy routine call/reference with the `ss_` equivalent.
3. Re-run fuzzy test paths to confirm unresolved routine errors are removed.
4. Run a final repository scan to ensure no stale references remain.
5. If regressions appear, rollback by reverting affected files and re-apply changes with narrower replacements.

## Open Questions

- Are there any intentionally preserved legacy names in non-fuzzy modules that should remain untouched?
- Should a central naming reference list be added later to prevent future drift between implementation and tests/docs?
