## Context

The public DSL entry point in `StringSimilarityFunctions.scala` is currently missing comprehensive Scaladoc coverage.
This makes metric behavior and parameter semantics hard to discover from IDE autocomplete and generated API docs.
The change is documentation-only and targets the user-facing DSL methods for string similarity metrics.

## Goals / Non-Goals

**Goals:**
- Add meaningful Scaladoc to each public DSL metric function in `StringSimilarityFunctions.scala`.
- Document what each metric computes in plain language, including any notable behavior.
- Document parameters, expected inputs, and result semantics for each function.
- Keep descriptions consistent in tone, terminology, and structure.

**Non-Goals:**
- No algorithmic or runtime behavior changes to metric implementations.
- No API signature changes, deprecations, or renames.
- No documentation expansion outside the public DSL file unless required to fix broken links/references.

## Decisions

1. **Document at the public DSL surface, not internals**
   - Rationale: `StringSimilarityFunctions.scala` is the main user entry point and where discoverability matters most.
   - Alternative considered: documenting only lower-level implementations; rejected because users interact with DSL methods first.

2. **Use a consistent Scaladoc template per metric**
   - Rationale: uniform docs improve readability and reduce ambiguity across many functions.
   - Planned structure per function: one-line summary, behavior notes, `@param` descriptions, and return/result meaning.
   - Alternative considered: free-form comments; rejected because style drift would reduce clarity.

3. **Favor practical metric explanations over formula-heavy descriptions**
   - Rationale: most DSL users need intuition and usage guidance more than mathematical derivations.
   - Alternative considered: adding formal equations in all docs; rejected as noisy for everyday use.

4. **Preserve source/API compatibility**
   - Rationale: this is a docs-focused change and should remain low risk.
   - Alternative considered: touching signatures to improve naming while documenting; deferred as out of scope.

## Risks / Trade-offs

- [Risk] Metric descriptions might over-simplify edge-case behavior. -> Mitigation: cross-check wording against existing implementation/tests before finalizing text.
- [Risk] Inconsistent parameter terminology across metrics. -> Mitigation: define a small naming glossary and apply it across all Scaladoc entries.
- [Trade-off] Practical explanations are easier to read but may omit deep theory. -> Mitigation: keep concise intuition in Scaladoc and leave advanced theory for external docs if needed.

## Migration Plan

1. Add/expand Scaladoc blocks for each public DSL metric method in `StringSimilarityFunctions.scala`.
2. Review generated/IDE-visible docs for formatting and completeness.
3. Run project checks/tests used for documentation and compile validation.
4. Rollback strategy: revert documentation-only commits if wording issues are discovered.

## Open Questions

- Should docs include short examples for every metric, or only where behavior is commonly misunderstood?
- Is there a preferred phrasing standard for score ranges (for example, explicit `[0, 1]` wording) across all metrics?
