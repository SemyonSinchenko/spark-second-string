# Proposal: Unicode Test Coverage, Penalty Convention Fix, SmithWaterman Tests, and Property-Based Testing

## Why

The current test suite validates all metrics exclusively with ASCII inputs. The `ResolvedStrings` class in `MatrixMetricKernelHelper` has a dual code path: an ASCII fast-path that operates on raw bytes and a fallback path that decodes to Java `String` for multi-byte UTF-8. The fallback path has zero test coverage. Identity resolution workloads routinely encounter diacritics (cafe vs cafe), CJK names, and mixed-script inputs, making this a real correctness risk area rather than a theoretical concern.

Additionally, the penalty parameter conventions are inconsistent across alignment metrics: `NeedlemanWunsch` and `SmithWaterman` accept negative penalties (score-maximization semantics), while `AffineGap` requires positive penalties (cost-minimization semantics). Both are internally correct, but the API is confusing for users who configure multiple alignment metrics in the same pipeline.

SmithWaterman is the only metric in the library without a dedicated unit test suite. It is tested only indirectly through integration tests in `StringSimExpressionSuite` (null propagation, codegen parity) and benchmarks (which do not validate correctness).

Finally, the existing test suites rely entirely on example-based assertions with hand-computed expected values. This approach cannot efficiently cover the combinatorial space of string pairs and is vulnerable to coincidental correctness (a wrong formula that happens to produce the right answer for the chosen examples).

## What Changes

- Add Unicode and multi-byte character test cases across all metric families (matrix, token, phonetic).
- Harmonize the penalty parameter convention between `AffineGap` and `NeedlemanWunsch`/`SmithWaterman`, or document the divergence explicitly in the API surface.
- Add a dedicated `SmithWatermanSuite` with the same depth as existing matrix metric test suites.
- Add property-based test suites that validate algebraic invariants (symmetry, identity, bounds, monotonicity) across all metrics.

## Capabilities

### New Capabilities

- `unicode-test-coverage`: Verify that all metrics produce correct, deterministic results for non-ASCII inputs including Latin diacritics, CJK characters, emoji, combining characters, and mixed-script strings. Verify that the ASCII fast-path in `ResolvedStrings` correctly detects multi-byte content and falls back to String decoding. Verify that `leftLength`/`rightLength` report character counts (not byte counts) for multi-byte inputs. Verify that token metrics correctly tokenize strings containing non-ASCII whitespace and non-ASCII word characters.

- `smith-waterman-test-suite`: Provide a dedicated `SmithWatermanSuite` covering: identical strings (1.0), both-empty (1.0), one-empty (0.0), disjoint strings (0.0), canonical local alignment examples with hand-computed expected values, asymmetric-length inputs, repeated characters, determinism, and score boundedness within [0.0, 1.0].

- `property-based-test-suite`: Provide property-based tests using random or generator-driven string pairs that validate algebraic invariants for every metric:
  - **Identity**: `sim(a, a) == 1.0` for all non-empty strings.
  - **Bounds**: `0.0 <= sim(a, b) <= 1.0` for all inputs.
  - **Symmetry**: `sim(a, b) == sim(b, a)` for symmetric metrics (all token metrics, Levenshtein, LCS, Jaro, JaroWinkler, NeedlemanWunsch with symmetric scoring). Note: MongeElkan is already symmetric by construction (bidirectional average).
  - **Empty identity**: `sim("", "") == 1.0` and `sim("", x) == 0.0` for non-empty `x`.
  - **Monotonicity** (where applicable): Adding a character to a matching prefix does not decrease Jaro/JaroWinkler scores.

### Modified Capabilities

- `string-sim-expression`: Extend the integration test `StringSimExpressionSuite` to include at least one non-ASCII test row in the test DataFrame, verifying that interpreted and codegen paths produce identical results for multi-byte inputs.

- `matrix-metric-kernel`: Update `MatrixMetricKernelHelper` documentation and/or tests to explicitly state the contract for `ResolvedStrings` behavior with multi-byte UTF-8 inputs: `leftLength`/`rightLength` must return character count, `leftCharAt`/`rightCharAt` must return Unicode code points (or at minimum, `char` values as ints).

