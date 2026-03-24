# Proposal: Initialize the build system of the project

## Intent
It is a brand new project of the string-matching expressions for the Apache Spark. The current goal is to initialize the build system of the project for future usage.

## Scope
- build.sbt file is created
- sparkVersion is passed as an argument with default equal to 4.0.2 (the latest)
- scalaVersion is determined based on the sparkVersion (latest available 2.12 for 3.5.x and 2.13 for 4.x.x)
- root subproject (".")
- benchmarks subproject ("benchmarks")
- dependencies: Apache Spark (based on the sparkVersion) -> "provided" for root, "runtime" for benchmarks
- sbt version is specified in build.properties, should be sbt.version=1.11.0+
- sbt plugins:
  - addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.6")
  - addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.5")
  - addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.11.2")
  - addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.4.8")
- testing dependencies (for root): scalates
- namespace: io.github.semyonsinchenko
- project name: spark-second-string
- version: dynamic, from the `sbt-ci-release` plugin (based on the future git-tags)
- metadata:
  - homepage: https://github.com/SemyonSinchenko/spark-second-string
  - licenses: "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")
  - versionScheme: "semver-spec"
  - scmInfo: as homepage
  - Developer: { id = "SemyonSinchenko", name = "Sem", email = "ssinchenko@apache.org", url = url("https://github.com/SemyonSinchenko") }
- artifact name: s"spark-second-string-$sparkMinorVersion" (like spark-second-string-4.1, spark-second-string-3.5, etc.)

## Out-of-scope
- do not write any code for now, except just add a hello-world to src/main/scala/io/github/semyonsinchenko/sparkss and hello-world like benchmark as well (to the coreesponinding folder)

## What Changes: Initialize the build system of the project
At the moment we are working on setting up a build system and the project' sceleton, no actual code changes.

## Approach
See Scope for details
