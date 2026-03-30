package io.github.semyonsinchenko.sparkss.fuzzy

import io.github.semyonsinchenko.sparkss.sql.StringSimilaritySparkSessionExtensions._
import org.apache.spark.sql.SparkSession

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

object FuzzyTestingCli {
  private[fuzzy] final case class CliOptions(seed: Long, rows: Long, out: String, saveOutput: Option[String])

  def main(args: Array[String]): Unit = {
    parseArgs(args).fold(
      message => {
        System.err.println(message)
        System.err.println(
          "Usage: FuzzyTestingCli [--seed <long>] [--rows <long>] --out <path> [--save-output <dir>]"
        )
        System.exit(1)
      },
      options => run(options)
    )
  }

  private[fuzzy] def parseArgs(args: Array[String]): Either[String, CliOptions] = {
    var seed: Long = 42L
    var rows: Long = 10000L
    var out: Option[String] = None
    var saveOutput: Option[String] = None
    var tail = args.toList

    while (tail.nonEmpty) {
      tail match {
        case Nil =>
          tail = Nil
        case "--seed" :: value :: rest =>
          parseLong("--seed", value) match {
            case Left(error) => return Left(error)
            case Right(parsed) =>
              seed = parsed
              tail = rest
          }
        case "--rows" :: value :: rest =>
          parseLong("--rows", value) match {
            case Left(error) => return Left(error)
            case Right(parsed) =>
              if (parsed < 0) {
                return Left("--rows must be >= 0")
              }
              rows = parsed
              tail = rest
          }
        case "--out" :: value :: rest =>
          out = Some(value)
          tail = rest
        case "--save-output" :: value :: rest =>
          saveOutput = Some(value)
          tail = rest
        case option :: _ =>
          return Left(s"Unknown option: $option")
      }
    }

    out match {
      case Some(path) => Right(CliOptions(seed = seed, rows = rows, out = path, saveOutput = saveOutput))
      case None       => Left("Missing required option: --out")
    }
  }

  private def run(options: CliOptions): Unit = {
    val spark = SparkSession
      .builder()
      .appName("fuzzy-testing")
      .master("local[*]")
      .config("spark.ui.enabled", "false")
      .config("spark.ui.showConsoleProgress", "false")
      .getOrCreate()

    try {
      spark.sparkContext.setLogLevel("ERROR")
      spark.registerStringSimilarityFunctions()

      println(s"Starting fuzzy testing run (seed=${options.seed}, rows=${options.rows})")
      val report = FuzzyTestingPipeline.run(spark, options.seed, options.rows, options.saveOutput)
      val markdown = MarkdownReportRenderer.render(report)

      val target = Paths.get(options.out)
      val parent = target.getParent
      if (parent != null) {
        Files.createDirectories(parent)
      }
      Files.write(target, markdown.getBytes(StandardCharsets.UTF_8))
      println(s"Report written to ${target.toAbsolutePath}")
      options.saveOutput.foreach(path => println(s"CSV tables written to ${Paths.get(path).toAbsolutePath}"))
    } finally {
      spark.stop()
    }
  }

  private def parseLong(name: String, value: String): Either[String, Long] = {
    try {
      Right(value.toLong)
    } catch {
      case _: NumberFormatException => Left(s"Invalid value for $name: '$value'")
    }
  }
}
