#!/usr/bin/env bash

set -euo pipefail

VERBOSE=0
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
OUTPUT_DIR="$ROOT_DIR/benchmarks/target/reports/suite"
MODE=""

print_usage() {
  echo "Usage: $0 --mode <native-only|compare-only> [-v|--verbose] [-o|--output-dir <path>]"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -v|--verbose)
      VERBOSE=1
      shift
      ;;
    -o|--output-dir)
      OUTPUT_DIR="$2"
      shift 2
      ;;
    -m|--mode)
      MODE="$2"
      shift 2
      ;;
    *)
      echo "Unknown option: $1"
      print_usage
      exit 1
      ;;
  esac
done

if [[ -z "$MODE" ]]; then
  echo "Missing required option: --mode"
  print_usage
  exit 1
fi

if [[ "$MODE" != "native-only" && "$MODE" != "compare-only" ]]; then
  echo "Unsupported mode: $MODE"
  print_usage
  exit 1
fi

NATIVE_JSON="$OUTPUT_DIR/native-jmh.json"
NATIVE_SPARK_JSON="$OUTPUT_DIR/native-spark-jmh.json"
LEGACY_JSON="$OUTPUT_DIR/legacy-udf-jmh.json"
COMPARE_TXT="$OUTPUT_DIR/compare-table.txt"
JMH_LOCK_FILE="${TMPDIR:-/tmp}/jmh.lock"
JMH_LOCK_TIMEOUT_SECONDS=60

mkdir -p "$OUTPUT_DIR"

run_phase() {
  local phase="$1"
  local command="$2"

  echo "==> $phase"
  if ! eval "$command"; then
    echo "Phase failed: $phase"
    exit 1
  fi
}

clear_or_wait_for_jmh_lock() {
  local waited=0

  while [[ -f "$JMH_LOCK_FILE" ]]; do
    if command -v lsof >/dev/null 2>&1; then
      if ! lsof "$JMH_LOCK_FILE" >/dev/null 2>&1; then
        rm -f "$JMH_LOCK_FILE"
        break
      fi
    elif command -v fuser >/dev/null 2>&1; then
      if ! fuser "$JMH_LOCK_FILE" >/dev/null 2>&1; then
        rm -f "$JMH_LOCK_FILE"
        break
      fi
    fi

    if [[ "$waited" -ge "$JMH_LOCK_TIMEOUT_SECONDS" ]]; then
      echo "Phase failed: JMH lock is held at $JMH_LOCK_FILE for over ${JMH_LOCK_TIMEOUT_SECONDS}s."
      echo "Close other running JMH processes and retry."
      exit 1
    fi

    sleep 1
    waited=$((waited + 1))
  done
}

validate_json_has_rows() {
  local label="$1"
  local json_path="$2"
  local rows

  rows="$(python -c 'import json,sys; print(len(json.load(open(sys.argv[1]))))' "$json_path")"
  if [[ "$rows" -eq 0 ]]; then
    echo "Phase failed: $label produced no benchmark rows at $json_path"
    exit 1
  fi
}

JMH_VERBOSE=""
if [[ "$VERBOSE" -eq 1 ]]; then
  JMH_VERBOSE="-v EXTRA"
fi

NATIVE_INCLUDE="io.github.semyonsinchenko.sparkss.benchmarks.(AffineGapBenchmark\.affineGap|NeedlemanWunschBenchmark\.needlemanWunsch|SmithWatermanBenchmark\.smithWaterman)"
NATIVE_SPARK_INCLUDE="io.github.semyonsinchenko.sparkss.benchmarks.NativeSpark(AffineGap|NeedlemanWunsch|SmithWaterman|JaroWinkler|MongeElkan)Benchmark"
LEGACY_INCLUDE="io.github.semyonsinchenko.sparkss.benchmarks.Legacy(AffineGap|NeedlemanWunsch|SmithWaterman|JaroWinkler|MongeElkan)UdfBenchmark"
COMPARE_ONLY_SCENARIOS="short-low-overlap,medium-medium-overlap,long-high-overlap"

if [[ "$MODE" == "native-only" ]]; then
  clear_or_wait_for_jmh_lock
  run_phase \
    "Native direct benchmarks" \
    "sbt \"benchmarks/jmh:run -bm thrpt -wi 2 -w 1s -i 3 -r 1s -f 1 $JMH_VERBOSE -rf json -rff $NATIVE_JSON $NATIVE_INCLUDE\""
  validate_json_has_rows "Native direct benchmarks" "$NATIVE_JSON"

  echo "Benchmark run completed successfully."
  echo "Mode: native-only"
  echo "Artifacts:"
  echo "- native (direct): $NATIVE_JSON"
else
  clear_or_wait_for_jmh_lock
  run_phase \
    "Native Spark SQL benchmarks" \
    "sbt \"benchmarks/jmh:run -bm thrpt -wi 6 -w 1s -i 6 -r 1s -f 1 -p scenario=$COMPARE_ONLY_SCENARIOS $JMH_VERBOSE -rf json -rff $NATIVE_SPARK_JSON $NATIVE_SPARK_INCLUDE\""
  validate_json_has_rows "Native Spark SQL benchmarks" "$NATIVE_SPARK_JSON"

  clear_or_wait_for_jmh_lock
  run_phase \
    "Legacy UDF benchmarks" \
    "sbt \"benchmarks/jmh:run -bm thrpt -wi 6 -w 1s -i 6 -r 1s -f 1 -p scenario=$COMPARE_ONLY_SCENARIOS $JMH_VERBOSE -rf json -rff $LEGACY_JSON $LEGACY_INCLUDE\""
  validate_json_has_rows "Legacy UDF benchmarks" "$LEGACY_JSON"

  run_phase \
    "Compare native Spark SQL vs legacy UDF" \
    "sbt \"benchmarkTools/runMain io.github.semyonsinchenko.sparkss.benchmarks.compare.BenchmarkCompareCli $NATIVE_SPARK_JSON $LEGACY_JSON --native-flow spark --out $COMPARE_TXT\""

  echo "Benchmark run completed successfully."
  echo "Mode: compare-only"
  echo "Artifacts:"
  echo "- native (spark): $NATIVE_SPARK_JSON"
  echo "- legacy: $LEGACY_JSON"
  echo "- compare: $COMPARE_TXT"
fi
