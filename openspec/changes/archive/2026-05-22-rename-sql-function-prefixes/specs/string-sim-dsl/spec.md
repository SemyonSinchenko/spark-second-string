## MODIFIED Requirements

### Requirement: Optional SQL registration extension
The system SHALL provide an optional thin SparkSession extension for SQL registration of available metrics.

#### Scenario: SQL registration as bonus path
- **WHEN** users need SQL access to already-implemented expressions
- **THEN** they SHALL be able to register functions via SparkSession extension
- **THEN** this registration layer SHALL remain thin and defer to existing DSL expression implementations
- **THEN** registration SHALL include `ss_jaccard`, `ss_sorensen_dice`, `ss_overlap_coefficient`, `ss_cosine`, `ss_braun_blanquet`, `ss_monge_elkan`, `ss_levenshtein`, `ss_lcs_similarity`, `ss_jaro`, `ss_jaro_winkler`, `ss_needleman_wunsch`, `ss_smith_waterman`, and `ss_affine_gap`
