## Context

Today SQL functions are not discovered through Spark's standard extension loading path, so users must register functions manually. This is especially painful for PySpark and pure SQL workflows where users expect function registration to happen through cluster or session configuration (`spark.sql.extensions`).

The change introduces a real `SparkSessionExtensions` implementation that performs function registration automatically. The existing extension path used by advanced JVM users remains available for backward compatibility.

## Goals / Non-Goals

**Goals:**
- Add a production-ready `SparkSessionExtensions` subclass that Spark can load from configuration.
- Register all library SQL functions automatically during SparkSession initialization.
- Preserve existing behavior for advanced JVM users by keeping the current extension class/path intact.
- Add tests that prove registration works for configured Spark SQL/PySpark sessions and prevent regressions.

**Non-Goals:**
- Redesigning function implementations or changing SQL semantics.
- Removing or breaking the existing manual/advanced registration path.
- Introducing unrelated Spark configuration features.

## Decisions

1. Add a new dedicated `SparkSessionExtensions` subclass
   - **Decision:** Implement a new extension class focused on Spark's built-in extension contract.
   - **Rationale:** Separates "Spark-loadable extension" concerns from existing advanced/JVM APIs, avoids risky behavioral coupling, and makes cluster-level configuration straightforward.
   - **Alternative considered:** Retrofit the existing class to satisfy Spark extension loading directly. Rejected because it risks breaking advanced user flows and mixes responsibilities.

2. Centralize function registration logic in reusable internal code
   - **Decision:** Keep one internal registration routine and invoke it from both the new Spark extension and the legacy path.
   - **Rationale:** Ensures consistency and avoids drift where one path gets new functions while the other does not.
   - **Alternative considered:** Duplicate registration logic per entry point. Rejected due to long-term maintenance and regression risk.

3. Validate with integration-style tests plus regression coverage
   - **Decision:** Add tests that initialize Spark with the new extension configured and assert function availability; keep regression tests for the legacy/advanced path.
   - **Rationale:** Unit tests alone would miss the real bootstrap path (`spark.sql.extensions`) used by PySpark/SQL users.
   - **Alternative considered:** Only unit tests around registration helpers. Rejected because it does not prove end-to-end extension loading behavior.

## Risks / Trade-offs

- [Extension class not loaded due to configuration/classpath mismatch] -> Mitigation: add tests that boot Spark with the configured extension and fail fast when functions are unavailable.
- [Behavior divergence between new extension and legacy path] -> Mitigation: share a single registration routine and keep regression tests for both entry points.
- [Spark version differences in extension initialization timing] -> Mitigation: avoid relying on fragile lifecycle side effects; test function visibility at query time.

## Migration Plan

1. Implement the new Spark extension class and wire it to shared registration logic.
2. Add/adjust tests for configured SparkSession startup and function visibility.
3. Document recommended configuration using `spark.sql.extensions` for cluster/session users.
4. Keep existing extension APIs available for advanced JVM integration.

Rollback strategy:
- Remove the new extension class from configuration and fall back to the existing registration path; no data migration is required.

## Open Questions

- Should docs include explicit PySpark startup examples (local and cluster) in this change, or follow-up docs-only change?
- Are there Spark version bounds that require conditional handling for extension registration?
