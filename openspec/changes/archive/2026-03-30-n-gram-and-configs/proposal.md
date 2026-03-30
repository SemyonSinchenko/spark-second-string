# Proposal: Configurable Parameters, N-Gram Tokenization, and Phonetic Pre-Processing

## Why

The library currently hard-codes all algorithm parameters (gap penalties, prefix scales, match/mismatch scores) and supports only whitespace tokenization. This limits three real-world identity-resolution scenarios:

1. **Tuning** — Users cannot adjust scoring parameters to their domain (e.g., heavier gap-open penalty for address matching, longer prefix cap for Jaro-Winkler on surnames). The only option today is to fork the source.
2. **Intra-token typo tolerance** — Token-based metrics treat each whitespace-delimited word as an opaque atom. A single character typo ("Smth" vs "Smith") produces zero overlap in Jaccard/Dice/Overlap because the tokens differ as strings. Character n-gram tokenization (bigrams, trigrams) is the standard remedy and is expected by practitioners.
3. **Phonetic normalization** — Names that sound alike but are spelled differently ("Stephen" / "Steven", "Schmidt" / "Smith") score poorly on both character-level and token-level metrics. Soundex, Refined Soundex, and Double Metaphone are the standard first-pass normalizers for name matching in English and are commonly used as companion functions alongside similarity scoring.

All three gaps are independent and can be delivered incrementally, but they share a common architectural concern: extending the expression constructor contract beyond the current fixed two-argument shape.

## Scope

### In scope

- **Configurable metric parameters**: Expose tuning knobs for metrics that have hard-coded constants (Jaro-Winkler prefix scale/cap, Needleman-Wunsch match/mismatch/gap scores, Smith-Waterman match/mismatch/gap scores, Affine Gap open/extend/mismatch penalties, Monge-Elkan inner metric selection).
- **Character n-gram tokenization**: Add a new tokenization mode that splits strings into character n-grams (bigrams, trigrams, or user-specified n) and wire it into existing token-metric expressions as an alternative to whitespace tokenization.
- **Phonetic pre-processing expressions**: Add standalone Catalyst expressions for Soundex, Refined Soundex, and Double Metaphone that return encoded strings. These are unary expressions (one string in, one string out) — not similarity metrics.
- **DSL and SQL surface updates**: Extend the public API and SQL registration for all new capabilities.
- **Test and benchmark coverage**: Algorithm-level tests, Catalyst integration tests (interpreted/codegen parity), and JMH benchmark coverage for every new code path.

### Out of scope

- Locale-aware or Unicode-normalization-aware tokenization (ICU, case folding).
- Weighted tokens or TF-IDF weighting.
- Configurable tokenization for matrix metrics (they operate on characters, not tokens).
- Phonetic algorithms for non-English languages (Kölner Phonetik, Caverphone, etc.).
- Combining phonetic encoding and similarity scoring into a single fused expression.
- Changes to the fuzzy-testing subproject or benchmark-tools CLI (these will adapt naturally).

---

## What Changes

This proposal covers three features delivered as three sequential changes. Each change is self-contained and shippable independently.

### Change 1: Configurable Metric Parameters

#### Problem

Seven metrics contain hard-coded constants:

| Metric | Hard-coded constants | Location |
|---|---|---|
| `JaroWinkler` | `PrefixScale = 0.1`, `PrefixCap = 4` | `JaroWinkler.scala` companion object |
| `NeedlemanWunsch` | `MatchScore = 1`, `MismatchPenalty = -1`, `GapPenalty = -1` | `NeedlemanWunsch.scala` companion object |
| `SmithWaterman` | `MatchScore = 2`, `MismatchPenalty = -1`, `GapPenalty = -1` | `SmithWaterman.scala` companion object |
| `AffineGap` | `MismatchPenalty = 1`, `GapOpenPenalty = 2`, `GapExtendPenalty = 1` | `AffineGap.scala` companion object |
| `MongeElkan` | Inner metric is always `JaroWinkler` | `MongeElkan.scala` `directedSimilarity` method |

