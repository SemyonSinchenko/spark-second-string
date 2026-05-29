# String Similarity DSL

## Purpose

Define the primary developer-facing API for string similarity expressions through Scala/Java DSL, with optional SQL registration via thin SparkSession extension.

## Requirements

### Requirement: DSL-first expression access
The system SHALL expose string similarity expressions as first-class Scala/Java DSL constructs, and SHALL treat DSL helpers in `StringSimilarityFunctions` as the only supported public construction surface.

#### Scenario: Primary consumer mode
- **WHEN** library and platform developers integrate metrics
- **THEN** they SHALL be able to construct metrics directly from Scala/Java code without SQL registration
- **THEN** the Scala/Java DSL SHALL expose constructors/helpers named `jaccard`, `sorensenDice`, `overlapCoefficient`, `cosine`, `braunBlanquet`, `mongeElkan`, `levenshtein`, `lcsSimilarity`, `jaro`, `jaroWinkler`, `needlemanWunsch`, `smithWaterman`, and `affineGap`

#### Scenario: Monge-elkan naming and arity parity
- **WHEN** developers construct `mongeElkan` via the DSL
- **THEN** the DSL entry point SHALL use the exact helper name `mongeElkan`
- **THEN** the DSL entry point SHALL require exactly two string-compatible arguments

#### Scenario: Affine-gap naming and arity parity
- **WHEN** developers construct `affineGap` via the DSL
- **THEN** the DSL entry point SHALL use the exact helper name `affineGap`
- **THEN** the DSL entry point SHALL require exactly two string-compatible arguments

#### Scenario: Expression constructors are not a public API
- **WHEN** a consumer attempts to instantiate string-similarity expression case classes directly
- **THEN** those constructors SHALL NOT be exposed as a supported public API surface
- **THEN** equivalent expression construction SHALL be available through `StringSimilarityFunctions` helpers

### Requirement: Optional SQL registration extension
The system SHALL provide an optional thin SparkSession extension for SQL registration of available metrics.

#### Scenario: SQL registration as bonus path
- **WHEN** users need SQL access to already-implemented expressions
- **THEN** they SHALL be able to register functions via SparkSession extension
- **THEN** this registration layer SHALL remain thin and defer to existing DSL expression implementations
- **THEN** registration SHALL include `ss_jaccard`, `ss_sorensen_dice`, `ss_overlap_coefficient`, `ss_cosine`, `ss_braun_blanquet`, `ss_monge_elkan`, `ss_levenshtein`, `ss_lcs_similarity`, `ss_jaro`, `ss_jaro_winkler`, `ss_needleman_wunsch`, `ss_smith_waterman`, and `ss_affine_gap`

### Requirement: Scope boundary for this phase
The system SHALL keep advanced SQL ergonomics out of scope in this phase.

#### Scenario: Non-goal enforcement
- **WHEN** planning this phase
- **THEN** SQL-first API design, SQL-specific optimizations, and SQL-only feature surface SHALL be out of scope

### Requirement: Public DSL helpers include complete Scaladoc
The system SHALL provide meaningful Scaladoc on every public helper in `StringSimilarityFunctions` so users can understand what each metric does and how to call it correctly.

#### Scenario: Metric helper documentation coverage
- **WHEN** a developer views `StringSimilarityFunctions`
- **THEN** each public metric helper (`jaccard`, `sorensenDice`, `overlapCoefficient`, `cosine`, `braunBlanquet`, `mongeElkan`, `levenshtein`, `lcsSimilarity`, `jaro`, `jaroWinkler`, `needlemanWunsch`, `smithWaterman`, `affineGap`) SHALL include Scaladoc

#### Scenario: Documentation describes metric semantics
- **WHEN** a developer reads a metric helper Scaladoc
- **THEN** the Scaladoc SHALL describe the similarity logic or interpretation of the score for that metric

#### Scenario: Documentation defines parameters and return meaning
- **WHEN** a developer reads a metric helper Scaladoc
- **THEN** the Scaladoc SHALL document each parameter (`left`, `right`, and metric-specific parameters where present)
- **THEN** the Scaladoc SHALL state the meaning of the returned score
