## 1. Bump CI test matrix (.github/workflows/ci.yml)

- [x] 1.1 Bump Spark 3.5.8 -> 3.5.9 in both test matrix lines (java-version 11 and 17)
- [x] 1.2 Bump Spark 4.0.2 -> 4.0.3 (java-version 17)
- [x] 1.3 Bump Spark 4.1.2 -> 4.1.3 (java-version 17)
- [x] 1.4 Add matrix line for Spark 4.2.0 (java-version 17)

## 2. Bump publish matrix (.github/workflows/publish-ci.yml)

- [x] 2.1 Bump Spark 3.5.8 -> 3.5.9 (java 8)
- [x] 2.2 Bump Spark 4.0.2 -> 4.0.3 (java 17)
- [x] 2.3 Bump Spark 4.1.2 -> 4.1.3 (java 17)
- [x] 2.4 Add matrix line for Spark 4.2.0 (java 17)

## 3. Verify tests pass on 4.2.x

- [x] 3.1 Run `sbt -DsparkVersion=4.2.0 scalafmtCheckAll` and `sbt -DsparkVersion=4.2.0 test` locally
- [x] 3.2 Run the fuzzy-testing suite with `-DsparkVersion=4.2.0` locally
- [x] 3.3 Fix failures surfaced by 4.2.0 (function registration API swap + fuzzy harness hardening) and re-run the suite
- [x] 3.4 Confirm all matrix cells (3.5.9, 4.0.3, 4.1.3, 4.2.0) are green in CI on the PR
