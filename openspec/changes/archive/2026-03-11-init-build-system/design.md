## Context

This is a new project for string-matching expressions in Apache Spark. The build system needs to be initialized from scratch to establish a solid foundation for future development. The project will use sbt as the build tool with support for multiple Scala versions based on Spark version compatibility.

**Current State:** No build configuration exists.

**Constraints:**
- Must support Spark 3.5.x (Scala 2.12) and 4.x.x (Scala 2.13)
- Version management via git tags using sbt-ci-release
- Must follow standard sbt project structure

## Goals / Non-Goals

**Goals:**
- Create a working `build.sbt` with dynamic Scala version resolution based on Spark version
- Set up two subprojects: root (`.`) and `benchmarks`
- Configure sbt plugins for formatting, linting, CI release, and benchmarking
- Establish proper dependency scopes (provided for root, runtime for benchmarks)
- Configure project metadata for Maven Central publishing
- Create minimal hello-world code structure to validate the build

**Non-Goals:**
- Implement actual string-matching logic (future work)
- Complex benchmark suites beyond hello-world validation
- CI/CD pipeline configuration (handled by sbt-ci-release plugin)

## Decisions

### 1. Dynamic Scala Version Based on Spark Version
**Decision:** Use `sparkVersion` as the primary input and derive `scalaVersion` dynamically.
- Spark 3.5.x → Scala 2.12
- Spark 4.x.x → Scala 2.13

**Rationale:** Spark tightly couples with specific Scala versions. Making Spark version the driver ensures compatibility and reduces configuration errors.

**Alternatives Considered:**
- Hardcode Scala version: Rejected because it would require manual updates when switching Spark versions.
- Separate build files per Spark version: Rejected due to maintenance overhead.

### 2. Multi-Project Structure
**Decision:** Root project (`.`) for the library, `benchmarks` subproject for JMH benchmarks.

**Rationale:** Standard sbt pattern for libraries with benchmarking needs. Keeps benchmark dependencies isolated from the main library.

### 3. Dependency Scoping
**Decision:** Apache Spark as `Provided` for root, `Runtime` for benchmarks.

**Rationale:** 
- Root: Library users provide Spark at runtime (standard for Spark libraries)
- Benchmarks: Need Spark on classpath for execution during development

### 4. Plugin Selection
**Decision:** 
- `sbt-scalafmt` (2.5.6): Code formatting
- `sbt-scalafix` (0.14.5): Linting and migrations
- `sbt-ci-release` (1.11.2): Automated versioning and publishing
- `sbt-jmh` (0.4.8): Benchmark harness

**Rationale:** Industry-standard plugins for Scala projects. Versions selected are stable and compatible with the target Scala versions.

### 5. Artifact Naming
**Decision:** `spark-second-string-$sparkMinorVersion` (e.g., `spark-second-string-4.1`)

**Rationale:** Clearly indicates Spark compatibility. Users can select the appropriate artifact for their Spark version.

### 6. Version Management
**Decision:** Dynamic versions from git tags via `sbt-ci-release`.

**Rationale:** Automates semantic versioning, reduces human error, integrates with CI/CD workflows.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Scala version derivation logic may need updates for future Spark versions | Centralize the logic in `build.sbt` for easy updates; add comments documenting the mapping |
| sbt plugin version conflicts with future Spark/Scala versions | Pin plugin versions; update systematically when upgrading Spark |
| `Provided` scope may confuse developers during local testing | Document in README; use `benchmarks` project for integration testing |
| Dynamic versioning may produce unexpected versions if git tags are malformed | Establish clear git tagging conventions; validate in CI |

## Migration Plan

Not applicable - this is a new project initialization with no existing codebase to migrate.

## Open Questions

None - the build system design is straightforward and well-understood.
