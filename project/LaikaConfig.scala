import laika.ast.Path.Root
import laika.helium.Helium
import laika.helium.config.*
import laika.theme.ThemeProvider

object LaikaConfig {
  def getLaikaTheme: String => ThemeProvider = (v: String) => Helium.defaults.all.metadata(
    title = Some("Spark Second String"),
    description = Some("Documentation for Spark Second String, a library for approximate string matching in Apache Spark."),
    language = Some("en"),
    version = Some(v),
  ).all
    .tableOfContent("Table of Content", depth = 3)
    .site
    .topNavigationBar(navLinks = Seq(
      IconLink.external("https://github.com/SemyonSinchenko/spark-second-string", HeliumIcon.github)
    ))
    .site
      .pageNavigation(depth = 1)
    .site
    .landingPage(
      title = Some("Spark Second String"),
      subtitle = Some("A library for approximate string matching in Apache Spark."),
      logo = None,
      latestReleases = Seq(ReleaseInfo("Latest Release", v)),
      license = Some("Apache 2-0"),
      documentationLinks = Seq(
          TextLink.internal(Root / "overview.md", "Overview"),
          TextLink.internal(Root / "quick-start.md", "Quick Start"),
          TextLink.internal(Root / "existing-metrics.md", "Supported Metrics"),
          TextLink.internal(Root / "fuzzy-testing.md", "Fuzzy Testing"),
          TextLink.internal(Root / "benchmarks.md", "Benchmarks"),
      ),
      projectLinks = Seq(),
      teasers = Seq(
        Teaser("Spark Native", "Low-level Catalyst expressions with code generation"),
        Teaser("Feature Rich", "Built-in ready to use metrics"),
        Teaser("Lightweight", "No external dependencies besides the Apache Spark itself"),
        Teaser("Well tested", "Fuzzy testing framework with correlation against the Java Second String")
      )
    )
    .build
}
