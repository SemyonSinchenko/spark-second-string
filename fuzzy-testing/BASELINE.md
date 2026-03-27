# Fuzzy Random Case Baseline

Post-change baseline expectations for deterministic random-case generation:

- Run `FuzzyTestingCli` with fixed seeds and row count to smoke-test report generation.
- For the same `(seed, rows)`, generated rows and case composition must be identical.
- For different seeds with the same `rows`, generated rows and case composition must differ.
- The generator defaults to `random-cases` mode and keeps `legacy-prefix` available as a temporary fallback through `sparkss.fuzzy.generator.mode`.

Smoke-check commands used during this change:

```bash
sbt "fuzzy-testing/runMain io.github.semyonsinchenko.sparkss.fuzzy.FuzzyTestingCli --seed 42 --rows 200 --out /tmp/fuzzy-seed42.md"
sbt "fuzzy-testing/runMain io.github.semyonsinchenko.sparkss.fuzzy.FuzzyTestingCli --seed 43 --rows 200 --out /tmp/fuzzy-seed43.md"
```

Automated baseline coverage is enforced in `FuzzyTestingSuite` with deterministic/cohort tests and fallback-mode tests.

## Excluded metrics

### AffineGap

AffineGap is excluded from the fuzzy-testing parity comparison because SecondString's
`com.wcohen.secondstring.AffineGap` implements a fundamentally different algorithm from the
native affine-gap edit distance:

| Aspect | Native | SecondString |
|---|---|---|
| Algorithm type | Global edit distance (minimize) | Semi-local alignment score (maximize) |
| Score extraction | Bottom-right cell | Max over entire DP matrix |
| Match scoring | Mismatch cost = 1 | Match = +2, mismatch = -1 (DIST_21) |
| Gap scoring | Open = 2, extend = 1 (costs) | Open = +2, extend = +1 (additive rewards) |
| Border conditions | Cumulative gap penalties | Zeros (free restarts, Smith-Waterman-style) |
| Known bugs | None | `InsertTMatrix` always reads row 1 instead of row `i-1` |

Consequences:
- Completely disjoint strings score high in SecondString (semi-local alignment finds
  positive-scoring sub-paths) but 0.0 in native (maximum edit distance).
- Repeated characters inflate SecondString scores beyond `2 * maxLength` due to the row-1 bug.
- No analytical scaler can map SecondString raw output to native normalized output because
  the DP recurrences, boundary conditions, and score semantics are all different.

The native AffineGap implementation is a correct affine-gap edit distance and remains
available as a Spark SQL function. Only the fuzzy-testing parity comparison is excluded.
