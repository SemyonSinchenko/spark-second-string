## Why

The current string-similarity expression foundation is brittle and does not scale to multiple metric families. It conflates Catalyst expression concerns with algorithm logic, has an incomplete code-generation contract, and makes future matrix-based metrics risky to add.

## What Changes

- Refactor the string-similarity expression contract to cleanly separate Catalyst expression behavior from metric algorithm behavior.
- Standardize compatibility requirements so token-based and matrix-based metrics share one consistent base contract.
- Define a code-generation-first contract for metrics, with explicit parity expectations between interpreted and generated execution.
- Set API direction to DSL-first (Scala/Java) with optional SQL registration via a thin SparkSession extension.
- Keep SQL-first ergonomics and advanced SQL integration out of scope for this change.

## Capabilities

### New Capabilities

- `matrix-string-similarity-metrics`: Introduce first-class support for matrix-based string metrics under the same expression framework; enforce deterministic scoring, null propagation consistency, and interpreted/codegen behavioral parity.
- `spark-session-extension-registration`: Provide an optional thin SparkSession extension for registering similarity expressions as SQL functions without changing the DSL-first usage model.

### Modified Capabilities

- `string-sim-expression`: Replace the current base abstraction with a stricter contract that separates expression lifecycle from metric kernels, requires correct child-expression evaluation semantics, and supports both token and matrix metric families.
- `jaccard`: Re-specify Jaccard as a token metric built on the new contract, including complete codegen behavior and parity guarantees with interpreted execution.

## Impact

- Improves correctness guarantees and reduces hidden runtime breakage risk in Catalyst integration.
- Enables adding new metrics without rewriting expression boilerplate per algorithm family.
- Establishes a stable DSL-first API for library and platform developers while preserving optional SQL exposure.
- Increases short-term refactor effort and test scope due to stricter contracts and parity requirements.
