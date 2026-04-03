#  E-Commerce Backend

> **Hệ thống ecommerce backend cấp doanh nghiệp** — lấy cảm hứng từ Shopee, xây dựng với Java 21 + Spring Boot 4 + PostgreSQL
> Domain-Driven Design · JWT Auth · RBAC · OTP qua Email & Twilio WhatsApp/SMS

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Migration-CC0200?style=flat-square&logo=flyway&logoColor=white)](https://flywaydb.org/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

> 🇬🇧 English version: [`README.md`](../../README.md)

---

##  Mục Lục

- [Tổng Quan](#-tổng-quan)
- [Kiến Trúc](#-kiến-trúc)
- [Công Nghệ](#-công-nghệ)
- [Bounded Contexts](#-bounded-contexts)
- [Cấu Trúc Dự Án](#-cấu-trúc-dự-án)
- [Bắt Đầu](#-bắt-đầu)
- [Biến Môi Trường](#-biến-môi-trường)
- [Tài Liệu API](#-tài-liệu-api)
- [Database Migration](#-database-migration)
- [Chiến Lược Branch](#-chiến-lược-branch)
- [Lộ Trình Phát Triển](#-lộ-trình-phát-triển)
- [Đóng Góp](#-đóng-góp)

---

##  Tổng Quan

**E-Commerce Backend** là hệ thống ecommerce backend hiệu năng cao, lấy cảm hứng từ Shopee, được thiết kế theo kiến trúc **Domain-Driven Design (DDD)** với **Layered Architecture**. Hệ thống hỗ trợ đầy đủ tính năng từ xác thực người dùng, quản lý sản phẩm, đến xử lý đơn hàng, thanh toán và livestream bán hàng.

**Mục tiêu chính:**
- Nền tảng ecommerce đầy đủ tính năng, production-ready
- Kiến trúc module hóa theo từng Bounded Context, dễ mở rộng
- Bảo mật đa lớp: JWT + RBAC + OTP (Email & Twilio WhatsApp/SMS)
- Developer experience tốt với OpenAPI docs, Actuator, và DevTools

---

##  Kiến Trúc

Dự án áp dụng **Domain-Driven Design (DDD) Layered Architecture**:

```
┌─────────────────────────────────────────────────────────┐
│                  Presentation Layer                     │
│         REST Controllers · OpenAPI · Thymeleaf          │
├─────────────────────────────────────────────────────────┤
│                  Application Layer                      │
│      Use Cases · DTOs · Mappers (MapStruct) · Commands  │
├─────────────────────────────────────────────────────────┤
│                    Domain Layer                         │
│    Entities · Value Objects · Domain Services · Events  │
├─────────────────────────────────────────────────────────┤
│                 Infrastructure Layer                    │
│  JPA Repositories · Flyway · Twilio · JWT · Security    │
└─────────────────────────────────────────────────────────┘
```

Xem chi tiết tại [`docs/architecture.md`](../architecture.md).

---

##  Công Nghệ
| Hạng Mục | Công Nghệ | Phiên Bản |
|---|---|---|
| Ngôn ngữ | Java (OpenJDK) | 21 LTS |
| Framework | Spring Boot | 4.0.x |
| Bảo mật | Spring Security + JWT | Latest |
| Cơ sở dữ liệu | PostgreSQL | 16 |
| Migration | Flyway | Latest |
| ORM | Spring Data JPA / Hibernate | Latest |
| Mapping | MapStruct | Latest |
| Boilerplate | Lombok | Latest |
| Rate Limiting | Bucket4j | Latest |
| Tài liệu API | Springdoc OpenAPI (Swagger UI) | Latest |
| Email | SendGrid | Latest |
| OTP / SMS | Twilio (WhatsApp Sandbox + SMS) | Latest |
| Template Email | Thymeleaf | Latest |
| Giám sát | Spring Actuator | Latest |
| Dev Tools | Spring DevTools | Latest |
| Container | Docker + Docker Compose | Latest |

---

##  Bounded Contexts

Hệ thống được chia thành các **Bounded Context** độc lập theo DDD:

| Context | Trạng Thái | Mô Tả |
|---|---|---|
| `auth` |  **Đang làm** | Xác thực & Phân quyền — JWT, RBAC, OTP (Email + Twilio WhatsApp/SMS) |
| `notification` |  **Đang làm** | Gửi Email / SMS / WhatsApp — kích hoạt bởi domain events |
| `user` |  Dự kiến | Quản lý tài khoản, hồ sơ, địa chỉ |
| `product` |  Dự kiến | Danh mục, sản phẩm, biến thể, tồn kho |
| `order` |  Dự kiến | Giỏ hàng, đặt hàng, trạng thái đơn hàng |
| `payment` |  Dự kiến | Tích hợp cổng thanh toán, lịch sử giao dịch |
| `shipping` |  Dự kiến | Vận chuyển, theo dõi đơn hàng |
| `live` |  Dự kiến | Livestream bán hàng, flash sale |

>  **Tại sao tách theo Bounded Context?** Mỗi context là một module nghiệp vụ độc lập.
> Ví dụ: `auth` không tự gửi email OTP — nó chỉ phát `OtpRequestedEvent`.
> `notification` lắng nghe event đó và thực hiện gửi. Nhờ vậy, thêm kênh thông báo mới
> (Zalo OA, push notification...) chỉ cần mở rộng `notification`, không đụng `auth`.

---

##  Cấu Trúc Dự Án

```
ecommerce-backend/
├── docs/
│   ├── README/
│   │   └── README.vi.md             # README tiếng Việt (file này)
│   ├── architecture.md              # Kiến trúc chi tiết hệ thống
│   ├── api-spec.md                  # Tổng quan API specification
│   ├── contributing.md              # Hướng dẫn đóng góp
│   └── adr/                         # Architecture Decision Records
│
├── src/main/java/com/ecommerce/
│   │
│   ├── auth/                        # Bounded Context: Xác thực
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
│   ├── notification/                # Bounded Context: Thông báo
│   │   ├── application/
│   │   │   ├── handler/             # OtpNotificationHandler, InvoiceHandler...
│   │   │   └── port/                # NotificationSender interface
│   │   ├── domain/
│   │   │   └── NotificationTemplate.java
│   │   └── infrastructure/
│   │       ├── email/               # SendGridEmailSender (Thymeleaf templates)
│   │       └── sms/                 # TwilioSmsSender (WhatsApp + SMS)
│   │
│   ├── user/                        # Bounded Context: Người dùng (dự kiến)
│   ├── product/                     # Bounded Context: Sản phẩm (dự kiến)
│   ├── order/                       # Bounded Context: Đơn hàng (dự kiến)
│   ├── payment/                     # Bounded Context: Thanh toán (dự kiến)
│   ├── shipping/                    # Bounded Context: Vận chuyển (dự kiến)
│   ├── live/                        # Bounded Context: Livestream (dự kiến)
│   │
│   └── shared/                      # Shared Kernel — dùng chung mọi context
│       ├── domain/                  # Base classes, Value Objects chung
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
├── .env.example                     # Template biến môi trường
├── .gitignore
├── CONTRIBUTING.md
├── LICENSE
├── pom.xml
└── README.md
```

---

##  Bắt Đầu

### Yêu Cầu

- Java 21+ (khuyến nghị dùng [SDKMAN](https://sdkman.io/): `sdk install java 21-tem`)
- Docker & Docker Compose
- Maven 3.9+

### 1. Clone repository

```bash
git clone https://github.com/faken-dev/ecommerce-backend.git
cd ecommerce-backend
```

### 2. Thiết lập môi trường

```bash
cp .env.example .env
# Chỉnh sửa .env với các giá trị phù hợp (DB, JWT secret, Twilio credentials...)
```

### 3. Khởi động infrastructure

```bash
docker-compose up -d
```

### 4. Chạy ứng dụng

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Ứng dụng chạy tại: `http://localhost:8080`

### 5. Mở Swagger UI

```
http://localhost:8080/swagger-ui.html
```

---

##  Biến Môi Trường

Tạo file `.env` từ `.env.example`:

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

>  **Không bao giờ commit file `.env`** — đã được thêm vào `.gitignore`.

---

##  Tài Liệu API

| URL | Mô Tả |
|---|---|
| `GET /swagger-ui.html` | Swagger UI tương tác |
| `GET /v3/api-docs` | OpenAPI JSON spec |
| `GET /actuator/health` | Health check |
| `GET /actuator/info` | App info |

### Auth Endpoints (Phase 1)

| Method | Endpoint | Mô Tả |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Đăng ký tài khoản mới |
| `POST` | `/api/v1/auth/login` | Đăng nhập, nhận JWT |
| `POST` | `/api/v1/auth/refresh` | Làm mới access token |
| `POST` | `/api/v1/auth/logout` | Đăng xuất, revoke token |
| `POST` | `/api/v1/auth/otp/send` | Gửi OTP (Email / WhatsApp / SMS) |
| `POST` | `/api/v1/auth/otp/verify` | Xác thực OTP |
| `POST` | `/api/v1/auth/password/forgot` | Quên mật khẩu |
| `POST` | `/api/v1/auth/password/reset` | Đặt lại mật khẩu |

---

##  Database Migration

Dự án dùng **Flyway** để quản lý schema. Migration files tại:

```
src/main/resources/db/migration/
├── V1__create_roles_and_permissions.sql
├── V2__create_users.sql
├── V3__create_otp_tokens.sql
└── ...
```

**Naming convention**: `V{version}__{mô_tả}.sql`

Flyway tự động chạy migration khi ứng dụng khởi động.

---

##  Chiến Lược Branch

Dự án dùng **Git Flow**:

```
main          ← Bản release production (tagged)
  └── develop ← Branch tích hợp (branch làm việc mặc định)
        ├── feature/auth-jwt         ← Tính năng mới
        ├── feature/otp-twilio
        ├── fix/token-expiry-bug     ← Bug fixes
        └── chore/update-deps        ← Bảo trì / dependencies
```

### Quy Trình Làm Việc

```bash
# Tạo feature branch từ develop
git checkout develop
git pull origin develop
git checkout -b feature/your-feature-name

# Làm việc và commit thường xuyên
git add .
git commit -m "feat(auth): add OTP verification via Twilio WhatsApp"

# Push và tạo Pull Request vào develop
git push origin feature/your-feature-name
# → Tạo PR trên GitHub, review, merge
```

### Quy Ước Commit ([Conventional Commits](https://www.conventionalcommits.org/))

```
feat(auth): add JWT refresh token rotation
fix(otp): handle expired OTP gracefully
chore(deps): upgrade Spring Boot to 4.0.1
docs(readme): update getting started section
test(auth): add integration tests for login endpoint
refactor(user): extract address validation to value object
```

---

##  Lộ Trình Phát Triển

###  Phase 0 — Khởi Tạo Dự Án
- [x] Khởi tạo project Spring Boot 4 + Java 21
- [ ] Cấu hình PostgreSQL + Flyway
- [ ] Docker Compose infrastructure
- [ ] Cấu trúc DDD Layered Architecture
- [ ] Tài liệu kiến trúc (`docs/architecture.md`)

###  Phase 1 — Xác Thực & Phân Quyền *(Đang thực hiện)*
- [ ] Đăng ký & đăng nhập (Email/Password)
- [ ] JWT Access Token + Refresh Token Rotation
- [ ] RBAC: `ADMIN`, `SELLER`, `BUYER`
- [ ] OTP qua Email (Spring Mail + Thymeleaf template)
- [ ] OTP qua Twilio SMS & WhatsApp Sandbox
- [ ] Luồng Quên/Đặt lại mật khẩu
- [ ] Rate limiting với Bucket4j (chống brute-force)
- [ ] Flyway migrations: users, roles, permissions, otp_tokens
- [ ] Notification context: gửi email/SMS theo event

###  Phase 2 — Quản Lý Người Dùng
- [ ] CRUD hồ sơ người dùng
- [ ] Sổ địa chỉ nhiều địa điểm
- [ ] Upload avatar
- [ ] Cài đặt tài khoản

###  Phase 3 — Danh Mục Sản Phẩm
- [ ] Cây danh mục
- [ ] Sản phẩm + biến thể (kích thước, màu sắc...)
- [ ] Quản lý tồn kho
- [ ] Tìm kiếm & lọc sản phẩm
- [ ] ...

###  Phase 4 — Đơn Hàng & Giỏ Hàng
- [ ] Giỏ hàng
- [ ] Đặt hàng & vòng đời đơn hàng
- [ ] Lịch sử đơn hàng
- [ ] ...

###  Phase 5 — Thanh Toán
- [ ] Tích hợp cổng thanh toán
- [ ] Lịch sử giao dịch
- [ ] Xử lý hoàn tiền
- [ ] ...

###  Phase 6 — Vận Chuyển
- [ ] Tích hợp đơn vị vận chuyển
- [ ] Theo dõi đơn hàng
- [ ] ...

###  Phase 7 — Livestream Bán Hàng
- [ ] Quản lý phiên livestream
- [ ] Flash sale engine
- [ ] ...

---

##  Đóng Góp

1. Fork repository
2. Tạo branch từ `develop`: `git checkout -b feature/amazing-feature`
3. Commit theo Conventional Commits
4. Push và tạo Pull Request vào `develop`
5. Đợi review và merge

Vui lòng đọc [`CONTRIBUTING.md`](../../CONTRIBUTING.md) trước khi đóng góp.

---

##  License

Dự án được phân phối dưới **MIT License** — xem [`LICENSE`](../../LICENSE) để biết chi tiết.

---

<div align="center">
  <sub>Built with ❤️ using Java 21 + Spring Boot 4 · Domain-Driven Design by faken</sub>
</div>