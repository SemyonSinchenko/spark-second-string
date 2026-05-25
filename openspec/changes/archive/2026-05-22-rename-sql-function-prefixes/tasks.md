## 1. SQL Registration Renaming

- [x] 1.1 Locate the central SQL function registration entry points and inventory all currently registered SQL function names.
- [x] 1.2 Rename SQL function mappings to `ss_`-prefixed names for all registered similarity functions.
- [x] 1.3 Apply explicit SQL name changes `levenshtein` -> `ss_levenshtein` and `jaro_winkler` -> `ss_jaro_winkler`.
- [x] 1.4 Remove any unprefixed SQL registration aliases so only canonical `ss_` names are exposed.

## 2. Validation and Test Coverage

- [x] 2.1 Update SQL registration tests to assert that `ss_levenshtein` and `ss_jaro_winkler` are present and unprefixed names are absent.
- [x] 2.2 Add or update a registry-wide invariant test to assert every exposed SQL function name matches the `^ss_` prefix policy.
- [x] 2.3 Run the relevant test suites for SQL registration and function invocation, then fix any failures caused by renamed SQL symbols.

## 3. SQL Usage Surface Alignment

- [x] 3.1 Update in-repo SQL examples and fixtures to use only `ss_`-prefixed function names.
- [x] 3.2 Search the codebase for unprefixed SQL metric names and replace SQL-call-site usages with canonical `ss_` names.
- [x] 3.3 Verify there are no remaining unprefixed SQL function references in maintained source/docs used by tests.
