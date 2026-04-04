# ADR-002: SendGrid API over Direct SMTP

| | |
|---|---|
| **Status** | Accepted |
| **Date** | 2026 |
| **Context** | E-Commerce Backend — Notification Bounded Context |

---

## Context

The system needs to send transactional emails: OTP codes, welcome emails, password reset links, and order confirmations. A delivery mechanism must be chosen.

## Decision

Use **SendGrid Web API** (`sendgrid-java` SDK) instead of JavaMail / Spring Mail over raw SMTP.

## Rationale

### Deliverability

Direct SMTP from an application server is routinely flagged as spam by major email providers (Gmail, Outlook). This is especially problematic for OTP emails — a delayed or filtered OTP creates immediate user friction. SendGrid manages IP reputation, SPF/DKIM/DMARC signing, and ISP relationships on behalf of the sender.

### Reliability vs complexity trade-off

| Concern | Raw SMTP | SendGrid API |
|---|---|---|
| Deliverability | Poor (no reputation management) | High (managed IPs, reputation) |
| Configuration | SMTP host, port, TLS, app password | Single API key |
| Bounce handling | Manual | Dashboard + webhooks |
| Open/click tracking | Not available | Built-in |
| Free tier | Depends on provider | 100 emails/day free |
| Vendor lock-in | Low | Medium |

For a solo project, the operational overhead of managing SMTP reputation is not justified. SendGrid's free tier covers development and early production volumes comfortably.

### Abstraction via port interface

The `NotificationSender` interface in the application layer means switching providers (SendGrid → AWS SES, Postmark, Resend, etc.) requires only a new infrastructure implementation — no changes to use cases or domain logic.

```java
// application/port/NotificationSender.java
public interface NotificationSender {
    void send(NotificationMessage message);
}

// infrastructure/email/SendGridEmailSender.java
public class SendGridEmailSender implements NotificationSender { ... }
```

## Consequences

**Positive:**
- High deliverability from day one — OTP emails reach the inbox reliably
- Zero SMTP infrastructure to manage
- Single env var (`SENDGRID_API_KEY`) replaces four SMTP settings
- Switching providers later requires only a new `NotificationSender` implementation

**Negative:**
- External service dependency — SendGrid outage affects email delivery
- Free tier limited to 100 emails/day (sufficient for development; paid plan needed at scale)
- Vendor lock-in at the infrastructure level (mitigated by the port interface)

## Implementation Notes

- API key loaded from `SENDGRID_API_KEY` env var
- Sender identity configured via `SENDGRID_FROM_EMAIL` and `SENDGRID_FROM_NAME`
- Email bodies rendered by **Thymeleaf** templates before passing to SendGrid SDK — keeps HTML rendering in the application, independent of the provider
- `SendGridEmailSender` implements `NotificationSender` in the infrastructure layer
- Retry logic: Spring `@Retryable` with exponential backoff on `SendGrid` API errors