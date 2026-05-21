## Why

The Scala helper API in `StringSimilarityFunctions` mixes snake_case with camelCase, which is inconsistent with Scala naming conventions and makes the DSL harder to use predictably from Scala/Java code. We need to standardize these public method names now to keep the API coherent as metric coverage and downstream usage continue to grow.

## What Changes

- Rename Scala helper methods in `StringSimilarityFunctions` from `monge_elkan` to `mongeElkan` and from `affine_gap` to `affineGap`, including all `Column` and `String` overloads.
- Update internal Scala call sites and tests to use the new camelCase helper names.
- Keep SQL function names and metric/report identifiers in snake_case exactly as they are today (for example `monge_elkan`, `affine_gap`, `needleman_wunsch`).
- Update docs so Scala DSL examples use camelCase helpers while SQL examples remain snake_case.
- **BREAKING**: Scala/Java callers using `monge_elkan` or `affine_gap` helper methods must migrate to `mongeElkan` and `affineGap`.

## Capabilities

### New Capabilities
- `scala-helper-api-camelcase`: Defines standardized camelCase naming for Scala helper methods in the string similarity DSL while preserving SQL-facing snake_case names.

### Modified Capabilities
- `string-sim-dsl`: Update public DSL requirements for metric helper naming and compatibility expectations for Scala/Java callers.

## Impact

- Affected code: Scala DSL helper definitions, Scala test call sites, fuzzy testing internal code identifiers, and user-facing docs.
- Affected APIs: Public Scala/Java helper API is breaking for `monge_elkan` and `affine_gap`; SQL API remains backward compatible.
- Dependencies/systems: No external dependency changes; no SQL registration or metric-ID contract changes.
