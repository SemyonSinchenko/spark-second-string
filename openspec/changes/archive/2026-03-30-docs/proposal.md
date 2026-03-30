## Why

The project needs first-class documentation so users can quickly understand where spark-native string metrics fit in identity-resolution pipelines and adopt them as a low-cost similarity stage before heavier model-based methods.

## What Changes

Create a documentation capability centered on a repository README and a docs subproject that publishes structured documentation for usage, metric coverage, quality validation, and performance results.

## Capabilities

### New Capabilities

- `docs-foundation`: Provide a top-level `README` with project overview and motivation, add a `/docs` source tree plus docs build subproject, use Typelevel Laika with mostly default Helium theme, define one markdown file per required section (Overview, Quick Start, Existing Metrics, Fuzzy Testing, Benchmarks), add project-level tooling that parses benchmark output and fuzzy-test comparison output into Laika variables consumed by the Benchmarks and Fuzzy Testing pages, and require docs build to assume benchmark and fuzzy-test outputs already exist.

### Modified Capabilities

- `build-lifecycle`: Extend repository build expectations so documentation generation is part of the build graph and is explicitly constrained by pre-run benchmark and fuzzy-test data availability; docs build behavior when prerequisite outputs are missing must be defined and documented.

## Impact

Establishes a maintained docs surface for onboarding and evaluation, formalizes docs as a build concern, and constrains scope to documentation and docs-data ingestion only (no new runtime string metrics, no algorithm changes, no extension of benchmark or fuzzy-test execution logic beyond reading their outputs).
