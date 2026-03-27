## Why

Current fuzzy-test input generation is structurally biased: every pair shares a fixed 4-character prefix from the same hash source. This reduces input diversity, inflates repeated patterns, and weakens the ability of parity reports to expose real metric behavior on truly random and adversarial string pairs.

## What Changes

Replace the current deterministic-but-structured pair builder with a deterministic randomized case generator that produces diverse pair relationships without any always-on shared-prefix rule.

The generator contract must stay seed-reproducible, row-count stable, and Spark DataFrame based.

## Capabilities

### New Capabilities

- `fuzzy-random-case-generation`: Fuzzy testing can generate deterministic random pair cohorts with explicit diversity constraints (exact-match, high-overlap, medium-overlap, low-overlap, disjoint, asymmetric-length, whitespace/punctuation-heavy, repeated-character, and empty-string cases) and with zero globally forced shared-prefix structure.

### Modified Capabilities

- `fuzzy-testing-subproject`: Input generation semantics change from fixed hash-slice composition to seed-driven randomized case construction; outputs remain deterministic for the same `(seed, rows)`, but distribution shape is intentionally broader and less correlated by construction.

## Impact

- Improves parity signal quality by reducing generator-induced artifacts and increasing coverage of realistic and adversarial string relationships.
- Existing trend comparisons against old reports may shift because input distribution changes; historical runs should not be interpreted as directly equivalent cohorts.
- Out of scope: metric algorithm changes, scaling-policy changes, report-format changes, pass/fail gating, and any non-deterministic runtime randomness.
