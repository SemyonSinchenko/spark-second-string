## Why

The library still has a parity gap with legacy Java SecondString for token-level soft matching. Adding `monge_elkan` closes a high-value gap for entity/name matching cases where exact token overlap metrics under-score near-matches and matrix metrics over-penalize token reordering.

## What Changes

- Add a canonical binary string similarity metric named `monge_elkan` with deterministic output normalized to `[0.0, 1.0]`.
- Expose `monge_elkan` across existing API surfaces with strict naming and arity parity (DSL-first; optional SQL registration remains secondary).
- Expand correctness, interpreted-vs-codegen parity, and benchmark coverage to include `monge_elkan`.
- Explicitly define semantic boundaries and non-goals to prevent ambiguous behavior and scope creep in this iteration.

## Capabilities

### New Capabilities

- `monge-elkan-similarity`: Introduce `monge_elkan` as a two-input string similarity metric that is deterministic, null-propagating via existing expression contracts, and bounded to `[0.0, 1.0]`; enforce explicit edge-case behavior for both-empty (`1.0`), one-empty (`0.0`), whitespace-only inputs, repeated tokens, punctuation-bearing tokens, asymmetric token counts, and token order differences; keep scoring semantics fixed in this phase (no runtime tunables).

### Modified Capabilities

- `string-sim-expression`: Extend the unified binary expression contract to include `monge_elkan` with identical interpreted/codegen results, complete codegen support (no placeholders), and full parity validation under nested-expression and null-input scenarios.
- `string-sim-dsl`: Add DSL and optional SQL registration support for `monge_elkan` with exact two-argument contract and naming parity with existing metrics.
- `token-metric-kernel`: Extend token-metric validation coverage to include `monge_elkan` boundary/consistency behavior and benchmark baselines versus existing token metrics and selected matrix baselines.

## Impact

- Improves parity with legacy Java SecondString by adding a missing soft token similarity family member.
- Expands test and benchmark obligations for `monge_elkan` across short/medium/long inputs and low/medium/high token overlap cohorts.
- Preserves backward compatibility: additive metric only; no behavior changes to existing metrics.
- Out of scope for this change: affine/weighted Monge-Elkan variants, configurable token-level inner metric knobs, corpus-trained/statistical metrics (`tfidf`, `soft_tfidf`), learner/training APIs, and non-DSL-first API redesign.
