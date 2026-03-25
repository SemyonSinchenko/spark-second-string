## Why

The library currently misses two useful similarity signals: a max-denominator token-set metric for subset-heavy matching and a sequence-aware matrix metric for order-sensitive matching. Adding one token metric and one matrix metric in the same change broadens practical coverage while validating cross-family parity requirements under a single review cycle.

## What Changes

- Add a new token-set similarity metric `braun_blanquet` with deterministic set semantics and bounded output in `[0.0, 1.0]`.
- Add a new matrix similarity metric `lcs_similarity` based on longest common subsequence length with normalized output in `[0.0, 1.0]`.
- Refactor duplicated token-kernel behavior into a shared helper capability used by token metrics to enforce one tokenization and set-intersection policy.
- Extend DSL and optional SQL registration to expose `braun_blanquet` and `lcs_similarity` without renaming or changing existing functions.
- Preserve contract-level behavior across all metrics: null propagation, deterministic empty-input handling, and interpreted/codegen parity.
- Keep scope strictly additive at the API level for new metrics and behavior-preserving for existing metrics.

## Capabilities

### New Capabilities
- `braun-blanquet`: Provide token-set Braun-Blanquet similarity `|A∩B| / max(|A|,|B|)` with constraints: duplicate tokens do not affect cardinality, whitespace-equivalent inputs tokenize identically, `""` with `""` returns `1.0`, one empty and one non-empty returns `0.0`, and score is always in `[0.0, 1.0]`.
- `lcs-similarity`: Provide normalized LCS similarity `LCS(left,right) / max(len(left), len(right))` with constraints: identical strings return `1.0`, `""` with `""` returns `1.0`, one empty and one non-empty returns `0.0`, and score is always in `[0.0, 1.0]`.
- `token-metric-kernel`: Provide a shared token-kernel contract for whitespace tokenization and set-overlap primitives so token metrics reuse one deterministic policy and avoid drift.

### Modified Capabilities
- `string-sim-expression`: Extend family coverage and parity requirements to include `braun_blanquet` and `lcs_similarity`, with explicit preservation of null propagation and interpreted/codegen output equivalence.
- `string-sim-dsl`: Extend DSL and optional SQL registration surfaces with `braun_blanquet` and `lcs_similarity` while keeping existing names and behaviors stable.

## Impact

- Broader matching coverage for subset-like token comparisons and order-sensitive string comparisons.
- Lower maintenance risk by removing duplicated token-kernel logic across token metrics.
- Additional spec, test, and benchmark scope for the two new metrics and shared token-kernel behavior.
- No breaking API removals; existing metric outputs remain unchanged.
- Out of scope: configurable tokenization, token-frequency weighting, locale/case normalization changes, SQL-first redesign, and additional metrics beyond `braun_blanquet` and `lcs_similarity`.
