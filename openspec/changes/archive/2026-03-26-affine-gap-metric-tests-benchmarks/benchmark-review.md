# Benchmark Review

Command used:

`sbt "benchmarks/jmh:run -rf json -rff target/reports/affine-gap.json AffineGapBenchmark"`

JSON report:

`benchmarks/target/reports/affine-gap.json`

## Per-cohort throughput and baseline deltas

| Scenario | affineGap ops/s | needlemanWunschBaseline ops/s | smithWatermanBaseline ops/s | Delta vs needleman | Delta vs smithWaterman |
| --- | ---: | ---: | ---: | ---: | ---: |
| short-low-overlap | 6,683,882.001 | 14,795,940.752 | 13,323,074.282 | -54.83% | -49.83% |
| short-medium-overlap | 6,215,448.411 | 14,289,240.893 | 12,718,839.899 | -56.50% | -51.13% |
| short-high-overlap | 6,472,345.147 | 15,051,188.980 | 12,729,549.212 | -57.00% | -49.15% |
| medium-low-overlap | 393,578.440 | 896,160.294 | 572,908.734 | -56.08% | -31.30% |
| medium-medium-overlap | 361,927.770 | 833,175.745 | 490,277.160 | -56.56% | -26.18% |
| medium-high-overlap | 439,617.250 | 1,089,782.330 | 654,276.158 | -59.66% | -32.81% |
| long-low-overlap | 50,117.285 | 171,294.423 | 98,262.233 | -70.74% | -49.00% |
| long-medium-overlap | 48,789.880 | 141,893.139 | 78,991.260 | -65.62% | -38.23% |
| long-high-overlap | 49,776.766 | 164,429.401 | 95,317.325 | -69.73% | -47.78% |

## Notes

- Affine-gap throughput is consistently below Needleman-Wunsch and frequently below Smith-Waterman in this first implementation.
- Largest slowdown appears in long-string cohorts, which suggests optimization work should focus on memory/state transitions in affine DP loops.
- Baseline deltas are captured for release/performance review and can be compared with follow-up optimization runs using the same report format.
