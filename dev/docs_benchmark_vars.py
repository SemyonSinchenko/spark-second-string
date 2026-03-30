#!/usr/bin/env python3

import argparse
from pathlib import Path


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Parse benchmark compare table into Laika variables")
    parser.add_argument("--input", required=True, help="Path to compare-table.txt")
    parser.add_argument("--output", required=True, help="Path to output benchmarks.conf")
    return parser.parse_args()


def parse_summary_table(lines: list[str]) -> tuple[list[str], str, str]:
    table: list[str] = []
    started = False

    for line in lines:
        trimmed = line.strip()
        if not started and trimmed == "algorithm | spark-native | UDF | diff":
            started = True

        if started:
            if trimmed == "":
                break
            table.append(trimmed)

    if len(table) < 3:
        raise ValueError("Could not parse benchmark summary table")

    best_algorithm = "n/a"
    best_diff = "n/a"
    best_abs = None

    for row in table[2:]:
        parts = [part.strip() for part in row.split("|")]
        if len(parts) != 4:
            continue
        algorithm, _, _, diff = parts
        try:
            numeric = float(diff.replace("%", ""))
            abs_value = abs(numeric)
        except ValueError:
            continue
        if best_abs is None or abs_value < best_abs:
            best_abs = abs_value
            best_algorithm = algorithm
            best_diff = diff

    return table, best_algorithm, best_diff


def hocon_string(value: str) -> str:
    escaped = value.replace("\\", "\\\\").replace('"', '\\"')
    return f'"{escaped}"'


def main() -> None:
    args = parse_args()
    input_path = Path(args.input)
    output_path = Path(args.output)

    if not input_path.exists():
        raise SystemExit(f"Missing input file: {input_path}")

    lines = input_path.read_text(encoding="utf-8").splitlines()
    table, best_algorithm, best_diff = parse_summary_table(lines)

    # Emit per-cell variables so markdown tables can reference individual values.
    # Laika substitutes variables as plain text, not markdown, so a single
    # summary_table variable would not render as a table.
    # Emit path relative to the project root (the scripts run with cwd = repoRoot)
    try:
        rel_path = input_path.resolve().relative_to(Path.cwd().resolve())
    except ValueError:
        rel_path = input_path

    hocon_lines = [
        "benchmarks {",
        f"  source_path = {hocon_string(str(rel_path))}",
        f"  algorithm_count = {hocon_string(str(len(table) - 2))}",
        f"  best_algorithm = {hocon_string(best_algorithm)}",
        f"  best_diff_percent = {hocon_string(best_diff)}",
    ]

    for row in table[2:]:  # skip header + separator
        parts = [part.strip() for part in row.split("|")]
        if len(parts) != 4:
            continue
        algorithm, native, udf, diff = parts
        # Use underscores in key names (HOCON-safe)
        key = algorithm.replace("-", "_")
        hocon_lines.append(f"  {key} {{")
        hocon_lines.append(f"    native = {hocon_string(native)}")
        hocon_lines.append(f"    udf = {hocon_string(udf)}")
        hocon_lines.append(f"    diff = {hocon_string(diff)}")
        hocon_lines.append("  }")

    hocon_lines.append("}")
    hocon_lines.append("")

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text("\n".join(hocon_lines), encoding="utf-8")


if __name__ == "__main__":
    main()
