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
