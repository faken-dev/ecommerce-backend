package com.ecommerce.auth.domain.entity;

import com.ecommerce.testutil.fixture.Fixtures;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OtpToken Entity — Domain Tests")
class OtpTokenTest {

    // ── Factory ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create token with correct fields")
        void createsCorrectly() {
            OtpToken token = OtpToken.create(
                    Fixtures.USER_ID,
                    "hash123",
                    OtpToken.Channel.EMAIL,
                    OtpToken.Purpose.EMAIL_VERIFICATION,
                    5
            );

            assertThat(token.getUserId()).isEqualTo(Fixtures.USER_ID);
            assertThat(token.getCodeHash()).isEqualTo("hash123");
            assertThat(token.getChannel()).isEqualTo(OtpToken.Channel.EMAIL);
            assertThat(token.getPurpose()).isEqualTo(OtpToken.Purpose.EMAIL_VERIFICATION);
            assertThat(token.getAttemptCount()).isZero();
            assertThat(token.isUsed()).isFalse();
            assertThat(token.isExpired()).isFalse();
        }

        @Test
        @DisplayName("should set expiry to 5 minutes from now")
        void setsExpiryCorrectly() {
            var before = java.time.Instant.now();
            OtpToken token = OtpToken.create(
                    Fixtures.USER_ID, "hash", OtpToken.Channel.SMS,
                    OtpToken.Purpose.PASSWORD_RESET, 5
            );
            var after = java.time.Instant.now();

            // 5 minutes = 300 seconds
            assertThat(token.getExpiresAt())
                    .isAfter(before.plusSeconds(299))
                    .isBefore(after.plusSeconds(301));
        }
    }

    // ── State Queries ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("State queries")
    class StateQueries {

        @Test
        @DisplayName("isExpired returns true when current time is after expiresAt")
        void expiredTrue() {
            assertThat(Fixtures.anExpiredOtpToken().isExpired()).isTrue();
        }

        @Test
        @DisplayName("isExpired returns false when current time is before expiresAt")
        void expiredFalse() {
            assertThat(Fixtures.anActiveOtpToken().isExpired()).isFalse();
        }

        @Test
        @DisplayName("isUsed returns true when usedAt is set")
        void usedTrue() {
            assertThat(Fixtures.aUsedOtpToken().isUsed()).isTrue();
        }

        @Test
        @DisplayName("isUsed returns false when usedAt is null")
        void usedFalse() {
            assertThat(Fixtures.anActiveOtpToken().isUsed()).isFalse();
        }
    }

    // ── validateForVerification ───────────────────────────────────────────────

    @Nested
    @DisplayName("validateForVerification(int maxAttempts)")
    class ValidateForVerification {

        @Test
        @DisplayName("should pass for valid active token")
        void validTokenPasses() {
            OtpToken token = Fixtures.anActiveOtpToken();
            token.validateForVerification(3); // must not throw
            assertThat(token.getAttemptCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should increment attemptCount on each call")
        void incrementsAttemptCount() {
            OtpToken token = Fixtures.anActiveOtpToken();

            token.validateForVerification(3);
            token.validateForVerification(3);
            token.validateForVerification(3);

            assertThat(token.getAttemptCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("should throw OTP_EXPIRED when token is expired")
        void expiredThrows() {
            assertThatThrownBy(() -> Fixtures.anExpiredOtpToken().validateForVerification(3))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.OTP_EXPIRED);
        }

        @Test
        @DisplayName("should throw OTP_ALREADY_USED when token is already used")
        void usedThrows() {
            assertThatThrownBy(() -> Fixtures.aUsedOtpToken().validateForVerification(3))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.OTP_ALREADY_USED);
        }

        @Test
        @DisplayName("should throw OTP_MAX_ATTEMPTS_EXCEEDED when attemptCount exceeds max")
        void maxAttemptsThrows() {
            OtpToken token = Fixtures.anActiveOtpToken();
            token.validateForVerification(3); // attempt 1: count=1, OK
            token.validateForVerification(3); // attempt 2: count=2, OK
            token.validateForVerification(3); // attempt 3: count=3, 3>3=false, OK
            assertThatThrownBy(() -> token.validateForVerification(3)) // attempt 4 → throws
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
        }

        @Test
        @DisplayName("should not throw when attemptCount == maxAttempts")
        void atMaxAttemptsDoesNotThrow() {
            OtpToken token = Fixtures.anActiveOtpToken();
            token.validateForVerification(3); // attempt 1

            // Now attemptCount=1, max=3. 1 > 3 is false, so should not throw
            token.validateForVerification(3); // attempt 2
            // Now attemptCount=2, max=3. 2 > 3 is false
            token.validateForVerification(3); // attempt 3
            // Now attemptCount=3, max=3. 3 > 3 is false — still OK
            // 4th call would exceed
            assertThat(token.getAttemptCount()).isEqualTo(3);
        }
    }

    // ── markAsUsed ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("markAsUsed()")
    class MarkAsUsed {

        @Test
        @DisplayName("should set usedAt to now")
        void setsUsedAt() {
            OtpToken token = Fixtures.anActiveOtpToken();
            token.markAsUsed();
            assertThat(token.isUsed()).isTrue();
            assertThat(token.getUsedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("incrementFailedAttempt()")
    class IncrementFailedAttempt {

        @Test
        @DisplayName("should increment attemptCount")
        void increments() {
            OtpToken token = Fixtures.anActiveOtpToken();
            token.incrementFailedAttempt();
            assertThat(token.getAttemptCount()).isEqualTo(1);
        }
    }

    // ── Channels & Purposes ──────────────────────────────────────────────────

    @Nested
    @DisplayName("Channels and Purposes")
    class ChannelsAndPurposes {

        @Test
        @DisplayName("should support all Channel values")
        void allChannels() {
            for (OtpToken.Channel ch : OtpToken.Channel.values()) {
                OtpToken token = OtpToken.create(
                        Fixtures.USER_ID, "hash", ch, OtpToken.Purpose.EMAIL_VERIFICATION, 5
                );
                assertThat(token.getChannel()).isEqualTo(ch);
            }
        }

        @Test
        @DisplayName("should support all Purpose values")
        void allPurposes() {
            for (OtpToken.Purpose p : OtpToken.Purpose.values()) {
                OtpToken token = OtpToken.create(
                        Fixtures.USER_ID, "hash", OtpToken.Channel.EMAIL, p, 5
                );
                assertThat(token.getPurpose()).isEqualTo(p);
            }
        }
    }
}
