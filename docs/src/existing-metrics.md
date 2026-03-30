# Supported Metrics

All similarity metrics return a `Double` in the range **\[0.0, 1.0\]** where 1.0 means identical and 0.0 means
completely different. Phonetic encoders return a `String` encoding. Every metric is available through both the DataFrame
DSL (`StringSimilarityFunctions`) and Spark SQL (`StringSimilaritySparkSessionExtensions`).

## Token-based metrics

Token metrics split input strings into token sets and measure set overlap. By default tokens are whitespace-separated
words. All token metrics accept an optional `ngramSize` parameter; when set to a value greater than zero the input is
split into character n-grams instead.

### Jaccard

Set intersection over set union.

**Formula:** `|A ∩ B| / |A ∪ B|`

|            |                                                                          |
|------------|--------------------------------------------------------------------------|
| DSL        | `jaccard(left, right)` / `jaccard(left, right, ngramSize)`               |
| SQL        | `jaccard(left, right)`                                                   |
| Parameters | `ngramSize: Int` (default 0 = whitespace tokens, >0 = character n-grams) |

### Sorensen-Dice

Doubled intersection over the sum of set sizes. Emphasizes overlap more than Jaccard.

**Formula:** `2 * |A ∩ B| / (|A| + |B|)`

|            |                                                                      |
|------------|----------------------------------------------------------------------|
| DSL        | `sorensenDice(left, right)` / `sorensenDice(left, right, ngramSize)` |
| SQL        | `sorensen_dice(left, right)`                                         |
| Parameters | `ngramSize: Int` (default 0)                                         |

### Overlap Coefficient

Intersection relative to the smaller set. A value of 1.0 means one token set is a subset of the other.

**Formula:** `|A ∩ B| / min(|A|, |B|)`

|            |                                                                                  |
|------------|----------------------------------------------------------------------------------|
| DSL        | `overlapCoefficient(left, right)` / `overlapCoefficient(left, right, ngramSize)` |
| SQL        | `overlap_coefficient(left, right)`                                               |
| Parameters | `ngramSize: Int` (default 0)                                                     |

### Cosine

Token-set cosine similarity (binary term vectors).

**Formula:** `|A ∩ B| / sqrt(|A| * |B|)`

|            |                                                          |
|------------|----------------------------------------------------------|
| DSL        | `cosine(left, right)` / `cosine(left, right, ngramSize)` |
| SQL        | `cosine(left, right)`                                    |
| Parameters | `ngramSize: Int` (default 0)                             |

### Braun-Blanquet

Intersection relative to the larger set. Stricter than Overlap Coefficient because it penalizes size differences.

**Formula:** `|A ∩ B| / max(|A|, |B|)`

|            |                                                                        |
|------------|------------------------------------------------------------------------|
| DSL        | `braunBlanquet(left, right)` / `braunBlanquet(left, right, ngramSize)` |
| SQL        | `braun_blanquet(left, right)`                                          |
| Parameters | `ngramSize: Int` (default 0)                                           |

### Monge-Elkan

A hybrid token metric. Each token in the left string is matched to its best-scoring counterpart in the right string
using a character-level inner metric, and the scores are averaged symmetrically (left-to-right and right-to-left).

|            |                                                                                                                                                                  |
|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| DSL        | `monge_elkan(left, right)` / `monge_elkan(left, right, innerMetric)` / `monge_elkan(left, right, innerMetric, ngramSize)`                                        |
| SQL        | `monge_elkan(left, right)`                                                                                                                                       |
| Parameters | `innerMetric: String` (default `"jaro_winkler"`, also accepts `"jaro"`, `"levenshtein"`, `"needleman_wunsch"`, `"smith_waterman"`), `ngramSize: Int` (default 0) |

## Matrix / edit-distance metrics

These metrics operate at the character level using dynamic-programming alignment algorithms. All results are normalized
to \[0.0, 1.0\].

### Levenshtein

Minimum number of single-character insertions, deletions, or substitutions to transform one string into the other,
normalized by the longer string length.

**Formula:** `1 - editDistance / max(|left|, |right|)`

|            |                            |
|------------|----------------------------|
| DSL        | `levenshtein(left, right)` |
| SQL        | `levenshtein(left, right)` |
| Parameters | none                       |

### LCS Similarity

Longest Common Subsequence length (order-preserving, not necessarily contiguous) normalized by the longer string length.

**Formula:** `lcsLength / max(|left|, |right|)`

|            |                               |
|------------|-------------------------------|
| DSL        | `lcsSimilarity(left, right)`  |
| SQL        | `lcs_similarity(left, right)` |
| Parameters | none                          |

### Jaro

Counts matching characters within a sliding window and transpositions (matches in different order).

**Formula:** `(m/|s1| + m/|s2| + (m - t/2)/m) / 3` where m = matches, t = transpositions

|            |                     |
|------------|---------------------|
| DSL        | `jaro(left, right)` |
| SQL        | `jaro(left, right)` |
| Parameters | none                |

### Jaro-Winkler

Extends Jaro with a bonus for a common prefix, making it especially effective for strings that share early characters (
e.g. name typos).

**Formula:** `jaro + prefixLength * prefixScale * (1 - jaro)`

