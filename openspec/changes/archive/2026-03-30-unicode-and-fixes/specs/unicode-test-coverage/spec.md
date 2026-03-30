## ADDED Requirements

### Requirement: Unicode and multi-byte metric correctness coverage
The system SHALL validate deterministic and bounded similarity behavior for non-ASCII inputs across matrix, token, and phonetic metric families.

#### Scenario: Latin diacritics and normalization-variant inputs are exercised
- **WHEN** test suites evaluate strings containing Latin diacritics and decomposed/pre-composed forms
- **THEN** every metric family SHALL execute these cases without runtime errors
- **THEN** score outputs SHALL remain deterministic and bounded within `[0.0, 1.0]`

#### Scenario: CJK, emoji, and mixed-script inputs are exercised
- **WHEN** test suites evaluate CJK characters, emoji, and mixed-script strings
- **THEN** every metric family SHALL execute these cases without runtime errors
- **THEN** score outputs SHALL remain deterministic and bounded within `[0.0, 1.0]`

### Requirement: ResolvedStrings fallback-path contract coverage
The system SHALL test `ResolvedStrings` behavior for non-ASCII UTF-8 inputs to ensure the multi-byte fallback path obeys its contract.

#### Scenario: ASCII detection falls back for multi-byte content
- **WHEN** `ResolvedStrings` is initialized with multi-byte UTF-8 strings
- **THEN** the implementation SHALL mark the input path as non-ASCII
- **THEN** character access SHALL use the fallback decoding path rather than raw-byte ASCII access

#### Scenario: Character-length and character-access semantics are validated
- **WHEN** `ResolvedStrings` receives multi-byte inputs
- **THEN** `leftLength` and `rightLength` SHALL report character counts rather than UTF-8 byte lengths
- **THEN** `leftCharAt` and `rightCharAt` SHALL return deterministic character values for indexed access

### Requirement: Unicode tokenization behavior coverage
The system SHALL validate token-metric behavior for Unicode token-boundary edge cases.

#### Scenario: Non-ASCII boundaries and separators are covered
- **WHEN** token metrics evaluate strings containing non-ASCII whitespace and non-ASCII word characters
- **THEN** tokenization behavior SHALL be tested and asserted as deterministic

#### Scenario: Zero-width and no-whitespace scripts are covered
- **WHEN** token metrics evaluate inputs containing zero-width characters and CJK text without whitespace
- **THEN** tokenization SHALL avoid phantom empty tokens
- **THEN** observed token-boundary behavior SHALL be asserted explicitly in tests
