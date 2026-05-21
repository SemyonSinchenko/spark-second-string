## Why

The fuzzy harness currently validates only four metrics (`needleman_wunsch`, `smith_waterman`, `jaro_winkler`, `monge_elkan`) against a SecondString baseline, leaving nine implemented metrics without external parity checks. Adding baseline coverage now reduces regression risk and improves confidence in metric correctness before broader adoption.

## What Changes

- Extend the fuzzy harness to compare additional metrics against SecondString where equivalent implementations exist.
- Add baseline checks for currently unvalidated metrics, including token-based metrics, plain `jaro`, `levenshtein`, `lcs`, and `affine_gap`.
- Keep existing checked metrics unchanged while making baseline wiring reusable and mechanically extensible for future metrics.

## Capabilities

### New Capabilities
- `expanded-secondstring-baseline-validation`: Validate a broader set of fuzzy metrics against SecondString reference outputs in the harness.

### Modified Capabilities
- None.

## Impact

- Affected code: fuzzy harness test/comparison logic and metric mapping/normalization used by baseline checks.
- APIs: no public API changes expected; this is validation-scope expansion.
- Dependencies/systems: continued use of SecondString as the external reference baseline; CI test runtime may increase modestly due to added comparisons.