`Jaro`, `Levenshtein`, and `LcsSimilarity` have no meaningful tuning knobs (their formulas are parameter-free). Token-set metrics (`Jaccard`, `SorensenDice`, `OverlapCoefficient`, `Cosine`, `BraunBlanquet`) are also parameter-free in their set-overlap formulas (tokenization mode is addressed in Change 2).

#### Design

**Expression constructor extension.** Each configurable metric case class gains additional constructor parameters with defaults matching the current hard-coded values. Example for `SmithWaterman`:

```scala
case class SmithWaterman(
    left: Expression,
    right: Expression,
    matchScore: Int = 2,
    mismatchPenalty: Int = -1,
    gapPenalty: Int = -1
) extends MatrixMetricExpression
```

The companion object `similarity` method signature changes correspondingly:

```scala
private[sparkss] def similarity(
    left: UTF8String,
    right: UTF8String,
    matchScore: Int,
    mismatchPenalty: Int,
    gapPenalty: Int
): Double
```

The current zero-argument `similarity(left, right)` overload is removed — callers always pass parameters explicitly. The case class default values ensure backward compatibility at the expression-construction level.

**Key design rules:**

1. Parameters are `Int` or `Double` Scala values, NOT Spark `Expression` objects. They are compile-time constants folded into codegen. They do not appear as child expressions and do not participate in Catalyst tree transformations.
2. Each parameter has a default value equal to the current hard-coded constant. Constructing `SmithWaterman(left, right)` without extra args produces identical behavior to today.
3. `withNewChildrenInternal` copies parameters through: `copy(left = newLeft, right = newRight)` (other fields are carried by the case class copy).
4. Codegen passes parameters as literal constants in the generated Java code string.
5. `equals`/`hashCode` (provided by case class) naturally includes parameters, so Catalyst's expression deduplication works correctly for different parameter configurations.

**Per-metric parameter catalog:**

| Metric | Parameter name | Type | Default | Constraint |
|---|---|---|---|---|
| `JaroWinkler` | `prefixScale` | `Double` | `0.1` | Must be in `(0.0, 0.25]` |
| `JaroWinkler` | `prefixCap` | `Int` | `4` | Must be in `[1, 10]` |
| `NeedlemanWunsch` | `matchScore` | `Int` | `1` | Must be `> 0` |
| `NeedlemanWunsch` | `mismatchPenalty` | `Int` | `-1` | Must be `< 0` |
| `NeedlemanWunsch` | `gapPenalty` | `Int` | `-1` | Must be `< 0` |
| `SmithWaterman` | `matchScore` | `Int` | `2` | Must be `> 0` |
| `SmithWaterman` | `mismatchPenalty` | `Int` | `-1` | Must be `<= 0` |
| `SmithWaterman` | `gapPenalty` | `Int` | `-1` | Must be `<= 0` |
| `AffineGap` | `mismatchPenalty` | `Int` | `1` | Must be `> 0` (cost model, not score) |
| `AffineGap` | `gapOpenPenalty` | `Int` | `2` | Must be `> 0` |
| `AffineGap` | `gapExtendPenalty` | `Int` | `1` | Must be `> 0` |
| `MongeElkan` | `innerMetric` | `String` | `"jaro_winkler"` | Must be one of: `"jaro_winkler"`, `"jaro"`, `"levenshtein"`, `"needleman_wunsch"`, `"smith_waterman"` |

**Parameter validation.** `checkInputDataTypes()` in the expression class validates parameter constraints and returns `TypeCheckFailure` with a descriptive message if violated. This runs at query analysis time, not per-row.

**MongeElkan inner metric dispatch.** The `innerMetric` string parameter selects which matrix metric to use for token-pair scoring. The companion object `directedSimilarity` method dispatches based on this string:

