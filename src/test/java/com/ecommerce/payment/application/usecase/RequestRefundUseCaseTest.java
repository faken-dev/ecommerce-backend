package com.ecommerce.payment.application.usecase;

import com.ecommerce.payment.application.command.RequestRefundCommand;
import com.ecommerce.payment.application.dto.RefundResponse;
import com.ecommerce.payment.application.mapper.RefundApplicationMapper;
import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.entity.PaymentMethodType;
import com.ecommerce.payment.domain.entity.PaymentProvider;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import com.ecommerce.payment.domain.entity.RefundStatus;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.testutil.fixture.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestRefundUseCase")
class RequestRefundUseCaseTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private RefundApplicationMapper mapper;
    @Mock private com.ecommerce.shared.event.outbox.OutboxService outboxService;

    private RequestRefundUseCase sut;

    @BeforeEach
    void setUp() {
        sut = new RequestRefundUseCase(paymentRepository, mapper, outboxService);
    }

    private Payment aPaidPayment() {
        return Payment.builder()
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
    }

    // RefundResponse: id, paymentId, orderId, requestedBy, amount, reason, status,
    //                  rejectionReason, providerRefundId, approvedAt, rejectedAt, completedAt, failedAt, createdAt
    private RefundResponse aPendingResponse() {
        return new RefundResponse(
                Fixtures.REFUND_ID,
                Fixtures.PAYMENT_ID,
                Fixtures.ORDER_ID,
                Fixtures.USER_ID,
                new BigDecimal("50000"),
                "Product damaged",
                RefundStatus.PENDING,
                null, // rejectionReason
                null, // providerRefundId
                null, // approvedAt
                null, // rejectedAt
                null, // completedAt
                null, // failedAt
                Instant.now() // createdAt
        );
    }

    private RequestRefundCommand aValidCommand() {
        return new RequestRefundCommand(
                Fixtures.PAYMENT_ID,
                Fixtures.ORDER_ID,
                Fixtures.USER_ID,
                new BigDecimal("50000"),
                "Product damaged",
                "1.1.1.1"
        );
    }

    @Nested
    @DisplayName("execute() — success")
    class Success {

        @Test
        @DisplayName("should create pending refund and return response")
        void createsRefund() {
            Payment payment = aPaidPayment();

            when(paymentRepository.findById(Fixtures.PAYMENT_ID))
                    .thenReturn(java.util.Optional.of(payment));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.toResponse(any())).thenReturn(aPendingResponse());

            RequestRefundCommand cmd = aValidCommand();
            RefundResponse result = sut.execute(cmd);

            assertThat(result.status()).isEqualTo(RefundStatus.PENDING);
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("50000"));
        }

        @Test
        @DisplayName("should publish RefundRequestedEvent")
        void publishesEvent() {
            Payment payment = aPaidPayment();
            when(paymentRepository.findById(Fixtures.PAYMENT_ID))
                    .thenReturn(java.util.Optional.of(payment));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.toResponse(any())).thenReturn(aPendingResponse());

            sut.execute(aValidCommand());

            verify(outboxService).saveEvent(any(
                    com.ecommerce.payment.domain.event.RefundRequestedEvent.class), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("execute() — error cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw PAYMENT_NOT_FOUND when payment not found")
        void paymentNotFound() {
            when(paymentRepository.findById(any())).thenReturn(java.util.Optional.empty());

            assertThatThrownBy(() -> sut.execute(aValidCommand()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("should throw PAYMENT_NOT_REFUNDABLE when payment not in PAID state")
        void notRefundable() {
            Payment pending = Payment.create(
                    Fixtures.ORDER_ID, Fixtures.USER_ID,
                    new BigDecimal("100000"), "VND",
                    PaymentProvider.COD, PaymentMethodType.COD,
                    "d", null, null, null, "ip", "ua"
            );
            when(paymentRepository.findById(Fixtures.PAYMENT_ID))
                    .thenReturn(java.util.Optional.of(pending));

            assertThatThrownBy(() -> sut.execute(aValidCommand()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_NOT_REFUNDABLE);
        }

        @Test
        @DisplayName("should throw PAYMENT_REFUND_AMOUNT_INVALID when amount exceeds max")
        void amountTooLarge() {
            Payment payment = aPaidPayment();
            when(paymentRepository.findById(Fixtures.PAYMENT_ID))
                    .thenReturn(java.util.Optional.of(payment));

            RequestRefundCommand cmd = new RequestRefundCommand(
                    Fixtures.PAYMENT_ID, Fixtures.ORDER_ID, Fixtures.USER_ID,
                    new BigDecimal("99999999"), "Too much", "1.1.1.1");

            assertThatThrownBy(() -> sut.execute(cmd))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_REFUND_AMOUNT_INVALID);
        }
    }
}