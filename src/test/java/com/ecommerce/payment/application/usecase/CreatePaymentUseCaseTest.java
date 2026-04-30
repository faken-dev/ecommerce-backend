package com.ecommerce.payment.application.usecase;

import com.ecommerce.payment.application.command.CreatePaymentCommand;
import com.ecommerce.payment.application.dto.PaymentInitiatedResponse;
import com.ecommerce.payment.application.port.PaymentInitiator;
import com.ecommerce.payment.application.port.PaymentInitiator.InitiationResult;
import com.ecommerce.payment.application.port.PaymentGatewayResolver;
import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.entity.PaymentMethodType;
import com.ecommerce.payment.domain.entity.PaymentProvider;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.payment.infrastructure.gateway.momo.MoMoGatewayAdapter;
import com.ecommerce.payment.infrastructure.gateway.vnpay.VNPayGatewayAdapter;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CreatePaymentUseCase")
class CreatePaymentUseCaseTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private com.ecommerce.shared.event.outbox.OutboxService outboxService;
    @Mock private PaymentGatewayResolver gatewayResolver;
    @Mock private PaymentInitiator initiator;
    @Mock private VNPayGatewayAdapter vnpayAdapter;
    @Mock private MoMoGatewayAdapter momoAdapter;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private final java.util.Map<java.util.UUID, Payment> db = new java.util.HashMap<>();
    private CreatePaymentUseCase sut;

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

        sut = new CreatePaymentUseCase(paymentRepository, outboxService, gatewayResolver, transactionTemplate, eventPublisher);
    }

    // ─── Command factories ───────────────────────────────────────────────────

    private CreatePaymentCommand onlineCommand() {
        return new CreatePaymentCommand(
                Fixtures.ORDER_ID,
                Fixtures.USER_ID,
                new BigDecimal("130000"),
                "VND",
                PaymentProvider.VNPAY,
                PaymentMethodType.WALLET,
                null,
                "idem-001",
                "https://app.example.com/return",
                "https://app.example.com/cancel",
                "192.168.1.1",
                "Mozilla/5.0"
        );
    }

    private CreatePaymentCommand momoCommand() {
        return new CreatePaymentCommand(
                Fixtures.ORDER_ID,
                Fixtures.USER_ID,
                new BigDecimal("130000"),
                "VND",
                PaymentProvider.MOMO,
                PaymentMethodType.WALLET,
                null,
                "idem-momo-001",
                "https://app.example.com/return",
                "https://app.example.com/cancel",
                "192.168.1.1",
                "Mozilla/5.0"
        );
    }

    private CreatePaymentCommand codCommand() {
        return new CreatePaymentCommand(
                Fixtures.ORDER_ID,
                Fixtures.USER_ID,
                new BigDecimal("130000"),
                "VND",
                PaymentProvider.COD,
                PaymentMethodType.COD,
                null,
                "idem-cod",
                null,
                null,
                "1.1.1.1",
                "UA"
        );
    }

    private CreatePaymentCommand stripeCommand() {
        return new CreatePaymentCommand(
                Fixtures.ORDER_ID,
                Fixtures.USER_ID,
                new BigDecimal("99000"),
                "USD",
                PaymentProvider.STRIPE,
                PaymentMethodType.CARD,
                null,
                "idem-stripe-001",
                "https://app.example.com/return",
                "https://app.example.com/cancel",
                "192.168.1.100",
                "Chrome/120"
        );
    }

    // ─── Tests ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("execute() — VNPay (redirect-based)")
    class VNPayProvider {

        @Test
        @DisplayName("should return payment page URL from VNPay adapter")
        void returnsVNPayRedirectUrl() {
            when(paymentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
            when(paymentRepository.findAllByOrderId(any())).thenReturn(List.of());
            when(gatewayResolver.getInitiator(PaymentProvider.VNPAY)).thenReturn(vnpayAdapter);
            when(vnpayAdapter.initiate(any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(InitiationResult.redirect(Fixtures.PAYMENT_ID.toString(), "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_ref=123"));

            PaymentInitiatedResponse response = sut.execute(onlineCommand());

            assertThat(response.paymentId()).isNotNull();
            assertThat(response.provider()).isEqualTo("VNPAY");
            assertThat(response.redirectUrl()).isNotNull();
            assertThat(response.redirectUrl()).contains("vnpayment.vn");

            verify(outboxService).saveEvent(any(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("execute() — MoMo (redirect-based)")
    class MoMoProvider {

        @Test
        @DisplayName("should return payment URL from MoMo adapter")
        void returnsMoMoRedirectUrl() {
            when(paymentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
            when(paymentRepository.findAllByOrderId(any())).thenReturn(List.of());
            when(gatewayResolver.getInitiator(PaymentProvider.MOMO)).thenReturn(momoAdapter);
            when(momoAdapter.initiate(any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(InitiationResult.redirect("momo-123", "momo://payment?partnerRefId=123"));

            PaymentInitiatedResponse response = sut.execute(momoCommand());

            assertThat(response.provider()).isEqualTo("MOMO");
            assertThat(response.redirectUrl()).isNotNull();
        }
    }

    @Nested
    @DisplayName("execute() — Stripe (client-side)")
    class StripeProvider {

        @Test
        @DisplayName("should return client_secret for Stripe.js")
        void returnsStripeClientSecret() {
            when(paymentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
            when(paymentRepository.findAllByOrderId(any())).thenReturn(List.of());
            when(gatewayResolver.getInitiator(PaymentProvider.STRIPE)).thenReturn(initiator);
            // Stripe adapter returns InitiationResult.sdk
            when(initiator.initiate(any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(InitiationResult.sdk("pi_3abc123", "secret_xyz"));

            PaymentInitiatedResponse response = sut.execute(stripeCommand());

            assertThat(response.provider()).isEqualTo("STRIPE");
            assertThat(response.providerReference()).isEqualTo("pi_3abc123");
            assertThat(response.redirectUrl()).isEqualTo("secret_xyz");
        }
    }

    @Nested
    @DisplayName("execute() — COD")
    class CodProvider {

        @Test
        @DisplayName("should NOT call any gateway for COD")
        void noGatewayCall() {
            when(paymentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
            when(paymentRepository.findAllByOrderId(any())).thenReturn(List.of());

            PaymentInitiatedResponse response = sut.execute(codCommand());

            assertThat(response.provider()).isEqualTo("COD");
            assertThat(response.redirectUrl()).isNull();
            verifyNoInteractions(gatewayResolver);
        }

        @Test
        @DisplayName("should NOT publish PaymentInitiatedEvent for COD (no gateway)")
        void noEventForCod() {
            when(paymentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
            when(paymentRepository.findAllByOrderId(any())).thenReturn(List.of());

            sut.execute(codCommand());

            // COD has no providerReference, so no outbox event is saved
            verifyNoInteractions(outboxService);
        }
    }

    @Nested
    @DisplayName("execute() — idempotency")
    class Idempotency {

        @Test
        @DisplayName("should return existing payment when idempotency key matches")
        void returnsExistingPayment() {
            Payment existing = Fixtures.aPendingPayment();

            when(paymentRepository.findByIdempotencyKey("idem-001")).thenReturn(Optional.of(existing));

            PaymentInitiatedResponse result = sut.execute(onlineCommand());

            assertThat(result.paymentId()).isEqualTo(existing.getId().toString());
            verifyNoInteractions(gatewayResolver);
            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("should NOT call gateway when idempotency key already exists")
        void noGatewayWhenIdempotent() {
            Payment existing = Fixtures.aPendingPayment();
            when(paymentRepository.findByIdempotencyKey("idem-001")).thenReturn(Optional.of(existing));

            sut.execute(onlineCommand());

            verify(gatewayResolver, never()).getInitiator(any());
        }
    }

    @Nested
    @DisplayName("execute() — duplicate order guard")
    class DuplicateOrder {

        @Test
        @DisplayName("should throw PAYMENT_ALREADY_PAID when active payment exists for order")
        void throwsWhenActivePaymentExists() {
            Payment activePayment = Payment.builder()
                    .id(Fixtures.PAYMENT_ID)
                    .orderId(Fixtures.ORDER_ID)
                    .buyerId(Fixtures.USER_ID)
                    .amount(new BigDecimal("130000"))
                    .refundedAmount(BigDecimal.ZERO)
                    .currency("VND")
                    .provider(PaymentProvider.VNPAY)
                    .methodType(PaymentMethodType.WALLET)
                    .providerReference("existing_ref")
                    .status(PaymentStatus.PAID)
                    .description("Thanh toan")
                    .ipAddress("1.1.1.1")
                    .userAgent("UA")
                    .paidAt(Instant.now())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            when(paymentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
            when(paymentRepository.findAllByOrderId(Fixtures.ORDER_ID))
                    .thenReturn(List.of(activePayment));

            assertThatThrownBy(() -> sut.execute(onlineCommand()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_ALREADY_PAID);
        }

        @Test
        @DisplayName("should NOT throw when existing payment is FAILED (not active)")
        void shouldAllowNewPaymentAfterFailure() {
            Payment failedPayment = Payment.builder()
                    .id(Fixtures.PAYMENT_ID)
                    .orderId(Fixtures.ORDER_ID)
                    .buyerId(Fixtures.USER_ID)
                    .amount(new BigDecimal("130000"))
                    .refundedAmount(BigDecimal.ZERO)
                    .currency("VND")
                    .provider(PaymentProvider.VNPAY)
                    .methodType(PaymentMethodType.WALLET)
                    .providerReference("failed_ref")
                    .status(PaymentStatus.FAILED)
                    .description("Thanh toan")
                    .ipAddress("1.1.1.1")
                    .userAgent("UA")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            when(paymentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
            when(paymentRepository.findAllByOrderId(Fixtures.ORDER_ID))
                    .thenReturn(List.of(failedPayment));
            when(gatewayResolver.getInitiator(PaymentProvider.VNPAY)).thenReturn(vnpayAdapter);
            when(vnpayAdapter.initiate(any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(InitiationResult.redirect("vnpay-ref", "https://vnpay.url"));

            // Should NOT throw — FAILED is terminal
            PaymentInitiatedResponse response = sut.execute(onlineCommand());
            assertThat(response.paymentId()).isNotNull();
        }
    }
}
