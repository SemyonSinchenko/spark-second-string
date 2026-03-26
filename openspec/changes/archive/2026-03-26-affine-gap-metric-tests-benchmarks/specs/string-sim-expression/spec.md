## MODIFIED Requirements

### Requirement: Metric family support
The unified contract SHALL support both token-based and matrix-based metric implementations.

#### Scenario: Token metric compatibility
- **WHEN** a token-based metric is implemented
- **THEN** it SHALL conform to the unified binary expression contract
- **THEN** the contract SHALL support multiple token metrics including `jaccard`, `sorensen_dice`, `overlap_coefficient`, `cosine`, `braun_blanquet`, and `monge_elkan`

#### Scenario: Matrix metric compatibility
- **WHEN** a matrix-based metric is implemented
- **THEN** it SHALL conform to the unified binary expression contract
- **THEN** the contract SHALL support normalized matrix metrics including `levenshtein`, `lcs_similarity`, `jaro`, `jaro_winkler`, `needleman_wunsch`, `smith_waterman`, and `affine_gap`

### Requirement: Interpreted and codegen parity
String similarity expressions SHALL provide consistent behavior between interpreted evaluation and generated code.

#### Scenario: Deterministic parity
- **WHEN** interpreted and code-generated paths are both available
- **THEN** both paths SHALL return identical results for identical inputs, including edge cases

#### Scenario: Incomplete codegen is forbidden
- **WHEN** a metric declares code-generation support
- **THEN** its codegen implementation SHALL be complete and executable
- **THEN** it SHALL NOT return placeholder or null code objects

#### Scenario: Per-metric parity enforcement
- **WHEN** token and matrix metrics are expanded
- **THEN** parity validation SHALL include `jaccard`, `sorensen_dice`, `overlap_coefficient`, `cosine`, `braun_blanquet`, `monge_elkan`, `levenshtein`, `lcs_similarity`, `jaro`, `jaro_winkler`, `needleman_wunsch`, `smith_waterman`, and `affine_gap`

#### Scenario: Nested-expression and null parity
- **WHEN** `affine_gap` is evaluated inside nested Catalyst expressions under interpreted and code-generated execution
- **THEN** both execution paths SHALL produce identical results for identical non-null inputs
- **THEN** both execution paths SHALL preserve identical NULL propagation semantics
