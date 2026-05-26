## Context

The legacy fuzzy scoring path currently constructs one `com.wcohen.secondstring.Jaccard`
instance per executor JVM and reuses it through a lazy-initialized scorer. Spark executors
run multiple task threads concurrently, so that single scorer instance is invoked in parallel.

`SimpleTokenizer.intern` inside SecondString mutates a shared `TreeMap` while interning
tokens. `TreeMap` is not thread-safe. Under concurrent writes, red-black tree rotations can
observe inconsistent child pointers, producing the observed
`NullPointerException` at `TreeMap.rotateLeft`.

Constraints:
- Keep the legacy flow behavior and scoring semantics unchanged.
- Avoid broad refactors in the fuzzy pipeline.
- Minimize runtime overhead and avoid global synchronization bottlenecks.

## Goals / Non-Goals

**Goals:**
- Eliminate cross-thread shared mutable state in legacy SecondString scorer instances.
- Preserve output compatibility for fuzzy scores in existing pipelines.
- Align implementation with the existing thread-confined scorer pattern used elsewhere.

**Non-Goals:**
- Rewriting or upgrading the SecondString library internals.
- Changing algorithm selection, tokenization semantics, or threshold logic.
- Redesigning broader Spark UDF execution architecture.

## Decisions

1. Use thread-confined scorer instances via `ThreadLocal` in `LegacyScorer`.
   - Why: each Spark task thread gets a dedicated scorer object graph (`Jaccard` and its
     tokenizer), removing concurrent mutation on shared `TreeMap` state.
   - Alternative considered: `synchronized` around score calls.
     - Rejected because it serializes all scoring on a single lock, reducing executor
       parallelism and potentially creating throughput regression under high task concurrency.
   - Alternative considered: instantiate a new scorer per `apply` call.
     - Rejected because repeated reflective construction per row is expensive and unnecessary.

2. Keep reflective class loading/method discovery behavior, but scope it per-thread.
   - Why: preserves compatibility with existing algorithm class wiring while still avoiding
     shared mutable scorer instances.
   - Alternative considered: a global cache keyed by class name.
     - Rejected because global caches reintroduce shared lifecycle complexity and thread-safety
       concerns for legacy implementations.

3. Keep fix localized to the legacy fuzzy flow path.
   - Why: observed race is specific to shared legacy scorer instance reuse; local change reduces
     regression surface.
   - Alternative considered: broad pipeline-level concurrency controls.
     - Rejected as over-scoped for this defect and likely to impact unrelated code paths.

## Risks / Trade-offs

- [Increased per-thread memory footprint] -> Mitigation: each active executor thread holds one
  scorer instance; expected footprint is small and bounded by task concurrency.
- [ThreadLocal lifecycle in long-lived executors] -> Mitigation: keep value type minimal and
  avoid retaining outer large objects; rely on thread reuse model and executor shutdown to
  reclaim.
- [Potential subtle behavior difference from shared warm tokenizer cache] -> Mitigation: score
  semantics remain the same; verify with representative regression tests and sample datasets.
- [Reflection failure handling differences if initialization moves to ThreadLocal] -> Mitigation:
  preserve existing exception handling and initialization path semantics per thread.

## Migration Plan

1. Replace shared lazy scorer initialization in the legacy scorer wrapper with `ThreadLocal`
   initialization.
2. Keep invocation contract unchanged (`(String, String) => Double`) to avoid downstream API
   changes.
3. Run fuzzy flow regression tests and concurrency-stress test to confirm no `TreeMap` NPE and
   no score drift.
4. Roll out normally with existing deployment process.

Rollback strategy:
- Revert the localized `ThreadLocal` change in legacy scorer wrapper if unexpected regressions
  appear.

## Open Questions

- Do we already have a deterministic fuzz/regression fixture for legacy SecondString scoring, or
  should one be added as part of implementation tasks?
- Should we proactively apply the same thread-confinement pattern to other legacy algorithm
  wrappers that might hide mutable internals?
