package io.github.semyonsinchenko.sparkss.benchmarks

object BenchmarkIdentifiers {
  val AffineGap: String = "affine_gap"
  val NeedlemanWunsch: String = "needleman_wunsch"
  val SmithWaterman: String = "smith_waterman"
  val Levenshtein: String = "levenshtein"
  val LcsSimilarity: String = "lcs_similarity"
  val Jaro: String = "jaro"
  val JaroWinkler: String = "jaro_winkler"
  val TokenMetrics: String = "token_metrics"
  val MongeElkan: String = "monge_elkan"

  val MatrixScenarios: Seq[String] = Seq(
    "short-low-overlap",
    "short-medium-overlap",
    "short-high-overlap",
    "medium-low-overlap",
    "medium-medium-overlap",
    "medium-high-overlap",
    "long-low-overlap",
    "long-medium-overlap",
    "long-high-overlap"
  )

  val LegacyNativeDirectClassToAlgorithm: Map[String, String] = Map(
    "AffineGapBenchmark" -> AffineGap,
    "NeedlemanWunschBenchmark" -> NeedlemanWunsch,
    "SmithWatermanBenchmark" -> SmithWaterman
  )

  val LegacyNativeSparkClassToAlgorithm: Map[String, String] = Map(
    "NativeSparkAffineGapBenchmark" -> AffineGap,
    "NativeSparkNeedlemanWunschBenchmark" -> NeedlemanWunsch,
    "NativeSparkSmithWatermanBenchmark" -> SmithWaterman,
    "NativeSparkJaroWinklerBenchmark" -> JaroWinkler,
    "NativeSparkMongeElkanBenchmark" -> MongeElkan
  )

  val LegacyNativeClassToAlgorithm: Map[String, String] = LegacyNativeDirectClassToAlgorithm

  val LegacyUdfClassToAlgorithm: Map[String, String] = Map(
    "LegacyAffineGapUdfBenchmark" -> AffineGap,
    "LegacyNeedlemanWunschUdfBenchmark" -> NeedlemanWunsch,
    "LegacySmithWatermanUdfBenchmark" -> SmithWaterman,
    "LegacyJaroWinklerUdfBenchmark" -> JaroWinkler,
    "LegacyMongeElkanUdfBenchmark" -> MongeElkan
  )

  val UnsupportedNativeClassToAlgorithm: Map[String, String] = Map(
    "LevenshteinBenchmark" -> Levenshtein,
    "LcsSimilarityBenchmark" -> LcsSimilarity,
    "JaroBenchmark" -> Jaro,
    "TokenMetricsBenchmark" -> TokenMetrics,
    "MongeElkanBenchmark" -> MongeElkan
  )
}

object BenchmarkInputData {
  private val matrixScenarioPairs: Map[String, (String, String)] = Map(
    "short-low-overlap" -> ("spark", "flint"),
    "short-medium-overlap" -> ("spark", "shark"),
    "short-high-overlap" -> ("spark", "spork"),
    "medium-low-overlap" -> ("distributed systems are fast", "batch processing can be costly"),
    "medium-medium-overlap" -> ("distributed systems are fast", "distributed batch jobs are costly"),
    "medium-high-overlap" -> ("distributed systems are fast", "distributed system are fast"),
    "long-low-overlap" -> (
      "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda mu",
      "omicron pi rho sigma tau upsilon phi chi psi omega aleph beth"
    ),
    "long-medium-overlap" -> (
      "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda mu",
      "alpha beta gamma sigma tau upsilon eta theta iota omega lambda nu"
    ),
    "long-high-overlap" -> (
      "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda mu",
      "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda nu"
    )
  )

  def matrixPairFor(scenario: String): (String, String) = {
    matrixScenarioPairs.getOrElse(scenario, throw new IllegalArgumentException(s"Unknown scenario: $scenario"))
  }
}
