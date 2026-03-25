# Benchmark Review

Command used:

`sbt "benchmarks/Jmh/run -wi 1 -i 1 -f 1 -r 1s -w 1s io.github.semyonsinchenko.sparkss.benchmarks.JaroBenchmark" "benchmarks/Jmh/run -wi 1 -i 1 -f 1 -r 1s -w 1s io.github.semyonsinchenko.sparkss.benchmarks.LevenshteinBenchmark" "benchmarks/Jmh/run -wi 1 -i 1 -f 1 -r 1s -w 1s io.github.semyonsinchenko.sparkss.benchmarks.LcsSimilarityBenchmark"`

## Jaro (new)

- short-high-overlap: 19,130,333.407 ops/s
- short-low-overlap: 25,112,531.235 ops/s
- medium-high-overlap: 3,245,130.006 ops/s
- medium-low-overlap: 2,287,961.563 ops/s
- long-high-overlap: 1,209,863.808 ops/s
- long-low-overlap: 538,851.226 ops/s

## Existing matrix baselines

Levenshtein:

- short-low-edit: 13,530,141.806 ops/s
- short-high-edit: 14,798,881.621 ops/s
- medium-low-edit: 854,780.768 ops/s
- medium-high-edit: 987,171.892 ops/s
- long-low-edit: 123,418.551 ops/s
- long-high-edit: 130,859.948 ops/s

LCS similarity:

- short-low-overlap: 16,886,845.474 ops/s
- short-high-overlap: 14,727,767.159 ops/s
- medium-low-overlap: 2,364,638.730 ops/s
- medium-high-overlap: 1,113,263.361 ops/s
- long-low-overlap: 205,321.382 ops/s
- long-high-overlap: 200,359.242 ops/s

## Review

- Jaro throughput scales down as length increases, consistent with expected character-alignment cost.
- Jaro remains in a comparable or higher throughput band than existing matrix baselines across short/medium/long buckets in this quick run.
- No immediate regression signal against current matrix metric baselines.
