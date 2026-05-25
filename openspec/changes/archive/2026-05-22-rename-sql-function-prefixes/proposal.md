## Why

Current SQL function names can collide with other UDFs in shared Spark environments, and some names are inconsistent with the desired `ss_` namespace. Standardizing SQL function exposure with an `ss_` prefix avoids collisions and makes function ownership explicit.

## What Changes

- Rename SQL-exposed function names to use the `ss_` prefix consistently.
- Explicitly rename SQL names `levenshtein` -> `ss_levenshtein` and `jaro_winkler` -> `ss_jaro_winkler`.
- Apply the same SQL-only prefixing rule to all currently exposed SQL functions.
- **BREAKING**: Existing SQL queries that call unprefixed function names must migrate to the new `ss_` names.

## Capabilities

### New Capabilities
- `sql-function-prefixing`: Defines and enforces the global `ss_` SQL naming convention for all exposed functions.

### Modified Capabilities
- `levenshtein`: Updates SQL invocation requirements to use `ss_levenshtein` as the canonical SQL name.
- `jaro-winkler-similarity`: Updates SQL invocation requirements to use `ss_jaro_winkler` as the canonical SQL name.
- `string-sim-dsl`: Updates SQL function registration and discoverability requirements to reflect `ss_`-prefixed SQL names.

## Impact

- Affected code: SQL function registration paths, SQL name mapping tables, and related tests.
- Affected interfaces: Spark SQL function names used by downstream queries and documentation examples.
- Operational impact: Consumers must update SQL queries and any generated SQL strings to the new prefixed names.

## Additional information

Project in the early alfa and there is no need to add any kind of migration guide, docs section about migration, etc. The project was not publishing and the change is a part of preparation for publishing.
