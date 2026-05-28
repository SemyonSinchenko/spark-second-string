# Proposal: Phonetic tests coverage

## Intent
At the moment phonetic encoders lack null-propagation unit tests. Soundex / RefinedSoundex / DoubleMetaphone get covered only implicitly via StringSimExpressionSuite. Add explicit null tests; the NullIntolerant contract is the one thing Spark optimizer relies on.

## Scope
- Extend tests coverage for phonetic
- Investigate the root in case new tests are failing

## What Changes: Phonetic tests coverage
New tests and any kind of fix or investigation only in case of failing tests.

## Approach
Extend the tests suite.
