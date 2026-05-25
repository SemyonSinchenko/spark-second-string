## Why

Expression case classes are currently directly constructible and expose default constructor arguments, which makes internal expression wiring part of the public API by accident. We need to tighten API boundaries so callers use `StringSimilarityFunctions` as the single supported entry point and internal constructor details can evolve safely.

## What Changes

- Make string-similarity `Expression` case classes package-private to `sparkss` so they are not public construction points.
- Remove default constructor arguments from expression case classes, including tunable-parameter expressions.
- Keep developer-facing construction through `StringSimilarityFunctions` helpers and overloads, including ergonomic defaults at the helper layer.
- **BREAKING**: direct external instantiation of expression case classes is no longer supported.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `string-sim-dsl`: tighten the API contract so expression creation is exposed through DSL helpers only, not public expression constructors.
- `configurable-metric-parameters`: preserve defaulted behavior through DSL overloads while removing default args from underlying expression constructors.

## Impact

- Affected code: Catalyst expression class visibility/constructors and `StringSimilarityFunctions` helper wiring.
- Affected API: external users relying on direct expression case-class constructors must migrate to DSL helper methods.
- Dependencies/systems: no new runtime dependencies; SQL registration remains thin and delegates through existing DSL paths.