- `string-sim-dsl`: If the penalty convention is harmonized (preferred approach), update the `StringSimilarityFunctions` API surface for `affineGap` to accept negative penalties consistent with `needlemanWunsch` and `smithWaterman`. If the convention is kept divergent, add explicit Scaladoc on each method documenting the expected sign convention.

## Impact

### Correctness

- Eliminates the blind spot on the `ResolvedStrings` multi-byte fallback path. Today, a bug in the `isAscii=false` branch (e.g., returning byte length instead of character count, or mishandling surrogate pairs) would go undetected by all existing tests.
- Closes the SmithWaterman unit test gap so that local alignment scoring, normalization, and edge-case behavior are validated directly rather than inferred from integration tests.
- Property-based tests guard against a class of bugs that example-based tests structurally cannot catch: formulas that are wrong by a constant factor, symmetry violations from index ordering bugs, and clamping failures on unusual input distributions.

### Usability

- Harmonizing penalty conventions (or prominently documenting the divergence) prevents user confusion when switching between `needlemanWunsch`, `smithWaterman`, and `affineGap` in the same pipeline. The current state requires users to pass `mismatchPenalty = -1` for NeedlemanWunsch but `mismatchPenalty = 1` for AffineGap to express the same intent, which is error-prone.

### Backward Compatibility

- Unicode tests and property-based tests are purely additive test code; no production behavior changes.
- SmithWatermanSuite is purely additive test code.
- If the penalty convention is harmonized for AffineGap, this is a **breaking API change**: existing code passing `affineGap(col1, col2, mismatchPenalty = 1, gapOpenPenalty = 2, gapExtendPenalty = 1)` would need to change to `affineGap(col1, col2, mismatchPenalty = -1, gapOpenPenalty = -2, gapExtendPenalty = -1)`. The `checkInputDataTypes` validation would reject old-style positive values at analysis time (fail-fast, not silent breakage). Given the library is pre-1.0, this is acceptable.

### Out of Scope

- Locale-aware normalization (e.g., Turkish dotless-i, German eszett expansion) is not addressed; metrics operate on raw character sequences.
- Full Unicode normalization (NFC/NFD) is not addressed; callers are responsible for normalizing inputs before comparison.
- Surrogate pair handling for supplementary plane characters (emoji, rare CJK) is documented as best-effort; `ResolvedStrings.leftCharAt` returns `Char`-based values, not full code points, which means supplementary characters may be split across two indices. Fixing this would require a deeper refactor of the character access pattern.
- ScalaCheck or other property-based testing framework selection is left to the design phase.
- Benchmark updates for Unicode inputs are not included in this change.

## Approach

### 1. Unicode Test Coverage

Add a new cross-cutting test suite `UnicodeSuite` (or per-family suites: `UnicodeMatrixMetricsSuite`, `UnicodeTokenMetricsSuite`, `UnicodePhoneticSuite`) that exercises every metric with the following input categories:

**Latin diacritics:**
- `"cafe"` vs `"cafe"` (identical ASCII, baseline)
- `"caf\u00e9"` vs `"cafe"` (pre-composed e-acute vs plain e, expected: high but not 1.0)
- `"caf\u0065\u0301"` vs `"caf\u00e9"` (NFD decomposed vs NFC pre-composed, documents current behavior)
- `"resume"` vs `"r\u00e9sum\u00e9"` (multiple diacritics)
- `"M\u00fcller"` vs `"Mueller"` (German umlaut vs digraph expansion)

**CJK characters:**
- `"\u6771\u4eac"` vs `"\u6771\u4eac"` (identical, Tokyo in kanji, expected: 1.0)
- `"\u6771\u4eac"` vs `"\u5927\u962a"` (different, Tokyo vs Osaka, expected: 0.0 for token metrics, low for matrix)
- `"\u6771\u4eac \u90fd"` vs `"\u6771\u4eac"` (partial overlap in token metrics)

