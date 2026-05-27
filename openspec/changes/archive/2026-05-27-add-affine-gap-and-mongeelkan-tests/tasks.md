## 1. Affine-gap validation coverage

- [x] 1.1 Inspect `AffineGapSuite.scala` for existing analyzer/type-check failure assertion patterns and helper usage
- [x] 1.2 Add negative tests that construct `affine_gap` with `0` and positive penalties and assert analysis/type-check rejection
- [x] 1.3 Assert stable error-message fragments indicating affine penalties must be negative

## 2. Monge-Elkan correctness and validation coverage

- [x] 2.1 Audit `MongeElkanSuite.scala` for current correctness matrix and identify missing required edge cases
- [x] 2.2 Add correctness tests for both-empty, one-empty, whitespace-only, repeated-token, punctuation-bearing, asymmetric-token-count, and token-order-difference inputs
- [x] 2.3 Add negative tests for unsupported `innerMetric` values and assert rejection with supported-values guidance

## 3. Benchmark matrix coverage for monge_elkan

- [x] 3.1 Locate benchmark suite(s) covering token similarity metrics and current `monge_elkan` benchmarking hooks
- [x] 3.2 Add benchmark cases for short, medium, and long inputs across low, medium, and high token-overlap cohorts
- [x] 3.3 Add baseline comparisons against existing token metrics and selected matrix metrics in benchmark results

## 4. Verification and follow-up

- [x] 4.1 Run targeted unit/integration suites for `AffineGap` and `MongeElkan` and confirm new coverage passes
- [x] 4.2 Run benchmark suite checks to confirm new matrix cases execute and report expected comparisons
- [x] 4.3 If any new negative tests fail, document the behavior gap and create implementation follow-up during apply