```scala
private def tokenPairScore(left: UTF8String, right: UTF8String, innerMetric: String): Double = {
  innerMetric match {
    case "jaro_winkler"    => JaroWinkler.similarity(left, right)
    case "jaro"            => Jaro.similarity(left, right)
    case "levenshtein"     => Levenshtein.similarity(left, right)
    case "needleman_wunsch" => NeedlemanWunsch.similarity(left, right, 1, -1, -1)
    case "smith_waterman"  => SmithWaterman.similarity(left, right, 2, -1, -1)
  }
}
```

The match is exhaustive; invalid values are caught by `checkInputDataTypes()`.

**DSL surface.** `StringSimilarityFunctions` gains overloaded methods:

```scala
// Existing shape preserved (uses defaults):
def smithWaterman(left: Column, right: Column): Column
def smithWaterman(leftColName: String, rightColName: String): Column

// New overloads with parameters:
def smithWaterman(left: Column, right: Column, matchScore: Int, mismatchPenalty: Int, gapPenalty: Int): Column
def smithWaterman(leftColName: String, rightColName: String, matchScore: Int, mismatchPenalty: Int, gapPenalty: Int): Column
```

Apply this pattern to all five configurable metrics. The two-argument overloads remain and delegate to the parameterized versions with defaults.

**SQL surface.** SQL functions remain two-argument for backward compatibility. Parameterized SQL variants are out of scope for this change — users who need custom parameters use the DSL. Document this limitation.

**Codegen.** The generated Java code embeds parameter values as literals:

```scala
override protected def genMatrixMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
  s"$SmithWatermanModule.similarity($leftValue, $rightValue, $matchScore, $mismatchPenalty, $gapPenalty)"
}
```

#### Test obligations

1. **Default-value parity**: For each configurable metric, assert that constructing with explicit default values produces identical output to the current (pre-change) implementation for a corpus of at least 10 representative input pairs covering all boundary and overlap categories.
2. **Custom-value correctness**: For each metric, test at least 3 non-default parameter configurations with hand-computed expected values.
3. **Parameter validation**: Assert that `checkInputDataTypes()` returns `TypeCheckFailure` for each constraint violation (e.g., `prefixScale = 0.0`, `prefixScale = 0.3`, `matchScore = -1`, `innerMetric = "invalid"`).
4. **Interpreted/codegen parity**: For each metric with non-default parameters, verify that interpreted and codegen paths produce identical results.
5. **MongeElkan inner metric**: Test `monge_elkan` with each of the 5 supported inner metrics on identical input, verify scores differ appropriately, and verify invalid inner metric names fail at analysis time.
6. **withNewChildrenInternal**: Verify that copying an expression with non-default parameters preserves the parameter values.

#### Benchmark obligations

- Run existing benchmark suite and verify no regression for default-parameter paths.
- Add one benchmark scenario per configurable metric with a non-default parameter set to confirm no unexpected overhead from parameterization.

---

### Change 2: Character N-Gram Tokenization

#### Problem

Current token metrics tokenize exclusively on whitespace boundaries via `TokenMetricKernelHelper.tokenizeToSet()`. This means single-token inputs like person names ("Smith" vs "Smth") or product codes ("AB-1234" vs "AB-1235") always score 0.0 on Jaccard/Dice/Overlap/Cosine/BraunBlanquet because the single tokens differ as whole strings. Character n-gram tokenization (splitting "Smith" into `{"Sm", "mi", "it", "th"}` for n=2) is the standard solution and is widely expected by identity-resolution practitioners.

#### Design

**New tokenization helper methods.** Add to `TokenMetricKernelHelper`:

```scala
private[sparkss] def tokenizeToCharNgramSet(value: String, n: Int): java.util.HashSet[String] = {
  val tokens = new java.util.HashSet[String]()
  val length = value.length
  if (length < n) {
    // String shorter than n-gram size: add the whole string as one token
    if (length > 0) {
      tokens.add(value)
    }
    return tokens
  }
  var i = 0
  while (i <= length - n) {
    tokens.add(value.substring(i, i + n))
    i += 1
  }
  tokens
}
```

Key semantics:
- Input string is NOT whitespace-tokenized first — n-grams slide over the entire raw string including spaces. This is the standard definition for character n-gram similarity.
- If the input string is shorter than `n`, the entire string is treated as a single token (not discarded).
- Empty string produces an empty set.
- The set naturally deduplicates repeated n-grams.

