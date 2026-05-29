## ADDED Requirements

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
