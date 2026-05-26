## 1. Inventory and map naming mismatches

- [x] 1.1 Compile the list of fuzzy-related legacy SQL routine names currently referenced in tests, docs, and code.
- [x] 1.2 Map each legacy routine reference to its canonical `ss_`-prefixed function name and identify exact files to update.

## 2. Update fuzzy-testing SQL invocation paths

- [x] 2.1 Replace unprefixed SQL function references in fuzzy-testing expression generation with `ss_`-prefixed names.
- [x] 2.2 Verify DataFrame-based fuzzy SQL evaluation paths invoke only registered `ss_` function names.

## 3. Sync documentation and examples

- [x] 3.1 Update Existing Metrics and related docs so all SQL function names use the `ss_` prefix convention.
- [x] 3.2 Confirm each documented metric still includes DSL name, SQL name, parameters/defaults, and output details after renaming.

## 4. Validate and guard against regressions

- [x] 4.1 Run fuzzy-testing workflows and targeted tests to confirm unresolved routine-name errors are eliminated.
- [x] 4.2 Run a repository-wide scan for known unprefixed legacy routine tokens and resolve remaining mismatches.
