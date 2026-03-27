package io.github.semyonsinchenko.sparkss.fuzzy

private[fuzzy] object MarkdownReportRenderer {
  def render(report: FuzzyTestingReport): String = {
    val builder = new StringBuilder

    builder.append("# Fuzzy Testing Report\n\n")
    builder.append(s"Seed: ${report.seed}  \n")
    builder.append(s"Rows: ${report.rows}\n\n")

    builder.append("## Cross-metric Summary\n\n")
    builder.append(
      "metric | compared rows | excluded NULL scaled rows | pearson | spearman | +-5% (count) | +-5% (%) | +-10% (count) | +-10% (%) | +-30% (count) | +-30% (%) | >30% (count) | >30% (%)\n"
    )
    builder.append("---|---|---|---|---|---|---|---|---|---|---|---|---\n")

    val totalComparedRows = report.metrics.map(_.comparedRowCount).sum
    val totalExcludedRows = report.metrics.map(_.excludedNullScaledCount).sum
    val totalWithin5 = report.metrics.map(_.deltaBands.within5).sum
    val totalWithin10 = report.metrics.map(_.deltaBands.within10).sum
    val totalWithin30 = report.metrics.map(_.deltaBands.within30).sum
    val totalOver30 = report.metrics.map(_.deltaBands.over30).sum

    report.metrics.foreach { metric =>
      builder
        .append(metric.metric)
        .append(" | ")
        .append(metric.comparedRowCount)
        .append(" | ")
        .append(metric.excludedNullScaledCount)
        .append(" | ")
        .append(formatDouble(metric.pearson))
        .append(" | ")
        .append(formatDouble(metric.spearman))
        .append(" | ")
        .append(metric.deltaBands.within5)
        .append(" | ")
        .append(formatPercent(metric.deltaBands.within5, metric.comparedRowCount))
        .append(" | ")
        .append(metric.deltaBands.within10)
        .append(" | ")
        .append(formatPercent(metric.deltaBands.within10, metric.comparedRowCount))
        .append(" | ")
        .append(metric.deltaBands.within30)
        .append(" | ")
        .append(formatPercent(metric.deltaBands.within30, metric.comparedRowCount))
        .append(" | ")
        .append(metric.deltaBands.over30)
        .append(" | ")
        .append(formatPercent(metric.deltaBands.over30, metric.comparedRowCount))
        .append('\n')
    }

    builder
      .append("ALL")
      .append(" | ")
      .append(totalComparedRows)
      .append(" | ")
      .append(totalExcludedRows)
      .append(" | ")
      .append("-")
      .append(" | ")
      .append("-")
      .append(" | ")
      .append(totalWithin5)
      .append(" | ")
      .append(formatPercent(totalWithin5, totalComparedRows))
      .append(" | ")
      .append(totalWithin10)
      .append(" | ")
      .append(formatPercent(totalWithin10, totalComparedRows))
      .append(" | ")
      .append(totalWithin30)
      .append(" | ")
      .append(formatPercent(totalWithin30, totalComparedRows))
      .append(" | ")
      .append(totalOver30)
      .append(" | ")
      .append(formatPercent(totalOver30, totalComparedRows))
      .append('\n')

    report.metrics.foreach { metric =>
      builder.append("\n")
      builder.append(s"## Metric: ${metric.metric}\n\n")
      builder.append(s"Compared rows: ${metric.comparedRowCount}  \n")
      builder.append(s"Excluded NULL scaled rows: ${metric.excludedNullScaledCount}\n\n")
      builder.append("correlation | value\n")
      builder.append("---|---\n")
      builder.append(s"pearson | ${formatDouble(metric.pearson)}\n")
      builder.append(s"spearman | ${formatDouble(metric.spearman)}\n\n")

      builder.append("delta band | count | percentage\n")
      builder.append("---|---|---\n")
      builder.append(
        s"+-5% | ${metric.deltaBands.within5} | ${formatPercent(metric.deltaBands.within5, metric.comparedRowCount)}\n"
      )
      builder.append(
        s"+-10% | ${metric.deltaBands.within10} | ${formatPercent(metric.deltaBands.within10, metric.comparedRowCount)}\n"
      )
      builder.append(
        s"+-30% | ${metric.deltaBands.within30} | ${formatPercent(metric.deltaBands.within30, metric.comparedRowCount)}\n"
      )
      builder.append(
        s">30% | ${metric.deltaBands.over30} | ${formatPercent(metric.deltaBands.over30, metric.comparedRowCount)}\n"
      )
    }

    builder.toString()
  }

  private def formatDouble(value: Double): String = {
    if (value.isNaN) {
      "NaN"
    } else {
      f"$value%.6f"
    }
  }

  private def formatPercent(count: Long, total: Long): String = {
    val ratio = if (total == 0L) 0.0 else count.toDouble / total.toDouble * 100.0
    f"$ratio%.2f%%"
  }
}
