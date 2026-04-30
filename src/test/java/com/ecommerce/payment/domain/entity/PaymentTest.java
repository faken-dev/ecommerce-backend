package com.ecommerce.payment.domain.entity;

import com.ecommerce.testutil.fixture.Fixtures;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Payment Entity — Domain Tests")
class PaymentTest {

    // ── Factory ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create payment with PENDING status and correct fields")
        void createsCorrectly() {
            Payment payment = Fixtures.aPendingPayment();

            assertThat(payment.getOrderId()).isEqualTo(Fixtures.ORDER_ID);
            assertThat(payment.getBuyerId()).isEqualTo(Fixtures.USER_ID);
            assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("130000"));
            assertThat(payment.getRefundedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getProvider()).isEqualTo(PaymentProvider.VNPAY);
        }

        @Test
        @DisplayName("should default currency to VND when null")
        void defaultsCurrency() {
            Payment payment = Payment.create(
                    Fixtures.ORDER_ID, Fixtures.USER_ID,
                    new BigDecimal("100000"), null,
                    PaymentProvider.COD, PaymentMethodType.COD,
                    "desc", null, null, null,
                    "1.1.1.1", "UA"
            );
            assertThat(payment.getCurrency()).isEqualTo("VND");
        }

        @Test
        @DisplayName("should throw VALIDATION_FAILED when amount is zero or negative")
        void invalidAmount() {
            assertThatThrownBy(() -> Payment.create(
                    Fixtures.ORDER_ID, Fixtures.USER_ID,
                    BigDecimal.ZERO, "VND",
                    PaymentProvider.COD, PaymentMethodType.COD,
                    "d", null, null, null, "1.1.1.1", "UA"
            )).isInstanceOf(BusinessException.class)
              .extracting(e -> ((BusinessException) e).getErrorCode())
              .isEqualTo(ErrorCode.VALIDATION_FAILED);
        }
    }

    // ── initiate ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("initiate()")
    class Initiate {

        @Test
        @DisplayName("should transition to PROCESSING and set providerReference")
        void transitionsToProcessing() {
            Payment payment = Fixtures.aPendingPayment();

            payment.initiate("vnpay_ref_123");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
            assertThat(payment.getProviderReference()).isEqualTo("vnpay_ref_123");
        }

        @Test
        @DisplayName("should throw VALIDATION_FAILED when providerReference is blank")
        void blankProviderRef() {
            Payment payment = Fixtures.aPendingPayment();

            assertThatThrownBy(() -> payment.initiate("   "))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.VALIDATION_FAILED);
        }

        @Test
        @DisplayName("should throw INVALID_TRANSITION when not in PENDING")
        void invalidFromNonPending() {
            Payment paid = Fixtures.aPaidPayment();

            assertThatThrownBy(() -> paid.initiate("ref"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_INVALID_STATUS_TRANSITION);
        }
    }

    // ── confirm ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("confirm()")
    class Confirm {

        @Test
        @DisplayName("should transition to PAID and set paidAt")
        void transitionsToPaid() {
            Payment payment = Fixtures.aPendingPayment();
            payment.initiate("ref");
            var before = java.time.Instant.now();

            payment.confirm(null, null);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
            assertThat(payment.getPaidAt()).isAfterOrEqualTo(before);
        }

        @Test
        @DisplayName("should use provided paidAt when given")
        void usesProvidedPaidAt() {
            Payment payment = Fixtures.aPendingPayment();
            payment.initiate("ref");
            var customTime = java.time.Instant.parse("2026-01-01T00:00:00Z");

            payment.confirm(null, customTime);

            assertThat(payment.getPaidAt()).isEqualTo(customTime);
        }

        @Test
        @DisplayName("should throw INVALID_TRANSITION when not in PROCESSING")
        void invalidFromPending() {
            Payment payment = Fixtures.aPendingPayment();

            assertThatThrownBy(() -> payment.confirm(null, null))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_INVALID_STATUS_TRANSITION);
        }
    }

    // ── fail ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("fail()")
    class Fail {

        @Test
        @DisplayName("should transition to FAILED and set failure details")
        void transitionsToFailed() {
            Payment payment = Fixtures.aPendingPayment();
            payment.initiate("ref");

            payment.fail("INSUFFICIENT_FUNDS", "Not enough balance");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(payment.getFailureCode()).isEqualTo("INSUFFICIENT_FUNDS");
            assertThat(payment.getFailureReason()).isEqualTo("Not enough balance");
        }
    }

    // ── cancel ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @Test
        @DisplayName("should transition to CANCELLED when cancelledBy owner")
        void ownerCancels() {
            Payment payment = Fixtures.aPendingPayment();

            payment.cancel(Fixtures.USER_ID);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(payment.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw ACCESS_DENIED when non-owner cancels")
        void nonOwnerDenied() {
            Payment payment = Fixtures.aPendingPayment();

            assertThatThrownBy(() -> payment.cancel(Fixtures.OTHER_USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ACCESS_DENIED);
        }
    }

    // ── requestRefund ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("requestRefund()")
    class RequestRefund {

        @Test
        @DisplayName("should create pending refund and transition to REFUNDING")
        void createsRefund() {
            Payment payment = Fixtures.aPaidPayment();

            Payment.RefundRequestResult result = payment.requestRefund(
                    Fixtures.ORDER_ID, Fixtures.USER_ID,
                    new BigDecimal("30000"), "Product damaged", "1.1.1.1");

            assertThat(result.refund().getStatus()).isEqualTo(RefundStatus.PENDING);
            assertThat(result.refund().getAmount()).isEqualByComparingTo(new BigDecimal("30000"));
            assertThat(result.event()).isNotNull();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDING);
        }

        @Test
        @DisplayName("should throw PAYMENT_NOT_REFUNDABLE when payment not in PAID state")
        void notRefundable() {
            Payment payment = Fixtures.aPendingPayment();

            assertThatThrownBy(() -> payment.requestRefund(
                    Fixtures.ORDER_ID, Fixtures.USER_ID,
                    new BigDecimal("100"), "r", "ip"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_NOT_REFUNDABLE);
        }

        @Test
        @DisplayName("should throw PAYMENT_REFUND_AMOUNT_INVALID when amount exceeds max")
        void amountExceedsMax() {
            Payment payment = Fixtures.aPaidPayment();

            assertThatThrownBy(() -> payment.requestRefund(
                    Fixtures.ORDER_ID, Fixtures.USER_ID,
                    new BigDecimal("99999999"), "r", "ip"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_REFUND_AMOUNT_INVALID);
        }

        @Test
        @DisplayName("should allow partial refund up to max refundable amount")
        void partialRefundAllowed() {
            // Fixtures.aPaidPayment() has refundedAmount=50000, so max refundable = 80000
            Payment payment = Fixtures.aPaidPayment();
            // A second refund of 30000 is within remaining max (80000)
            var result = payment.requestRefund(
                    Fixtures.ORDER_ID, Fixtures.USER_ID,
                    new BigDecimal("30000"), "Partial refund", "1.1.1.1");

            assertThat(result.refund().getStatus()).isEqualTo(RefundStatus.PENDING);
            assertThat(result.refund().getAmount()).isEqualByComparingTo(new BigDecimal("30000"));
        }
    }

    // ── approveRefund ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("approveRefund()")
    class ApproveRefund {

        @Test
        @DisplayName("should approve refund and update refundedAmount")
        void approvesAndUpdatesAmount() {
            Payment payment = Fixtures.aPaidPayment();
            var result = payment.requestRefund(
                    Fixtures.ORDER_ID, Fixtures.USER_ID,
                    new BigDecimal("30000"), "damaged", "1.1.1.1");
            var refundId = result.refund().getId();

            payment.approveRefund(refundId, "stripe_ref_123", null);

            assertThat(result.refund().getStatus()).isEqualTo(RefundStatus.APPROVED);
            assertThat(payment.getRefundedAmount()).isEqualByComparingTo(new BigDecimal("30000"));
        }

        @Test
        @DisplayName("should transition to REFUNDED when fully refunded")
        void transitionsToRefunded() {
            Payment payment = Fixtures.aPaidPayment();
            var result = payment.requestRefund(
                    Fixtures.ORDER_ID, Fixtures.USER_ID,
                    payment.getAmount(), "Full refund", "1.1.1.1");

            payment.approveRefund(result.refund().getId(), "ref", null);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("should throw when refund not found in this payment")
        void refundNotFound() {
            Payment payment = Fixtures.aPaidPayment();

            assertThatThrownBy(() -> payment.approveRefund(UUID.randomUUID(), "ref", null))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_REFUND_NOT_FOUND);
        }
    }

    // ── rejectRefund ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("rejectRefund()")
    class RejectRefund {

        @Test
        @DisplayName("should reject refund and revert to PAID")
        void rejectsAndReverts() {
            Payment payment = Fixtures.aPaidPayment();
            var result = payment.requestRefund(
                    Fixtures.ORDER_ID, Fixtures.USER_ID,
                    new BigDecimal("30000"), "Not eligible", "1.1.1.1");

            payment.rejectRefund(result.refund().getId(), "Not eligible for refund");

            assertThat(result.refund().getStatus()).isEqualTo(RefundStatus.REJECTED);
            assertThat(result.refund().getRejectionReason()).isEqualTo("Not eligible for refund");
        }
    }

    // ── Queries ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Queries")
    class Queries {

        @Test
        @DisplayName("getMaxRefundableAmount returns amount - refundedAmount")
        void maxRefundable() {
            Payment payment = Fixtures.aPartiallyRefundedPayment();
            assertThat(payment.getMaxRefundableAmount())
                    .isEqualByComparingTo(new BigDecimal("80000")); // 130000 - 50000
        }

        @Test
        @DisplayName("isOwnedBy returns true for matching buyerId")
        void isOwnedBy() {
            assertThat(Fixtures.aPaidPayment().isOwnedBy(Fixtures.USER_ID)).isTrue();
            assertThat(Fixtures.aPaidPayment().isOwnedBy(Fixtures.OTHER_USER_ID)).isFalse();
        }

        @Test
        @DisplayName("isFullyRefunded returns true when fully refunded")
        void fullyRefunded() {
            Payment payment = Fixtures.aPaidPayment();
            payment.requestRefund(Fixtures.ORDER_ID, Fixtures.USER_ID,
                    payment.getAmount(), "Full", "ip");
            payment.approveRefund(payment.getRefunds().get(0).getId(), "ref", null);

            assertThat(payment.isFullyRefunded()).isTrue();
        }
    }

    // ── Domain Events ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("Domain Event Factories")
    class DomainEvents {

        @Test
        @DisplayName("toConfirmedEvent contains payment data")
        void confirmedEvent() {
            Payment payment = Fixtures.aPaidPayment();
            var event = payment.toConfirmedEvent();
            assertThat(event.paymentId()).isEqualTo(payment.getId());
            assertThat(event.orderId()).isEqualTo(Fixtures.ORDER_ID);
        }

        @Test
        @DisplayName("toFailedEvent contains failure details")
        void failedEvent() {
            Payment payment = Fixtures.aPendingPayment();
            payment.initiate("ref");
            payment.fail("ERR_CODE", "Failure reason");
            var event = payment.toFailedEvent();
            assertThat(event.failureCode()).isEqualTo("ERR_CODE");
        }

        @Test
        @DisplayName("toCancelledEvent contains cancel info")
        void cancelledEvent() {
            Payment payment = Fixtures.aPendingPayment();
            payment.cancel(Fixtures.USER_ID);
            var event = payment.toCancelledEvent(Fixtures.USER_ID);
            assertThat(event.cancelledBy()).isEqualTo(Fixtures.USER_ID);
        }
    }
}
