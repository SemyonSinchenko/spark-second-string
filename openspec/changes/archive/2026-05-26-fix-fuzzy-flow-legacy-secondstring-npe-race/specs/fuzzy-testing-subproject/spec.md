## MODIFIED Requirements

### Requirement: DataFrame-only evaluation pipeline
The system SHALL evaluate native metrics and legacy baseline metrics exclusively through Spark DataFrame transformations, and the fuzzy-testing SQL evaluation path SHALL invoke only registered `ss_`-prefixed SQL function names.

#### Scenario: No local collection scoring path
- **WHEN** the fuzz-testing workflow computes metric values
- **THEN** native and baseline scores are produced from Spark DataFrame operations rather than local row-by-row loops

#### Scenario: Legacy baseline uses Spark UDF wrappers
- **WHEN** baseline SecondString metrics are computed
- **THEN** each baseline score is produced through Spark UDF wrappers over DataFrame columns

#### Scenario: Legacy scorer instances are thread-confined
- **WHEN** multiple Spark task threads concurrently invoke the same legacy baseline metric UDF on an executor
- **THEN** each thread MUST use an isolated legacy SecondString scorer instance rather than a shared mutable scorer object graph
- **THEN** the flow MUST avoid cross-thread mutation of tokenizer intern pools that can corrupt shared state

#### Scenario: SQL metric calls use prefixed names
- **WHEN** fuzzy-testing SQL expressions are generated for registered native metrics
- **THEN** every invoked SQL function name starts with `ss_`
- **THEN** no unprefixed SQL function name is invoked
