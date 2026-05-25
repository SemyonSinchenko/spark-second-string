## 1. Extension Entry Point and Shared Registration

- [x] 1.1 Identify the existing JVM/advanced registration path and extract a shared internal registration routine that can be reused.
- [x] 1.2 Add a new production `SparkSessionExtensions` subclass that invokes the shared registration routine during Spark extension initialization.
- [x] 1.3 Keep the existing extension/registration API behavior unchanged while wiring it to the shared routine.

## 2. Function Coverage and Compatibility

- [x] 2.1 Ensure the shared registration routine registers the complete supported SQL function set with no omissions.
- [x] 2.2 Verify registration behavior is consistent between the new Spark extension path and the legacy JVM path.
- [x] 2.3 Validate compatibility for Spark SQL and PySpark startup flows using `spark.sql.extensions` configuration.

## 3. Tests and Regression Protection

- [x] 3.1 Add an integration-style test that starts Spark with the new extension configured and asserts project SQL functions resolve without manual registration.
- [x] 3.2 Add PySpark-oriented coverage (or equivalent cross-language startup test) proving SQL queries can execute project functions via configured extension loading.
- [x] 3.3 Add regression coverage confirming the legacy JVM registration path remains usable and functionally unchanged.

## 4. Documentation and Verification

- [x] 4.1 Document recommended usage of `spark.sql.extensions` for cluster/session configuration, including expected behavior.
- [x] 4.2 Run the relevant test suites and record verification outcomes for extension loading, function availability, and backward compatibility.

## Verification outcomes

- `sbt "testOnly io.github.semyonsinchenko.sparkss.sql.ConfiguredStringSimilaritySparkSessionExtensionsSuite"` (pass)
