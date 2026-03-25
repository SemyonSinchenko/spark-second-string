## Why

The library still has a parity gap with legacy Java SecondString for local-alignment similarity. Adding `smith_waterman` closes that gap and complements existing global-alignment coverage (`needleman_wunsch`) for partial-substring matching cases.

## What Changes

- Introduce a new matrix similarity metric named `smith_waterman` with fixed, canonical semantics and deterministic output in `[0.0, 1.0]`.
- Extend DSL and optional SQL registration surfaces so `smith_waterman` has naming and arity parity with the existing metric roster.
- Expand validation scope to include correctness, boundary behavior, null propagation, nested-expression evaluation, and interpreted/codegen parity for `smith_waterman`.
- Expand benchmark coverage with a dedicated `smith_waterman` benchmark and matrix-metric baselines across short/medium/long inputs and low/medium/high overlap cohorts.
- Explicitly define out-of-scope items for this change: affine-gap variants, Monge-Elkan variants, token/statistical metrics (`tfidf`, `soft_tfidf`), configurable scoring parameters, and any learner/training APIs.

## Capabilities

### New Capabilities

- `smith-waterman-similarity`: Provide a canonical binary local-alignment string similarity metric (`smith_waterman`) with strict boundary rules (`""`/`""` -> `1.0`, one-empty -> `0.0`), deterministic behavior, null propagation via existing expression contracts, and parity validation between interpreted and codegen execution.

### Modified Capabilities

- `string-sim-expression`: Extend the supported matrix metric family and per-metric parity coverage to include `smith_waterman` while preserving existing binary-expression contracts.
- `string-sim-dsl`: Add Scala/Java DSL constructors and optional SQL registration wiring for `smith_waterman` with the same two-argument contract as other metrics.

## Impact

- Improves functional parity with legacy Java SecondString in a high-value metric family (local alignment).
- Increases test and benchmark scope to reduce semantic and performance regression risk.
- Does not change existing metric semantics or introduce training/stateful APIs.
