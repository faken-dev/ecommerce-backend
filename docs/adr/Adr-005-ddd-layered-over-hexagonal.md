# ADR-005: DDD Layered Architecture over Hexagonal / Ports & Adapters

| | |
|---|---|
| **Status** | Accepted |
| **Date** | 2026 |
| **Context** | E-Commerce Backend — Overall Architecture Pattern |

---

## Context

A structural architecture pattern must be chosen for each Bounded Context. The two most common candidates for DDD-based Spring Boot applications are **DDD Layered Architecture** and **Hexagonal Architecture (Ports & Adapters)**. Both protect the domain from infrastructure concerns; they differ in how explicitly they model the boundary.

## Decision

Use **DDD Layered Architecture** (Presentation → Application → Domain → Infrastructure) rather than Hexagonal Architecture.

## Rationale

### Comparison

| Concern | DDD Layered | Hexagonal |
|---|---|---|
| Learning curve | Familiar to most Java/Spring devs | Steeper — ports/adapters vocabulary |
| Boilerplate | Low | Higher — explicit port interfaces for everything |
| Domain isolation | Good — domain has no upward dependency | Excellent — domain has zero knowledge of adapters |
| Testability | Good — domain is a plain Java library | Excellent — adapters are fully swappable |
| Complexity for solo project | Proportionate | Can feel over-engineered |
| Migration to microservices | Straightforward | Also straightforward |

### Why Layered is sufficient here

Hexagonal Architecture's primary benefit is making the domain **completely unaware of all external adapters** — including the REST layer. In practice, for a Spring Boot monolith, both patterns achieve the same goal: the domain layer is pure Java with no framework dependency.

The additional Hexagonal ceremony (defining an explicit port interface for every inbound use case, every outbound repository, every external service) adds boilerplate that slows down a solo developer without providing meaningful extra protection for this project's scale.

DDD Layered achieves the critical guarantee: **the domain layer has no dependency on Spring, JPA, or any infrastructure library**. This is verified by package structure and build-time constraints.

### What is preserved from Hexagonal thinking

Even though the full Hexagonal pattern is not adopted, these Hexagonal principles are applied:

- Repository interfaces are **defined in the domain layer**, implemented in infrastructure — the domain never depends on JPA
- External services (`NotificationSender`, `EventPublisher`) are **interfaces in the application layer** — the domain/application never depends on SendGrid or Twilio directly
- Use cases are the single entry point to the domain — controllers never touch repositories or domain services directly

This captures ~80% of Hexagonal's benefit with significantly less boilerplate.

## Consequences

**Positive:**
- Familiar structure — easy to onboard contributors without explaining ports/adapters
- Less boilerplate — faster to build features in the early stages
- Domain isolation is still enforced at the package level
- Easy to evolve toward Hexagonal for a specific context if complexity warrants it

**Negative:**
- Slightly less strict boundary than full Hexagonal — depends on discipline to not shortcut layers
- Application layer use cases are not modeled as explicit inbound ports (a Hexagonal concept) — some testability benefit is sacrificed

## Implementation Notes

### Enforced layer rules (by package structure)

```
domain/      → zero imports from org.springframework.*, javax.persistence.*
application/ → may import domain; no imports from infrastructure.*
infrastructure/ → may import domain and application
presentation/ → may import application (DTOs); no imports from domain entities directly
```

These rules can be enforced with **ArchUnit** tests:

```java
@Test
void domainLayerShouldHaveNoDependencyOnSpring() {
    noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAPackage("org.springframework..")
        .check(importedClasses);
}
```

Adding ArchUnit tests is recommended as a future improvement to make layer enforcement automatic.