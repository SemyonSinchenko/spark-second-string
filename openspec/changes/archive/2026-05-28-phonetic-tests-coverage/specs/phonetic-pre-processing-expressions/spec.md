## MODIFIED Requirements

### Requirement: Phonetic expressions are null-intolerant and string-typed
The system SHALL accept string input, return string output, and propagate null input to null output for `soundex`, `refined_soundex`, and `double_metaphone`.

#### Scenario: Null input propagates for each phonetic expression
- **WHEN** a phonetic expression receives a null input row value
- **THEN** the expression output is null for that row

#### Scenario: Null-intolerant contract is covered by explicit unit tests
- **WHEN** the phonetic expression unit test suite is executed
- **THEN** it includes dedicated null-propagation assertions for `soundex`, `refined_soundex`, and `double_metaphone`
