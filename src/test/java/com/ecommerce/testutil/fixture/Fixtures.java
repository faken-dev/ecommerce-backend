package com.ecommerce.testutil.fixture;

import com.ecommerce.auth.domain.entity.*;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.HashedPassword;
import com.ecommerce.auth.domain.valueobject.OAuth2Provider;
import com.ecommerce.auth.domain.valueobject.PhoneNumber;
import com.ecommerce.catalog.domain.entity.Category;
import com.ecommerce.catalog.domain.entity.Product;
import com.ecommerce.order.domain.entity.Cart;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.entity.OrderItem;
import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.entity.PaymentMethodType;
import com.ecommerce.payment.domain.entity.PaymentProvider;
import com.ecommerce.payment.domain.entity.Refund;
import com.ecommerce.user.domain.entity.Address;
import com.ecommerce.user.domain.entity.UserProfile;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.UUID;

/**
 * Shared test fixtures for consistent domain objects across tests.
 */
public class Fixtures {

    public static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
    public static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID PRODUCT_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    public static final UUID VARIANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    public static final UUID CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    public static final UUID ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000006");
    public static final UUID PAYMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000007");
    public static final UUID REFUND_ID = UUID.fromString("00000000-0000-0000-0000-000000000008");
    public static final UUID ADDRESS_ID = UUID.fromString("00000000-0000-0000-0000-000000000009");
    public static final UUID SHIPPING_ADDRESS_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    public static final String USER_EMAIL = "test@example.com";
    public static final String USER_PASSWORD = "password123";
    public static final String USER_FULLNAME = "Test User";
    public static final String USER_PHONE = "+84123456789";

    // ── Auth / User ──────────────────────────────────────────────────────────

