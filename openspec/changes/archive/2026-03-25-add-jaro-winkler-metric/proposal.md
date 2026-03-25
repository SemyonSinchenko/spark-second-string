## Why

The metric set has `jaro` but lacks a canonical prefix-aware variant for short identifiers and person/entity names where leading-character agreement should increase similarity. Adding fixed-parameter Jaro-Winkler fills this gap with minimal API growth and deterministic behavior.

## What Changes

Add a new similarity metric named `jaro_winkler` with a fixed canonical definition. Expand validation and benchmark coverage to include Jaro-Winkler semantics and parity expectations.

## Capabilities

### New Capabilities

- `jaro-winkler-similarity`: Provide a binary string similarity metric `jaro_winkler` using canonical fixed parameters only, with explicit constraints: scaling factor `p = 0.1`, maximum common-prefix length `l = 4`, score computed as `jaro + (l * p * (1 - jaro))`, score clamped to `[0.0, 1.0]`, both-empty inputs return `1.0`, one-empty input returns `0.0`, no matching characters return `0.0`, identical strings return `1.0`, repeated characters behave deterministically, and interpreted/codegen execution must produce identical results.

### Modified Capabilities

- `string-sim-expression`: Extend supported matrix metrics to include `jaro_winkler`, preserving null propagation, deterministic semantics, and interpreted/codegen parity guarantees.
- `string-sim-dsl`: Extend Scala/Java DSL and optional SQL registration to include `jaro_winkler` with exactly two string arguments and no additional tuning parameters.

## Impact

Adds one new matrix metric without breaking existing metric behavior or API signatures. Requires updated validation and benchmark coverage for Jaro-Winkler boundary cases and representative short/medium/long overlap scenarios. Explicitly out of scope: configurable prefix length, configurable scaling factor, weighted or locale-aware variants, case-folding/normalization policy changes, and non-canonical Jaro-Winkler formulas.
