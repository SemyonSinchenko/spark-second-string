# Proposal: Extend tests coverage for the SQL flow

## Intent
At the moment registered SQL functions are not tested properly. Only 2 of 16 registered SQL functions are tested properly for SparkSecondStringExtensionSuite. To avoid accidently breaking of the public API I want to add end2end tests for all of them to avoid any kind of breaking in future refactorings.

## Scope
- Extend tests for SparkSecondStringExtensionSuite

## What Changes: Extend tests coverage for the SQL flow
.

## Approach
Cover all the SQL functions with one simplest but e2e test for each of them (corner cases and logic are tested in test suites of underlying expressions.)
