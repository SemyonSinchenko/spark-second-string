## Why

The library currently offers only token-set overlap metrics, which limits matching quality for two common scenarios: (1) sparse bag-of-words comparison where vector-angle scoring is preferred, and (2) character-level typo/ordering comparison where edit-distance behavior is required. Adding one token-set cosine metric and one matrix-based normalized Levenshtein metric expands practical coverage while validating the matrix metric family path that is already present in the expression contract.

## What Changes

- Add a new token-set similarity capability for Cosine using the same whitespace token boundary and token deduplication policy as existing token metrics.
- Add a new matrix-based similarity capability for normalized Levenshtein to establish first-class `MatrixMetricExpression` usage.
- Define a minimal shared matrix helper/abstraction for reusable matrix-metric primitives so future matrix metrics do not duplicate core boundary logic.
- Extend DSL and optional SQL registration to expose both new metrics in the same style as existing functions.
- Extend test and benchmark coverage to include algorithm-level edge cases, Catalyst-level null/codegen parity, and representative benchmark scenarios for both new metrics.

## Capabilities

### New Capabilities

- `cosine`: Provide token-set cosine similarity with deterministic bounds and constraints: score range is [0.0, 1.0]; both empty inputs return 1.0; one empty and one non-empty returns 0.0; duplicate tokens do not change cardinality; mixed/repeated whitespace yields equivalent tokenization; null input on either side propagates null.
- `levenshtein`: Provide normalized matrix-based Levenshtein similarity with deterministic bounds and constraints: score range is [0.0, 1.0]; normalization is explicit and stable across interpreted and codegen paths; identical strings return 1.0; both empty inputs return 1.0; one empty and one non-empty returns 0.0; null input on either side propagates null.
- `matrix-metric-kernel`: Provide a reusable matrix-metric helper abstraction that centralizes shared matrix boundary behavior and parity constraints for interpreted and generated execution.

### Modified Capabilities

- `string-sim-expression`: Extend contract usage coverage to include concrete matrix metric implementation and enforce interpreted/codegen parity for matrix metrics in addition to token metrics.
- `string-sim-dsl`: Extend DSL and optional SQL registration surface with `cosine` and `levenshtein` functions while preserving existing function behavior and naming stability.

## Impact

- Broader metric coverage for token and character-level similarity use-cases without changing existing metric outputs.
- Clearer matrix-family extension path for future additions (for example Damerau-Levenshtein, Jaro, Jaro-Winkler) through shared helper semantics.
- Increased test and benchmark scope; no API removals, no SQL-first redesign, no tokenizer configurability, no weighted-token behavior, and no locale-specific normalization in this change.
