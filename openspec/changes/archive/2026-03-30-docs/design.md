## Context

This repository currently ships code, tests, benchmark runners, and fuzzy-testing outputs, but does not provide a structured user-facing documentation surface. The proposal introduces documentation as a first-class artifact with two entry points: a top-level `README` and a generated docs site.

The docs site must be implemented as a dedicated subproject under the existing build, using Typelevel Laika with mostly default Helium theme. Content is markdown-driven, one file per required section:
- Overview
- Quick Start
- Existing Metrics
- Fuzzy Testing
- Benchmarks

Two generated-data inputs are required by the docs:
- Benchmark output parser: transforms benchmark report output into Laika variables for the Benchmarks page.
- Fuzzy-testing comparison parser: transforms Spark-vs-SS fuzzy comparison output into Laika variables for the Fuzzy Testing page.

The build constraint is explicit: docs generation expects benchmark and fuzzy-testing pipelines to have been executed beforehand.

## Goals / Non-Goals

**Goals:**
- Add a discoverable top-level `README` explaining project purpose and motivation (spark-native similarity checks for identity-resolution and cheap blocking before heavier models).
- Add a `/docs` source tree and build-integrated docs subproject based on Laika + Helium.
- Establish a deterministic docs content structure with the required section pages.
- Provide repeatable parsing tools in the project folder that map benchmark/fuzzy artifacts into Laika variables.
- Define and document prerequisite handling for docs build when required upstream outputs are absent.

**Non-Goals:**
- No new string metric implementations or algorithm behavior changes.
- No rework of benchmark execution logic or fuzzy-testing execution logic beyond reading their produced artifacts.
- No custom theme system beyond minimal Helium configuration needed for project identity and navigation.
- No dynamic runtime docs service; output remains static site generation.

## Decisions

1. Use a docs subproject in the existing build graph.
   - Rationale: keeps docs versioned and released with code, avoids external tooling drift.
   - Alternative considered: separate repository/site pipeline.
   - Rejected because it increases operational overhead and weakens version coupling.

2. Use Typelevel Laika with mostly default Helium theme.
   - Rationale: native Scala ecosystem fit, static-site workflow, minimal customization burden.
   - Alternative considered: fully custom docs framework/theme.
   - Rejected because current need is reliable content delivery, not custom UX.

3. Keep docs content as markdown files with one file per required section.
   - Rationale: simple authoring model, low barrier for contributors, explicit IA.
   - Alternative considered: generated monolithic page.
   - Rejected because it reduces maintainability and discoverability.

4. Implement generated-data ingestion as Python parser tools that emit per-cell HOCON variables.
   - Rationale: separates data extraction from prose, supports reproducible docs generation. Per-cell variables (not monolithic table strings) allow markdown tables with variable references in each cell, which Laika renders as proper HTML tables.
   - Alternative considered: single multiline summary_table variable containing the whole markdown table.
   - Rejected because Laika substitutes variables as plain text, not markdown; a multiline table variable renders as a `<p>` block, not `<table>`.
   - Alternative considered: inline raw reports in docs pages.
   - Rejected because raw artifacts are noisy and inconsistent for readers.

5. Fail docs build fast when prerequisite benchmark/fuzzy outputs are missing, with actionable error messaging.
   - Rationale: prevents silently stale or empty metrics/quality pages.
   - Alternative considered: best-effort build with placeholders.
   - Rejected because placeholders can be mistaken for valid published results.

## Risks / Trade-offs

- [Risk] Parser tools become coupled to unstable benchmark/fuzzy output formats. -> Mitigation: define accepted input contracts and fail with explicit schema/field mismatch errors.
- [Risk] Contributors run docs build without prerequisites and treat failures as build instability. -> Mitigation: document preconditions in README + docs quick start and emit precise failure hints.
- [Risk] Default Helium theme may limit branding flexibility. -> Mitigation: constrain current scope to functional docs delivery; defer deeper visual customization to a future change.
- [Risk] Documentation freshness can lag behind code changes. -> Mitigation: integrate docs subproject into normal build/test workflows and require generated variable refresh before docs publication.

## Migration Plan

1. Introduce README baseline content and docs subproject scaffold.
2. Add markdown section files and navigation wiring.
3. Add parser tools and variable injection pipeline for benchmark and fuzzy-testing pages.
4. Wire docs generation into build lifecycle with prerequisite checks.
5. Validate by running prerequisites then docs build end-to-end in CI/local workflow.

Rollback strategy: remove docs subproject wiring and parser-tool integration from build while retaining source markdown files as dormant assets if release pressure requires temporary disablement.

## Resolved Questions

- **Canonical input paths**: `benchmarks/target/reports/suite/compare-table.txt` and `fuzzy-testing/target/reports/fuzzy-report.md` (relative to project root).
- **Prerequisite check timing**: checks run at task initialization (`generateDocsVariables` task), before any variable parsing. This gives the earliest possible failure with actionable error messages.
- **Variable namespace**: variables are top-level HOCON keys in `directory.conf` (e.g. `benchmarks.algorithm_count`), NOT under `laika.variables` which is a reserved Laika namespace. This follows the pattern used by GraphFrames.
- **directory.conf lifecycle**: `docs/src/directory.conf` is generated by the `generateDocsVariables` sbt task (merging navigation config with Python-generated variable files) and is gitignored.
- **Path format**: all `source_path` values are relative to the project root.
- **Publication cadence**: deferred to future decision.
