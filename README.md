#  E-Commerce Backend

> **Enterprise-grade ecommerce backend** — Shopee-inspired, built with Java 21 + Spring Boot 4 + PostgreSQL
> Domain-Driven Design · JWT Auth · RBAC · OTP via Email by Sendgrid & Twilio WhatsApp/SMS

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Migration-CC0200?style=flat-square&logo=flyway&logoColor=white)](https://flywaydb.org/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

---

##  Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Bounded Contexts](#-bounded-contexts)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Environment Variables](#-environment-variables)
- [API Documentation](#-api-documentation)
- [Database Migrations](#-database-migrations)
- [Branch Strategy](#-branch-strategy)
- [Roadmap](#-roadmap)
- [Contributing](#-contributing)

---

##  Overview

**E-Commerce Backend** is a high-performance ecommerce backend system inspired by Shopee, designed with **Domain-Driven Design (DDD)** and **Layered Architecture**. The system supports a full feature set — from user authentication, product management, to order processing, payments, and live commerce.

**Key goals:**
- Full-featured, production-ready ecommerce platform
- Modular architecture per Bounded Context, easy to scale
- Multi-layer security: JWT + RBAC + OTP (Email & Twilio WhatsApp/SMS)
- Great developer experience with OpenAPI docs, Actuator, and DevTools

---

## Architecture

The project applies **Domain-Driven Design (DDD) Layered Architecture**:

```
┌─────────────────────────────────────────────────────────┐
│                   Presentation Layer                    │
│         REST Controllers · OpenAPI · Thymeleaf          │
├─────────────────────────────────────────────────────────┤
│                   Application Layer                     │
│      Use Cases · DTOs · Mappers (MapStruct) · Commands  │
├─────────────────────────────────────────────────────────┤
│                     Domain Layer                        │
│    Entities · Value Objects · Domain Services · Events  │
├─────────────────────────────────────────────────────────┤
│                  Infrastructure Layer                   │
│  JPA Repositories · Flyway · Twilio · JWT · Security    │
└─────────────────────────────────────────────────────────┘
```

See [`docs/architecture.md`](docs/architecture.md) for full details.

---

##  Tech Stack

| Category | Technology | Version |
|---|---|---|
| Language | Java (OpenJDK) | 21 LTS |
| Framework | Spring Boot | 4.0.x |
| Security | Spring Security + JWT | Latest |
| Database | PostgreSQL | 16 |
| Migration | Flyway | Latest |
| ORM | Spring Data JPA / Hibernate | Latest |
| Mapping | MapStruct | Latest |
| Boilerplate | Lombok | Latest |
| Rate Limiting | Bucket4j | Latest |
| API Docs | Springdoc OpenAPI (Swagger UI) | Latest |
| Email | SendGrid | Latest |
| OTP / SMS | Twilio (WhatsApp Sandbox + SMS) | Latest |
| Templates | Thymeleaf | Latest |
| Monitoring | Spring Actuator | Latest |
| Dev Tools | Spring DevTools | Latest |
| Containerization | Docker + Docker Compose | Latest |

---

##  Bounded Contexts

The system is divided into independent **Bounded Contexts** following DDD:

| Context | Status | Description |
|---|---|---|
| `auth` |  **In Progress** | Authentication & Authorization — JWT, RBAC, OTP (Email + Twilio WhatsApp/SMS) |
| `notification` |  **In Progress** | Email / SMS / WhatsApp delivery — triggered by domain events |
| `user` |  Planned | User accounts, profiles, address book |
| `product` |  Planned | Categories, products, variants, inventory |
| `order` |  Planned | Cart, order placement, order lifecycle |
| `payment` |  Planned | Payment gateway integration, transaction history |
| `shipping` |  Planned | Shipping providers, order tracking |
| `live` |  Planned | Livestream commerce, flash sales |

---

##  Project Structure

```
ecommerce-backend/
├── docs/
│   ├── README/
│   │   └── README.vi.md             # Vietnamese README
│   ├── architecture.md              # Detailed system architecture
│   ├── api-spec.md                  # API specification overview
│   ├── contributing.md              # Contribution guidelines
│   └── adr/                         # Architecture Decision Records
│
├── src/main/java/com/ecommerce/
│   │
│   ├── auth/                        # Bounded Context: Auth
│   │   ├── domain/
│   │   │   ├── entity/              # User, Role, Permission, OtpToken
│   │   │   ├── event/               # OtpRequestedEvent, UserRegisteredEvent
│   │   │   └── service/             # Domain services
│   │   ├── application/
│   │   │   ├── command/             # RegisterCommand, LoginCommand...
│   │   │   ├── dto/                 # Request/Response DTOs
│   │   │   ├── mapper/              # MapStruct mappers
│   │   │   └── usecase/             # Use case handlers
│   │   ├── infrastructure/
│   │   │   ├── persistence/         # JPA repositories
│   │   │   └── security/            # JWT filter, RBAC config
│   │   └── presentation/
│   │       └── controller/          # REST controllers
│   │
│   ├── notification/                # Bounded Context: Notification
│   │   ├── application/
│   │   │   ├── handler/             # OtpNotificationHandler, InvoiceHandler...
│   │   │   └── port/                # NotificationSender interface
│   │   ├── domain/
│   │   │   └── NotificationTemplate.java
│   │   └── infrastructure/
│   │       ├── email/               # SendGridEmailSender (Thymeleaf templates)
│   │       └── sms/                 # TwilioSmsSender (WhatsApp + SMS)
│   │
│   ├── user/                        # Bounded Context: User (planned)
│   ├── product/                     # Bounded Context: Product (planned)
│   ├── order/                       # Bounded Context: Order (planned)
│   ├── payment/                     # Bounded Context: Payment (planned)
│   ├── shipping/                    # Bounded Context: Shipping (planned)
│   ├── live/                        # Bounded Context: Live (planned)
│   │
│   └── shared/                      # Shared Kernel
│       ├── domain/                  # Base classes, shared Value Objects
│       ├── event/                   # EventPublisher interface
│       ├── exception/               # Global exception handling
│       ├── config/                  # Spring configs (Security, OpenAPI...)
│       └── util/                    # Utility classes
│
├── src/main/resources/
│   ├── db/migration/                # Flyway SQL migrations (V1__, V2__...)
│   ├── templates/                   # Thymeleaf email templates
│   ├── application.yml
│   ├── application-dev.yml
│   └── application-prod.yml
│
├── src/test/
│   ├── unit/                        # Unit tests (JUnit 5 + Mockito)
│   └── integration/                 # Integration tests (Testcontainers)
│
├── docker-compose.yml               # PostgreSQL + services
├── docker-compose.override.yml      # Dev overrides
├── .env.example                     # Environment variable template
├── .gitignore
├── CONTRIBUTING.md
├── LICENSE
├── pom.xml
└── README.md
```

---

##  Getting Started

### Prerequisites

- Java 21+ ([SDKMAN](https://sdkman.io/) recommended: `sdk install java 21-tem`)
- Docker & Docker Compose
- Maven 3.9+

### 1. Clone the repository

```bash
git clone https://github.com/faken-dev/ecommerce-backend.git
cd ecommerce-backend
```

### 2. Setup environment

```bash
cp .env.example .env
# Edit .env with your values (DB credentials, JWT secret, Twilio...)
```

### 3. Start infrastructure

```bash
docker-compose up -d
```

### 4. Run the application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

App runs at: `http://localhost:8080`

### 5. Open Swagger UI

```
http://localhost:8080/swagger-ui.html
```

---

##  Environment Variables

Create `.env` from `.env.example`:

```dotenv
# Database
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=ecommerce_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password

# JWT
JWT_SECRET=your_very_long_and_secure_jwt_secret_key_here
JWT_EXPIRATION_MS=86400000
JWT_REFRESH_EXPIRATION_MS=604800000

# OTP
OTP_EXPIRY_MINUTES=5
OTP_LENGTH=6

# Email (SendGrid)
SENDGRID_API_KEY=SG.your_api_key_here
SENDGRID_FROM_EMAIL=no-reply@yourdomain.com
SENDGRID_FROM_NAME=YourAppName

# Twilio
TWILIO_ACCOUNT_SID=your_twilio_account_sid
TWILIO_AUTH_TOKEN=your_twilio_auth_token
TWILIO_FROM_PHONE=+1234567890
TWILIO_WHATSAPP_FROM=whatsapp:+14155238886
```

>  **Never commit `.env`** — it is already listed in `.gitignore`.

---

##  API Documentation

| URL | Description |
|---|---|
| `GET /swagger-ui.html` | Interactive Swagger UI |
| `GET /v3/api-docs` | OpenAPI JSON spec |
| `GET /actuator/health` | Health check |
| `GET /actuator/info` | App info |

### Auth Endpoints (Phase 1)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Register a new account |
| `POST` | `/api/v1/auth/login` | Login, receive JWT |
| `POST` | `/api/v1/auth/refresh` | Refresh access token |
| `POST` | `/api/v1/auth/logout` | Logout, revoke token |
| `POST` | `/api/v1/auth/otp/send` | Send OTP (Email / WhatsApp / SMS) |
| `POST` | `/api/v1/auth/otp/verify` | Verify OTP |
| `POST` | `/api/v1/auth/password/forgot` | Forgot password |
| `POST` | `/api/v1/auth/password/reset` | Reset password |

---

##  Database Migrations

Managed by **Flyway**. Migration files live at:

```
src/main/resources/db/migration/
├── V1__create_roles_and_permissions.sql
├── V2__create_users.sql
├── V3__create_otp_tokens.sql
└── ...
```

**Naming convention**: `V{version}__{description}.sql`

Flyway runs migrations automatically on startup.

---

##  Branch Strategy

The project follows **Git Flow**:

```
main          ← Production-ready releases (tagged)
  └── develop ← Integration branch (default working branch)
        ├── feature/auth-jwt         ← New features
        ├── feature/otp-twilio
        ├── fix/token-expiry-bug     ← Bug fixes
        └── chore/update-deps        ← Maintenance
```

### Workflow

```bash
# Create a feature branch from develop
git checkout develop
git pull origin develop
git checkout -b feature/your-feature-name

# Work and commit often
git add .
git commit -m "feat(auth): add OTP verification via Twilio WhatsApp"

# Push and open a Pull Request into develop
git push origin feature/your-feature-name
```

### Commit Convention ([Conventional Commits](https://www.conventionalcommits.org/))

```
feat(auth): add JWT refresh token rotation
fix(otp): handle expired OTP gracefully
chore(deps): upgrade Spring Boot to 4.0.1
docs(readme): update getting started section
test(auth): add integration tests for login endpoint
refactor(user): extract address validation to value object
```

---

##  Roadmap

###  Phase 0 — Project Setup
- [x] Initialize Spring Boot 4 + Java 21 project
- [ ] Configure PostgreSQL + Flyway
- [ ] Docker Compose infrastructure
- [ ] DDD Layered Architecture structure
- [ ] Architecture documentation (`docs/architecture.md`)

###  Phase 1 — Authentication & Authorization *(In Progress)*
- [ ] User registration & login (Email/Password)
- [ ] JWT Access Token + Refresh Token Rotation
- [ ] RBAC: `ADMIN`, `SELLER`, `BUYER`
- [ ] OTP via Email (Spring Mail + Thymeleaf template)
- [ ] OTP via Twilio SMS & WhatsApp Sandbox
- [ ] Forgot/Reset Password flow
- [ ] Rate limiting with Bucket4j (brute-force protection)
- [ ] Flyway migrations: users, roles, permissions, otp_tokens
- [ ] Notification context: event-driven email/SMS delivery

###  Phase 2 — User Management
- [ ] User profile CRUD
- [ ] Multiple address book
- [ ] Avatar upload
- [ ] Account settings
- [ ]  ...

###  Phase 3 — Product Catalog
- [ ] Category tree
- [ ] Products + variants (size, color...)
- [ ] Inventory management
- [ ] Product search & filtering
- [ ] ...

###  Phase 4 — Order & Cart
- [ ] Shopping cart
- [ ] Order placement & lifecycle
- [ ] Order history
- [ ] ...

###  Phase 5 — Payment
- [ ] Payment gateway integration
- [ ] Transaction history
- [ ] Refund handling
- [ ] ...

###  Phase 6 — Shipping
- [ ] Shipping provider integration
- [ ] Order tracking
- [ ] ...

###  Phase 7 — Live Commerce
- [ ] Livestream session management
- [ ] Flash sale engine
- [ ] ...

---

##  Contributing

1. Fork the repository
2. Create a branch from `develop`: `git checkout -b feature/amazing-feature`
3. Commit following Conventional Commits
4. Push and open a Pull Request into `develop`
5. Wait for review and merge

Please read [`CONTRIBUTING.md`](CONTRIBUTING.md) before contributing.

---

##  License

Distributed under the **MIT License** — see [`LICENSE`](LICENSE) for details.

---

<div align="center">
  <sub>Built with ❤️ using Java 21 + Spring Boot 4 · Domain-Driven Design by faken</sub>
</div>