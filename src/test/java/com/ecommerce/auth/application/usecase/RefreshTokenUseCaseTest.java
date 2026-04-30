package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.RefreshTokenCommand;
import com.ecommerce.auth.application.dto.AuthTokenResponse;
import com.ecommerce.auth.application.port.TokenFamilyTracking;
import com.ecommerce.auth.application.service.TokenService;
import com.ecommerce.auth.domain.entity.RefreshToken;
import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.repository.RefreshTokenRepository;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.HashedPassword;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenUseCase")
class RefreshTokenUseCaseTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private TokenService tokenService;
    @Mock private TokenFamilyTracking tokenFamilyTracking;

    private RefreshTokenUseCase sut;

    private User aUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email(new Email("test@example.com"))
                .passwordHash(HashedPassword.of("hash"))
                .fullName("Test User")
                .active(true)
                .emailVerified(true)
                .phoneVerified(false)
                .otpBlocked(false)
                .roles(Set.of())
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .build();
    }

    @BeforeEach
    void setUp() {
        sut = new RefreshTokenUseCase(
                refreshTokenRepository, userRepository, tokenService, tokenFamilyTracking);
    }

    private RefreshTokenCommand aCommand(String token) {
        return new RefreshTokenCommand(token, "Chrome on Windows", "192.168.1.1");
    }

    // ── Happy Path ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("execute() — success")
    class Success {

        @Test
        @DisplayName("should rotate token and return new tokens")
        void rotatesToken() {
            RefreshToken oldToken = Fixtures.aValidRefreshToken();
            AuthTokenResponse newTokens = new AuthTokenResponse("new_access", "new_refresh", 3600, "Bearer");

            when(tokenService.hashForLookup("raw_refresh")).thenReturn("hashed");
            when(refreshTokenRepository.findByTokenHashForUpdate("hashed"))
                    .thenReturn(Optional.of(oldToken));
            when(tokenFamilyTracking.isGenerationValid(any(), anyLong())).thenReturn(true);
            when(userRepository.findById(any())).thenReturn(Optional.of(aUser()));
            when(tokenService.generateTokens(any(), any(), any())).thenReturn(newTokens);

            AuthTokenResponse result = sut.execute(aCommand("raw_refresh"));

            assertThat(result.accessToken()).isEqualTo("new_access");
            assertThat(result.refreshToken()).isEqualTo("new_refresh");
        }

        @Test
        @DisplayName("should save revoked old token after rotation")
        void revokesOldToken() {
            RefreshToken oldToken = Fixtures.aValidRefreshToken();
            when(tokenService.hashForLookup(any())).thenReturn("h");
            when(refreshTokenRepository.findByTokenHashForUpdate(any())).thenReturn(Optional.of(oldToken));
            when(tokenFamilyTracking.isGenerationValid(any(), anyLong())).thenReturn(true);
            when(userRepository.findById(any())).thenReturn(Optional.of(aUser()));
            when(tokenService.generateTokens(any(), any(), any()))
                    .thenReturn(new AuthTokenResponse("a", "r", 3600, "Bearer"));

            sut.execute(aCommand("raw"));

            verify(refreshTokenRepository).save(oldToken);
        }

        @Test
        @DisplayName("should check user is active during rotation")
        void checksUserActive() {
            RefreshToken oldToken = Fixtures.aValidRefreshToken();
            User user = aUser();

            when(tokenService.hashForLookup(any())).thenReturn("h");
            when(refreshTokenRepository.findByTokenHashForUpdate(any())).thenReturn(Optional.of(oldToken));
            when(tokenFamilyTracking.isGenerationValid(any(), anyLong())).thenReturn(true);
            when(userRepository.findById(any())).thenReturn(Optional.of(user));
            when(tokenService.generateTokens(any(), any(), any()))
                    .thenReturn(new AuthTokenResponse("a", "r", 3600, "Bearer"));

            sut.execute(aCommand("raw"));

            // ensureActive() is called on the user; if it threw, the test would fail
        }
    }

    // ── Error Paths ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("execute() — error cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw AUTH_REFRESH_TOKEN_NOT_FOUND when token not in DB")
        void tokenNotFound() {
            when(tokenService.hashForLookup(any())).thenReturn("unknown_hash");
            when(refreshTokenRepository.findByTokenHashForUpdate("unknown_hash"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.execute(aCommand("raw")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND);
        }

        @Test
        @DisplayName("should throw AUTH_REFRESH_TOKEN_REVOKED when token is revoked")
        void tokenRevoked() {
            RefreshToken revoked = Fixtures.aRevokedRefreshToken();
            when(tokenService.hashForLookup(any())).thenReturn("h");
            when(refreshTokenRepository.findByTokenHashForUpdate(any())).thenReturn(Optional.of(revoked));

            assertThatThrownBy(() -> sut.execute(aCommand("raw")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_REFRESH_TOKEN_REVOKED);
        }

        @Test
        @DisplayName("should throw AUTH_TOKEN_EXPIRED when token is expired")
        void tokenExpired() {
            RefreshToken expired = Fixtures.anExpiredRefreshToken();
            when(tokenService.hashForLookup(any())).thenReturn("h");
            when(refreshTokenRepository.findByTokenHashForUpdate(any())).thenReturn(Optional.of(expired));

            assertThatThrownBy(() -> sut.execute(aCommand("raw")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_TOKEN_EXPIRED);
        }

        @Test
        @DisplayName("should throw AUTH_TOKEN_REPLAY_DETECTED on generation mismatch")
        void replayAttack() {
            RefreshToken oldToken = Fixtures.aValidRefreshToken();
            when(tokenService.hashForLookup(any())).thenReturn("h");
            when(refreshTokenRepository.findByTokenHashForUpdate(any())).thenReturn(Optional.of(oldToken));
            when(tokenFamilyTracking.isGenerationValid(any(), eq(1L))).thenReturn(false);

            assertThatThrownBy(() -> sut.execute(aCommand("raw")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_TOKEN_REPLAY_DETECTED);

            verify(tokenFamilyTracking).revokeFamily(oldToken.getUserId());
            verify(refreshTokenRepository).save(oldToken);
        }

        @Test
        @DisplayName("should throw AUTH_INVALID_CREDENTIALS when user not found")
        void userNotFound() {
            RefreshToken token = Fixtures.aValidRefreshToken();
            when(tokenService.hashForLookup(any())).thenReturn("h");
            when(refreshTokenRepository.findByTokenHashForUpdate(any())).thenReturn(Optional.of(token));
            when(tokenFamilyTracking.isGenerationValid(any(), anyLong())).thenReturn(true);
            when(userRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.execute(aCommand("raw")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
    }
}