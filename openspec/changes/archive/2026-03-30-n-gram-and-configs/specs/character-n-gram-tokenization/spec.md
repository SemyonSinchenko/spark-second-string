## ADDED Requirements

### Requirement: Token metrics support character n-gram mode
The system SHALL allow token metrics to switch tokenization mode via `ngramSize` where `0` means whitespace tokenization and values greater than `0` mean character n-gram tokenization over the full raw string.

#### Scenario: Bigrams are used when requested
- **WHEN** a user evaluates `jaccard("abcd", "abce", ngramSize = 2)`
- **THEN** the tokenizer produces `{"ab","bc","cd"}` and `{"ab","bc","ce"}` and the resulting similarity is `0.5`

### Requirement: Token metrics preserve whitespace behavior by default
The system SHALL preserve pre-existing whitespace-tokenized behavior for all token metrics when `ngramSize` is omitted or equals `0`.

#### Scenario: Default mode matches prior implementation
- **WHEN** a user calls a token metric with no `ngramSize` argument
- **THEN** tokenization and score computation are identical to the legacy whitespace-based path

### Requirement: Short-string and empty-string n-gram semantics
The system SHALL treat strings shorter than `ngramSize` as a single token, SHALL return an empty token set for empty strings, and SHALL keep existing empty-input similarity boundary behavior.

#### Scenario: Short string below n uses whole token
- **WHEN** a user evaluates `jaccard("ab", "ab", ngramSize = 3)`
- **THEN** each side contributes one token (`"ab"`) and the similarity is `1.0`

### Requirement: N-gram validation occurs at analysis time
The system SHALL reject `ngramSize` values below zero during analysis with a descriptive validation error.

#### Scenario: Negative n-gram size fails fast
- **WHEN** a user defines a token metric call with `ngramSize = -1`
- **THEN** query analysis fails before execution with an error explaining that `ngramSize` must be non-negative

### Requirement: DSL overloads expose n-gram control
The system SHALL provide DSL overloads for token metrics that accept `ngramSize` while preserving existing no-parameter overloads.

#### Scenario: Caller selects trigram mode
- **WHEN** a user calls a token metric DSL helper with `ngramSize = 3`
- **THEN** the expression is constructed in n-gram mode and executes using trigram tokenization
