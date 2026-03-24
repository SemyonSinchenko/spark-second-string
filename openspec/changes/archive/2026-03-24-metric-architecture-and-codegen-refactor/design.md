## Context

The current string similarity expression base class mixes Catalyst execution mechanics with algorithm logic. It manually reads ordinals from `InternalRow`, relies on `CodegenFallback`, and leaves code generation contracts incomplete in concrete metrics (for example, `Jaccard#doGenCode` returns a placeholder).

This change introduces a stricter architecture that separates:
- expression lifecycle and Catalyst integration (type checks, null handling, child evaluation, codegen plumbing)
- metric kernels (token or matrix algorithm implementation)

The proposal and specs require a unified binary contract, support for both token and matrix families, deterministic interpreted/codegen parity, and a DSL-first API with optional SQL registration through a thin SparkSession extension.

## Goals / Non-Goals

**Goals:**
- Define a reusable expression framework for string similarity metrics that is correct under Catalyst child-evaluation semantics.
- Establish a codegen-first metric contract where interpreted and generated paths produce identical results.
- Refactor `Jaccard` to conform to the new contract with complete, executable `doGenCode` logic.
- Keep metric algorithm code isolated so additional token and matrix metrics can be added with minimal expression boilerplate.
- Provide a thin optional SQL registration extension that reuses DSL expressions rather than creating parallel SQL-only implementations.

**Non-Goals:**
- Building SQL-first ergonomics, SQL-specific optimizations, or a SQL-only feature surface.
- Delivering a broad set of new metrics beyond the framework-ready shape and Jaccard rework.
- Changing external Spark Catalyst semantics or introducing custom query-planner rules.

## Decisions

1. Introduce a two-layer architecture: expression contract + metric kernel interfaces
   - Decision: Create a single binary expression base that owns Catalyst concerns (input type checks, null intolerance, child evaluation, codegen orchestration), and delegate scoring to metric-family-specific kernel interfaces.
   - Rationale: This removes current coupling, avoids duplicated expression lifecycle code in each metric, and supports both token and matrix families under one stable contract.
   - Alternatives considered:
     - Keep one monolithic `StringSimExpression` and add more abstract methods: rejected because coupling remains high and family-specific behavior leaks into the base.
     - Duplicate per-family expression bases (token base, matrix base): rejected due to repeated Catalyst logic and higher divergence risk.

2. Make child expression evaluation canonical and ordinal-free
   - Decision: Rely on Catalyst child evaluation flow (`nullSafeEval` and generated children code) instead of reading fixed ordinal positions from `InternalRow`.
   - Rationale: Fixed ordinal access is brittle when expressions are nested or reordered and violates expected Catalyst expression behavior.
   - Alternatives considered:
     - Continue manual `eval(input)` with hardcoded ordinals: rejected for correctness risk and maintenance burden.

3. Require complete code generation for metrics that claim codegen support
   - Decision: Each metric must provide executable codegen output; placeholder or null codegen output is forbidden. Interpreted and generated paths must share the same algorithmic semantics.
   - Rationale: Prevents silent fallback behavior and runtime surprises; creates deterministic performance and correctness expectations.
   - Alternatives considered:
     - Permit partial codegen and fallback in hot paths: rejected because it weakens parity guarantees and obscures performance behavior.

4. Implement Jaccard with shared algorithm semantics across interpreted and generated paths
   - Decision: Define Jaccard tokenization, deduplication, and edge-case behavior once, then mirror the same logic in generated code.
   - Rationale: Spec requires exact parity, including empty-input and whitespace normalization behavior.
   - Alternatives considered:
     - Keep interpreted-only implementation via `CodegenFallback`: rejected by spec parity requirements.

5. Preserve DSL-first API and add optional thin SparkSession registration
   - Decision: Keep Scala/Java constructors/functions as the primary integration surface; provide optional SQL function registration as an adapter over existing expressions.
   - Rationale: Aligns with project direction while allowing SQL consumers to opt in without creating a separate API model.
   - Alternatives considered:
     - SQL-first wrappers as primary API: rejected by scope and proposal direction.

6. Expand tests around contract behavior, not only algorithm output
   - Decision: Add tests for null propagation, child evaluation correctness, interpreted/codegen parity, and Jaccard edge cases (empty strings, duplicate tokens, whitespace variants).
   - Rationale: Contract regressions are likely in refactors and may not be detected by pure algorithm-level tests.
   - Alternatives considered:
     - Limit tests to `getSim` outputs: rejected because it misses Catalyst integration failures.

## Risks / Trade-offs

- [Generated code complexity increases implementation effort] -> Mitigation: keep codegen logic narrow per metric and add parity tests that compare interpreted vs codegen execution paths.
- [Refactor can break existing expression behavior] -> Mitigation: add compatibility-focused integration tests and preserve public DSL signatures where possible.
- [Token and matrix abstractions can over-generalize too early] -> Mitigation: define minimal family interfaces and evolve only when a new metric family requires expansion.
- [Performance regressions from stricter contracts] -> Mitigation: benchmark hot-loop Jaccard paths and prefer allocation-light loops/collections in both interpreted and generated implementations.

## Migration Plan

1. Introduce the new base contract and family kernel interfaces behind existing package structure.
2. Refactor `Jaccard` to the new contract and implement complete codegen behavior.
3. Add/adjust DSL entry points if needed, preserving current usage style.
4. Add optional SparkSession registration extension that delegates to existing expression constructors.
5. Expand test coverage for contract semantics and parity.
6. Remove obsolete logic (manual ordinal-based evaluation, placeholder codegen patterns) once tests pass.

Rollback strategy: keep changes in a single isolated refactor set so reversion can restore prior expression classes if unexpected Catalyst/runtime regressions appear.

## Open Questions

- Should matrix metrics share the exact same tokenizer/normalization hooks as token metrics, or only the expression contract?
- Is the SQL registration extension expected to register all metrics eagerly or allow selective registration?
- Do we need explicit microbenchmarks in this change, or are parity and correctness tests sufficient for acceptance?