**Expression-level integration.** Token metric expressions gain an optional `ngramSize` parameter:

```scala
case class Jaccard(
    left: Expression,
    right: Expression,
    ngramSize: Int = 0  // 0 means whitespace tokenization (default)
) extends TokenMetricExpression
```

When `ngramSize > 0`, the expression uses `tokenizeToCharNgramSet` instead of `tokenizeToSet`. When `ngramSize == 0`, behavior is identical to today.

Apply this to all six token metrics: `Jaccard`, `SorensenDice`, `OverlapCoefficient`, `Cosine`, `BraunBlanquet`, `MongeElkan`.

**MongeElkan with n-grams.** When `ngramSize > 0` for MongeElkan, the outer tokenization changes to n-gram-based, but the inner metric (character-level comparison between token pairs) remains unchanged. This is semantically correct: n-gram MongeElkan compares n-gram "tokens" using character-level alignment.

However, this interaction deserves careful thought. An alternative is to keep MongeElkan always whitespace-tokenized (since it already does character-level comparison internally via Jaro-Winkler). The implementer SHOULD default to: MongeElkan with `ngramSize > 0` uses n-gram tokenization for outer splitting, and the inner metric compares n-gram substrings. But if this produces degenerate behavior in testing (e.g., all scores near 1.0 because short n-grams are trivially similar), the implementer SHOULD fall back to excluding MongeElkan from n-gram support and documenting why.

**Token metric companion object changes.** Each token metric companion object's `similarity` method gains the `ngramSize` parameter:

```scala
object Jaccard {
  private[sparkss] def similarity(left: UTF8String, right: UTF8String, ngramSize: Int): Double = {
    val leftString = left.toString
    val rightString = right.toString

    if (leftString.isEmpty && rightString.isEmpty) return 1.0
    if (leftString.isEmpty || rightString.isEmpty) return 0.0

    val leftTokens = if (ngramSize > 0)
      TokenMetricKernelHelper.tokenizeToCharNgramSet(leftString, ngramSize)
    else
      TokenMetricKernelHelper.tokenizeToSet(leftString)

    val rightTokens = if (ngramSize > 0)
      TokenMetricKernelHelper.tokenizeToCharNgramSet(rightString, ngramSize)
    else
      TokenMetricKernelHelper.tokenizeToSet(rightString)

    val interSize = TokenMetricKernelHelper.intersectionSize(leftTokens, rightTokens)
    val unionSize = leftTokens.size + rightTokens.size - interSize
    if (unionSize == 0) 1.0 else interSize.toDouble / unionSize.toDouble
  }
}
```

**Parameter validation.** `checkInputDataTypes()` validates: `ngramSize >= 0`. When `ngramSize == 1`, every character is a token — this is valid but produces high similarity for most inputs. No upper bound is enforced, but values above 10 are unusual.

**DSL surface.** `StringSimilarityFunctions` gains overloads:

```scala
// Existing shape preserved (whitespace tokenization):
def jaccard(left: Column, right: Column): Column
def jaccard(leftColName: String, rightColName: String): Column

// New overloads with n-gram size:
def jaccard(left: Column, right: Column, ngramSize: Int): Column
def jaccard(leftColName: String, rightColName: String, ngramSize: Int): Column
```

Naming convention: the parameter is called `ngramSize` in Scala, and appears as the third positional argument. No `ngramSize = 0` overload ambiguity because `Int` vs no-arg is unambiguous.

Apply to all six token metrics.

**SQL surface.** SQL functions remain two-argument. Parameterized SQL variants are out of scope. Document this limitation.

**Codegen.** The generated Java code embeds `ngramSize` as a literal integer:

```scala
override protected def genTokenMetricCode(ctx: CodegenContext, leftValue: String, rightValue: String): String = {
  s"$JaccardModule.similarity($leftValue, $rightValue, $ngramSize)"
}
```

#### Test obligations

