# Proposal: extend tests for some corner cases

## Intent
At the moment there is no AffineGap positive-penalty rejection test: must be < 0 / rejects / TypeCheckFailure / AnalysisException in AffineGapSuite.scala; As well there is no MongeElkan invalid-innerMetric rejection test: SupportedInner / rejection patterns in MongeElkanSuite.scala

## Scope
- Extend AffineGap tests coverage
- Extend MongeElkan tests coverage
- Report/analyze/investgigate if new tests are failing

## What Changes: extend tests for some corner cases
Extend tests coverage

## Approach
New tests and nothing else if new tests are "green".
