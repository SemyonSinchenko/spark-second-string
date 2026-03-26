## Context

The repository currently benchmarks Spark-native (Catalyst/codegen) implementations, but it does not produce a matched baseline against legacy Java SecondString executed through Spark UDF calls. This makes migration and regression analysis difficult because results are not generated under the same settings and are not compared in a single, deterministic output.

This change introduces a dual-flow benchmark suite:
- native Spark expression benchmarks (existing flow), and
- legacy Java SecondString wrapped as Spark UDF benchmarks (new flow).

Both flows must run with identical JMH settings, avoid timed-method I/O, and emit JSON outputs that can be compared algorithm-by-algorithm by a local helper CLI. The scope is local benchmark reproducibility and comparability, not production behavior changes.

## Goals / Non-Goals

**Goals:**
- Produce repeatable, apples-to-apples benchmark outputs for native and legacy-UDF flows.
- Guarantee warmup/measurement/fork parity across flows.
- Eliminate file/network I/O from timed benchmark methods by preparing and caching DataFrames during setup.
- Provide a deterministic JSON comparison step that renders a compact table (`algorithm | spark-native | UDF | diff`).
- Provide one orchestration entrypoint that runs flows in strict order and writes predictable output artifacts.

**Non-Goals:**
- Changing algorithm semantics or introducing functional rewrites.
- Changing production SQL/DSL behavior.
- Per-algorithm auto-tuning of JMH parameters.
- Distributed cluster benchmarking or environment-specific optimization.
- Replacing JMH JSON as the canonical source format.
- Publishing the comparison CLI as an external artifact.

## Decisions

1. **Run two independent benchmark flows, then compare JSON outputs.**
   - **Why:** Keeps native and UDF execution paths explicit and traceable while preserving existing native benchmarks.
   - **Alternatives considered:**
     - Single mixed benchmark class containing both paths. Rejected because it increases coupling and makes flow-specific setup constraints harder to enforce.
     - Manual side-by-side review of separate reports. Rejected because it is error-prone and non-deterministic.

2. **Enforce shared JMH configuration between native and UDF suites.**
   - **Why:** Runtime parity is required for meaningful baseline comparisons.
   - **Alternatives considered:**
     - Independent settings per flow. Rejected because outcome differences could reflect configuration drift instead of implementation differences.

3. **Prepare benchmark input DataFrames during setup, cache, and materialize before timed measurement.**
   - **Why:** Prevents I/O and lazy materialization overhead from contaminating timed method measurements.
   - **Alternatives considered:**
     - Building/loading data inside timed methods. Rejected because it invalidates benchmark intent.
     - Materializing only one flow. Rejected because it breaks comparability.

4. **Use a hardcoded benchmark-class mapping in a non-published Scala CLI helper.**
   - **Why:** Mapping between native and legacy benchmark identifiers is finite and known; hardcoding avoids ambiguous matching rules and keeps output deterministic.
   - **Alternatives considered:**
     - Fuzzy name matching. Rejected because it can silently pair wrong algorithms.
     - External mapping file. Deferred to reduce operational complexity for this change.

5. **Create `/dev/benchmarks_suite.sh` as the canonical orchestration entrypoint (native -> UDF -> compare).**
   - **Why:** Encodes required run order and output conventions in one repeatable command.
   - **Alternatives considered:**
     - Relying on ad hoc manual command sequences. Rejected due to inconsistency risk.
     - Embedding orchestration inside benchmark classes. Rejected because orchestration concern belongs outside benchmark execution logic.

## Risks / Trade-offs

- **[Risk] Legacy UDF coverage does not exist for all native algorithms** -> **Mitigation:** Maintain explicit unsupported mappings and fail/report clearly instead of approximating.
- **[Risk] Hardcoded mapping drifts as benchmark class names evolve** -> **Mitigation:** Keep mapping in one place and add validation checks for unmatched classes during compare step.
- **[Risk] Benchmark runtime increases significantly because suites run twice** -> **Mitigation:** Document expected duration, keep deterministic defaults, and allow controlled verbosity for troubleshooting.
- **[Risk] Cache/materialization strategy can increase memory pressure** -> **Mitigation:** Keep benchmark datasets bounded and use explicit setup boundaries.
- **[Risk] Orchestration script path assumptions vary by environment** -> **Mitigation:** Use repo-relative conventions and deterministic output paths.

## Migration Plan

1. Implement the legacy UDF benchmark flow alongside the existing native flow.
2. Align both flows to a shared JMH parameter set and setup-time data preparation requirements.
3. Add the compare CLI helper and hardcoded class mapping for supported algorithms.
4. Add `/dev/benchmarks_suite.sh` to execute native flow first, UDF flow second, then JSON comparison.
5. Validate that output files are deterministic and comparison output is produced with required columns.

Rollback strategy:
- If integration issues occur, stop using the orchestration script and compare helper, and continue running the existing native benchmark flow unchanged.
- Remove or disable legacy UDF benchmark entrypoints without affecting existing benchmark semantics.

## Open Questions

- Should unmatched algorithms in compare output be treated as hard failures by default, or warnings with non-zero exit only behind a strict flag?
- What default output directory structure best balances CI artifact retention and local developer convenience?
- Do we need a minimal machine-readable compare output mode (for CI checks) in addition to the human-readable table?
