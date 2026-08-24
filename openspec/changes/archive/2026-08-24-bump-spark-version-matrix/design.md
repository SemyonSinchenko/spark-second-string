## Context

See proposal.md for motivation. Current state:

- `.github/workflows/ci.yml` test matrix: Spark `3.5.8` (Java 11, 17), `4.0.2` (Java 17), `4.1.2` (Java 17).
- `.github/workflows/publish-ci.yml` release matrix: Spark `3.5.8` (Java 8), `4.0.2` (Java 17), `4.1.2` (Java 17).
- `build.sbt` defaults to Spark `4.0.2` and selects the `spark3`/`spark4` shim directory from the Spark major version; the version is overridable via `-DsparkVersion=...`.

## Goals / Non-Goals

**Goals:**
- Bump the Spark patch versions in both workflows to the latest upstream: `3.5.9`, `4.0.3`, `4.1.3`.
- Add Spark `4.2.0` to both the CI test matrix and the publish matrix.
- Verify the test suite (and scalafmt check) passes on the new matrix, including `4.2.x`.

**Non-Goals:**
- No API, shim, or behavior changes beyond what the version bump requires.
- No changes to supported Spark majors (3.x and 4.x stay the same).
- No spec-level requirement changes (CI/tooling only; `skip_specs: true`).

## Decisions

- **Update versions as a plain matrix edit**: The workflows already parametrize the Spark version via `-DsparkVersion=${{ matrix.spark-version }}` and `build.sbt` derives the shim directory from the Spark major, so a version bump needs no build changes. Alternative (centralizing versions in `build.sbt` only) was rejected: the workflows are the source of truth for what gets tested/published.
- **Java versions stay unchanged per line**: Keep Java 8 for the 3.5.x publish line and Java 11/17 for tests, matching the current matrix layout. Java 8 is retained because Spark 3.5.x still targets Java 8.
- **Add 4.2.0 with Java 17**: Matches the existing 4.x entries (`4.0.2`, `4.1.2` run on Java 17). Java 17 is the minimum for Spark 4.x, and the shim selection (`spark4`) already covers it.
- **Verify with a local run first, then let CI confirm**: Run the test suite locally on `4.2.0` (the newest version) before pushing, since it is the only version not previously exercised; the existing patch bumps are low-risk. Note: if a local run is infeasible in this environment, rely on the CI matrix run after the change lands.

## Compatibility note (Spark 4.2.0 function registration)

Spark 4.2.0 requires function identifiers registered via `FunctionRegistry.registerFunction` to be fully qualified (3-part). The legacy registration path (`spark.registerStringSimilarityFunctions()` / `registerAllFunctionsPy4j`) was switched to Spark's temp-function API (`FunctionRegistry.createOrReplaceTempFunction(name, builder, source)` — the same path `spark.udf.register` uses). Spark qualifies the plain function name per version (session namespace on 4.2+), so no internal namespace such as `system.builtin` is hardcoded, and the same shared code compiles against 3.5.9, 4.0.x, 4.1.x and 4.2.x. The `spark.sql.extensions` path is unchanged (`extensions.injectFunction`).

## Risks / Trade-offs

- [Spark 4.2.0 may have behavior changes vs 4.1.x] → Run the full test suite and fuzzy tests on 4.2.0 before merging; if failures surface, adapt the affected code (no shim changes were needed for 4.2.0 — only the registration API swap above).
- [Fuzzy harness: legacy `com.wcohen.secondstring` algorithms share a static mutable tokenizer, corrupting under concurrent scoring on multi-core `local[*]`] → Serialize legacy scoring in `LegacySecondStringUdfs.LegacyScorer` with a JVM-wide lock; fuzzy runs are deterministic on any core count and any Spark version.
- [Spark 3.5.9 / 4.0.3 / 4.1.3 patch releases could introduce regressions] → Patch releases are backwards-compatible by policy; CI runs the full suite on every matrix cell.
- [Fuzzy tests are slow (50k rows) on a new version] → `fail-fast: false` keeps other matrix cells reporting; fuzzy runs only in the test workflow, not publish.
