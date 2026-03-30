## 1. Docs Subproject Setup

- [x] 1.1 Add a `docs` subproject to the build configuration so it appears in `sbt projects`.
- [x] 1.2 Add Typelevel Laika and Helium configuration for the `docs` subproject with mostly default theme settings.
- [x] 1.3 Create the `/docs` source tree and wire it as the documentation content input for the docs subproject.

## 2. Core Documentation Content

- [x] 2.1 Create or update top-level `README.md` with project overview and motivation for identity-resolution and cheap-blocking use cases.
- [x] 2.2 Add `Overview` markdown page under `/docs`.
- [x] 2.3 Add `Quick Start` markdown page under `/docs` covering installation, Spark compatibility matrix, and both usage flows (direct API and Spark SQL extension).
- [x] 2.4 Add `Existing Metrics` markdown page under `/docs` with comprehensive metric reference (formulas, DSL/SQL names, parameters, tokenization modes, phonetic encoders).
- [x] 2.5 Add `Fuzzy Testing` markdown page under `/docs` with per-metric HTML table populated from generated variables.
- [x] 2.6 Add `Benchmarks` markdown page under `/docs` with per-algorithm HTML table populated from generated variables.

## 3. Generated Data Tooling

- [x] 3.1 Add Python parser tool (`dev/docs_benchmark_vars.py`) that emits per-algorithm per-cell HOCON variables from benchmark compare-table report.
- [x] 3.2 Integrate benchmark-derived variables into Benchmarks page as markdown table with `${benchmarks.<algo>.<field>}` cell references.
- [x] 3.3 Add Python parser tool (`dev/docs_fuzzy_vars.py`) that emits per-metric per-cell HOCON variables from fuzzy-testing report.
- [x] 3.4 Integrate fuzzy-testing-derived variables into Fuzzy Testing page as markdown table with `${fuzzy.<metric>.<field>}` cell references.

## 4. Build Preconditions and Validation

- [x] 4.1 Add docs build precondition checks for required benchmark outputs with explicit failure messages.
- [x] 4.2 Add docs build precondition checks for required fuzzy-testing outputs with explicit failure messages.
- [x] 4.3 Document docs build prerequisites and expected failure behavior in user-facing docs.
- [x] 4.4 Validate end-to-end flow by running benchmark and fuzzy-testing pipelines, then running docs build successfully.

## 5. Variable Pipeline Fixes

- [x] 5.1 Move variables from `laika.variables` reserved namespace to top-level HOCON keys (following GraphFrames pattern).
- [x] 5.2 Replace monolithic `summary_table` multiline variables with per-cell variables so Laika renders proper HTML tables.
- [x] 5.3 Merge generated variable files into `docs/src/directory.conf` via `generateDocsVariables` sbt task.
- [x] 5.4 Gitignore `docs/src/directory.conf` since it is generated.
- [x] 5.5 Emit relative paths (not absolute) for `source_path` variables.
