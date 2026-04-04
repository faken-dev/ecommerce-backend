# ADR-004: PostgreSQL as Primary Database

| | |
|---|---|
| **Status** | Accepted |
| **Date** | 2026 |
| **Context** | E-Commerce Backend — Infrastructure |

---

## Context

The system needs a primary relational database to persist all core domain data: users, roles, OTP tokens, products, orders, payments, etc.

## Decision

Use **PostgreSQL 16** as the sole primary database for all Bounded Contexts in the modular monolith phase.

## Rationale

### Why relational

E-commerce data is inherently relational: users place orders, orders contain products, products belong to categories, payments reference orders. Strong ACID guarantees are critical — partial order creation or double-charged payments are unacceptable failure modes. A relational database with transactions is the natural fit.

### Why PostgreSQL over MySQL / MariaDB

| Concern | PostgreSQL | MySQL |
|---|---|---|
| JSON support | `jsonb` — indexed, queryable | `json` — limited query capability |
| Full-text search | Built-in `tsvector`/`tsquery` | Limited, usually requires plugin |
| Advanced types | Arrays, enums, ranges, UUID native | Fewer native types |
| Window functions | Full support | Full support (MySQL 8+) |
| Concurrency | MVCC — no read locks | MVCC in InnoDB |
| Ecosystem | Flyway, Hibernate, pgAdmin — first-class | Also well supported |
| License | BSD (fully open source) | GPL (Oracle-owned) |

PostgreSQL's `jsonb` support is particularly valuable for the `product` context (variant attributes, specs) and the `live` context (session metadata) without needing a separate document store.

### One database, schema-per-context

During the modular monolith phase, all contexts share one PostgreSQL instance but are logically separated via **table name prefixes**:

```
auth_users
auth_roles
auth_otp_tokens
user_profiles
user_addresses
product_categories
product_items
order_carts
order_items
payment_transactions
```

No foreign keys cross context boundaries in the database — cross-context references are maintained by the application. This mirrors the eventual microservices separation at the data layer.

### Migration path

When a context is extracted to a microservice, it gets its own PostgreSQL instance. The migration is:
1. Export the relevant tables to a new DB instance
2. Update the context's datasource config
3. Remove the table prefix convention (optional)

No application code changes are required — only infrastructure config.

## Consequences

**Positive:**
- ACID transactions protect order, payment, and inventory consistency
- `jsonb` handles semi-structured data (product variants, settings) without a separate DB
- Mature tooling: pgAdmin, Flyway, Hibernate dialect, Testcontainers PostgreSQL image
- Schema-per-context prefix prepares for microservices extraction

**Negative:**
- Single DB is a potential bottleneck at very high scale (acceptable for current stage)
- Read-heavy workloads (product catalog, search) may require read replicas later
- Full-text search capability is limited compared to Elasticsearch at scale

## Implementation Notes

- Version: PostgreSQL 16 (via Docker in development, managed service in production)
- Connection pooling: **HikariCP** (Spring Boot default)
- UUID as primary key type across all tables (`gen_random_uuid()`)
- All timestamps in **UTC**, stored as `TIMESTAMPTZ`
- Flyway manages all schema changes — no manual DDL ever
- Testcontainers uses `postgres:16-alpine` image for integration tests