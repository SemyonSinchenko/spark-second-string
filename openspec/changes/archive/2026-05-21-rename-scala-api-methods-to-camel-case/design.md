## Context

`StringSimilarityFunctions` currently exposes a mixed naming surface in Scala (`monge_elkan`, `affine_gap`, and existing camelCase helpers). This is inconsistent with standard Scala/Java API expectations and creates avoidable friction for users navigating the DSL.

The proposal requires a targeted, breaking rename of the Scala/Java helper methods to camelCase while keeping SQL function names and metric/report identifiers unchanged in snake_case. This means the change must be isolated to language-level helper APIs and their Scala-facing call sites, without touching SQL registration contracts.

## Goals / Non-Goals

**Goals:**
- Standardize Scala/Java helper method names for affected metrics to camelCase (`mongeElkan`, `affineGap`) across all public overloads.
- Preserve SQL-facing names (`monge_elkan`, `affine_gap`) and internal metric/report IDs exactly as-is.
- Update tests and documentation so examples reflect the canonical Scala DSL names after the rename.
- Make the API break explicit and migration path clear for Scala/Java callers.

**Non-Goals:**
- Renaming SQL functions, SQL examples, or metric IDs.
- Introducing compatibility shims or dual-name support for the removed snake_case Scala helper methods.
- Refactoring unrelated string similarity helpers or changing similarity algorithm behavior.

## Decisions

1. Rename only public Scala helper method identifiers, not SQL identifiers.
   - **Why:** The inconsistency is in the Scala DSL surface; SQL naming already follows established conventions and is part of stable external contracts.
   - **Alternative considered:** Rename both Scala and SQL identifiers for uniformity. Rejected because it would create unnecessary SQL breakage and cross-system churn.

2. Apply the rename consistently to all overloads (`Column`, `String`, and mixed signatures).
   - **Why:** Partial renaming would leave an inconsistent API and create overload-resolution surprises.
   - **Alternative considered:** Rename only most-used overloads first. Rejected because it prolongs inconsistency and increases migration complexity.

3. Update all internal Scala call sites and tests in the same change.
   - **Why:** Keeping the tree fully migrated ensures compile-time validation and avoids shipping stale examples/usages.
   - **Alternative considered:** Rename helpers first and defer call-site updates. Rejected due to temporary breakage and higher risk of missed references.

4. Treat this as a documented, explicit breaking Scala/Java API change.
   - **Why:** Existing callers using snake_case method names will fail at compile time; this should be clear in release notes/docs.
   - **Alternative considered:** Keep deprecated aliases for one release. Rejected to avoid API bloat and because the proposal explicitly calls for a breaking rename now.

## Risks / Trade-offs

- **[Risk]** Downstream Scala/Java projects break on upgrade due to missing `monge_elkan`/`affine_gap` methods. **-> Mitigation:** Document migration clearly with before/after examples and highlight in changelog/release notes.
- **[Risk]** Incomplete rename leaves hidden snake_case call sites in tests or internal code. **-> Mitigation:** Use repository-wide symbol search and compile/test runs to validate no stale method usage remains.
- **[Trade-off]** No backward-compatibility aliases reduces transition smoothness but keeps API surface clean. **-> Mitigation:** Provide concise migration guidance and keep change scope small/predictable.

## Migration Plan

1. Rename `StringSimilarityFunctions` public helper method definitions from `monge_elkan` -> `mongeElkan` and `affine_gap` -> `affineGap` for all overloads.
2. Update Scala internal call sites and tests to use new camelCase names.
3. Update DSL documentation/examples to use camelCase for Scala and preserve snake_case in SQL examples.
4. Validate with compile/tests and verify SQL registrations and metric IDs remain unchanged.
5. Communicate breaking API change in change notes and release messaging.

Rollback strategy: if issues are found before release, revert this change set and restore prior helper names; no data or persistent state migration is involved.

## Open Questions

- Where should the breaking-change migration note be surfaced in this repository (release notes, migration guide, or both)?
- Are there generated docs or external example repos that also need synchronized helper-name updates?
