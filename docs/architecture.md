# System Architecture

> **E-Commerce Backend** — Shopee-inspired, Java 21 + Spring Boot 4 + PostgreSQL  
> Solo project · Modular Monolith → Microservices migration path

---

## Table of Contents

- [System Architecture](#system-architecture)
  - [Table of Contents](#table-of-contents)
  - [Overview](#overview)
  - [Architecture Style](#architecture-style)
  - [DDD Layered Architecture](#ddd-layered-architecture)
    - [Presentation Layer](#presentation-layer)
    - [Application Layer](#application-layer)
    - [Domain Layer](#domain-layer)
    - [Infrastructure Layer](#infrastructure-layer)
  - [Bounded Contexts](#bounded-contexts)
    - [Context Map](#context-map)
  - [Request Lifecycle](#request-lifecycle)
    - [Authenticated Request (e.g., `GET /api/v1/products`)](#authenticated-request-eg-get-apiv1products)
    - [OTP Flow](#otp-flow)
  - [Event-Driven Communication](#event-driven-communication)
    - [Event Flow](#event-flow)
    - [Event Contract Rules](#event-contract-rules)
  - [Security Architecture](#security-architecture)
    - [Layers of Defense](#layers-of-defense)
    - [JWT Strategy](#jwt-strategy)
    - [RBAC Model](#rbac-model)
    - [OTP Security](#otp-security)
  - [Database Design Principles](#database-design-principles)
    - [Migration Naming](#migration-naming)
  - [Migration Path to Microservices](#migration-path-to-microservices)
    - [Extraction Checklist (per context)](#extraction-checklist-per-context)
    - [Migration Steps](#migration-steps)
  - [Key Architecture Decisions](#key-architecture-decisions)

---

## Overview

This system is a **modular monolith** — all Bounded Contexts run in a single deployable JAR today, but are structured so that each context can be extracted into an independent microservice in the future with minimal rework.

The two guiding principles:

1. **No shared mutable state between contexts** — contexts communicate only via domain events, never by calling each other's repositories or services directly.
2. **Layers are enforced by package structure** — the domain layer has zero Spring or JPA dependencies; infrastructure details never leak upward.

---

## Architecture Style

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client (HTTP)                            │
└─────────────────────────────┬───────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                     Presentation Layer                          │
│          REST Controllers · OpenAPI · Thymeleaf (email)         │
├─────────────────────────────────────────────────────────────────┤
│                     Application Layer                           │
│        Use Cases · DTOs · MapStruct Mappers · Commands          │
├─────────────────────────────────────────────────────────────────┤
│                       Domain Layer                              │
│     Entities · Value Objects · Domain Services · Events         │
│                  ← No Spring. No JPA. Pure Java. →              │
├─────────────────────────────────────────────────────────────────┤
│                   Infrastructure Layer                          │
│    JPA Repositories · Flyway · SendGrid · Twilio · JWT          │
└─────────────────────────────────────────────────────────────────┘
```

Each **Bounded Context** (`auth`, `notification`, `user`, `product`, ...) has its own internal instance of this four-layer stack. They do not share layers horizontally.

See [`docs/adr/ADR-005-ddd-layered-over-hexagonal.md`](adr/ADR-005-ddd-layered-over-hexagonal.md) for why DDD Layered was chosen over Hexagonal/Ports & Adapters.

---

## DDD Layered Architecture

### Presentation Layer

- **Responsibility:** Accept HTTP requests, validate input shape (JSR-303), delegate to Application layer, return HTTP responses.
- **Must NOT contain:** Business logic, direct repository calls, domain object manipulation.
- **Key classes:** `@RestController`, `@RestControllerAdvice`, OpenAPI annotations.

### Application Layer

- **Responsibility:** Orchestrate use cases. Fetch domain objects, invoke domain services, publish domain events, persist results.
- **Must NOT contain:** Business rules (those live in Domain), Spring Security config, JPA annotations.
- **Key classes:** `*UseCase`, `*Command`, `*Dto`, `*Mapper` (MapStruct).

### Domain Layer

- **Responsibility:** Express the core business model. This layer is the heart of the system.
- **Must NOT contain:** Any framework dependency — no `@Component`, no `@Entity`, no `@Transactional`.
- **Key classes:** Entities, Value Objects, Domain Services, Domain Events, Repository interfaces (defined here, implemented in Infrastructure).

### Infrastructure Layer

- **Responsibility:** Implement the interfaces defined in the Domain layer using concrete technologies.
- **Key classes:** JPA `@Entity` classes (separate from domain entities), `JpaRepository` implementations, `SendGridEmailSender`, `TwilioSmsSender`, `JwtTokenProvider`.

---

## Bounded Contexts

| Context | Status | Responsibility |
|---|---|---|
| `auth` | In Progress | Authentication, authorization, JWT, RBAC, OTP |
| `notification` | In Progress | Email (SendGrid), SMS/WhatsApp (Twilio) delivery |
| `user` | Planned | User profiles, address book, avatar |
| `product` | Planned | Categories, products, variants, inventory |
| `order` | Planned | Cart, order lifecycle |
| `payment` | Planned | Payment gateway, transaction history |
| `shipping` | Planned | Shipping providers, order tracking |
| `live` | Planned | Livestream sessions, flash sales |

### Context Map

```
auth ──publishes──► UserRegisteredEvent ──► notification (sends welcome email)
auth ──publishes──► OtpRequestedEvent  ──► notification (sends OTP email/SMS)
auth ──publishes──► UserRegisteredEvent ──► user        (creates user profile)

order ──publishes──► OrderPlacedEvent  ──► payment
order ──publishes──► OrderPlacedEvent  ──► notification (sends confirmation)
payment ──publishes──► PaymentConfirmedEvent ──► shipping
shipping ──publishes──► OrderShippedEvent   ──► notification (sends tracking info)
```

Contexts are **upstream/downstream** — upstream contexts define the event contract, downstream contexts adapt to it. No direct method calls across context boundaries.

---

## Request Lifecycle

### Authenticated Request (e.g., `GET /api/v1/products`)

```
HTTP Request
    │
    ▼
JwtAuthenticationFilter          ← Extracts + validates JWT from Authorization header
    │
    ▼
Spring Security FilterChain      ← Checks RBAC permissions via @PreAuthorize
    │
    ▼
Bucket4jRateLimitFilter          ← Rejects if rate limit exceeded (429)
    │
    ▼
@RestController                  ← Validates request DTO (JSR-303)
    │
    ▼
*UseCase                         ← Orchestrates domain logic
    │
    ▼
Domain Layer                     ← Business rules applied
    │
    ▼
JPA Repository                   ← Reads/writes PostgreSQL
    │
    ▼
ApiResponse<T>                   ← Wrapped, consistent response shape
    │
    ▼
HTTP Response
```

### OTP Flow

```
POST /api/v1/auth/otp/send
    │
    ▼
SendOtpUseCase
    ├── Generate OTP (6-digit, SecureRandom)
    ├── Hash + persist to otp_tokens (expires in 5 min)
    └── Publish OtpRequestedEvent
            │
            ▼
    OtpNotificationHandler (notification context)
            ├── channel = EMAIL  → SendGridEmailSender (Thymeleaf template)
            └── channel = SMS    → TwilioSmsSender
                       WHATSAPP  → TwilioWhatsAppSender

POST /api/v1/auth/otp/verify
    │
    ▼
VerifyOtpUseCase
    ├── Load token from otp_tokens
    ├── Check expiry + attempt count
    ├── Compare hash (constant-time)
    ├── Mark as used (prevent replay)
    └── Return result
```

---

## Event-Driven Communication

Domain events are the only communication channel between Bounded Contexts.

### Event Flow

```
Domain Layer          Application Layer         Infrastructure
─────────────         ─────────────────         ──────────────
DomainEvent  ──────►  EventPublisher(interface)  ◄── SpringEventPublisher(impl)
                               │
                               ▼
                      @EventListener in target context
```

### Event Contract Rules

- Events are **immutable** value objects (use Java records)
- Events carry only **primitive data** or IDs — never domain objects from another context
- Events are **named in past tense**: `UserRegisteredEvent`, `OtpRequestedEvent`, `OrderPlacedEvent`
- Event classes live in the **publishing context's domain layer**

```java
// Example — lives in auth/domain/event/
public record UserRegisteredEvent(
    UUID userId,
    String email,
    String fullName,
    Instant occurredAt
) {}
```

---

## Security Architecture

### Layers of Defense

```
Request
  │
  ├─ 1. Rate Limiting (Bucket4j)        ← Block brute force before JWT check
  ├─ 2. JWT Validation                  ← Stateless authentication
  ├─ 3. RBAC (@PreAuthorize)            ← Role + permission check
  └─ 4. Input Validation (JSR-303)      ← Reject malformed input early
```

### JWT Strategy

- **Access Token:** Short-lived (15 min default). Signed with HMAC-SHA256. Carries `userId`, `roles`, `permissions`.
- **Refresh Token:** Long-lived (7 days). Stored hashed in DB. Supports rotation — each use issues a new refresh token and invalidates the old one.
- **Logout:** Refresh token is revoked in DB. Access tokens remain valid until expiry (acceptable for short TTL).

See [`docs/adr/ADR-001-jwt-stateless-auth.md`](adr/ADR-001-jwt-stateless-auth.md).

### RBAC Model

```
User ──has──► Role(s) ──has──► Permission(s)

Roles:       ADMIN · SELLER · BUYER
Permissions: product:read, product:write, order:manage, user:manage, ...
```

Permissions are checked at the controller level via `@PreAuthorize("hasAuthority('product:write')")`.

### OTP Security

- Generated with `SecureRandom` (cryptographically secure)
- Stored **hashed** (BCrypt) in DB — plaintext never persisted
- 5-minute expiry, max 3 attempts per token
- Marked as `used` after successful verification (replay attack prevention)
- Separate rate limit on `/otp/send` endpoint

---

## Database Design Principles

- **One schema per Bounded Context** (enforced via table prefixes: `auth_*`, `user_*`, `product_*`, ...)
- **No foreign keys across contexts** — referential integrity is maintained at the application level for cross-context data
- **All tables have:** `id` (UUID), `created_at`, `updated_at`
- **Soft deletes** for user-facing entities: `deleted_at` timestamp (NULL = active)
- **Migrations only via Flyway** — no schema changes outside of `db/migration/`
- **No breaking migrations** — additive changes only; deprecate columns before removing

### Migration Naming

```
V{version}__{bounded_context}_{description}.sql

V1__auth_create_roles_and_permissions.sql
V2__auth_create_users.sql
V3__auth_create_otp_tokens.sql
V4__user_create_profiles.sql
```

---

## Migration Path to Microservices

The modular monolith is designed so individual contexts can be extracted with predictable effort.

### Extraction Checklist (per context)

When a context is ready to be extracted:

- [ ] No direct cross-context repository calls exist (enforced from day one)
- [ ] All cross-context communication uses domain events
- [ ] Events are published to a message broker (Kafka/RabbitMQ) instead of Spring's in-process `ApplicationEventPublisher`
- [ ] The context has its own Flyway migration set (already the case via naming convention)
- [ ] The context has its own `application.yml` datasource config

### Migration Steps

```
Phase A — Today (Modular Monolith)
  └── Single JAR, single DB, in-process events via Spring ApplicationEventPublisher

Phase B — Broker Introduction
  └── Add Kafka/RabbitMQ; switch EventPublisher impl to publish to broker
  └── Consumers in other contexts read from broker (no code change in domain)

Phase C — Context Extraction
  └── Move context package to its own Spring Boot module
  └── Point at its own DB schema
  └── Deploy independently
```

The domain layer and application layer require **zero changes** during this migration — only the infrastructure `EventPublisher` implementation changes.

---

## Key Architecture Decisions

| Decision | Choice | ADR |
|---|---|---|
| Authentication mechanism | JWT (stateless) | [ADR-001](adr/ADR-001-jwt-stateless-auth.md) |
| Email delivery | SendGrid API | [ADR-002](adr/ADR-002-sendgrid-over-smtp.md) |
| Rate limiting | Bucket4j | [ADR-003](adr/ADR-003-bucket4j-rate-limiting.md) |
| Primary database | PostgreSQL 16 | [ADR-004](adr/ADR-004-postgresql.md) |
| Architecture pattern | DDD Layered | [ADR-005](adr/ADR-005-ddd-layered-over-hexagonal.md) |