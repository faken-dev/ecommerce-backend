package com.ecommerce.auth.domain.entity;

import com.ecommerce.testutil.fixture.Fixtures;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

@DisplayName("RefreshToken Entity — Domain Tests")
class RefreshTokenTest {

    // ── Factory ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create token with correct fields")
        void createsCorrectly() {
            RefreshToken token = Fixtures.aValidRefreshToken();

            assertThat(token.getUserId()).isEqualTo(Fixtures.USER_ID);
            assertThat(token.getTokenHash()).isEqualTo("hashed_token");
            assertThat(token.getDeviceInfo()).isEqualTo("Chrome on Windows");
            assertThat(token.getIpAddress()).isEqualTo("192.168.1.1");
            assertThat(token.getGeneration()).isEqualTo(1);
            assertThat(token.isRevoked()).isFalse();
            assertThat(token.isExpired()).isFalse();
        }

        @Test
        @DisplayName("should set expiresAt correctly")
        void setsExpiry() {
            var before = java.time.Instant.now();
            RefreshToken token = Fixtures.aValidRefreshToken();
            var after = java.time.Instant.now();

            // 7 days in milliseconds
            assertThat(token.getExpiresAt())
                    .isAfter(before.plusMillis(7 * 24 * 3600 * 1000L - 1000))
                    .isBefore(after.plusMillis(7 * 24 * 3600 * 1000L + 1000));
        }
    }

    // ── State Queries ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("State queries")
    class StateQueries {

        @Test
        @DisplayName("isExpired returns true for expired token")
        void expiredTrue() {
            assertThat(Fixtures.anExpiredRefreshToken().isExpired()).isTrue();
        }

        @Test
        @DisplayName("isExpired returns false for valid token")
        void expiredFalse() {
            assertThat(Fixtures.aValidRefreshToken().isExpired()).isFalse();
        }

        @Test
        @DisplayName("isRevoked returns true for revoked token")
        void revokedTrue() {
            assertThat(Fixtures.aRevokedRefreshToken().isRevoked()).isTrue();
        }

        @Test
        @DisplayName("isRevoked returns false for active token")
        void revokedFalse() {
            assertThat(Fixtures.aValidRefreshToken().isRevoked()).isFalse();
        }

        @Test
        @DisplayName("isValid returns true only when not revoked and not expired")
        void isValid() {
            assertThat(Fixtures.aValidRefreshToken().isValid()).isTrue();
            assertThat(Fixtures.anExpiredRefreshToken().isValid()).isFalse();
            assertThat(Fixtures.aRevokedRefreshToken().isValid()).isFalse();
        }
    }

    // ── ensureValid ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ensureValid()")
    class EnsureValid {

        @Test
        @DisplayName("should pass for valid token")
        void validPasses() {
            Fixtures.aValidRefreshToken().ensureValid();
        }

        @Test
        @DisplayName("should throw AUTH_REFRESH_TOKEN_REVOKED when revoked")
        void revokedThrows() {
            assertThatThrownBy(Fixtures.aRevokedRefreshToken()::ensureValid)
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_REFRESH_TOKEN_REVOKED);
        }

        @Test
        @DisplayName("should throw AUTH_TOKEN_EXPIRED when expired")
        void expiredThrows() {
            assertThatThrownBy(Fixtures.anExpiredRefreshToken()::ensureValid)
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_TOKEN_EXPIRED);
        }
    }

    // ── revoke ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("revoke()")
    class Revoke {

        @Test
        @DisplayName("revoke() without params should set revokedAt and setUpdatedBy to SYSTEM_ACTOR")
        void revokeWithoutParams() {
            RefreshToken token = Fixtures.aValidRefreshToken();

            token.revoke();

            assertThat(token.isRevoked()).isTrue();
            assertThat(token.getRevokedAt()).isNotNull();
            assertThat(token.getUpdatedBy()).isEqualTo(RefreshToken.SYSTEM_ACTOR);
        }

        @Test
        @DisplayName("revoke(UUID) should set replacedBy token id")
        void revokeWithReplacedBy() {
            RefreshToken token = Fixtures.aValidRefreshToken();
            UUID replacedBy = UUID.randomUUID();

            token.revoke(replacedBy);

            assertThat(token.isRevoked()).isTrue();
            assertThat(token.getUpdatedBy()).isEqualTo(replacedBy);
        }

        @Test
        @DisplayName("revoke() should be idempotent")
        void idempotent() {
            RefreshToken token = Fixtures.aValidRefreshToken();

            token.revoke();
            java.time.Instant firstRevokedAt = token.getRevokedAt();

            token.revoke();
            java.time.Instant secondRevokedAt = token.getRevokedAt();

            // Both should return same time (idempotent)
            assertThat(firstRevokedAt).isEqualTo(secondRevokedAt);
        }
    }

    // ── Equality ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Equals / HashCode")
    class Equality {

        @Test
        @DisplayName("tokens with same ID are equal")
        void sameIdEqual() {
            UUID id =  UUID.randomUUID();
            RefreshToken t1 = RefreshToken.builder()
                    .id(id)
                    .userId(Fixtures.USER_ID)
                    .tokenHash("hash")
                    .deviceInfo("d")
                    .ipAddress("ip")
                    .expiresAt(java.time.Instant.now().plusSeconds(86400))
                    .generation(1)
                    .createdAt(java.time.Instant.now())
                    .updatedAt(java.time.Instant.now())
                    .build();
            RefreshToken t2 = RefreshToken.builder()
                    .id(id)
                    .userId(Fixtures.USER_ID)
                    .tokenHash("hash")
                    .deviceInfo("d")
                    .ipAddress("ip")
                    .expiresAt(java.time.Instant.now().plusSeconds(86400))
                    .generation(1)
                    .createdAt(java.time.Instant.now())
                    .updatedAt(java.time.Instant.now())
                    .build();

            assertThat(t1).isEqualTo(t2);
            assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
        }
    }
}