    public static User aUser() {
        return User.builder()
                .id(USER_ID)
                .email(new Email(USER_EMAIL))
                .passwordHash(HashedPassword.of(USER_PASSWORD))
                .fullName(USER_FULLNAME)
                .phoneNumber(new PhoneNumber(USER_PHONE))
                .active(true)
                .emailVerified(false)
                .phoneVerified(false)
                .otpBlocked(false)
                .roles(new HashSet<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static User aUser(UUID id) {
        return User.builder()
                .id(id)
                .email(new Email("user-" + id + "@example.com"))
                .passwordHash(HashedPassword.of("hashed"))
                .fullName("User " + id)
                .active(true)
                .roles(new HashSet<>())
                .build();
    }

    public static User anActiveEmailVerifiedUser() {
        User user = User.builder()
                .id(USER_ID)
                .email(new Email(USER_EMAIL))
                .passwordHash(HashedPassword.of(USER_PASSWORD))
                .fullName(USER_FULLNAME)
                .active(true)
                .emailVerified(true)
                .roles(new HashSet<>())
                .build();
        user.addRole(buyerRole());
        return user;
    }

    public static User anOAuthUser() {
        User user = User.builder()
                .id(USER_ID)
                .email(new Email(USER_EMAIL))
                .fullName(USER_FULLNAME)
                .provider(OAuth2Provider.GOOGLE)
                .providerUserId("google-sub-123")
                .active(true)
                .emailVerified(true)
                .roles(new HashSet<>())
                .build();
        user.addRole(buyerRole());
        return user;
    }

    public static User aBlockedUser() {
        return User.builder()
                .id(USER_ID)
                .email(new Email(USER_EMAIL))
                .active(false)
                .build();
    }

    public static User aDeletedUser() {
        return User.builder()
                .id(USER_ID)
                .email(new Email(USER_EMAIL))
                .deletedAt(Instant.now())
                .deletedBy(ADMIN_ID)
                .build();
    }

    public static Role buyerRole() {
        return Role.builder()
                .id(UUID.randomUUID())
                .name("BUYER")
                .permissions(new HashSet<>())
                .build();
    }

    public static Role sellerRole() {
        return Role.builder()
                .id(UUID.randomUUID())
                .name("SELLER")
                .permissions(new HashSet<>())
                .build();
    }

    public static RefreshToken aValidRefreshToken() {
        return RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .tokenHash("hashed_token")
                .deviceInfo("Chrome on Windows")
                .ipAddress("192.168.1.1")
                .generation(1)
                .expiresAt(Instant.now().plusSeconds(7 * 24 * 3600))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static RefreshToken aRevokedRefreshToken() {
        return RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .tokenHash("hashed_token")
                .expiresAt(Instant.now().plusSeconds(3600))
                .revokedAt(Instant.now())
                .build();
    }

    public static RefreshToken anExpiredRefreshToken() {
        return RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .tokenHash("hashed_token")
                .expiresAt(Instant.now().minusSeconds(3600))
                .build();
    }

    public static OtpToken anActiveOtpToken() {
        return OtpToken.create(
                Fixtures.USER_ID,
                "hash123",
                OtpToken.Channel.EMAIL,
                OtpToken.Purpose.EMAIL_VERIFICATION,
                5
        );
    }

    public static OtpToken anExpiredOtpToken() {
        return OtpToken.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .codeHash("hashed_code")
                .expiresAt(Instant.now().minusSeconds(300))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static OtpToken aUsedOtpToken() {
        return OtpToken.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .codeHash("hashed_code")
                .expiresAt(Instant.now().plusSeconds(300))
                .usedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ── User Profile / Address ────────────────────────────────────────────────

    public static UserProfile aUserProfile() {
        return UserProfile.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .fullName(USER_FULLNAME)
                .build();
    }

    public static UserProfile aCompleteUserProfile() {
        return UserProfile.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .fullName("Full Name")
                .gender(UserProfile.Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .profilePictureUrl("https://cdn.example.com/avatar.jpg")
                .bio("Bio text")
                .defaultAddressId(ADDRESS_ID)
                .build();
    }

    public static Address anAddress() {
        return Address.builder()
                .id(ADDRESS_ID)
                .userId(USER_ID)
                .recipientName("Test Recipient")
                .recipientPhone("+84909123456")
                .addressLine("123 Main Street")
                .ward("Ward 1")
                .district("District 1")
                .province("Ho Chi Minh City")
                .build();
    }

    public static Address aDefaultAddress() {
        return Address.builder()
                .id(ADDRESS_ID)
                .userId(USER_ID)
                .recipientName("Test Recipient")
                .recipientPhone("+84909123456")
                .addressLine("123 Main Street")
                .ward("Ward 1")
                .district("District 1")
                .province("Ho Chi Minh City")
                .defaultAddress(true)
                .build();
    }

    // ── Category ──────────────────────────────────────────────────────────────

    public static Category aRootCategory() {
        return Category.createRoot("electronics", "Electronics", "Electronic devices");
    }

    public static Category aChildCategory(UUID parentId) {
        return Category.createChild("smartphones", "Smartphones", "Mobile phones", parentId);
    }

    // ── Product ───────────────────────────────────────────────────────────────

    public static Product aProduct() {
        return Product.builder()
                .id(PRODUCT_ID)
                .sellerId(SELLER_ID)
                .name("iPhone 15 Pro")
                .slug("iphone-15-pro")
                .description("Latest Apple flagship")
                .price(new BigDecimal("29990000"))
                .status(Product.Status.ACTIVE)
                .isFeatured(false)
                .visibility(Product.Visibility.SHOP)
                .reviewCount(0)
                .weightUnit("KG")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static Product aDraftProduct() {
        return Product.createAsDraft(
                SELLER_ID,
                "Draft Product",
                "draft-product",
                new BigDecimal("9900000")
        );
    }

    // ── Cart ─────────────────────────────────────────────────────────────────

    public static Cart aCart() {
        return Cart.createCart(USER_ID);
    }

    // ── Order ────────────────────────────────────────────────────────────────

    public static Order anOrder() {
        return Order.builder()
                .id(ORDER_ID)
                .buyerId(USER_ID)
                .sellerId(SELLER_ID)
                .status(com.ecommerce.order.domain.entity.OrderStatus.PENDING)
                .shippingAddressId(SHIPPING_ADDRESS_ID)
                .subtotal(new BigDecimal("100000"))
                .shippingFee(new BigDecimal("20000"))
                .taxAmount(new BigDecimal("10000"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("130000"))
                .currency("VND")
                .paymentMethod(null)
                .paymentStatus(com.ecommerce.order.domain.entity.PaymentStatus.PENDING)
                .cancelWindowSec(1800L)
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .deletedAt(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static OrderItem anOrderItem() {
        return OrderItem.create(
                ORDER_ID,
                PRODUCT_ID,
                CATEGORY_ID,
                "iPhone 15 Pro",
                "SKU-001",
                "https://cdn.example.com/img.jpg",
                VARIANT_ID,
                "128GB Black",
                2,
                new BigDecimal("29990000")
        );
    }

    // ── Payment ───────────────────────────────────────────────────────────────

    public static Payment aPendingPayment() {
        return Payment.create(
                ORDER_ID,
                USER_ID,
                new BigDecimal("130000"),
                "VND",
                PaymentProvider.VNPAY,
                PaymentMethodType.WALLET,
                "Thanh toan don hang",
                "idem-key-001",
                "https://app.example.com/payment/return",
                "https://app.example.com/payment/cancel",
                "192.168.1.1",
                "Mozilla/5.0"
        );
    }

    public static Payment aPaidPayment() {
        return Payment.builder()
                .id(PAYMENT_ID)
                .orderId(ORDER_ID)
                .buyerId(USER_ID)
                .amount(new BigDecimal("130000"))
                .refundedAmount(BigDecimal.ZERO)
                .currency("VND")
                .provider(PaymentProvider.VNPAY)
                .methodType(PaymentMethodType.WALLET)
                .providerReference("vnpay_pi_123")
                .status(com.ecommerce.payment.domain.entity.PaymentStatus.PAID)
                .description("Thanh toan don hang")
                .idempotencyKey("idem-key-001")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .paidAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static Payment aPartiallyRefundedPayment() {
        return Payment.builder()
                .id(PAYMENT_ID)
                .orderId(ORDER_ID)
                .buyerId(USER_ID)
                .amount(new BigDecimal("130000"))
                .refundedAmount(new BigDecimal("50000"))
                .currency("VND")
                .provider(PaymentProvider.COD)
                .methodType(PaymentMethodType.COD)
                .providerReference(null)
                .status(com.ecommerce.payment.domain.entity.PaymentStatus.PARTIALLY_REFUNDED)
                .description("Thanh toan don hang")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .paidAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ── Refund ───────────────────────────────────────────────────────────────

    public static Refund aPendingRefund() {
        return Refund.create(
                REFUND_ID,
                PAYMENT_ID,
                ORDER_ID,
                USER_ID,
                new BigDecimal("50000"),
                "Product defective",
                "192.168.1.1"
        );
    }
}