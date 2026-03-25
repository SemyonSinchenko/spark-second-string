## 1. Metric implementation and registration

- [x] 1.1 Add `jaro_winkler` to the centralized metric registry/validation path used by matrix similarity expressions.
- [x] 1.2 Implement canonical Jaro-Winkler scoring on top of the existing Jaro core using fixed parameters (`p = 0.1`, prefix cap `4`) and clamp output to `[0.0, 1.0]`.
- [x] 1.3 Ensure null propagation and deterministic behavior match existing matrix metric semantics.

## 2. Expression and DSL/SQL integration

- [x] 2.1 Wire `jaro_winkler` through expression evaluation so interpreted execution resolves and computes the metric.
- [x] 2.2 Wire `jaro_winkler` through codegen/shared helpers to preserve interpreted/codegen parity.
- [x] 2.3 Expose `jaro_winkler` in Scala/Java DSL builders and optional SQL registration extension alongside existing metrics.

## 3. Correctness and parity tests

- [x] 3.1 Add correctness tests for required boundary cases: both-empty, one-empty, no-match, identical, repeated characters, and asymmetric lengths.
- [x] 3.2 Add interpreted vs codegen parity tests for `jaro_winkler` across representative inputs.
- [x] 3.3 Extend metric-family contract/validation tests to include `jaro_winkler` where metric enums/lists are asserted.

## 4. Benchmarks and validation

- [x] 4.1 Add `jaro_winkler` benchmark entries for short/medium/long string pairs with both high-overlap and low-overlap cases.
- [x] 4.2 Run existing test and benchmark suites relevant to string similarity and resolve regressions.
- [x] 4.3 Verify no API signature changes were introduced beyond additive `jaro_winkler` support.
