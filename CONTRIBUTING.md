# Contributing

Thanks for contributing to `spark-second-string`.

Run commands from the repository root unless noted otherwise.

## Prerequisites

- Java 11+ (Java 17 recommended)
- `sbt`
- Python 3 (used by docs variable-generation scripts)

You can override Spark line for most `sbt` commands:

```bash
sbt -DsparkVersion=4.0.2 test
```

## Core Build and Validation Commands

Format check:

```bash
sbt scalafmtCheckAll
```

Build + test:

```bash
sbt test
```

CI-equivalent run (choose Spark/Java matrix entry locally):

```bash
sbt -DsparkVersion=4.0.2 scalafmtCheckAll
sbt -DsparkVersion=4.0.2 test
```

## Fuzzy Testing

Run fuzzy parity testing and write markdown report:

```bash
sbt "fuzzy-testing/runMain io.github.semyonsinchenko.sparkss.fuzzy.FuzzyTestingCli --seed 42 --rows 100000 --out target/reports/fuzzy-report.md --save-output target/reports/fuzzy-csv"
```

The command is executed in the `fuzzy-testing` subproject context, so `--out target/...` resolves to `fuzzy-testing/target/...` at repo level.

### FuzzyTestingCli arguments

- `--seed <long>`: optional random seed (default: `42`).
- `--rows <long>`: optional row count to generate (default: `10000`, must be `>= 0`).
- `--out <path>`: required output path for markdown report.
- `--save-output <dir>`: optional directory for CSV artifacts.

Common outputs:

- Report: `fuzzy-testing/target/reports/fuzzy-report.md`
- CSV tables: `fuzzy-testing/target/reports/fuzzy-csv`

## Benchmarks

Run benchmark comparison suite (native Spark SQL vs legacy UDF):

```bash
./dev/benchmarks_suite.sh --mode compare-only
```

Run native-direct benchmark subset only:

```bash
./dev/benchmarks_suite.sh --mode native-only
```

### `dev/benchmarks_suite.sh` arguments

- `--mode <native-only|compare-only>`: required run mode.
- `--output-dir <path>`: optional artifact output directory (default: `benchmarks/target/reports/suite`).
- `--verbose`: optional JMH verbose mode (`-v EXTRA`).

Primary artifacts:

- `native-jmh.json` (native-only mode)
- `native-spark-jmh.json` (compare-only mode)
- `legacy-udf-jmh.json` (compare-only mode)
- `compare-table.txt` (compare-only mode)

## Docs Build

Docs pages consume generated benchmark and fuzzy-testing report variables.

Generate required artifacts first:

```bash
./dev/benchmarks_suite.sh --mode compare-only
sbt "fuzzy-testing/runMain io.github.semyonsinchenko.sparkss.fuzzy.FuzzyTestingCli --seed 42 --rows 100000 --out target/reports/fuzzy-report.md --save-output target/reports/fuzzy-csv"
```

Then build docs:

```bash
sbt docs/laikaSite
```

Alternative local docs target:

```bash
sbt docs/laikaHTML
```
