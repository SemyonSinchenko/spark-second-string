## 1. Project Structure Setup

- [x] 1.1 Create `project/build.properties` with `sbt.version=1.11.0+`
- [x] 1.2 Create `project/plugins.sbt` with all required plugin declarations
- [x] 1.3 Create directory structure: `src/main/scala/io/github/semyonsinchenko/sparkss`
- [x] 1.4 Create directory structure: `benchmarks/src/main/scala`

## 2. Build Configuration (build.sbt)

- [x] 2.1 Define `sparkVersion` setting with default value `4.0.2`
- [x] 2.2 Implement Scala version derivation logic (2.12 for Spark 3.5.x, 2.13 for Spark 4.x.x)
- [x] 2.3 Configure root project settings (name, organization, scalaVersion)
- [x] 2.4 Configure `benchmarks` subproject definition
- [x] 2.5 Set up Apache Spark dependency with `provided` scope for root project
- [x] 2.6 Set up Apache Spark dependency with `runtime` scope for benchmarks project
- [x] 2.7 Add ScalaTest dependency for root project

## 3. Project Metadata Configuration

- [x] 3.1 Configure homepage URL in build settings
- [x] 3.2 Configure Apache-2.0 license with URL
- [x] 3.3 Set version scheme to `semver-spec`
- [x] 3.4 Configure SCM info matching homepage
- [x] 3.5 Add developer information (id, name, email, url)
- [x] 3.6 Configure dynamic artifact naming pattern `spark-second-string-$sparkMinorVersion`

## 4. Hello-World Implementation

- [x] 4.1 Create hello-world Scala source file in `src/main/scala/io/github/semyonsinchenko/sparkss/`
- [x] 4.2 Create hello-world benchmark file in `benchmarks/src/main/scala/`

## 5. Verification

- [x] 5.1 Run `sbt compile` and verify root project compiles successfully
- [x] 5.2 Run `sbt benchmarks/compile` and verify benchmarks project compiles
- [x] 5.3 Run `sbt projects` and verify both projects are listed
- [x] 5.4 Verify `sbt scalafmtCheck` runs without plugin errors
- [x] 5.5 Verify `sbt show version` reflects dynamic versioning
