package io.github.semyonsinchenko.sparkss.benchmarks.compare

import io.github.semyonsinchenko.sparkss.benchmarks.{BenchmarkIdentifiers, BenchmarkSuiteConfig}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

object BenchmarkCompareCli {
  private final case class CliOptions(
    nativePath: String,
    legacyPath: String,
    outputPath: Option[String],
    nativeFlow: String
  )

  private final case class BenchmarkRow(
    className: String,
    scenario: String,
    score: Double,
    scoreError: Double,
    scoreUnit: String,
    warmupIterations: Int,
    measurementIterations: Int,
    forks: Int,
    mode: String
  )

  private final case class Parsed(rows: Seq[BenchmarkRow], unmappedClasses: Set[String], unsupportedAlgorithms: Set[String])

  def main(args: Array[String]): Unit = {
    val options = parseArgs(args).fold(
      message => {
        System.err.println(message)
        System.exit(1)
        throw new IllegalStateException("unreachable")
      },
      identity
    )

    val nativeClassToAlgorithm = options.nativeFlow match {
      case "direct" => BenchmarkIdentifiers.LegacyNativeDirectClassToAlgorithm
      case "spark" => BenchmarkIdentifiers.LegacyNativeSparkClassToAlgorithm
    }

    val nativeUnsupported = options.nativeFlow match {
      case "direct" => BenchmarkIdentifiers.UnsupportedNativeClassToAlgorithm
      case "spark" => Map.empty[String, String]
    }

    val nativeParsed = parse(options.nativePath, nativeClassToAlgorithm, nativeUnsupported)
    val legacyParsed = parse(options.legacyPath, BenchmarkIdentifiers.LegacyUdfClassToAlgorithm, Map.empty)

    validateBenchmarkSettings(nativeParsed.rows, "native")
    validateBenchmarkSettings(legacyParsed.rows, "legacy")

    val nativeById = nativeParsed.rows.map(row => (row.className, row.scenario) -> row).toMap
    val legacyById = legacyParsed.rows.map(row => (row.className, row.scenario) -> row).toMap

    val paired = nativeClassToAlgorithm.toSeq.flatMap { case (nativeClass, algorithmId) =>
      val legacyClassOpt = BenchmarkIdentifiers.LegacyUdfClassToAlgorithm.collectFirst { case (klass, id) if id == algorithmId => klass }
      legacyClassOpt.toSeq.flatMap { legacyClass =>
        BenchmarkIdentifiers.MatrixScenarios.flatMap { scenario =>
          for {
            native <- nativeById.get((nativeClass, scenario))
            legacy <- legacyById.get((legacyClass, scenario))
          } yield (algorithmId, scenario, native, legacy)
        }
      }
    }

    if (paired.isEmpty) {
      throw new IllegalStateException(
        s"No comparable native/legacy benchmark rows found. Native rows=${nativeParsed.rows.size}, legacy rows=${legacyParsed.rows.size}."
      )
    }

    val perAlgorithm = paired.groupBy(_._1).toSeq.sortBy(_._1).map { case (algorithm, rows) =>
      val nativeScore = rows.map(_._3.score).sum / rows.size
      val nativeError = rows.map(_._3.scoreError).sum / rows.size
      val legacyScore = rows.map(_._4.score).sum / rows.size
      val legacyError = rows.map(_._4.scoreError).sum / rows.size
      val diffPct = ((legacyScore - nativeScore) / nativeScore) * 100.0
      val scoreUnit = rows.head._3.scoreUnit
      (algorithm, nativeScore, nativeError, legacyScore, legacyError, diffPct, scoreUnit)
    }

    val reportBuilder = new StringBuilder
    reportBuilder.append("algorithm | spark-native | UDF | diff\n")
    reportBuilder.append("---|---|---|---\n")

    perAlgorithm.foreach { case (algorithm, nScore, nError, lScore, lError, diffPct, unit) =>
      reportBuilder
        .append(algorithm)
        .append(" | ")
        .append(formatScore(nScore, nError, unit))
        .append(" | ")
        .append(formatScore(lScore, lError, unit))
        .append(" | ")
        .append(f"$diffPct%.2f%%")
        .append('\n')
    }

    reportBuilder.append("\nalgorithm | scenario | spark-native | UDF | diff\n")
    reportBuilder.append("---|---|---|---|---\n")

    paired.sortBy(t => (t._1, t._2)).foreach { case (algorithm, scenario, native, legacy) =>
      val diffPct = ((legacy.score - native.score) / native.score) * 100.0
      reportBuilder
        .append(algorithm)
        .append(" | ")
        .append(scenario)
        .append(" | ")
        .append(formatScore(native.score, native.scoreError, native.scoreUnit))
        .append(" | ")
        .append(formatScore(legacy.score, legacy.scoreError, legacy.scoreUnit))
        .append(" | ")
        .append(f"$diffPct%.2f%%")
        .append('\n')
    }

    val unmatchedNativeClasses = nativeParsed.unmappedClasses.toSeq.sorted
    val unmatchedLegacyClasses = legacyParsed.unmappedClasses.toSeq.sorted
    if (unmatchedNativeClasses.nonEmpty || unmatchedLegacyClasses.nonEmpty) {
      reportBuilder.append('\n').append("Unmapped benchmark classes\n")
      unmatchedNativeClasses.foreach(c => reportBuilder.append(s"- native: $c\n"))
      unmatchedLegacyClasses.foreach(c => reportBuilder.append(s"- legacy: $c\n"))
    }

    val unsupported = (nativeParsed.unsupportedAlgorithms ++ legacyParsed.unsupportedAlgorithms).toSeq.sorted
    if (unsupported.nonEmpty) {
      reportBuilder.append('\n').append("Unsupported algorithms\n")
      unsupported.foreach(a => reportBuilder.append(s"- $a\n"))
    }

    val report = reportBuilder.toString()
    println(report)

    options.outputPath.foreach { path =>
      val target = Paths.get(path)
      val parent = target.getParent
      if (parent != null) {
        Files.createDirectories(parent)
      }
      Files.write(target, report.getBytes(StandardCharsets.UTF_8))
    }
  }

