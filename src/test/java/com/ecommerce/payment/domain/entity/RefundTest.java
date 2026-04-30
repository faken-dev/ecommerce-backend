package com.ecommerce.payment.domain.entity;

import com.ecommerce.testutil.fixture.Fixtures;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Refund Entity — Domain Tests")
class RefundTest {

    // ── Factory ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create refund with PENDING status")
        void createsCorrectly() {
            Refund refund = Fixtures.aPendingRefund();

            assertThat(refund.getPaymentId()).isEqualTo(Fixtures.PAYMENT_ID);
            assertThat(refund.getOrderId()).isEqualTo(Fixtures.ORDER_ID);
            assertThat(refund.getRequestedBy()).isEqualTo(Fixtures.USER_ID);
            assertThat(refund.getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
            assertThat(refund.getReason()).isEqualTo("Product defective");
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.PENDING);
            assertThat(refund.getIpAddress()).isEqualTo("192.168.1.1");
        }

        @Test
        @DisplayName("should throw when amount is null")
        void nullAmount() {
            assertThatThrownBy(() -> Refund.create(
                    Fixtures.REFUND_ID, Fixtures.PAYMENT_ID, Fixtures.ORDER_ID,
                    Fixtures.USER_ID, null, "reason", "ip"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_REFUND_AMOUNT_INVALID);
        }

        @Test
        @DisplayName("should throw when amount is zero")
        void zeroAmount() {
            assertThatThrownBy(() -> Refund.create(
                    Fixtures.REFUND_ID, Fixtures.PAYMENT_ID, Fixtures.ORDER_ID,
                    Fixtures.USER_ID, BigDecimal.ZERO, "reason", "ip"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_REFUND_AMOUNT_INVALID);
        }

        @Test
        @DisplayName("should throw when amount is negative")
        void negativeAmount() {
            assertThatThrownBy(() -> Refund.create(
                    Fixtures.REFUND_ID, Fixtures.PAYMENT_ID, Fixtures.ORDER_ID,
                    Fixtures.USER_ID, new BigDecimal("-100"), "reason", "ip"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_REFUND_AMOUNT_INVALID);
        }
    }

    // ── Status Transitions ───────────────────────────────────────────────────

    @Nested
    @DisplayName("approve()")
    class Approve {

        @Test
        @DisplayName("should transition to APPROVED from PENDING")
        void approvesFromPending() {
            Refund refund = Fixtures.aPendingRefund();
            var before = java.time.Instant.now();

            refund.approve("stripe_ref_456", null);

            assertThat(refund.getStatus()).isEqualTo(RefundStatus.APPROVED);
            assertThat(refund.getProviderRefundId()).isEqualTo("stripe_ref_456");
            assertThat(refund.getApprovedAt()).isAfterOrEqualTo(before);
        }

        @Test
        @DisplayName("should throw from non-PENDING status")
        void throwsFromNonPending() {
            Refund refund = Fixtures.aPendingRefund();
            refund.approve("ref", null);

            assertThatThrownBy(() -> refund.approve("ref2", null))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_REFUND_INVALID_STATUS_TRANSITION);
        }
    }

    @Nested
    @DisplayName("reject()")
    class Reject {

        @Test
        @DisplayName("should transition to REJECTED and set reason")
        void rejects() {
            Refund refund = Fixtures.aPendingRefund();

            refund.reject("Not eligible");

            assertThat(refund.getStatus()).isEqualTo(RefundStatus.REJECTED);
            assertThat(refund.getRejectionReason()).isEqualTo("Not eligible");
            assertThat(refund.getRejectedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw from non-PENDING status")
        void throwsFromNonPending() {
            Refund refund = Fixtures.aPendingRefund();
            refund.approve("ref", null);

            assertThatThrownBy(() -> refund.reject("reason"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_REFUND_INVALID_STATUS_TRANSITION);
        }
    }

    @Nested
    @DisplayName("complete()")
    class Complete {

        @Test
        @DisplayName("should transition to COMPLETED from APPROVED")
        void completes() {
            Refund refund = Fixtures.aPendingRefund();
            refund.approve("ref", null);

            refund.complete();

            assertThat(refund.getStatus()).isEqualTo(RefundStatus.COMPLETED);
            assertThat(refund.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw from PENDING")
        void throwsFromPending() {
            Refund refund = Fixtures.aPendingRefund();

            assertThatThrownBy(refund::complete)
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_REFUND_INVALID_STATUS_TRANSITION);
        }
    }

    @Nested
    @DisplayName("fail()")
    class Fail {

        @Test
        @DisplayName("should transition to FAILED from APPROVED")
        void fails() {
            Refund refund = Fixtures.aPendingRefund();
            refund.approve("ref", null);

            refund.fail();

            assertThat(refund.getStatus()).isEqualTo(RefundStatus.FAILED);
            assertThat(refund.getFailedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw from PENDING")
        void throwsFromPending() {
            Refund refund = Fixtures.aPendingRefund();

            assertThatThrownBy(refund::fail)
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PAYMENT_REFUND_INVALID_STATUS_TRANSITION);
        }
    }

    // ── Soft Delete ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("softDelete()")
    class SoftDelete {

        @Test
        @DisplayName("should set deletedAt")
        void setsDeletedAt() {
            Refund refund = Fixtures.aPendingRefund();
            refund.softDelete();
            assertThat(refund.getDeletedAt()).isNotNull();
        }
    }

    // ── Domain Events ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Domain Event Factories")
    class DomainEvents {

        @Test
        @DisplayName("toRequestedEvent should contain refund details")
        void requestedEvent() {
            Refund refund = Fixtures.aPendingRefund();
            var event = refund.toRequestedEvent();
            assertThat(event.refundId()).isEqualTo(refund.getId());
            assertThat(event.amount()).isEqualTo(refund.getAmount());
        }

        @Test
        @DisplayName("toApprovedEvent should contain provider refund id")
        void approvedEvent() {
            Refund refund = Fixtures.aPendingRefund();
            refund.approve("stripe-123", null);
            var event = refund.toApprovedEvent();
            assertThat(event.providerRefundId()).isEqualTo("stripe-123");
        }

        @Test
        @DisplayName("toRejectedEvent should contain rejection reason")
        void rejectedEvent() {
            Refund refund = Fixtures.aPendingRefund();
            refund.reject("Not eligible");
            var event = refund.toRejectedEvent();
            assertThat(event.reason()).isEqualTo("Not eligible");
        }
    }
}