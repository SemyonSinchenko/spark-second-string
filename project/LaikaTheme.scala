import laika.ast.Path.Root
import laika.helium.Helium
import laika.helium.config.*
import laika.theme.ThemeProvider
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object LaikaTheme {
  def getLaikaTheme: String => ThemeProvider = (v: String) =>
    Helium.defaults.all
      .metadata(
        title = Some("Spark Second String"),
        description =
          Some("Documentation for Spark Second String, a library for approximate string matching in Apache Spark."),
        language = Some("en"),
        version = Some(v)
      )
      .all
      .tableOfContent("Table of Content", depth = 3)
      .site
      .topNavigationBar(navLinks =
        Seq(
          IconLink.external("https://github.com/SemyonSinchenko/spark-second-string", HeliumIcon.github)
        )
      )
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
          TextLink.internal(Root / "api" / "scaladoc" / "index.html", "API (Scaladoc)")
        ),
        projectLinks = Seq(
          TextLink.external("https://github.com/SemyonSinchenko/spark-second-string", "GitHub")
        ),
        teasers = Seq(
          Teaser("First-class SQL", "Spark Extension to register SQL functions"),
          Teaser("Spark Native", "Low-level Catalyst expressions with code generation"),
          Teaser("Feature Rich", "Built-in ready to use metrics"),
          Teaser("Lightweight", "No external dependencies besides the Apache Spark itself"),
          Teaser("Well tested", "Fuzzy testing framework with correlation against the Java Second String")
        )
      )
      .build

  def copyAll(targetDir: Path, sourceDir: Path): Unit = {
    if (!Files.exists(sourceDir)) {
      println(s"Directory $sourceDir does not exist. Skipping.")
      return
    }
    val directoryConf = targetDir.resolve("directory.conf")

    if (Files.exists(targetDir)) {
      println(s"Removing files in the existing documentation directory: ${targetDir.toAbsolutePath}")
      Files
        .walk(targetDir)
        .sorted(java.util.Comparator.reverseOrder())
        .filter(!_.equals(targetDir))
        .filter(!_.equals(directoryConf))
        .forEach(f => Files.delete(f))
    }

    println(s"Copying files of the documentation from ${sourceDir.toAbsolutePath} to ${targetDir.toAbsolutePath}")
    Files
      .walk(sourceDir)
      .forEach { source =>
        val target = targetDir.resolve(sourceDir.relativize(source))
        if (Files.isDirectory(source)) {
          Files.createDirectories(target)
        } else {
          Files.createDirectories(target.getParent)
          Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
        }
      }
  }
}
