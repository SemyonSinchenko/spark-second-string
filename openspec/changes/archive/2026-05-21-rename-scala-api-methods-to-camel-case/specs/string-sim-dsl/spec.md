## MODIFIED Requirements

### Requirement: DSL-first expression access
The system SHALL expose string similarity expressions as first-class Scala/Java DSL constructs.

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
