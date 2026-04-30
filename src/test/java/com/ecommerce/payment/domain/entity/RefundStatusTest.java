package com.ecommerce.payment.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RefundStatus — State Machine Tests")
class RefundStatusTest {

    @Nested
    @DisplayName("canTransitionTo()")
    class CanTransitionTo {

        @Test
        @DisplayName("PENDING can go to APPROVED or REJECTED")
        void pendingTransitions() {
            assertThat(RefundStatus.PENDING.canTransitionTo(RefundStatus.APPROVED)).isTrue();
            assertThat(RefundStatus.PENDING.canTransitionTo(RefundStatus.REJECTED)).isTrue();
        }

        @Test
        @DisplayName("PENDING cannot go to COMPLETED or FAILED directly")
        void pendingCannotSkip() {
            assertThat(RefundStatus.PENDING.canTransitionTo(RefundStatus.COMPLETED)).isFalse();
            assertThat(RefundStatus.PENDING.canTransitionTo(RefundStatus.FAILED)).isFalse();
        }

        @Test
        @DisplayName("APPROVED can go to COMPLETED or FAILED")
        void approvedTransitions() {
            assertThat(RefundStatus.APPROVED.canTransitionTo(RefundStatus.COMPLETED)).isTrue();
            assertThat(RefundStatus.APPROVED.canTransitionTo(RefundStatus.FAILED)).isTrue();
        }

        @Test
        @DisplayName("REJECTED, COMPLETED, FAILED are terminal — no transitions allowed")
        void terminalStates() {
            for (RefundStatus target : RefundStatus.values()) {
                assertThat(RefundStatus.REJECTED.canTransitionTo(target)).isFalse();
                assertThat(RefundStatus.COMPLETED.canTransitionTo(target)).isFalse();
                assertThat(RefundStatus.FAILED.canTransitionTo(target)).isFalse();
            }
        }

        @Test
        @DisplayName("same-to-same transition always returns false")
        void noSelfTransition() {
            for (RefundStatus s : RefundStatus.values()) {
                assertThat(s.canTransitionTo(s)).isFalse();
            }
        }
    }
}