## MODIFIED Requirements

### Requirement: Reproducible synthetic dataset generation
The system SHALL generate seeded deterministic string pairs using a case-driven randomized construction model so that runs with identical `(seed, rows)` produce identical generated datasets and report values, while avoiding globally forced shared-prefix structure.

#### Scenario: Repeated run yields identical output
- **WHEN** the CLI is executed twice with the same `--seed` and `--rows`
- **THEN** both runs produce identical generated pairs and identical markdown report tables

#### Scenario: Seed change alters generated data
- **WHEN** the CLI is executed with different `--seed` values and the same `--rows`
- **THEN** the generated string pairs differ between runs

#### Scenario: Generation uses diverse case cohorts without universal prefixing
- **WHEN** synthetic pairs are generated for parity testing
- **THEN** pair relationships are produced by deterministic case builders for required cohorts and no fixed shared-prefix rule is applied to every pair