**Emoji and supplementary plane:**
- `"\uD83D\uDE00"` vs `"\uD83D\uDE00"` (identical grinning face, expected: 1.0)
- `"\uD83D\uDE00"` vs `"\uD83D\uDE01"` (grinning face vs grinning face with smiling eyes)
- Document behavior: supplementary characters occupy 2 Java chars, so `ResolvedStrings.leftLength` returns 2 and `leftCharAt(0)` returns the high surrogate

**Mixed script:**
- `"hello \u4E16\u754C"` vs `"hello world"` (partial match across scripts)
- `"John \u0421\u043C\u0438\u0442"` vs `"John Smith"` (Latin name + Cyrillic surname vs all-Latin)

**Tokenization behavior:**
- Verify that CJK strings without whitespace are treated as single tokens by token metrics
- Verify that non-breaking space (`\u00A0`) is or is not treated as a token delimiter (document whichever behavior exists)
- Verify that zero-width characters do not produce phantom empty tokens

**ResolvedStrings unit tests:**
- Directly test `ResolvedStrings` with multi-byte inputs to verify `isAscii` returns `false`
- Verify `leftLength` equals character count, not byte count
- Verify `leftCharAt(i)` returns expected char values for known multi-byte strings

### 2. Penalty Convention Harmonization

**Preferred approach: Harmonize AffineGap to use negative penalties.**

Current state:
| Metric | Parameter | Valid Range | Semantics |
|---|---|---|---|
| NeedlemanWunsch | `matchScore` | `> 0` | Reward (positive) |
| NeedlemanWunsch | `mismatchPenalty` | `< 0` | Penalty (negative) |
| NeedlemanWunsch | `gapPenalty` | `< 0` | Penalty (negative) |
| SmithWaterman | `matchScore` | `> 0` | Reward (positive) |
| SmithWaterman | `mismatchPenalty` | `<= 0` | Penalty (negative or zero) |
| SmithWaterman | `gapPenalty` | `<= 0` | Penalty (negative or zero) |
| AffineGap | `mismatchPenalty` | `> 0` | **Cost (positive)** |
| AffineGap | `gapOpenPenalty` | `> 0` | **Cost (positive)** |
| AffineGap | `gapExtendPenalty` | `> 0` | **Cost (positive)** |

Proposed state:
| Metric | Parameter | Valid Range | Semantics |
|---|---|---|---|
| AffineGap | `mismatchPenalty` | `< 0` | Penalty (negative) |
| AffineGap | `gapOpenPenalty` | `< 0` | Penalty (negative) |
| AffineGap | `gapExtendPenalty` | `< 0` | Penalty (negative) |

Implementation changes required:
- `AffineGap.checkInputDataTypes`: Change validation from `> 0` to `< 0` for all three penalty parameters.
- `AffineGap.DefaultMismatchPenalty`: Change from `1` to `-1`.
- `AffineGap.DefaultGapOpenPenalty`: Change from `2` to `-2`.
- `AffineGap.DefaultGapExtendPenalty`: Change from `1` to `-1`.
- `AffineGap.similarity`: Negate penalties internally before using them in the DP recurrence (the algorithm itself still needs positive cost values, so `val cost = -mismatchPenalty` at the top of the method).
- `StringSimilarityFunctions.affineGap`: Update Scaladoc and parameter names.
- `AffineGapSuite`: Update test cases for new default values.
- `ConfigurableMatrixMetricsSuite`: Update any AffineGap parameter tests.
- Fuzzy testing baselines: Re-run and update if AffineGap parity expectations change.
- Benchmark configs: Update if any benchmarks pass explicit penalty values.

**Alternative approach (if breaking change is unacceptable):** Keep the divergent conventions but add prominent Scaladoc on every penalty parameter of `affineGap` in `StringSimilarityFunctions.scala`, and add a note in `existing-metrics.md` documentation explaining the convention difference with a code example showing both styles side by side.

### 3. SmithWaterman Test Suite

Create `SmithWatermanSuite.scala` in `src/test/scala/.../expressions/matrix/` following the established pattern from `NeedlemanWunschSuite`, `JaroSuite`, and `LevenshteinSuite`.

