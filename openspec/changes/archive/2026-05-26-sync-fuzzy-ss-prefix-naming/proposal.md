# Proposal: Sync new naming to fuzzy flow

## Intent
At the moment fuzzy testing fails with an error:
 [error] Exception in thread "main" org.apache.spark.sql.AnalysisException: [UNRESOLVED_ROUTINE] Cannot resolve routine `needleman_wunsch` on search path [`system`.`builtin`, `system`.`session`, `spark_catalog`.`default`]. SQLSTATE: 42883; line 1 pos 0

IRL I made a unification of the naming and in a new flow all the functions are named with prefix like ss_needleman_wunsch

## Scope
- Fix fuzzy testing
- Scan the repository for other names mismatch
- Check docs and update if needed

## What Changes: Sync new naming to fuzzy flow
Fix fuzzy-flow and sync new naming to all the subproject.

## Approach
Scan/grep for old names, patch new names. Project did not published and is in 0.0.0 version, so no need to add any kind of migrarion notes.
