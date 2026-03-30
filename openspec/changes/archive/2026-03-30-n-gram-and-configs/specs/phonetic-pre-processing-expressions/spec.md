## ADDED Requirements

### Requirement: Unary phonetic expressions are available
The system SHALL provide unary Catalyst expressions for `soundex`, `refined_soundex`, and `double_metaphone` that take one string input and return one encoded string output.

#### Scenario: Expression composes with similarity metrics
- **WHEN** a user evaluates a composed expression such as `jaccard(soundex(a), soundex(b))`
- **THEN** Catalyst analysis and execution succeed and return a similarity score using encoded values

### Requirement: Phonetic expressions are null-intolerant and string-typed
The system SHALL accept string input, return string output, and propagate null input to null output for all phonetic expressions.

#### Scenario: Null input propagates
- **WHEN** a phonetic expression receives a null input row value
- **THEN** the expression output is null for that row

### Requirement: Soundex implements canonical US Census behavior
The system SHALL implement Soundex as uppercase first letter plus three digits, removing non-alphabetic characters, suppressing adjacent duplicate codes, and padding/truncating to exactly four characters.

#### Scenario: Canonical Soundex pair matches
- **WHEN** a user encodes `"Robert"` and `"Rupert"`
- **THEN** both values encode to `"R163"`

### Requirement: Refined Soundex implements variable-length code output
The system SHALL implement Refined Soundex using the refined mapping table, adjacent duplicate suppression, and variable-length output without fixed-length padding.

#### Scenario: Refined Soundex produces deterministic code
- **WHEN** a user encodes the same alphabetic input under different input letter case
- **THEN** the resulting refined code is identical in both evaluations

### Requirement: Double Metaphone primary code is exposed
The system SHALL implement Double Metaphone primary-code encoding with a maximum output length of four characters and deterministic behavior for known phonetic equivalents.

#### Scenario: Phonetic equivalents align under primary code
- **WHEN** a user encodes known equivalent names such as `"Stephen"` and `"Steven"`
- **THEN** the returned primary metaphone code matches for the pair

### Requirement: SQL registrations avoid built-in name collisions
The system SHALL register SQL phonetic functions under namespaced identifiers `ss_soundex`, `ss_refined_soundex`, and `ss_double_metaphone`.

#### Scenario: Namespaced SQL function executes
- **WHEN** a user runs `SELECT ss_soundex(name) FROM people`
- **THEN** the query resolves to the library phonetic expression without conflicting with Spark built-in functions
