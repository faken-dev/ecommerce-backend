# Contributing Guide

> Quick version: [`CONTRIBUTING.md`](../CONTRIBUTING.md) at the repository root.

---

## Table of Contents

- [Contributing Guide](#contributing-guide)
  - [Table of Contents](#table-of-contents)
  - [Branch Strategy](#branch-strategy)
  - [Coding Style](#coding-style)
    - [General](#general)
    - [Naming](#naming)
    - [Code Quality](#code-quality)
  - [Commit Convention](#commit-convention)
    - [Types](#types)
    - [Scopes](#scopes)
    - [Examples](#examples)
  - [Pull Request Checklist](#pull-request-checklist)
  - [Testing](#testing)
    - [Unit Tests](#unit-tests)
    - [Integration Tests](#integration-tests)
    - [Coverage](#coverage)
  - [Project Structure Reminders](#project-structure-reminders)
  - [Questions?](#questions)

---

## Branch Strategy

The project follows **Git Flow**:

```
main          ← Production-ready releases (tagged, never commit directly)
  └── develop ← Default working branch — all PRs target here
        ├── feature/<name>   ← New features
        ├── fix/<name>       ← Bug fixes
        └── chore/<name>     ← Maintenance, dependency updates, tooling
```

**Rules:**
- Always branch off `develop`, not `main`
- `main` is updated only via release merges from `develop`
- Keep branches short-lived — one feature or fix per branch
- Delete the branch after the PR is merged

```bash
git checkout develop
git pull upstream develop
git checkout -b feature/auth-otp-email
```

---

## Coding Style

### General

- Language: **Java 21** — use modern features (records, sealed classes, pattern matching) where appropriate
- Follow **DDD Layered Architecture** strictly — do not mix layers (e.g., no JPA annotations in the domain layer)
- Each Bounded Context (`auth`, `notification`, `user`, ...) must remain self-contained
- No cross-context direct dependencies — communicate via domain events through `shared/event/`

### Naming

| Element | Convention | Example |
|---|---|---|
| Classes | PascalCase | `OtpVerificationUseCase` |
| Methods / Variables | camelCase | `sendOtpToEmail()` |
| Constants | UPPER_SNAKE_CASE | `MAX_OTP_ATTEMPTS` |
| Database tables | snake_case | `otp_tokens` |
| Flyway migrations | `V{n}__{description}.sql` | `V3__create_otp_tokens.sql` |
| REST endpoints | kebab-case | `/api/v1/auth/otp/verify` |

### Code Quality

- All public methods and classes in the domain layer must have Javadoc
- Avoid raw types, unchecked casts, and `@SuppressWarnings` without justification
- Prefer constructor injection over field injection (`@Autowired` on fields is discouraged)
- Use `@Value` or `@ConfigurationProperties` for configuration — never hardcode env values
- DTOs must be immutable where possible (use records or Lombok `@Value`)

---

## Commit Convention

This project follows [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short description>

[optional body]

[optional footer]
```

### Types

| Type | When to use |
|---|---|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code restructuring without behavior change |
| `test` | Adding or updating tests |
| `docs` | Documentation only |
| `chore` | Build, deps, tooling — no production code change |
| `perf` | Performance improvement |
| `ci` | CI/CD configuration changes |

### Scopes

Use the Bounded Context name as scope: `auth`, `notification`, `user`, `product`, `order`, `payment`, `shipping`, `shared`.

### Examples

```
feat(auth): add JWT refresh token rotation
fix(otp): handle expired OTP gracefully
refactor(user): extract address validation to value object
test(auth): add integration tests for login endpoint
chore(deps): upgrade Spring Boot to 4.0.1
docs(readme): update getting started section
```

**Rules:**
- Subject line max 72 characters
- Use imperative mood: "add feature" not "added feature"
- Do not end the subject line with a period
- Reference issues in the footer: `Closes #42`

---

## Pull Request Checklist

Before opening a PR, verify the following:

**Code**
- [ ] Branch is up to date with `develop` (`git pull upstream develop`)
- [ ] No unrelated changes in the diff
- [ ] No hardcoded secrets, credentials, or environment-specific values
- [ ] No commented-out code left in (remove or convert to a TODO with issue reference)

**Build & Tests**
- [ ] `./mvnw verify` passes locally with no errors
- [ ] New features have corresponding unit tests
- [ ] Integration tests pass (`./mvnw verify -Pintegration`)

**Database**
- [ ] New Flyway migrations follow naming: `V{n}__{snake_case_description}.sql`
- [ ] Migrations are non-destructive (no `DROP` without proper versioning)
- [ ] Migrations have been tested locally against a clean database

**Documentation**
- [ ] New endpoints are documented via OpenAPI annotations
- [ ] README or `docs/` updated if behavior has changed
- [ ] Javadoc added for public domain layer methods

**PR Description**
- [ ] Clearly describes *what* changed and *why*
- [ ] Includes steps to test the change
- [ ] Links to the relevant issue(s) if applicable

---

## Testing

### Unit Tests

Located in `src/test/unit/`. Use **JUnit 5 + Mockito**.

- Test domain services and use cases in isolation
- Mock all infrastructure dependencies (repositories, external APIs)
- Naming: `<ClassUnderTest>Test` — e.g., `OtpVerificationUseCaseTest`

```bash
./mvnw test
```

### Integration Tests

Located in `src/test/integration/`. Use **Testcontainers** (PostgreSQL spun up automatically).

- Test full request lifecycle through the REST layer
- Cover happy path and key error scenarios (invalid input, expired OTP, etc.)

```bash
./mvnw verify -Pintegration
```

### Coverage

- Aim for **≥ 80% line coverage** on the `application` and `domain` layers
- Infrastructure layer (JPA, external APIs) is acceptable at lower coverage — focus on integration tests there

---

## Project Structure Reminders

When adding new code, always respect the DDD layer boundaries:

```
<context>/
├── domain/          ← Pure business logic — no Spring, no JPA here
│   ├── entity/
│   ├── event/
│   └── service/
├── application/     ← Use cases, DTOs, mappers — orchestrates domain
│   ├── command/
│   ├── dto/
│   ├── mapper/
│   └── usecase/
├── infrastructure/  ← Spring, JPA, external services
│   ├── persistence/
│   └── security/
└── presentation/    ← REST controllers only — no business logic
    └── controller/
```

Cross-cutting concerns (exception handling, security config, shared base classes) go in `shared/`.

---

## Questions?

Open an [Issue](https://github.com/faken-dev/ecommerce-backend/issues) or start a [Discussion](https://github.com/faken-dev/ecommerce-backend/discussions).