1. **Whitespace-mode parity**: For each token metric, assert that `ngramSize = 0` produces identical output to the pre-change implementation for the full existing test corpus.
2. **N-gram correctness (n=2, bigrams)**:
   - `jaccard("abcd", "abce", ngramSize=2)`: left = `{"ab","bc","cd"}`, right = `{"ab","bc","ce"}`, intersection = `{"ab","bc"}`, union = `{"ab","bc","cd","ce"}`, expected = `2/4 = 0.5`.
   - `jaccard("abc", "abc", ngramSize=2)`: expected = `1.0`.
   - `jaccard("abc", "xyz", ngramSize=2)`: expected = `0.0`.
   - `jaccard("ab", "abc", ngramSize=2)`: left = `{"ab"}`, right = `{"ab","bc"}`, expected = `1/2 = 0.5`.
   - Repeat equivalent hand-computed cases for `sorensenDice`, `overlapCoefficient`, `cosine`, `braunBlanquet`.
3. **N-gram correctness (n=3, trigrams)**: At least 3 cases with hand-computed expected values.
4. **Short string handling**: Strings shorter than n-gram size are treated as a single token. Test: `jaccard("ab", "ab", ngramSize=3)` = `1.0`, `jaccard("ab", "cd", ngramSize=3)` = `0.0`, `jaccard("a", "abc", ngramSize=3)` = `0.0` (single token "a" vs single token "abc").
5. **Empty string handling**: Same boundary behavior as whitespace mode (both empty = 1.0, one empty = 0.0).
6. **Whitespace in n-grams**: Spaces are included in n-grams. `jaccard("a b", "a b", ngramSize=2)` = `1.0`. `jaccard("a b", "a c", ngramSize=2)`: left = `{"a ", " b"}`, right = `{"a ", " c"}`, intersection = `{"a "}`, union = `{"a "," b"," c"}`, expected = `1/3`.
7. **Interpreted/codegen parity**: For each token metric with `ngramSize=2`, verify identical results between interpreted and codegen execution.
8. **MongeElkan with n-grams**: Test `monge_elkan` with `ngramSize=2` and verify it produces a score. If degenerate (see design note above), document and disable.
9. **Parameter validation**: Assert `checkInputDataTypes()` rejects `ngramSize < 0`.

#### Benchmark obligations

- Add one JMH benchmark scenario per token metric with `ngramSize=2` on the medium-overlap test data.
- Compare throughput against whitespace-tokenized baseline to quantify n-gram overhead.

---

### Change 3: Phonetic Pre-Processing Expressions

#### Problem

Names that sound alike but are spelled differently ("Stephen" / "Steven", "Schmidt" / "Smith", "Catherine" / "Katherine") score poorly on all current metrics. Phonetic encoding is the standard first-pass normalizer: encode both strings, then compare the codes. The library should provide the encoding step as standalone Catalyst expressions so users can compose `jaccard(soundex(col("name1")), soundex(col("name2")))`.

#### Design

**Three new unary expressions.** Each is a standalone Catalyst `UnaryExpression` (not a similarity metric) that takes a string input and returns a string output:

| Expression | Package | Algorithm | Output |
|---|---|---|---|
| `Soundex` | `io.github.semyonsinchenko.sparkss.expressions.phonetic` | American Soundex (US Census variant) | 4-character code (letter + 3 digits) |
| `RefinedSoundex` | `io.github.semyonsinchenko.sparkss.expressions.phonetic` | Robert C. Russell's Refined Soundex | Variable-length numeric code |
| `DoubleMetaphone` | `io.github.semyonsinchenko.sparkss.expressions.phonetic` | Lawrence Philips' Double Metaphone (primary code) | Variable-length alphabetic code (up to 4 chars) |

**New package.** Create `io.github.semyonsinchenko.sparkss.expressions.phonetic` for all phonetic expressions. This is a new expression family, separate from `token` and `matrix`.

**Base class.** Create `PhoneticExpression` extending `UnaryExpression with ImplicitCastInputTypes with Serializable`:

