# ADR-001: Stateless Authentication with JWT

| | |
|---|---|
| **Status** | Accepted |
| **Date** | 2026 |
| **Context** | E-Commerce Backend — Auth Bounded Context |

---

## Context

The system needs an authentication mechanism for a REST API consumed by web and mobile clients. The solution must support stateless horizontal scaling, RBAC, and token revocation on logout.

## Decision

Use **JWT (JSON Web Tokens)** with a dual-token strategy:

- **Access Token** — short-lived (15 minutes), stateless, carries identity + roles + permissions
- **Refresh Token** — long-lived (7 days), stored hashed in the database, supports rotation

## Rationale

### Why JWT over session-based auth

| Concern | Session | JWT |
|---|---|---|
| Server memory | Requires session store (Redis/DB) | Stateless — no server state |
| Horizontal scaling | Sticky sessions or shared store required | Works out of the box |
| Mobile clients | Cookie handling varies | Authorization header is universal |
| Microservices readiness | Session store must be shared across services | Each service validates token independently |

Session-based auth would require a shared Redis store from day one. JWT keeps the infrastructure simple for a solo project while remaining compatible with the planned modular monolith → microservices migration.

### Why short-lived access tokens + refresh rotation

A purely stateless JWT cannot be revoked before expiry. The dual-token approach balances this:

- **Short access token TTL (15 min):** Limits the damage window if an access token is leaked. No revocation needed in the common case.
- **Refresh token in DB:** Allows true logout (revoke the refresh token). Rotation on every use detects token theft — if a stolen refresh token is used, the legitimate session is also invalidated, and the server can flag the anomaly.

### Why HMAC-SHA256 over RSA

- Solo project with a single trusted service — asymmetric signing (RSA/EC) is overkill
- RSA adds key management complexity with no benefit when there is only one token issuer and verifier
- Can be migrated to RSA when extracting Auth to a standalone service

## Consequences

**Positive:**
- No shared session store required — simpler infrastructure
- Scales horizontally without config changes
- Tokens are self-describing — downstream services can validate without DB lookup
- Migration path to microservices is clean

**Negative:**
- Access tokens cannot be instantly revoked — 15-minute window is accepted risk
- Refresh token rotation requires a DB write on every token refresh
- JWT payload is base64-encoded (not encrypted) — do not store sensitive data in claims

## Implementation Notes

- JWT secret loaded from `JWT_SECRET` env var — never hardcoded
- Access token claims: `sub` (userId), `roles`, `permissions`, `iat`, `exp`
- Refresh tokens stored as BCrypt hash in `auth_refresh_tokens` table
- `JwtAuthenticationFilter` extends `OncePerRequestFilter`
- Token validation uses constant-time comparison to prevent timing attacks