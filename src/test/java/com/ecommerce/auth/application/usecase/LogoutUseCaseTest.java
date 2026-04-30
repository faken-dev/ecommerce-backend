package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.port.TokenBlacklistService;
import com.ecommerce.auth.application.port.TokenFamilyTracking;
import com.ecommerce.auth.domain.entity.RefreshToken;
import com.ecommerce.auth.domain.repository.RefreshTokenRepository;
import com.ecommerce.auth.infrastructure.security.TokenHasher;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutUseCase")
class LogoutUseCaseTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private TokenHasher tokenHasher;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private TokenFamilyTracking tokenFamilyTracking;

    private LogoutUseCase sut;

    @BeforeEach
    void setUp() {
        sut = new LogoutUseCase(
                refreshTokenRepository, tokenHasher, tokenBlacklistService, tokenFamilyTracking);
    }

    // ── execute(String, String, long) ─────────────────────────────────────────

    @Nested
    @DisplayName("execute(refreshToken, accessToken, remainingMs)")
    class SingleDeviceLogout {

        @Test
        @DisplayName("should hash token, find and save (revoke) refresh token")
        void revokesRefreshToken() {
            RefreshToken token = Fixtures.aValidRefreshToken();
            when(tokenHasher.hash("raw_refresh_token")).thenReturn("hashed_token");
            when(refreshTokenRepository.findByTokenHash("hashed_token"))
                    .thenReturn(Optional.of(token));

            sut.execute("raw_refresh_token", "access_token_xyz", 3600_000L);

            verify(refreshTokenRepository).save(token);
        }

        @Test
        @DisplayName("should blacklist access token when remainingMs > 0")
        void blacklistsAccessToken() {
            RefreshToken token = Fixtures.aValidRefreshToken();
            when(tokenHasher.hash(any())).thenReturn("h");
            when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

            sut.execute("raw", "access_token_xyz", 3600_000L);

            verify(tokenBlacklistService).blacklist("access_token_xyz", 3600_000L);
        }

        @Test
        @DisplayName("should NOT blacklist when remainingMs == 0")
        void noBlacklistWhenExpired() {
            RefreshToken token = Fixtures.aValidRefreshToken();
            when(tokenHasher.hash(any())).thenReturn("h");
            when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

            sut.execute("raw", "access_token_xyz", 0L);

            verify(tokenBlacklistService, never()).blacklist(any(), anyLong());
        }

        @Test
        @DisplayName("should throw AUTH_REFRESH_TOKEN_NOT_FOUND when token not found")
        void tokenNotFound() {
            when(tokenHasher.hash(any())).thenReturn("unknown_hash");
            when(refreshTokenRepository.findByTokenHash("unknown_hash"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.execute("unknown", "access", 1000))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND);
        }
    }

    // ── executeAllDevices ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("executeAllDevices(userId, accessToken, remainingMs)")
    class AllDevicesLogout {

        @Test
        @DisplayName("should revoke token family in Redis")
        void revokesFamily() {
            UUID userId = Fixtures.USER_ID;

            sut.executeAllDevices(userId, "access_token", 1800_000L);

            verify(tokenFamilyTracking).revokeFamily(userId);
        }

        @Test
        @DisplayName("should revoke all refresh tokens in DB")
        void revokesAllDbTokens() {
            UUID userId = Fixtures.USER_ID;

            sut.executeAllDevices(userId, "access_token", 1800_000L);

            verify(refreshTokenRepository).revokeAllByUserId(userId);
        }

        @Test
        @DisplayName("should blacklist current access token")
        void blacklistsAccessToken() {
            UUID userId = Fixtures.USER_ID;

            sut.executeAllDevices(userId, "access_token", 1800_000L);

            verify(tokenBlacklistService).blacklist("access_token", 1800_000L);
        }
    }
}