  private def parse(
    path: String,
    classToAlgorithm: Map[String, String],
    unsupportedClassToAlgorithm: Map[String, String]
  ): Parsed = {
    val entries = ujson.read(Files.readString(Paths.get(path))).arr.toSeq

    val parsed = entries.flatMap { entry =>
      val benchmarkPath = entry("benchmark").str
      val className = benchmarkPath.split("\\.").dropRight(1).lastOption.getOrElse("")
      val scenario = entry.obj.get("params").flatMap(_.obj.get("scenario")).map(_.str).getOrElse("default")
      val algorithmFromUnsupported = unsupportedClassToAlgorithm.get(className)

      if (algorithmFromUnsupported.nonEmpty) {
        None
      } else if (!classToAlgorithm.contains(className)) {
        None
      } else {
        Some(
          BenchmarkRow(
            className = className,
            scenario = scenario,
            score = entry("primaryMetric")("score").num,
            scoreError = entry("primaryMetric")("scoreError").num,
            scoreUnit = entry("primaryMetric")("scoreUnit").str,
            warmupIterations = entry("warmupIterations").num.toInt,
            measurementIterations = entry("measurementIterations").num.toInt,
            forks = entry("forks").num.toInt,
            mode = entry("mode").str
          )
        )
      }
    }

    val allClasses = entries.map(entry => entry("benchmark").str.split("\\.").dropRight(1).lastOption.getOrElse("")).toSet
    val mapped = classToAlgorithm.keySet
    val unsupportedAlgorithms = allClasses.flatMap(unsupportedClassToAlgorithm.get)
    val unmapped = allClasses -- mapped -- unsupportedClassToAlgorithm.keySet

    Parsed(parsed, unmapped, unsupportedAlgorithms)
  }

  private def validateBenchmarkSettings(rows: Seq[BenchmarkRow], flowName: String): Unit = {
    val invalid = rows.find { row =>
      row.warmupIterations != BenchmarkSuiteConfig.WarmupIterations ||
      row.measurementIterations != BenchmarkSuiteConfig.MeasurementIterations ||
      row.forks != BenchmarkSuiteConfig.Forks ||
      row.mode != BenchmarkSuiteConfig.Mode
    }

    invalid.foreach { row =>
      val message =
        s"$flowName benchmark settings mismatch for class ${row.className}. " +
          s"Expected warmup=${BenchmarkSuiteConfig.WarmupIterations}, measurement=${BenchmarkSuiteConfig.MeasurementIterations}, forks=${BenchmarkSuiteConfig.Forks}, mode=${BenchmarkSuiteConfig.Mode}. " +
          s"Actual warmup=${row.warmupIterations}, measurement=${row.measurementIterations}, forks=${row.forks}, mode=${row.mode}."
      throw new IllegalStateException(message)
    }
  }

  private def parseArgs(args: Array[String]): Either[String, CliOptions] = {
    if (args.length < 2) {
      Left("Usage: BenchmarkCompareCli <native-jmh-json> <legacy-jmh-json> [--native-flow <direct|spark>] [--out <path>]")
    } else {
      val nativePath = args(0)
      val legacyPath = args(1)

      var outputPath: Option[String] = None
      var nativeFlow = "direct"
      var tail = args.drop(2).toList

      while (tail.nonEmpty) {
        tail match {
          case Nil =>
            tail = Nil
          case "--out" :: path :: rest =>
            outputPath = Some(path)
            tail = rest
          case "--native-flow" :: flow :: rest if flow == "direct" || flow == "spark" =>
            nativeFlow = flow
            tail = rest
          case "--native-flow" :: flow :: _ =>
            return Left(s"Unsupported native flow '$flow'. Expected one of: direct, spark")
          case option :: _ =>
            return Left(s"Unknown option: $option")
        }
      }

      Right(CliOptions(nativePath, legacyPath, outputPath, nativeFlow))
    }
  }

  private def formatScore(score: Double, error: Double, unit: String): String = {
    f"$score%.2f +/- $error%.2f $unit"
  }
}