```scala
abstract class PhoneticExpression extends UnaryExpression with ImplicitCastInputTypes with Serializable {
  override def dataType: DataType = StringType
  override def inputTypes: Seq[DataType] = Seq(StringType)
  override def nullIntolerant: Boolean = true

  protected def encode(input: UTF8String): UTF8String

  protected def genEncodeCode(ctx: CodegenContext, inputValue: String): String

  final override protected def nullSafeEval(input: Any): Any = {
    encode(input.asInstanceOf[UTF8String])
  }

  final override protected def doGenCode(ctx: CodegenContext, ev: ExprCode): ExprCode = {
    nullSafeCodeGen(ctx, ev, inputValue => {
      val encodeExpr = genEncodeCode(ctx, inputValue)
      s"${ev.value} = $encodeExpr;"
    })
  }
}
```

**Soundex algorithm specification.** American Soundex (Robert C. Russell / US Census Bureau variant):

1. Retain the first letter of the name.
2. Replace consonants with digits:
   - B, F, P, V → 1
   - C, G, J, K, Q, S, X, Z → 2
   - D, T → 3
   - L → 4
   - M, N → 5
   - R → 6
3. Remove all occurrences of A, E, I, O, U, H, W, Y (after first letter).
4. If two or more adjacent letters have the same Soundex code, keep only the first. Also applies across the first letter (e.g., "Pfister" → P not P1 because P and F both map to 1).
5. Pad with zeros or truncate to exactly 4 characters.
6. Non-alphabetic input characters are ignored (stripped before encoding).
7. Empty string input returns empty string.
8. Case-insensitive: convert to uppercase before processing.

Implementation SHALL be in the `Soundex` companion object as `def encode(input: UTF8String): UTF8String`. Use a pre-computed `Array[Int]` mapping char → digit for O(1) lookup. Use `StringBuilder` with pre-allocated capacity 4.

**Refined Soundex algorithm specification.** Refined Soundex (Robert C. Russell, 1918 patent variant):

1. Retain the first letter of the name (uppercased).
2. Map all letters to codes using the refined mapping table:
   - A, E, I, O, U, H, W, Y → 0
   - B, P → 1
   - C, K, G, J, Q, S, X, Z → 2 (note: C,K share a code; G,J share a code; etc.)

   The exact refined mapping table (0-indexed by letter A-Z):
   ```
   A=0, B=1, C=2, D=3, E=0, F=5, G=6, H=0, I=0, J=6, K=2, L=7,
   M=8, N=8, O=0, P=1, Q=2, R=9, S=2, T=3, U=0, V=5, W=0, X=2, Y=0, Z=2
   ```
   (Note: this is the Apache Commons Codec mapping. Verify against the reference.)
3. Remove adjacent duplicate codes (but keep the first occurrence).
4. Do NOT truncate or pad — output length is variable.
5. Non-alphabetic characters are ignored.
6. Empty string returns empty string.
7. Case-insensitive.

**Double Metaphone algorithm specification.** This is the most complex of the three. The implementer SHALL follow Lawrence Philips' published algorithm (2000). Key rules:

1. Returns a primary code (and optionally an alternate code — for this library, return only the primary code).
2. Maximum output length: 4 characters (configurable, but default to 4).
3. The algorithm has ~40 rules handling specific letter combinations (GH, PH, CH, SH, TH, WR, etc.).
4. Case-insensitive input.
5. Non-alphabetic characters are ignored.
6. Empty string returns empty string.

**Implementation guidance for Double Metaphone:** This algorithm is well-specified but has many branching rules. The implementer SHOULD:
- Reference the Apache Commons Codec `DoubleMetaphone` Java implementation as a behavioral oracle. The library is available at `org.apache.commons:commons-codec`.
- Do NOT copy-paste from Apache Commons. Write a clean implementation following the published algorithm.
- Use a `while` loop over the input string with a position cursor that can advance by 1 or 2 characters depending on the rule.
- Use a `StringBuilder` for output with capacity 4.
- Test against a corpus of at least 20 known name → code mappings from published references.

