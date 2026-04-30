package com.ecommerce.payment.application.usecase;

import com.ecommerce.payment.application.command.ApproveRefundCommand;
import com.ecommerce.payment.application.dto.RefundResponse;
import com.ecommerce.payment.application.mapper.RefundApplicationMapper;
import com.ecommerce.payment.application.port.RefundIssuer;
import com.ecommerce.payment.application.port.PaymentGatewayResolver;
import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.entity.PaymentMethodType;
import com.ecommerce.payment.domain.entity.PaymentProvider;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import com.ecommerce.payment.domain.entity.RefundStatus;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.testutil.fixture.Fixtures;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ApproveRefundUseCase")
class ApproveRefundUseCaseTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private RefundApplicationMapper mapper;
    @Mock private PaymentGatewayResolver gatewayResolver;
    @Mock private RefundIssuer refundIssuer;
    @Mock private com.ecommerce.shared.event.outbox.OutboxService outboxService;
    @Mock private TransactionTemplate transactionTemplate;

    private ApproveRefundUseCase sut;
    private final java.util.Map<java.util.UUID, Payment> db = new java.util.HashMap<>();

    @BeforeEach

    void setUp() {
        db.clear();
        // Mock TransactionTemplate to execute the callback immediately
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        // Track saved payments in a map to simulate a real DB for findById calls
        when(paymentRepository.save(any())).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            db.put(p.getId(), p);
            return p;
        });

        when(paymentRepository.findById(any())).thenAnswer(inv -> {
            java.util.UUID id = inv.getArgument(0);
            return Optional.ofNullable(db.get(id));
        });

        sut = new ApproveRefundUseCase(paymentRepository, mapper, gatewayResolver, outboxService, transactionTemplate);
    }

    private Payment aPaidPaymentWithRefund() {
        Payment payment = Payment.builder()
                .id(Fixtures.PAYMENT_ID)
                .orderId(Fixtures.ORDER_ID)
                .buyerId(Fixtures.USER_ID)
                .amount(new BigDecimal("130000"))
                .refundedAmount(BigDecimal.ZERO)
                .currency("VND")
                .provider(PaymentProvider.VNPAY)
                .methodType(PaymentMethodType.WALLET)
                .providerReference("vnpay_pi_123")
                .status(PaymentStatus.PAID)
                .description("Thanh toan")
                .ipAddress("1.1.1.1")
                .userAgent("UA")
                .paidAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        payment.requestRefund(Fixtures.ORDER_ID, Fixtures.USER_ID,
                new BigDecimal("50000"), "damaged", "1.1.1.1");
        return payment;
    }

    @Nested
    @DisplayName("execute() — success")
    class Success {

        @Test
        @DisplayName("should call gateway for online provider and approve refund")
        void onlineProvider() {
            Payment payment = aPaidPaymentWithRefund();
            db.put(payment.getId(), payment);
            UUID refundId = payment.getRefunds().get(0).getId();

            when(gatewayResolver.getRefundIssuer(PaymentProvider.VNPAY)).thenReturn(refundIssuer);
            when(refundIssuer.issueRefund(any(), any(), any(), any()))
                    .thenReturn("vnpay_refund_789");
            when(mapper.toResponse(any())).thenReturn(new RefundResponse(
                    refundId, Fixtures.PAYMENT_ID, Fixtures.ORDER_ID,
                    Fixtures.USER_ID,
                    new BigDecimal("50000"), "damaged",
                    RefundStatus.APPROVED, null, "vnpay_refund_789",
                    Instant.now(), null, null, null, Instant.now()));

            // ApproveRefundCommand: refundId, paymentId, providerRefundId, approvedAt
            ApproveRefundCommand cmd = new ApproveRefundCommand(
                    refundId, Fixtures.PAYMENT_ID, "vnpay_refund_789", Instant.now());
            RefundResponse result = sut.execute(cmd);

            assertThat(result.status()).isEqualTo(RefundStatus.APPROVED);
            verify(refundIssuer).issueRefund(refundId, "vnpay_pi_123",
                    new BigDecimal("50000"), "VND");
        }

        @Test
        @DisplayName("should generate internal refund ID for COD provider")
        void codProvider() {
            Payment payment = Payment.builder()
                    .id(Fixtures.PAYMENT_ID)
                    .orderId(Fixtures.ORDER_ID)
                    .buyerId(Fixtures.USER_ID)
                    .amount(new BigDecimal("130000"))
                    .refundedAmount(BigDecimal.ZERO)
                    .currency("VND")
                    .provider(PaymentProvider.COD)
                    .methodType(PaymentMethodType.COD)
                    .providerReference(null)
                    .status(PaymentStatus.PAID)
                    .description("Thanh toan")
                    .ipAddress("1.1.1.1")
                    .userAgent("UA")
                    .paidAt(Instant.now())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            payment.requestRefund(Fixtures.ORDER_ID, Fixtures.USER_ID,
                    new BigDecimal("50000"), "damaged", "1.1.1.1");
            db.put(payment.getId(), payment);
            UUID refundId = payment.getRefunds().get(0).getId();

            when(mapper.toResponse(any())).thenReturn(new RefundResponse(
                    refundId, Fixtures.PAYMENT_ID, Fixtures.ORDER_ID,
                    Fixtures.USER_ID,
                    new BigDecimal("50000"), "damaged",
                    RefundStatus.APPROVED, null, "COD-REFUND-xxx",
                    Instant.now(), null, null, null, Instant.now()));

            ApproveRefundCommand cmd = new ApproveRefundCommand(
                    refundId, Fixtures.PAYMENT_ID, "COD-REFUND-xxx", Instant.now());
            RefundResponse result = sut.execute(cmd);

            assertThat(result.status()).isEqualTo(RefundStatus.APPROVED);
            verify(gatewayResolver, never()).getRefundIssuer(any());
        }

        @Test
        @DisplayName("should publish RefundApprovedEvent")
        void publishesEvent() {
            Payment payment = aPaidPaymentWithRefund();
            db.put(payment.getId(), payment);
            UUID refundId = payment.getRefunds().get(0).getId();

            when(gatewayResolver.getRefundIssuer(PaymentProvider.VNPAY)).thenReturn(refundIssuer);
            when(refundIssuer.issueRefund(any(), any(), any(), any())).thenReturn("ref");
            when(mapper.toResponse(any())).thenReturn(new RefundResponse(
                    refundId, Fixtures.PAYMENT_ID, Fixtures.ORDER_ID,
                    Fixtures.USER_ID,
                    new BigDecimal("50000"), "damaged",
                    RefundStatus.APPROVED, null, "ref",
                    Instant.now(), null, null, null, Instant.now()));

            ApproveRefundCommand cmd = new ApproveRefundCommand(
                    refundId, Fixtures.PAYMENT_ID, "ref", Instant.now());
            sut.execute(cmd);

            verify(outboxService).saveEvent(any(
                    com.ecommerce.payment.domain.event.RefundApprovedEvent.class), anyString(), anyString());
        }
    }

    // ── Error Paths ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("execute() — error cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw PAYMENT_NOT_FOUND when payment does not exist")
        void paymentNotFound() {
            when(paymentRepository.findById(any())).thenReturn(Optional.empty());

            ApproveRefundCommand cmd = new ApproveRefundCommand(
                    Fixtures.REFUND_ID, Fixtures.PAYMENT_ID, "ref", Instant.now());

            assertThatThrownBy(() -> sut.execute(cmd))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("should throw PAYMENT_REFUND_NOT_FOUND when refundId not found in payment")
        void refundNotFound() {
            Payment payment = aPaidPaymentWithRefund();
            db.put(payment.getId(), payment);
            UUID wrongRefundId = UUID.randomUUID();


            ApproveRefundCommand cmd = new ApproveRefundCommand(
                    wrongRefundId, Fixtures.PAYMENT_ID, "ref", Instant.now());

            assertThatThrownBy(() -> sut.execute(cmd))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_REFUND_NOT_FOUND);
        }
    }
}