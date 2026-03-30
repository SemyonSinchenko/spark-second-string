import laika.config.SyntaxHighlighting
import laika.format.Markdown.GitHubFlavor

import scala.sys.process.*

val sparkVersion = settingKey[String]("Spark version to build against")
ThisBuild / sparkVersion := sys.props.getOrElse("sparkVersion", "4.0.2")

val baseVersion = settingKey[String]("Version truncated to major.minor.patch")

ThisBuild / baseVersion := {
  val raw = (ThisBuild / version).value
  val SemVerCore = raw"""^(\d+)\.(\d+)\.(\d+).*$$""".r

  raw match {
    case SemVerCore(major, minor, patch) => s"$major.$minor.$patch"
    case _                               => raw // fallback if version doesn't start with x.y.z
  }
}

val generateDocsVariables = taskKey[Unit]("Generate Laika variable files from benchmark and fuzzy reports")

// Dynamic Scala version based on Spark version
ThisBuild / scalaVersion := {
  val sv = (ThisBuild / sparkVersion).value
  if (sv.startsWith("3.5")) {
    "2.12.21"
  } else if (sv.startsWith("4.")) {
    "2.13.18"
  } else {
    "2.13.18"
  }
}

// Project metadata
ThisBuild / name := "spark-second-string"
ThisBuild / organization := "io.github.semyonsinchenko"
ThisBuild / homepage := Some(url("https://github.com/SemyonSinchenko/spark-second-string"))
ThisBuild / licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / versionScheme := Some("semver-spec")
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/SemyonSinchenko/spark-second-string"),
    "scm:git@github.com:SemyonSinchenko/spark-second-string.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "SemyonSinchenko",
    name = "Sem",
    email = "ssinchenko@apache.org",
    url = url("https://github.com/SemyonSinchenko")
  )
)

// Dynamic artifact naming based on Spark minor version
ThisBuild / artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  val sparkMinor = (ThisBuild / sparkVersion).value.split("\\.").take(2).mkString(".")
  s"spark-second-string-$sparkMinor"
}

// Required JVM options for Spark (ADD_OPENS)
val sparkJavaOptions = Seq(
  "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
  "--add-opens=java.base/java.lang=ALL-UNNAMED",
  "--add-opens=java.base/java.nio=ALL-UNNAMED",
  "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
  "--add-opens=java.base/java.util=ALL-UNNAMED",
  "--add-opens=java.base/sun.security.action=ALL-UNNAMED",
  "--add-opens=java.base/java.io=ALL-UNNAMED"
)

// Common settings
lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xlint"
  ),
  javaOptions ++= sparkJavaOptions,
  Test / fork := true,
  Compile / unmanagedSourceDirectories += {
    val sparkMajor = (ThisBuild / sparkVersion).value.split("\\.").headOption.getOrElse("4")
    val shimDir = if (sparkMajor == "3") "spark3" else "spark4"
    baseDirectory.value / "src" / "main" / shimDir
  }
)

// Root project (library)
lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "spark-second-string",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % (ThisBuild / sparkVersion).value % Provided,
      "commons-codec" % "commons-codec" % "1.21.0" % Provided,
      "org.scalatest" %% "scalatest" % "3.2.20" % Test,
      "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test
    )
  )

// Benchmarks subproject
lazy val benchmarks = (project in file("benchmarks"))
  .enablePlugins(JmhPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "spark-second-string-benchmarks",
    publish / skip := true,
    resolvers += "Cogcomp" at "https://cogcomp.seas.upenn.edu/m2repo/",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % (ThisBuild / sparkVersion).value,
      "commons-codec" % "commons-codec" % "1.21.0",
      "com.wcohen" % "SecondString" % "1.0" % Runtime
    )
  )
  .dependsOn(root)

// Benchmark tools subproject (e.g., report generation)
lazy val benchmarkTools = (project in file("benchmark-tools"))
  .settings(commonSettings: _*)
  .settings(
    name := "spark-second-string-benchmark-tools",
    publish / skip := true,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "ujson" % "4.4.3"
    )
  )
  .dependsOn(benchmarks)

