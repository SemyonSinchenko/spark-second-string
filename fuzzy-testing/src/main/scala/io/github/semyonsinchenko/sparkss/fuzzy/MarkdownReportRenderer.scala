package io.github.semyonsinchenko.sparkss.fuzzy

private[fuzzy] object MarkdownReportRenderer {
  def render(report: FuzzyTestingReport): String = {
    val builder = new StringBuilder

    builder.append("# Fuzzy Testing Report\n\n")
    builder.append(s"Seed: ${report.seed}  \n")
    builder.append(s"Rows: ${report.rows}\n\n")

    builder.append("## Cross-metric Summary\n\n")
    builder.append("metric | pearson | spearman | +-5% (count) | +-5% (%) | +-10% (count) | +-10% (%) | +-30% (count) | +-30% (%) | >30% (count) | >30% (%)\n")
    builder.append("---|---|---|---|---|---|---|---|---|---|---\n")

    report.metrics.foreach { metric =>
      builder
        .append(metric.metric)
        .append(" | ")
        .append(formatDouble(metric.pearson))
        .append(" | ")
        .append(formatDouble(metric.spearman))
        .append(" | ")
        .append(metric.deltaBands.within5)
        .append(" | ")
        .append(formatPercent(metric.deltaBands.within5, metric.rowCount))
        .append(" | ")
        .append(metric.deltaBands.within10)
        .append(" | ")
        .append(formatPercent(metric.deltaBands.within10, metric.rowCount))
        .append(" | ")
        .append(metric.deltaBands.within30)
        .append(" | ")
        .append(formatPercent(metric.deltaBands.within30, metric.rowCount))
        .append(" | ")
        .append(metric.deltaBands.over30)
        .append(" | ")
        .append(formatPercent(metric.deltaBands.over30, metric.rowCount))
        .append('\n')
    }

    report.metrics.foreach { metric =>
      builder.append("\n")
      builder.append(s"## Metric: ${metric.metric}\n\n")
      builder.append("correlation | value\n")
      builder.append("---|---\n")
      builder.append(s"pearson | ${formatDouble(metric.pearson)}\n")
      builder.append(s"spearman | ${formatDouble(metric.spearman)}\n\n")

      builder.append("delta band | count | percentage\n")
      builder.append("---|---|---\n")
      builder.append(s"+-5% | ${metric.deltaBands.within5} | ${formatPercent(metric.deltaBands.within5, metric.rowCount)}\n")
      builder.append(s"+-10% | ${metric.deltaBands.within10} | ${formatPercent(metric.deltaBands.within10, metric.rowCount)}\n")
      builder.append(s"+-30% | ${metric.deltaBands.within30} | ${formatPercent(metric.deltaBands.within30, metric.rowCount)}\n")
      builder.append(s">30% | ${metric.deltaBands.over30} | ${formatPercent(metric.deltaBands.over30, metric.rowCount)}\n")
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
