# API Sample Test Suite

[![Build](https://github.com/Zap4ick/api-sample/actions/workflows/build.yml/badge.svg)](https://github.com/Zap4ick/api-sample/actions/workflows/build.yml)

## Overview
This project contains TestNG-based API tests for player management endpoints. Tests are organized by endpoint and cover positive and negative scenarios.

## Requirements
- **Java:** 21+
- **Build Tool:** Gradle 9.2+

## Test Coverage (By Class)
- `CreatePlayerTest`
  - Positive: supervisor/admin can create players (supervisor→admin, admin→user)
  - Positive: boundary ages and password lengths
  - Negative: invalid ages, invalid passwords, invalid gender, duplicate login/screenName, regular user cannot create
- `RetrievePlayerTest`
  - Positive: get player by ID, user can get self
  - Negative: non-existing ID, invalid ID format, user cannot get another user
- `UpdatePlayerTest`
  - Positive: supervisor/admin update one field
  - Positive: supervisor/admin update multiple fields with boundary values (min/max age and password)
  - Positive: user updates own fields (one and multiple)
  - Positive: empty update body does not change fields
  - Negative: update login/id, invalid age type, invalid gender, boundary violations
  - Negative: duplicate login/screenName
  - Negative: unprivileged users cannot change their own role (USER→ADMIN)
- `DeletePlayerTest`
  - Positive: supervisor/admin can delete users (supervisor→user, admin→admin)
  - Positive: admin can delete itself (created admin)
  - Negative: cannot delete protected admin user (user/admin cannot delete admin)
  - Negative: cannot delete supervisor (user/admin cannot delete supervisor from config)
  - Negative: cannot delete themselves, invalid ID type, non-existing ID
- `GetAllPlayersTest`
  - Positive: get-all contains created player
  - Sanity: default supervisor/admin exist
  - Positive: schema/boundary validation in global list (limited to 100 players)
  - Negative: regular user cannot get all players

## Key Assumptions
- Only `ADMIN` and `SUPERVISOR` can create players.
- Creating `SUPERVISOR` users is not allowed (tests avoid creating supervisor accounts).
- Gender values are limited to `MALE` and `FEMALE`.
- Age boundaries: 17–59 inclusive.
- Password boundaries: 7–15 characters inclusive, and must contain digits.
- Regular users cannot access or modify other users' data.
- Deletion returns HTTP 204 (no body).
- Update cannot change immutable fields like `login` or `id`.
- Duplicate `login` and `screenName` are rejected.
- Role changes are restricted: only privileged users (ADMIN/SUPERVISOR) can change roles; regular users cannot change their own role or others' roles.

## Testing Approaches

### Actor Creation Strategy
We create all actor roles (`USER`, `ADMIN`) needed for tests. However, we **do NOT create new `SUPERVISOR` accounts** because:
- Creating supervisors is not allowed by the API.
- Supervisor identity is sourced from `TestConfig` (e.g., `TestConfig.getSupervisorId()`, `TestConfig.getSupervisorLogin()`).
- This ensures we test with a known, immutable supervisor identity and avoid dependency on creation endpoints for privileged roles.

### Enum vs String in Request/Response DTOs
Request DTOs use **enums** (e.g., `Role.ADMIN`, `Gender.MALE`) for type safety and validation at compile time. Response DTOs use **strings** (e.g., `"ADMIN"`, `"MALE"`) because:
- The API returns JSON with string values, and we deserialize them as-is for schema fidelity.
- We provide helper methods in response DTOs (e.g., `getRoleAsEnum()`, `getGenderAsEnum()`) to convert to enums when needed for assertions.
- This approach decouples request validation from response parsing and catches schema mismatches early (if the API returns unexpected values, the enum conversion will fail).

## Suites
Suite XML files are in `src/test/resources/suites/`:
- `full.xml`: full regression suite
- `sanity.xml`: sanity subset (group `sanity`)

## Test Data & Cleanup
- Tests use `TestDataGenerator` for randomized data.
- Created players are tracked and deleted in `@AfterMethod` cleanup.

## Logs in Allure
Step logs are written via `BaseTest.log(...)`, which uses `Allure.addAttachment(...)` to attach steps to the report.
`Allure.step(...)` was avoided to prevent excessive nesting in the report.

## Commands

### Clean build output
```bash
./gradlew clean
```

### Run tests (from IDE)
- Run `full.xml` or `sanity.xml` as a TestNG suite.

### Run tests (Gradle)
If your IDE is not used, you can run all tests:
```bash
./gradlew test
```
To run a suite with the dedicated tasks:
```bash
./gradlew testFull
./gradlew testSanity
```

### Generate and view Allure report
```bash
allure serve build/allure-results
```
This generates the report and opens it in your default browser. The report is served on `http://localhost:4040` by default.

### Generate report only
```bash
allure generate build/allure-results -o build/allure-report
```
The report will be generated in `build/allure-report/` (use this if you prefer to open the report manually).

### GitHub Actions (full suite + Allure artifacts)
You can run the full suite and generate Allure artifacts from GitHub Actions:
1. Go to **Actions → Build → Run workflow**
2. Set `run_tests` to `true`
3. After completion, download `allure-report` and `allure-results` artifacts
