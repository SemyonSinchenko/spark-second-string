## Why

The project currently exposes only one token-set metric (`jaccard`). We need a single, focused change that delivers plan item 1 and plan item 2 together (Sørensen-Dice and Overlap Coefficient) so users can choose metrics tuned for overlap-heavy matching and subset-style matching without moving to matrix/edit-distance algorithms.

## What Changes

- Add two token-set similarity metrics in one change: Sørensen-Dice and Overlap Coefficient.
- Expose both metrics through the same primary DSL-first API shape used by existing metrics.
- Add optional SQL registration entries for both metrics through the existing thin SparkSession extension.
- Add algorithm-level tests, Catalyst integration tests (including interpreted/codegen parity), and benchmark coverage for both new metrics.
- Preserve existing behavior for `jaccard` and existing public APIs.

## Capabilities

### New Capabilities

- `sorensen-dice`: Provide token-set Sørensen-Dice similarity `2*|intersection|/(|A|+|B|)` with explicit constraints: score is in `[0.0, 1.0]`; both empty inputs return `1.0`; one empty input returns `0.0`; duplicate tokens do not increase cardinality; mixed/repeated whitespace is normalized by token boundaries; null inputs propagate null; generated and interpreted execution must return identical results.
- `overlap-coefficient`: Provide token-set Overlap Coefficient similarity `|intersection|/min(|A|,|B|)` with explicit constraints: score is in `[0.0, 1.0]`; both empty inputs return `1.0`; one empty input returns `0.0`; duplicate tokens do not increase cardinality; mixed/repeated whitespace is normalized by token boundaries; null inputs propagate null; generated and interpreted execution must return identical results.

### Modified Capabilities

- `string-sim-dsl`: Extend DSL function surface to include constructors/helpers for both new metrics with the same argument semantics as existing binary string metrics.
- `string-sim-expression`: Reuse the existing unified expression contract for two additional token metrics and require full interpreted/codegen parity checks for both.

## Impact

- User-facing: broader metric choice for token similarity with no breaking API change.
- Quality: new test coverage must include algorithm correctness, null propagation, nested-expression correctness, and interpreted/codegen parity for each new metric.
- Performance: add benchmark coverage for both new metrics and compare against existing token metric baseline to detect regressions.
- Out of scope: matrix-based metrics (for example Levenshtein/Damerau/Jaro), tokenizer customization, locale-aware normalization, weighted tokens, and SQL-first ergonomics redesign.