Required test cases:
- `"identical strings should return 1.0"`: `score("spark", "spark") === 1.0`
- `"both empty strings should return 1.0"`: `score("", "") === 1.0`
- `"one empty string should return 0.0"`: `score("", "abc") === 0.0`, `score("abc", "") === 0.0`
- `"disjoint strings should return 0.0"`: `score("abc", "xyz") === 0.0`
- `"canonical local alignment score"`: Hand-compute SmithWaterman for a known pair. For example, `"ACACACTA"` vs `"AGCACACA"` with default scoring (match=2, mismatch=-1, gap=-1) — compute the expected best local alignment score and normalization.
- `"substring match should score high"`: `score("abc", "xabcx")` should be close to 1.0 because local alignment finds the full substring match.
- `"repeated characters should be scored deterministically"`: Call twice, assert identical results.
- `"asymmetric-length strings remain bounded"`: Assert result in [0.0, 1.0].
- `"whitespace-only strings remain bounded"`: Assert result in [0.0, 1.0].
- `"punctuation-bearing strings remain bounded"`: Assert result in [0.0, 1.0].
- `"normalization divides by matchScore * min(lengths)"`: Verify that a perfect match of the shorter string in the longer string produces score `min(m,n) / min(m,n) == 1.0` (or close to it, depending on alignment).

### 4. Property-Based Testing

Introduce a property-based testing approach. Implementation options (to be decided in design phase):
- **ScalaCheck** via `scalatest-plus-scalacheck` (most idiomatic for ScalaTest-based projects)
- **Hand-rolled generators** using `scala.util.Random` with fixed seeds (simpler dependency story, similar to the existing fuzzy-testing approach)

Regardless of framework, the following properties must be tested for every metric:

**Universal properties (all metrics):**

```
forAll(nonEmptyString) { s =>
  sim(s, s) == 1.0                    // Identity
}

forAll(string, string) { (a, b) =>
  val score = sim(a, b)
  score >= 0.0 && score <= 1.0         // Bounds
}

forAll(nonEmptyString) { s =>
  sim("", s) == 0.0 && sim(s, "") == 0.0   // Empty vs non-empty
}

assert(sim("", "") == 1.0)            // Both empty
```

**Symmetry (all symmetric metrics):**

Symmetric metrics: Jaccard, SorensenDice, OverlapCoefficient, Cosine, BraunBlanquet, MongeElkan, Levenshtein, LcsSimilarity, Jaro, JaroWinkler (with default parameters), NeedlemanWunsch (when mismatch and gap penalties are symmetric), SmithWaterman (when penalties are symmetric), AffineGap (when penalties are symmetric).

```
forAll(string, string) { (a, b) =>
  sim(a, b) == sim(b, a)              // Symmetry
}
```

**Triangle inequality (where applicable):**

For metrics that are proper distance metrics (Levenshtein, LCS when converted to distance), the triangle inequality holds: `d(a,c) <= d(a,b) + d(b,c)`. This is harder to test for similarity scores but can be converted: `d = 1 - sim`.

**Monotonicity (edit-distance family):**

```
forAll(string) { s =>
  // Appending a character cannot increase similarity to the original
  sim(s, s + "x") <= 1.0
  // Removing a character cannot increase similarity beyond the original
}
```

**Token metric specific:**

```
forAll(string) { s =>
  // Duplicate tokens don't change score (set semantics)
  sim(s, s) == sim(s + " " + s, s + " " + s)
}

forAll(string, string) { (a, b) =>
  // Overlap coefficient is at least as large as Jaccard for the same inputs
  overlap(a, b) >= jaccard(a, b)
}
```

**Generator requirements:**
- ASCII strings (varying lengths 0-100, including empty)
- Multi-byte UTF-8 strings (Latin diacritics, CJK, mixed)
- Whitespace-heavy strings (tabs, multiple spaces, leading/trailing)
- Strings with punctuation and special characters
- Very short strings (1-3 chars) to exercise boundary conditions in match windows (Jaro) and DP table initialization
- Moderately long strings (500-1000 chars) to exercise workspace reuse in ThreadLocal patterns

**Failure reporting:** When a property violation is found, the test must report the exact input pair that caused the failure, the expected property, and the actual score, to enable deterministic reproduction.