|            |                                                                                                       |
|------------|-------------------------------------------------------------------------------------------------------|
| DSL        | `jaroWinkler(left, right)` / `jaroWinkler(left, right, prefixScale, prefixCap)`                       |
| SQL        | `jaro_winkler(left, right)`                                                                           |
| Parameters | `prefixScale: Double` (default 0.1, range `(0, 0.25]`), `prefixCap: Int` (default 4, range `[1, 10]`) |

### Needleman-Wunsch

Global sequence alignment: aligns entire strings end-to-end, penalizing every gap and mismatch.

**Normalization:** `(rawScore + maxLength) / (2 * maxLength)`

|            |                                                                                                                |
|------------|----------------------------------------------------------------------------------------------------------------|
| DSL        | `needlemanWunsch(left, right)` / `needlemanWunsch(left, right, matchScore, mismatchPenalty, gapPenalty)`       |
| SQL        | `needleman_wunsch(left, right)`                                                                                |
| Parameters | `matchScore: Int` (default 1, >0), `mismatchPenalty: Int` (default -1, <0), `gapPenalty: Int` (default -1, <0) |

### Smith-Waterman

Local sequence alignment: finds the best-matching substring pair, ignoring unrelated regions at the ends.

**Normalization:** `rawScore / (matchScore * min(|left|, |right|))`

|            |                                                                                                                  |
|------------|------------------------------------------------------------------------------------------------------------------|
| DSL        | `smithWaterman(left, right)` / `smithWaterman(left, right, matchScore, mismatchPenalty, gapPenalty)`             |
| SQL        | `smith_waterman(left, right)`                                                                                    |
| Parameters | `matchScore: Int` (default 2, >0), `mismatchPenalty: Int` (default -1, <=0), `gapPenalty: Int` (default -1, <=0) |

### Affine Gap

Sequence alignment with affine gap penalties: opening a gap is more expensive than extending one, which better models
real-world string variations where insertions and deletions tend to cluster.

**Gap cost:** `gapOpenPenalty + gapLength * gapExtendPenalty`

**Normalization:** `1 - distance / max(|left|, |right|)`

|            |                                                                                                                           |
|------------|---------------------------------------------------------------------------------------------------------------------------|
| DSL        | `affine_gap(left, right)` / `affine_gap(left, right, mismatchPenalty, gapOpenPenalty, gapExtendPenalty)`                  |
| SQL        | `affine_gap(left, right)`                                                                                                 |
| Parameters | `mismatchPenalty: Int` (default -1, <0), `gapOpenPenalty: Int` (default -2, <0), `gapExtendPenalty: Int` (default -1, <0) |

## Phonetic encoders

Phonetic encoders convert a string into a code that represents its pronunciation. Two strings that sound alike produce
the same (or similar) code. These are unary functions (single input column) and return a `String`.

### Soundex

American Soundex algorithm. Produces a 4-character code: the first letter followed by three digits derived from
consonant groups.

|     |                     |
|-----|---------------------|
| DSL | `soundex(input)`    |
| SQL | `ss_soundex(input)` |

### Refined Soundex

NARA-variant Soundex with a finer consonant mapping and variable-length output. More discriminative than standard
Soundex.

|     |                             |
|-----|-----------------------------|
| DSL | `refinedSoundex(input)`     |
| SQL | `ss_refined_soundex(input)` |

### Double Metaphone

Generates a phonetic code that handles diverse language origins better than Soundex. Returns the primary code (up to 4
characters).

|     |                              |
|-----|------------------------------|
| DSL | `doubleMetaphone(input)`     |
| SQL | `ss_double_metaphone(input)` |

## Tokenization modes

All token-based metrics support two tokenization modes controlled by the `ngramSize` parameter:

| `ngramSize` | Mode               | Example for `"hello world"`                                       |
|-------------|--------------------|-------------------------------------------------------------------|
| 0 (default) | Whitespace         | `{"hello", "world"}`                                              |
| 2           | Character bigrams  | `{"he", "el", "ll", "lo", "o ", " w", "wo", "or", "rl", "ld"}`    |
| 3           | Character trigrams | `{"hel", "ell", "llo", "lo ", "o w", " wo", "wor", "orl", "rld"}` |

Character n-gram tokenization is useful when inputs are single tokens without natural word boundaries (e.g. company
names, product codes).

## Configurable parameters

SQL similarity functions remain two-argument for compatibility. To use configurable parameters (scoring weights, prefix
scale, inner metric, n-gram size), use the `StringSimilarityFunctions` DSL overloads.

| Metric            | Configurable Parameters                                 |
|-------------------|---------------------------------------------------------|
| Jaro-Winkler      | `prefixScale`, `prefixCap`                              |
| Needleman-Wunsch  | `matchScore`, `mismatchPenalty`, `gapPenalty`           |
| Smith-Waterman    | `matchScore`, `mismatchPenalty`, `gapPenalty`           |
| Affine Gap        | `mismatchPenalty`, `gapOpenPenalty`, `gapExtendPenalty` |
| Monge-Elkan       | `innerMetric`, `ngramSize`                              |
| All token metrics | `ngramSize`                                             |