// Fuzzy testing subproject
lazy val fuzzyTesting = Project("fuzzy-testing", file("fuzzy-testing"))
  .settings(commonSettings: _*)
  .settings(
    name := "spark-second-string-fuzzy-testing",
    Compile / run / fork := true,
    Compile / run / javaOptions ++= sparkJavaOptions,
    publish / skip := true,
    resolvers += "Cogcomp" at "https://cogcomp.seas.upenn.edu/m2repo/",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % (ThisBuild / sparkVersion).value,
      "org.apache.spark" %% "spark-mllib" % (ThisBuild / sparkVersion).value,
      "commons-codec" % "commons-codec" % "1.21.0",
      "com.wcohen" % "SecondString" % "1.0" % Runtime,
      "org.scalatest" %% "scalatest" % "3.2.20" % Test
    )
  )
  .dependsOn(root)

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(LaikaPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "spark-second-string-docs",
    publish / skip := true,
    laikaTheme := LaikaConfig.getLaikaTheme((ThisBuild / baseVersion).value),
    laikaExtensions := Seq(GitHubFlavor, SyntaxHighlighting),
    generateDocsVariables := {
      val repoRoot = baseDirectory.value.getParentFile
      val benchmarkReport = repoRoot / "benchmarks" / "target" / "reports" / "suite" / "compare-table.txt"
      val fuzzyReport = repoRoot / "fuzzy-testing" / "target" / "reports" / "fuzzy-report.md"
      val benchmarkVars = baseDirectory.value / "variables" / "benchmarks.conf"
      val fuzzyVars = baseDirectory.value / "variables" / "fuzzy-testing.conf"
      val benchmarkParser = repoRoot / "dev" / "docs_benchmark_vars.py"
      val fuzzyParser = repoRoot / "dev" / "docs_fuzzy_vars.py"
      val directoryConf = baseDirectory.value / "src" / "directory.conf"

      if (!benchmarkReport.exists()) {
        sys.error(
          s"""
           | Docs build precondition failed: benchmark report is missing at ${benchmarkReport.getAbsolutePath}.
           | Run ./dev/benchmarks_suite.sh --mode compare-only first.""".stripMargin
        )
      }

      if (!fuzzyReport.exists()) {
        sys.error(
          s"""
           | Docs build precondition failed: fuzzy testing report is missing at ${fuzzyReport.getAbsolutePath}.
           | Run the fuzzy-testing CLI to generate fuzzy-testing/target/reports/fuzzy-report.md first.""".stripMargin
        )
      }

      IO.createDirectory(benchmarkVars.getParentFile)

      val benchmarkExit = Process(
        Seq(
          "python3",
          benchmarkParser.getAbsolutePath,
          "--input",
          benchmarkReport.getAbsolutePath,
          "--output",
          benchmarkVars.getAbsolutePath
        ),
        repoRoot
      ).!
      if (benchmarkExit != 0) {
        sys.error("Docs build precondition failed: benchmark variable generation failed.")
      }

      val fuzzyExit = Process(
        Seq(
          "python3",
          fuzzyParser.getAbsolutePath,
          "--input",
          fuzzyReport.getAbsolutePath,
          "--output",
          fuzzyVars.getAbsolutePath
        ),
        repoRoot
      ).!
      if (fuzzyExit != 0) {
        sys.error("Docs build precondition failed: fuzzy-testing variable generation failed.")
      }
      val benchmarkTopLevel = IO.read(benchmarkVars)
      val fuzzyTopLevel = IO.read(fuzzyVars)
      val merged =
        s"""|laika.title = "Documentation"
            |
            |laika.navigationOrder = [
            |  overview.md
            |  quick-start.md
            |  existing-metrics.md
            |  fuzzy-testing.md
            |  benchmarks.md
            |]
            |
            |$benchmarkTopLevel
            |
            |$fuzzyTopLevel
            |""".stripMargin
      IO.write(directoryConf, merged)
    },
    laikaSite := (laikaSite dependsOn generateDocsVariables).value,
    laikaHTML := (laikaHTML dependsOn generateDocsVariables).value,
    Laika / sourceDirectories := Seq((ThisBuild / baseDirectory).value / "docs" / "src")
  )
