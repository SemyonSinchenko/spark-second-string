## 1. Scope and baseline

- [x] 1.1 Inventory all public DSL helpers in `StringSimilarityFunctions.scala` and confirm the required metric list matches the spec.
- [x] 1.2 Capture current Scaladoc coverage gaps for each helper (`jaccard`, `sorensenDice`, `overlapCoefficient`, `cosine`, `braunBlanquet`, `mongeElkan`, `levenshtein`, `lcsSimilarity`, `jaro`, `jaroWinkler`, `needlemanWunsch`, `smithWaterman`, `affineGap`).

## 2. Add consistent Scaladoc content

- [x] 2.1 Add or expand Scaladoc for each listed helper with a clear metric summary and score interpretation.
- [x] 2.2 Add complete `@param` docs for `left`, `right`, and any metric-specific parameters on every helper.
- [x] 2.3 Add `@return` docs that describe result meaning and keep terminology/range phrasing consistent across helpers.

## 3. Validate and finalize

- [x] 3.1 Review generated/IDE-visible Scaladoc formatting to ensure comments render correctly and remain readable.
- [x] 3.2 Run compile and relevant checks to confirm the documentation-only update introduces no API or behavior changes.
- [x] 3.3 Do a final pass against spec/design requirements and adjust wording for accuracy where edge-case interpretation could be unclear.
