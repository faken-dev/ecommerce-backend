# Contributing to E-Commerce Backend

Thank you for your interest in contributing! Please take a moment to read through these guidelines before getting started.

> For in-depth coding standards, branch conventions, and PR checklist, see [`docs/contributing.md`](docs/contributing.md).

---

## Quick Start

```bash
# 1. Fork the repository, then clone your fork
git clone https://github.com/<your-username>/ecommerce-backend.git
cd ecommerce-backend

# 2. Add the upstream remote
git remote add upstream https://github.com/faken-dev/ecommerce-backend.git

# 3. Create a feature branch from develop
git checkout develop
git pull upstream develop
git checkout -b feature/your-feature-name

# 4. Make your changes, then push and open a Pull Request into develop
git push origin feature/your-feature-name
```

> **Always branch off `develop`, never `main`.**

---

## Commit Convention

This project follows [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short description>
```

| Type | When to use |
|---|---|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code change that neither fixes a bug nor adds a feature |
| `test` | Adding or updating tests |
| `docs` | Documentation only changes |
| `chore` | Build process, dependency updates, tooling |

**Examples:**

```
feat(auth): add JWT refresh token rotation
fix(otp): handle expired OTP gracefully
chore(deps): upgrade Spring Boot to 4.0.1
docs(readme): update getting started section
test(auth): add integration tests for login endpoint
refactor(user): extract address validation to value object
```

---

## Opening a Pull Request

1. Make sure your branch is up to date with `develop`
2. Ensure the build passes locally: `./mvnw verify`
3. Open a PR targeting `develop` — never `main`
4. Fill in the PR description clearly (what, why, how to test)
5. Wait for at least one review before merging

---

## Code of Conduct

Be respectful and constructive. We follow the [Contributor Covenant](https://www.contributor-covenant.org/) Code of Conduct. Harassment, discrimination, or toxic behavior of any kind will not be tolerated.

If you experience or witness unacceptable behavior, please report it by opening a private issue or contacting the maintainer directly.

---

## Questions?

Open an [Issue](https://github.com/faken-dev/ecommerce-backend/issues) or start a [Discussion](https://github.com/faken-dev/ecommerce-backend/discussions).