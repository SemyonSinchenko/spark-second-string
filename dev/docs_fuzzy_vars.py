#!/usr/bin/env python3

import argparse
from pathlib import Path


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Parse fuzzy report into Laika variables")
    parser.add_argument("--input", required=True, help="Path to fuzzy-report.md")
    parser.add_argument("--output", required=True, help="Path to output fuzzy-testing.conf")
    return parser.parse_args()


def parse_summary_table(lines: list[str]) -> list[str]:
    table: list[str] = []
    started = False

    for line in lines:
        trimmed = line.strip()
        if not started and trimmed.startswith("metric | compared rows | excluded NULL scaled rows"):
            started = True

        if started:
            if trimmed == "":
                break
            table.append(trimmed)

    if len(table) < 3:
        raise ValueError("Could not parse fuzzy summary table")

    return table


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
    table = parse_summary_table(lines)

    # Parse summary-level stats from the ALL row and per-metric rows
    total_compared_rows = 0
    overall_within_5 = "0.00%"
    lowest_metric = "n/a"
    lowest_metric_over_30 = "n/a"
    lowest_value = None

    # Emit path relative to the project root (the scripts run with cwd = repoRoot)
    try:
        rel_path = input_path.resolve().relative_to(Path.cwd().resolve())
    except ValueError:
        rel_path = input_path

    hocon_lines = [
        "fuzzy {",
        f"  source_path = {hocon_string(str(rel_path))}",
    ]

    for row in table[2:]:  # skip header + separator
        parts = [part.strip() for part in row.split("|")]
        if len(parts) != 13:
            continue

        metric = parts[0]
        compared_rows = parts[1]
        pearson = parts[3]
        spearman = parts[4]
        within_5_pct = parts[6]
        within_10_pct = parts[8]
        within_30_pct = parts[10]
        over_30_pct = parts[12]

        if metric == "ALL":
            try:
                total_compared_rows = int(compared_rows)
            except ValueError:
                total_compared_rows = 0
            overall_within_5 = within_5_pct

            hocon_lines.append(f"  total_compared_rows = {hocon_string(str(total_compared_rows))}")
            hocon_lines.append(f"  overall_within_5_percent = {hocon_string(overall_within_5)}")
            continue

        # Track metric with lowest >30% drift
        try:
            value = float(over_30_pct.replace("%", ""))
        except ValueError:
            continue
        if lowest_value is None or value < lowest_value:
            lowest_value = value
            lowest_metric = metric
            lowest_metric_over_30 = over_30_pct

        # Emit per-metric per-cell variables
        key = metric.replace("-", "_")
        hocon_lines.append(f"  {key} {{")
        hocon_lines.append(f"    rows = {hocon_string(compared_rows)}")
        hocon_lines.append(f"    pearson = {hocon_string(pearson)}")
        hocon_lines.append(f"    spearman = {hocon_string(spearman)}")
        hocon_lines.append(f"    within_5 = {hocon_string(within_5_pct)}")
        hocon_lines.append(f"    within_10 = {hocon_string(within_10_pct)}")
        hocon_lines.append(f"    within_30 = {hocon_string(within_30_pct)}")
        hocon_lines.append(f"    over_30 = {hocon_string(over_30_pct)}")
        hocon_lines.append("  }")

    hocon_lines.append(f"  lowest_over_30_metric = {hocon_string(lowest_metric)}")
    hocon_lines.append(f"  lowest_over_30_percent = {hocon_string(lowest_metric_over_30)}")
    hocon_lines.append("}")
    hocon_lines.append("")

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text("\n".join(hocon_lines), encoding="utf-8")


if __name__ == "__main__":
    main()
