# Token Metric Kernel

## Purpose

Define shared token-set helper semantics used by token-based string similarity metrics.

## Requirements

### Requirement: Shared token metric helper abstraction
The system SHALL provide a reusable helper abstraction for token-based similarity metrics.

#### Scenario: Reusable tokenization behavior
- **WHEN** token-based metrics are implemented
- **THEN** shared whitespace tokenization into unique token sets SHALL be provided by the helper abstraction
- **THEN** token metrics SHALL be able to reuse tokenization behavior without duplicating parsing logic

#### Scenario: Reusable set-overlap primitives
- **WHEN** token-based metrics compute overlap-based formulas
- **THEN** shared primitives for set intersection and related cardinality operations SHALL be provided by the helper abstraction
- **THEN** token metrics SHALL be able to reuse these primitives without metric-specific duplication

#### Scenario: Deterministic empty-input conventions
- **WHEN** token metrics evaluate both-empty or one-empty inputs
- **THEN** helper semantics SHALL support consistent boundary behavior required by each metric formula

#### Scenario: Token-sequence support for soft token metrics
- **WHEN** token metrics require ordered token-sequence comparison semantics such as `monge_elkan`
- **THEN** shared helper behavior SHALL provide deterministic token-sequence access in addition to set-overlap helpers
- **THEN** sequence semantics SHALL preserve token order and repeated-token occurrences

### Requirement: Behavior-preserving token metric migration
The system SHALL preserve existing token metric behavior when migrating token metrics to the shared token helper.

#### Scenario: Existing metric output invariance
- **WHEN** existing token metrics are migrated to shared helper usage
- **THEN** `jaccard`, `sorensen_dice`, `overlap_coefficient`, and `cosine` SHALL preserve existing outputs for equivalent inputs
- **THEN** duplicate-token handling and whitespace normalization semantics SHALL remain unchanged

### Requirement: Token helper validation coverage
The system SHALL provide test coverage for shared token helper behavior used by token metrics.

#### Scenario: Shared semantics validation
- **WHEN** helper behavior is tested
- **THEN** tests SHALL cover tokenization equivalence for repeated and mixed whitespace separators
- **THEN** tests SHALL cover set-overlap primitives and duplicate-token collapse semantics

#### Scenario: Monge-elkan boundary and consistency coverage
- **WHEN** token helper and metric validation suites are executed for `monge_elkan`
- **THEN** coverage SHALL include both-empty, one-empty, whitespace-only, repeated-token, punctuation-bearing, asymmetric token-count, and token-order-variation inputs
- **THEN** repeated execution over identical inputs SHALL return identical scores

#### Scenario: Monge-elkan benchmark baselines
- **WHEN** token-metric benchmark suites are executed
- **THEN** `monge_elkan` benchmarks SHALL include short, medium, and long input cohorts across low, medium, and high token overlap
- **THEN** results SHALL include baselines against existing token metrics and selected matrix metrics

### Requirement: Token benchmark dual-flow comparability with explicit legacy support boundaries
The token metric kernel capability SHALL define dual-flow benchmark obligations for Spark-native and legacy-UDF baselines where legacy equivalents exist, and SHALL require explicit handling for unsupported legacy mappings.

#### Scenario: Supported token metrics are compared across equivalent flows
- **WHEN** token metric benchmark suites include algorithms with defined legacy equivalents
- **THEN** native and legacy flows MUST run under identical JMH settings with aligned algorithm and scenario identifiers for deterministic matching

#### Scenario: Unsupported legacy mappings are handled explicitly
- **WHEN** a token metric has no supported legacy equivalent
- **THEN** the benchmark and comparison workflow MUST report the metric as unsupported for legacy matching
- **THEN** the workflow MUST NOT substitute approximate or alternate algorithms implicitly
