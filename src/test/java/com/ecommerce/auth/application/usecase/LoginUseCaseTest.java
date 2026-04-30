package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.LoginCommand;
import com.ecommerce.auth.application.dto.AuthTokenResponse;
import com.ecommerce.auth.application.dto.LoginResponse;
import com.ecommerce.auth.application.dto.UserResponse;
import com.ecommerce.auth.application.mapper.AuthApplicationMapper;
import com.ecommerce.auth.application.port.CaptchaChallengeService;
import com.ecommerce.auth.application.port.DummyPasswordChecker;
import com.ecommerce.auth.application.port.LoginAttemptTracking;
import com.ecommerce.auth.application.port.PasswordEncoder;
import com.ecommerce.auth.application.service.TokenService;
import com.ecommerce.auth.domain.entity.User;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCase")
class LoginUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private DummyPasswordChecker dummyPasswordChecker;
    @Mock private LoginAttemptTracking loginAttemptTracking;
    @Mock private CaptchaChallengeService captchaChallengeService;
    @Mock private TokenService tokenService;
    @Mock private AuthApplicationMapper mapper;
    @Mock private SendOtpUseCase sendOtpUseCase;
    @Mock private TransactionTemplate transactionTemplate;

    private LoginUseCase sut;

    private User activeUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email(new Email("test@example.com"))
                .passwordHash(HashedPassword.of("hashed"))
                .fullName("Test User")
                .active(true)
                .emailVerified(true)
                .phoneVerified(false)
                .otpBlocked(false)
                .roles(Set.of())
                .build();
    }

    // LoginCommand record: email, password, deviceInfo, ipAddress, captchaToken
    private LoginCommand aCommand() {
        return new LoginCommand(
                "test@example.com",
                "password123",
                null,         // deviceInfo
                "127.0.0.1",  // ipAddress
                null          // captchaToken
        );
    }

    @BeforeEach
    void setUp() {
        sut = new LoginUseCase(
                userRepository, passwordEncoder, dummyPasswordChecker,
                loginAttemptTracking, captchaChallengeService,
                tokenService, mapper, sendOtpUseCase, transactionTemplate);
    }

    // ── Happy Path ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("execute() — success")
    class Success {

        @BeforeEach
        void arrange() {
            when(userRepository.findByEmail(any(Email.class)))
                    .thenReturn(Optional.of(activeUser()));
            when(loginAttemptTracking.isLocked(any(UUID.class))).thenReturn(false);
            when(captchaChallengeService.isCaptchaRequired(any(), any(), any())).thenReturn(false);
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        }

        @Test
        @DisplayName("should return LoginResponse with tokens on successful login")
        void returnsTokens() {
            AuthTokenResponse tokens = AuthTokenResponse.of("access_token", "refresh_token", 3600);
            UserResponse userResp = new UserResponse(UUID.randomUUID(), "test@example.com",
                    "Test User", null, true, false, Set.of(), Set.of(), null);

            when(tokenService.generateTokens(any(), any(), any())).thenReturn(tokens);
            when(mapper.toUserResponse(any())).thenReturn(userResp);

            LoginResponse response = sut.execute(aCommand());

            assertThat(response).isNotNull();
            assertThat(response.tokens().accessToken()).isEqualTo("access_token");
            assertThat(response.tokens().refreshToken()).isEqualTo("refresh_token");
        }

        @Test
        @DisplayName("should reset failure counter on successful login")
        void resetsFailureCounter() {
            AuthTokenResponse tokens = AuthTokenResponse.of("a", "r", 3600);
            UserResponse userResp = new UserResponse(UUID.randomUUID(), "e", "n", null, true, false, Set.of(), Set.of(), null);

            when(tokenService.generateTokens(any(), any(), any())).thenReturn(tokens);
            when(mapper.toUserResponse(any())).thenReturn(userResp);

            sut.execute(aCommand());

            verify(loginAttemptTracking).recordSuccessfulLogin(any(UUID.class));
        }
    }

    // ── Error Paths ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("execute() — error cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw AUTH_INVALID_CREDENTIALS when user not found")
        void userNotFound() {
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.execute(aCommand()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_INVALID_CREDENTIALS);

            verify(dummyPasswordChecker).dummyCheck(anyString());
        }

        @Test
        @DisplayName("should throw AUTH_ACCOUNT_LOCKED when account is locked")
        void accountLocked() {
            User user = activeUser();
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(loginAttemptTracking.isLocked(any(UUID.class))).thenReturn(true);
            when(loginAttemptTracking.getRemainingLockoutSeconds(any(UUID.class))).thenReturn(30L);

            assertThatThrownBy(() -> sut.execute(aCommand()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_ACCOUNT_LOCKED);
        }

        @Test
        @DisplayName("should throw AUTH_CAPTCHA_REQUIRED when captcha is required but missing")
        void captchaRequired() {
            when(userRepository.findByEmail(any(Email.class)))
                    .thenReturn(Optional.of(activeUser()));
            when(loginAttemptTracking.isLocked(any())).thenReturn(false);
            when(captchaChallengeService.isCaptchaRequired(any(), any(), any())).thenReturn(true);

            assertThatThrownBy(() -> sut.execute(aCommand()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_CAPTCHA_REQUIRED);
        }

        @Test
        @DisplayName("should throw AUTH_INVALID_CREDENTIALS when wrong password")
        void wrongPassword() {
            when(userRepository.findByEmail(any(Email.class)))
                    .thenReturn(Optional.of(activeUser()));
            when(loginAttemptTracking.isLocked(any())).thenReturn(false);
            when(captchaChallengeService.isCaptchaRequired(any(), any(), any())).thenReturn(false);
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            assertThatThrownBy(() -> sut.execute(aCommand()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_INVALID_CREDENTIALS);

            verify(loginAttemptTracking).recordFailedAttempt(any(), any());
        }

        @Test
        @DisplayName("should throw AUTH_INVALID_CREDENTIALS for OAuth user attempting password login")
        void oauthUserLoginFails() {
            User oauthUser = Fixtures.anOAuthUser();

            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(oauthUser));
            when(loginAttemptTracking.isLocked(any())).thenReturn(false);
            when(captchaChallengeService.isCaptchaRequired(any(), any(), any())).thenReturn(false);

            assertThatThrownBy(() -> sut.execute(aCommand()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
    }
}