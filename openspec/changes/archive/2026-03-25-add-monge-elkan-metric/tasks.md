## 1. Token Kernel And Metric Semantics

- [x] 1.1 Add `monge_elkan` to the binary string similarity expression family with deterministic output clamped to `[0.0, 1.0]` and exact two-argument contract
- [x] 1.2 Implement token-sequence helper support that preserves token order and repeated tokens while keeping shared whitespace-tokenization behavior for existing token metrics
- [x] 1.3 Implement canonical boundary behavior for both-empty (`1.0`), one-empty (`0.0`), whitespace-only-as-empty, punctuation-preserving tokens, and NULL propagation

## 2. DSL And Registration Integration

- [x] 2.1 Expose a DSL entry point named exactly `monge_elkan` for Scala/Java usage with two string-compatible arguments
- [x] 2.2 Register `monge_elkan` in the optional SQL SparkSession extension using the same name and arity as the DSL/expression contract
- [x] 2.3 Verify interpreted and codegen execution paths are both wired for `monge_elkan` with no placeholder codegen implementation

## 3. Correctness And Parity Tests

- [x] 3.1 Add unit tests for token helper behavior covering repeated and mixed-whitespace tokenization, set-overlap primitives, and sequence-preservation semantics
- [x] 3.2 Add `monge_elkan` correctness tests for both-empty, one-empty, whitespace-only, repeated-token, punctuation-bearing, asymmetric-count, and token-order-variation scenarios
- [x] 3.3 Add interpreted-vs-codegen parity tests for direct and nested-expression evaluation, including deterministic repeatability and NULL semantics

## 4. Benchmark Coverage

- [x] 4.1 Add `monge_elkan` benchmark cohorts for short/medium/long inputs across low/medium/high overlap matrices
- [x] 4.2 Include benchmark comparison baselines against existing token metrics and selected matrix metrics
- [x] 4.3 Validate benchmark outputs are stable and reportable for release review without introducing tunables or weighted/affine variants
