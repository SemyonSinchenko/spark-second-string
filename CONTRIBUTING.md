# Contributing

Thanks for contributing to `spark-second-string`.

## AI-assisted contributions

**AI-generated pull requests are welcome.** They aren't second-class, and the rule below is not gatekeeping — it's a request for the inputs I need to review them at the speed your tool produced them.

### The rule

- **Trivial changes** (typos, dead links, comment fixes, single-line bug fixes, `scalafmt`-only diffs): just describe the change in the PR body. No extra paperwork.
- **Everything else** (new features, refactors, new metrics, anything touching the public API, the build system, or CI): include an OpenSpec change folder describing intent, design, and tasks.

### Why

AI-generated code is often plausible-looking but architecturally surprising or quietly hallucinated. Reviewing it without the prompt and constraints feels like proofreading a translation without seeing the source — possible, but slow. The OpenSpec change folder gives me the same artifacts I produce when I drive an AI through a change, so I can cross-check the diff against intent the same way.

### What to include

Add `openspec/changes/<YYYY-MM-DD-short-slug>/` with:

- `proposal.md` — one paragraph: what is changing and why.
- `design.md` — the key decisions, the alternatives considered, and the constraints (e.g., "must keep SQL surface 2-arg", "must work on Spark 3.5 and 4.x").
- `tasks.md` — the steps the AI followed, or the steps a reviewer should verify.
- `specs/<area>/spec.md` — spec deltas, if the change updates an existing spec.

See `openspec/changes/archive/` for dozens of well-formed examples. Recent ones such as `2026-05-26-fix-fuzzy-flow-legacy-secondstring-npe-race` and `2026-05-25-privatize-expression-case-classes` show the expected level of detail.

### Tool-neutral

OpenSpec is not vendor-locked — it works with [any coding agent supported by OpenSpec](https://github.com/Fission-AI/OpenSpec/blob/main/docs/supported-tools.md). The artifacts are plain Markdown — what matters is the *content*, not which tool generated it.

### One thing I do not want

Please do **not** paste raw conversation transcripts. They're long, contain personal context, and aren't what a reviewer needs. The OpenSpec folder is the artifact; the conversation that produced it is not.

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
