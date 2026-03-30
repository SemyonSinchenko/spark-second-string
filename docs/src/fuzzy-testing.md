# Fuzzy Testing

Parity comparison between Spark-native implementations and the Java SecondString library. Both implementations are run on the same randomly generated input pairs and their output scores are compared row by row.

## Summary

Total compared rows: ${fuzzy.total_compared_rows}

Overall rows within +-5% agreement: ${fuzzy.overall_within_5_percent}

Metric with lowest >30% drift: ${fuzzy.lowest_over_30_metric} (${fuzzy.lowest_over_30_percent})

| Metric | Rows | Pearson | Spearman | +-5% | +-10% | +-30% | >30% |
|---|---|---|---|---|---|---|---|
| needleman_wunsch | ${fuzzy.needleman_wunsch.rows} | ${fuzzy.needleman_wunsch.pearson} | ${fuzzy.needleman_wunsch.spearman} | ${fuzzy.needleman_wunsch.within_5} | ${fuzzy.needleman_wunsch.within_10} | ${fuzzy.needleman_wunsch.within_30} | ${fuzzy.needleman_wunsch.over_30} |
| smith_waterman | ${fuzzy.smith_waterman.rows} | ${fuzzy.smith_waterman.pearson} | ${fuzzy.smith_waterman.spearman} | ${fuzzy.smith_waterman.within_5} | ${fuzzy.smith_waterman.within_10} | ${fuzzy.smith_waterman.within_30} | ${fuzzy.smith_waterman.over_30} |
| jaro_winkler | ${fuzzy.jaro_winkler.rows} | ${fuzzy.jaro_winkler.pearson} | ${fuzzy.jaro_winkler.spearman} | ${fuzzy.jaro_winkler.within_5} | ${fuzzy.jaro_winkler.within_10} | ${fuzzy.jaro_winkler.within_30} | ${fuzzy.jaro_winkler.over_30} |
| monge_elkan | ${fuzzy.monge_elkan.rows} | ${fuzzy.monge_elkan.pearson} | ${fuzzy.monge_elkan.spearman} | ${fuzzy.monge_elkan.within_5} | ${fuzzy.monge_elkan.within_10} | ${fuzzy.monge_elkan.within_30} | ${fuzzy.monge_elkan.over_30} |

## How to read the table

- **Pearson / Spearman**: correlation coefficients between the native and reference scores. Values close to 1.0 indicate strong agreement.
- **+-5% / +-10% / +-30%**: percentage of rows where the absolute difference between the two scores falls within that tolerance band.
- **>30%**: rows with more than 30% absolute difference, indicating significant divergence.

High >30% counts typically indicate a known algorithmic difference rather than a bug (e.g. Monge-Elkan uses a symmetric average while SecondString uses a one-directional score).

## Reproducing

```bash
sbt "fuzzy-testing/runMain io.github.semyonsinchenko.sparkss.fuzzy.FuzzyTestingCli \
  --seed 42 --rows 100000 \
  --out fuzzy-testing/target/reports/fuzzy-report.md \
  --save-output fuzzy-testing/target/reports/fuzzy-csv"
```

Artifact source: ${fuzzy.source_path}
