# Overview

`spark-second-string` provides Spark-native similarity metrics for string-heavy workloads such as identity resolution, entity matching, and duplicate detection.

The core idea is to keep similarity scoring inside Spark execution, so teams can use broad and explainable string heuristics as an inexpensive stage before expensive model-based matching.

Typical pipeline shape:

1. Build candidate pairs from join/blocking logic.
2. Compute one or more string metrics with Spark-native expressions.
3. Apply thresholding/pruning to keep likely matches.
4. Send reduced candidates to heavier rankers/classifiers.

This pattern helps reduce cost while preserving useful recall in early stages.
