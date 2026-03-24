// Spark version setting with default value
val sparkVersion = settingKey[String]("Spark version to build against")
ThisBuild / sparkVersion := sys.props.getOrElse("sparkVersion", "4.0.2")

// Dynamic Scala version based on Spark version
ThisBuild / scalaVersion := {
  val sv = (ThisBuild / sparkVersion).value
  if (sv.startsWith("3.5")) {
    "2.12.20"
  } else if (sv.startsWith("4.")) {
    "2.13.16"
  } else {
    // Default to 2.13 for unknown versions
    "2.13.16"
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
  javaOptions ++= sparkJavaOptions
)

// Root project (library)
lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "spark-second-string",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % (ThisBuild / sparkVersion).value % Provided,
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    )
  )

// Benchmarks subproject
lazy val benchmarks = (project in file("benchmarks"))
  .enablePlugins(JmhPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "spark-second-string-benchmarks",
    publish / skip := true,
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % (ThisBuild / sparkVersion).value
    )
  )
  .dependsOn(root)
