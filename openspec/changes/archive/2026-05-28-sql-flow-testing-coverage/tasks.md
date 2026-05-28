## 1. Build function coverage inventory

- [x] 1.1 Enumerate all SQL functions registered by the extension and record the expected list of 16 function names for test coverage mapping
- [x] 1.2 Compare the current `SparkSecondStringExtensionSuite` coverage against the inventory and identify missing function tests

## 2. Add SQL flow tests for missing functions

- [x] 2.1 Add one minimal happy-path SQL query test for each uncovered registered function in `SparkSecondStringExtensionSuite`
- [x] 2.2 Ensure each new test invokes the function through Spark SQL parsing and execution, then asserts query output results

## 3. Keep tests deterministic and maintainable

- [x] 3.1 Normalize fixtures and inputs to small canonical literals or minimal DataFrame setup to keep tests stable and fast
- [x] 3.2 Refactor repetitive test setup into shared helpers where needed while preserving clear per-function assertions

## 4. Validate coverage and stability

- [x] 4.1 Run the extension suite and confirm all registered functions are covered with at least one end-to-end SQL flow test
- [x] 4.2 Run broader project tests required by CI for this area and resolve any regressions introduced by the new coverage