**DSL surface.** `StringSimilarityFunctions` gains unary helpers:

```scala
def soundex(input: Column): Column
def soundex(inputColName: String): Column

def refinedSoundex(input: Column): Column
def refinedSoundex(inputColName: String): Column

def doubleMetaphone(input: Column): Column
def doubleMetaphone(inputColName: String): Column
```

**SQL surface.** Register all three as SQL functions via `registerStringSimilarityFunctions()`:

```sql
SELECT soundex(name) FROM people;
SELECT jaccard(soundex(name1), soundex(name2)) FROM pairs;
```

Note: Spark 4.x already has a built-in `soundex` function. The library's version MUST be registered under a namespaced name to avoid collision: `ss_soundex`, `ss_refined_soundex`, `ss_double_metaphone`. The DSL methods can keep the short names since they live in the `StringSimilarityFunctions` object namespace and do not collide.

**Codegen.** Each phonetic expression generates inline Java code calling the companion object's `encode` method, following the same pattern as metric expressions.

#### Test obligations

1. **Soundex canonical cases:**
   - `"Robert"` → `"R163"`
   - `"Rupert"` → `"R163"` (same as Robert)
   - `"Ashcraft"` → `"A261"`
   - `"Tymczak"` → `"T522"`
   - `"Pfister"` → `"P236"`
   - `""` → `""`
   - `"A"` → `"A000"`
   - `"AEIOU"` → `"A000"` (vowels stripped after first letter)
2. **Refined Soundex canonical cases:**
   - `"Robert"` → known code (verify against Apache Commons Codec)
   - `"Rupert"` → same code as Robert
   - At least 5 pairs of phonetically-similar names producing identical codes.
3. **Double Metaphone canonical cases:**
   - `"Smith"` → `"SM0"` or `"XMT"` (verify against published reference)
   - `"Schmidt"` → same primary code as Smith
   - `"Stephen"` → same primary code as `"Steven"`
   - At least 10 name pairs from published Double Metaphone test corpora.
4. **Null propagation**: Null input → null output for all three expressions.
5. **Interpreted/codegen parity**: For all three expressions, verify identical output between interpreted and codegen execution for a corpus of at least 10 inputs.
6. **Non-alphabetic input**: Numbers and punctuation are stripped. `"O'Brien"` → Soundex of `"OBRIEN"`. `"123"` → `""`.
7. **Case insensitivity**: `"SMITH"` and `"smith"` produce identical codes for all three algorithms.
8. **Composition with similarity metrics**: Test `jaccard(soundex(col("a")), soundex(col("b")))` end-to-end in a DataFrame to verify the expressions compose correctly through Catalyst.

#### Benchmark obligations

- Add JMH benchmarks for each phonetic expression encoding throughput (short, medium, long inputs).
- Add one composed benchmark: `jaccard(soundex(left), soundex(right))` to measure the composition overhead vs plain `jaccard`.

---

## Approach

### Delivery order

1. **Change 1 (Configurable Parameters)** first — it modifies existing expression constructors and companion objects, establishing the pattern for non-child parameters. This change has the highest risk of breaking existing tests and must be landed cleanly before building on top.
2. **Change 2 (N-Gram Tokenization)** second — it adds another non-child parameter (`ngramSize`) to token expressions, reusing the pattern from Change 1. It also modifies `TokenMetricKernelHelper`, so it should land before Change 3 to avoid merge conflicts.
3. **Change 3 (Phonetic Expressions)** third — it is fully additive (new package, new base class, new expressions) and does not modify existing code except for DSL/SQL surface additions.

### Architectural decisions

