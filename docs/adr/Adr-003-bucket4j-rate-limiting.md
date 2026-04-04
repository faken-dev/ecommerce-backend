# ADR-003: Rate Limiting with Bucket4j

| | |
|---|---|
| **Status** | Accepted |
| **Date** | 2026 |
| **Context** | E-Commerce Backend — Shared Infrastructure |

---

## Context

Several endpoints are vulnerable to abuse without rate limiting:

- `/auth/login` — credential stuffing, brute force
- `/auth/otp/send` — OTP flooding (cost via Twilio/SendGrid, user harassment)
- `/auth/otp/verify` — OTP brute force (6-digit space = 1,000,000 combinations)
- `/auth/password/forgot` — account enumeration, spam

A rate limiting solution must be chosen.

## Decision

Use **Bucket4j** with an in-memory (local) token bucket per client IP, applied via a `HandlerInterceptor`.

## Rationale

### Why token bucket algorithm

Token bucket is the standard algorithm for API rate limiting because it:
- Allows short bursts (up to bucket capacity) while enforcing a sustained rate
- Is intuitive to configure: "5 requests per minute with a burst of 10"
- Handles traffic spikes gracefully compared to fixed-window counters

### Why Bucket4j over alternatives

| Option | Rationale |
|---|---|
| **Bucket4j (chosen)** | Pure Java, no external dependency, integrates with Spring via interceptor, supports Caffeine/Redis backends for future scaling |
| Spring Cloud Gateway | Overkill — adds a full API gateway layer for a monolith |
| Custom filter with `ConcurrentHashMap` | Reinventing the wheel; no burst support, no TTL management |
| Redis + custom Lua script | Correct at scale, but requires Redis from day one; premature for solo project |

### In-memory vs distributed

For a single-instance modular monolith (current state), in-memory buckets via **Caffeine** cache are sufficient. When the service scales horizontally or Auth is extracted to a microservice, Bucket4j supports a **Redis backend** with zero algorithm change — only the `BucketProxyManager` implementation is swapped.

```
Today:   Bucket4j + Caffeine (in-memory, single node)
Future:  Bucket4j + Redis    (distributed, multi-node)
                    ↑
         Only this line changes
```

## Consequences

**Positive:**
- Protects OTP and auth endpoints from brute force with minimal setup
- No external infrastructure required in development
- Seamless migration to Redis backend when horizontal scaling is needed
- Per-endpoint, per-IP configuration is straightforward

**Negative:**
- In-memory buckets are not shared across instances — multiple instances = each has its own limit (acceptable until horizontal scaling is introduced)
- IP-based limiting can be bypassed via IP rotation (accepted trade-off for v1)
- Bucket state is lost on restart (acceptable for rate limiting; buckets refill naturally)

## Implementation Notes

- Applied as a `HandlerInterceptor` before the controller layer
- Rate limit key: `{endpoint}:{clientIp}` — allows different limits per endpoint
- Configured limits (starting point, tunable via env):

| Endpoint | Limit |
|---|---|
| `POST /auth/login` | 5 requests / 1 minute |
| `POST /auth/otp/send` | 3 requests / 5 minutes |
| `POST /auth/otp/verify` | 3 requests / 5 minutes |
| `POST /auth/password/forgot` | 3 requests / 10 minutes |
| General API | 60 requests / 1 minute |

- Returns `429 Too Many Requests` with `Retry-After` header on limit exceeded
- Bucket capacity (burst) = 2× the sustained rate limit