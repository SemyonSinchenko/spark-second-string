## Purpose

This specification defines the documentation infrastructure for the spark-second-string project, including the docs subproject, content structure, generated-data tooling, and the variable injection pipeline.

## Requirements

### Requirement: Top-level project README
The repository SHALL include a top-level `README.md` that states project purpose, motivation, and positioning as spark-native string metrics for identity-resolution workflows and low-cost blocking before heavier model-based stages.

#### Scenario: README explains motivation and use case
- **WHEN** a new user reads `README.md`
- **THEN** the user can identify why to use the project before LLM-scale similarity workflows

### Requirement: Documentation subproject and docs source tree
The build configuration SHALL define a dedicated documentation subproject and a `/docs` source tree as the canonical source for generated documentation.

#### Scenario: Docs subproject is discoverable
- **WHEN** a user runs the project listing command for the build tool
- **THEN** a docs subproject is listed alongside existing subprojects

### Requirement: Laika engine with Helium theme
The documentation subproject SHALL use Typelevel Laika as the docs engine and SHALL use a mostly default Helium theme configuration.

#### Scenario: Docs engine and theme are configured
- **WHEN** a user inspects docs build configuration
- **THEN** Laika and Helium configuration are present with only minimal custom overrides

### Requirement: Sectioned markdown information architecture
The `/docs` tree SHALL contain separate markdown pages for Overview, Quick Start, Existing Metrics, Fuzzy Testing, and Benchmarks.

#### Scenario: Required pages exist as separate files
- **WHEN** a user inspects the docs content directory
- **THEN** each required section exists as its own markdown file

### Requirement: Existing Metrics page provides comprehensive reference
The Existing Metrics page SHALL describe every supported metric with its formula or algorithm summary, DSL and SQL function names, configurable parameters with defaults and valid ranges, and return type. It SHALL also document tokenization modes and the phonetic encoder family.

#### Scenario: User can find metric parameters and API names
- **WHEN** a user reads the Existing Metrics page
- **THEN** every metric lists its DSL name, SQL name, parameters with defaults, and output range

### Requirement: Benchmark-derived documentation variables
The project SHALL provide a Python parser tool (`dev/docs_benchmark_vars.py`) that reads the benchmark compare-table report and emits per-algorithm per-cell HOCON variables consumed by the Benchmarks page markdown table.

#### Scenario: Benchmarks page renders a proper HTML table
- **WHEN** benchmark output artifacts are available and docs generation is executed
- **THEN** the Benchmarks page renders an HTML table with per-algorithm throughput and diff values populated from parsed variables

### Requirement: Fuzzy-testing-derived documentation variables
The project SHALL provide a Python parser tool (`dev/docs_fuzzy_vars.py`) that reads the fuzzy-testing report and emits per-metric per-cell HOCON variables consumed by the Fuzzy Testing page markdown table.

#### Scenario: Fuzzy Testing page renders a proper HTML table
- **WHEN** fuzzy-testing comparison artifacts are available and docs generation is executed
- **THEN** the Fuzzy Testing page renders an HTML table with per-metric correlation and tolerance band values populated from parsed variables

### Requirement: Variables are top-level HOCON keys
Generated documentation variables SHALL be defined as top-level HOCON keys in `docs/src/directory.conf` (e.g. `benchmarks.affine_gap.native`, `fuzzy.smith_waterman.pearson`), NOT under the reserved `laika.variables` namespace. The `directory.conf` file is generated at build time by the `generateDocsVariables` sbt task and SHALL be gitignored.

#### Scenario: Variables resolve in rendered HTML
- **WHEN** a user runs `sbt docs/laikaSite`
- **THEN** all `${benchmarks.*}` and `${fuzzy.*}` references in markdown are replaced with actual values in the generated HTML

### Requirement: Generated paths are relative to project root
All path values emitted by parser tools (e.g. `benchmarks.source_path`, `fuzzy.source_path`) SHALL be relative to the project root directory, not absolute filesystem paths.

#### Scenario: Source path is portable
- **WHEN** a user inspects the rendered artifact source path on the Benchmarks or Fuzzy Testing page
- **THEN** the path is relative (e.g. `benchmarks/target/reports/suite/compare-table.txt`)