1. **Parameters as Scala values, not Spark Expressions.** Metric parameters are tuning constants, not data columns. Making them Spark Expressions would add unnecessary complexity (they'd need to be evaluated, could be non-deterministic, would complicate codegen). Scala constructor parameters with defaults are the right abstraction.

2. **No new base class for "configurable metrics."** The existing `MatrixMetricExpression` / `TokenMetricExpression` hierarchy remains unchanged. Parameters are added directly to the case classes. A shared "ConfigurableMetricExpression" would be premature abstraction — the parameters differ per metric.

3. **N-gram as a tokenization mode, not a new metric family.** N-gram Jaccard is still Jaccard — just with a different tokenizer. Adding `ngramSize` to existing token metrics is cleaner than creating parallel `NgramJaccard`, `NgramDice`, etc. classes.

4. **Phonetic expressions as unary, not binary.** Phonetic encoding is a string transformation, not a similarity measure. Making them unary expressions that compose freely with existing metrics (`jaccard(soundex(a), soundex(b))`) is more flexible than building fused "phonetic similarity" expressions.

5. **SQL name collision avoidance.** Spark's built-in `soundex` function exists. Prefixing SQL names with `ss_` (for spark-second-string) avoids collision while keeping the names recognizable.

### Risk mitigation

| Risk | Mitigation |
|---|---|
| Changing expression constructors breaks Catalyst tree serialization | Case class defaults ensure old construction patterns still work. Kryo/Java serialization tests should be added if not present. |
| N-gram tokenization is much slower than whitespace tokenization | Benchmark before/after. N-gram set construction is O(n) where n is string length — same complexity as whitespace tokenization but with more tokens. HashSet insertion dominates. |
| Double Metaphone is complex to implement correctly | Test against Apache Commons Codec as a behavioral oracle. Use the published algorithm specification, not reverse-engineering. |
| MongeElkan with n-grams may be degenerate | Test explicitly. Fall back to excluding MongeElkan from n-gram support if scores are degenerate. |
| `ss_soundex` SQL name is ugly | Users can alias: `SELECT ss_soundex(name) AS soundex_code`. DSL users get clean names. Document both paths. |

### File change summary

**Change 1 — Modified files:**
- `expressions/matrix/JaroWinkler.scala` — Add `prefixScale`, `prefixCap` parameters
- `expressions/matrix/NeedlemanWunsch.scala` — Add `matchScore`, `mismatchPenalty`, `gapPenalty` parameters
- `expressions/matrix/SmithWaterman.scala` — Add `matchScore`, `mismatchPenalty`, `gapPenalty` parameters
- `expressions/matrix/AffineGap.scala` — Add `mismatchPenalty`, `gapOpenPenalty`, `gapExtendPenalty` parameters
- `expressions/token/MongeElkan.scala` — Add `innerMetric` parameter
- `StringSimilarityFunctions.scala` — Add parameterized overloads
- Test files: new/modified suites for parameter validation and custom-value correctness

**Change 2 — Modified files:**
- `expressions/token/TokenMetricKernelHelper.scala` — Add `tokenizeToCharNgramSet`
- `expressions/token/Jaccard.scala` — Add `ngramSize` parameter
- `expressions/token/SorensenDice.scala` — Add `ngramSize` parameter
- `expressions/token/OverlapCoefficient.scala` — Add `ngramSize` parameter
- `expressions/token/Cosine.scala` — Add `ngramSize` parameter
- `expressions/token/BraunBlanquet.scala` — Add `ngramSize` parameter
- `expressions/token/MongeElkan.scala` — Add `ngramSize` parameter (conditional — see design note)
- `StringSimilarityFunctions.scala` — Add n-gram overloads
- Test files: new/modified suites for n-gram correctness

**Change 3 — New files:**
- `expressions/phonetic/PhoneticExpression.scala` — Base class
- `expressions/phonetic/Soundex.scala` — Soundex expression + companion
- `expressions/phonetic/RefinedSoundex.scala` — Refined Soundex expression + companion
- `expressions/phonetic/DoubleMetaphone.scala` — Double Metaphone expression + companion
- `StringSimilarityFunctions.scala` — Add phonetic DSL methods (modified)
- `sql/StringSimilaritySparkSessionExtensions.scala` — Register `ss_soundex`, `ss_refined_soundex`, `ss_double_metaphone` (modified)
- Test files: new suites for phonetic expressions
- Benchmark files: new JMH benchmarks for phonetic encoding throughput
