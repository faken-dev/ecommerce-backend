package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.VerifyOtpCommand;
import com.ecommerce.auth.application.handler.OtpVerificationHandler;
import com.ecommerce.auth.domain.entity.OtpToken;
import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.repository.OtpTokenRepository;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.HashedPassword;
import com.ecommerce.auth.infrastructure.security.OtpHasher;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.testutil.fixture.Fixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerifyOtpUseCase")
class VerifyOtpUseCaseTest {

    @Mock private OtpTokenRepository otpTokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private OtpHasher otpHasher;
    @Mock private EventPublisher eventPublisher;

    private User aUser() {
        return User.builder()
                .id(Fixtures.USER_ID)
                .email(new Email("test@example.com"))
                .passwordHash(HashedPassword.of("hash"))
                .fullName("Test User")
                .active(true)
                .emailVerified(false)
                .phoneVerified(false)
                .otpBlocked(false)
                .roles(Set.of())
                .build();
    }

    /** Creates a mock handler stubbed to support exactly the given purpose. */
    private static OtpVerificationHandler mockHandler(OtpToken.Purpose purpose) {
        OtpVerificationHandler h = mock(OtpVerificationHandler.class);
        lenient().when(h.supports(purpose)).thenReturn(true);
        lenient().when(h.handle(any())).thenReturn(new com.ecommerce.auth.application.dto.VerifyOtpResponse(null, null, "success"));
        return h;
    }

    /** Returns a SUT pre-configured with given handlers and maxAttempts=3. */
    private VerifyOtpUseCaseTestable sut(List<OtpVerificationHandler> handlers) {
        return sut(3, handlers);
    }

    private VerifyOtpUseCaseTestable sut(int maxAttempts, List<OtpVerificationHandler> handlers) {
        VerifyOtpUseCaseTestable s = new VerifyOtpUseCaseTestable(
                handlers, otpTokenRepository, userRepository, otpHasher, eventPublisher);
        s.setMaxAttempts(maxAttempts);
        return s;
    }

    // ── Happy Path ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("execute() — EMAIL_VERIFICATION success")
    class EmailVerificationSuccess {

        @Test
        @DisplayName("should mark OTP as used and verify email")
        void marksOtpAndVerifiesEmail() {
            User user = aUser();
            OtpToken token = Fixtures.anActiveOtpToken();

            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(otpTokenRepository.findLatestActiveByUserIdAndPurpose(any(), any()))
                    .thenReturn(Optional.of(token));
            when(otpHasher.verify(anyString(), any())).thenReturn(true);

            sut(List.of(mockHandler(OtpToken.Purpose.EMAIL_VERIFICATION)))
                    .execute(new VerifyOtpCommand(
                            "test@example.com", "123456", OtpToken.Purpose.EMAIL_VERIFICATION));

            assertThat(token.isUsed()).isTrue();
            assertThat(user.isEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("should save updated OTP and user")
        void savesUpdates() {
            User user = aUser();
            OtpToken token = Fixtures.anActiveOtpToken();

            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(otpTokenRepository.findLatestActiveByUserIdAndPurpose(any(), any()))
                    .thenReturn(Optional.of(token));
            when(otpHasher.verify(anyString(), any())).thenReturn(true);

            sut(List.of(mockHandler(OtpToken.Purpose.EMAIL_VERIFICATION)))
                    .execute(new VerifyOtpCommand(
                            "test@example.com", "123456", OtpToken.Purpose.EMAIL_VERIFICATION));

            verify(otpTokenRepository).save(token);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should publish EmailVerifiedEvent")
        void publishesEvent() {
            User user = aUser();
            OtpToken token = Fixtures.anActiveOtpToken();

            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(otpTokenRepository.findLatestActiveByUserIdAndPurpose(any(), any()))
                    .thenReturn(Optional.of(token));
            when(otpHasher.verify(anyString(), any())).thenReturn(true);

            sut(List.of(mockHandler(OtpToken.Purpose.EMAIL_VERIFICATION)))
                    .execute(new VerifyOtpCommand(
                            "test@example.com", "123456", OtpToken.Purpose.EMAIL_VERIFICATION));

            verify(eventPublisher).publish(any(
                    com.ecommerce.auth.domain.event.EmailVerifiedEvent.class));
        }
    }

    // ── Error Paths ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("execute() — error cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw AUTH_INVALID_CREDENTIALS when user not found")
        void userNotFound() {
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut(List.of(mockHandler(OtpToken.Purpose.EMAIL_VERIFICATION)))
                    .execute(new VerifyOtpCommand(
                            "test@example.com", "123456", OtpToken.Purpose.EMAIL_VERIFICATION)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("should throw OTP_INVALID when no active OTP found")
        void noActiveOtp() {
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(aUser()));
            when(otpTokenRepository.findLatestActiveByUserIdAndPurpose(any(), any()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut(List.of(mockHandler(OtpToken.Purpose.EMAIL_VERIFICATION)))
                    .execute(new VerifyOtpCommand(
                            "test@example.com", "123456", OtpToken.Purpose.EMAIL_VERIFICATION)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.OTP_INVALID);
        }

        @Test
        @DisplayName("should throw OTP_EXPIRED when token is expired")
        void expiredOtp() {
            User user = aUser();
            OtpToken token = Fixtures.anExpiredOtpToken();

            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(otpTokenRepository.findLatestActiveByUserIdAndPurpose(any(), any()))
                    .thenReturn(Optional.of(token));

            assertThatThrownBy(() -> sut(List.of(mockHandler(OtpToken.Purpose.EMAIL_VERIFICATION)))
                    .execute(new VerifyOtpCommand(
                            "test@example.com", "123456", OtpToken.Purpose.EMAIL_VERIFICATION)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.OTP_EXPIRED);
        }

        @Test
        @DisplayName("should throw OTP_ALREADY_USED when token was already used")
        void alreadyUsedOtp() {
            User user = aUser();
            OtpToken token = Fixtures.aUsedOtpToken();

            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(otpTokenRepository.findLatestActiveByUserIdAndPurpose(any(), any()))
                    .thenReturn(Optional.of(token));

            assertThatThrownBy(() -> sut(List.of(mockHandler(OtpToken.Purpose.EMAIL_VERIFICATION)))
                    .execute(new VerifyOtpCommand(
                            "test@example.com", "123456", OtpToken.Purpose.EMAIL_VERIFICATION)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.OTP_ALREADY_USED);
        }

        @Test
        @DisplayName("should throw OTP_INVALID when hash does not match")
        void wrongHash() {
            User user = aUser();
            OtpToken token = Fixtures.anActiveOtpToken();

            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(otpTokenRepository.findLatestActiveByUserIdAndPurpose(any(), any()))
                    .thenReturn(Optional.of(token));
            when(otpHasher.verify(anyString(), any())).thenReturn(false);

            assertThatThrownBy(() -> sut(List.of(mockHandler(OtpToken.Purpose.EMAIL_VERIFICATION)))
                    .execute(new VerifyOtpCommand(
                            "test@example.com", "wrong", OtpToken.Purpose.EMAIL_VERIFICATION)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.OTP_INVALID);

            verify(otpTokenRepository).save(token);
        }

        @Test
        @DisplayName("should throw OTP_MAX_ATTEMPTS_EXCEEDED when attemptCount exceeds max")
        void maxAttemptsExceeded() {
            User user = aUser();
            OtpToken token = OtpToken.builder()
                    .id(UUID.randomUUID())
                    .userId(Fixtures.USER_ID)
                    .codeHash("hash")
                    .channel(OtpToken.Channel.EMAIL)
                    .purpose(OtpToken.Purpose.EMAIL_VERIFICATION)
                    .expiresAt(java.time.Instant.now().plusSeconds(300))
                    .attemptCount(3)
                    .createdAt(java.time.Instant.now())
                    .updatedAt(java.time.Instant.now())
                    .build();

            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(otpTokenRepository.findLatestActiveByUserIdAndPurpose(any(), any()))
                    .thenReturn(Optional.of(token));

            assertThatThrownBy(() -> sut(3, List.of(mockHandler(OtpToken.Purpose.EMAIL_VERIFICATION)))
                    .execute(new VerifyOtpCommand(
                            "test@example.com", "123456", OtpToken.Purpose.EMAIL_VERIFICATION)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
        }

        @Test
        @DisplayName("should throw INTERNAL_SERVER_ERROR when no handler found")
        void noHandlerFound() {
            User user = aUser();
            OtpToken token = Fixtures.anActiveOtpToken();

            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(otpTokenRepository.findLatestActiveByUserIdAndPurpose(any(), any()))
                    .thenReturn(Optional.of(token));
            when(otpHasher.verify(anyString(), any())).thenReturn(true);

            // Empty handlers list → no handler matches → INTERNAL_SERVER_ERROR
            assertThatThrownBy(() -> sut(List.of())
                    .execute(new VerifyOtpCommand(
                            "test@example.com", "123456", OtpToken.Purpose.EMAIL_VERIFICATION)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ── Purpose-specific behaviour ────────────────────────────────────────────

    @Nested
    @DisplayName("processUserUpdates — purpose-specific")
    class PurposeSpecific {

        @Test
        @DisplayName("PHONE_VERIFICATION should verify phone without event")
        void phoneVerification() {
            User user = aUser();
            OtpToken token = Fixtures.anActiveOtpToken();

            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(otpTokenRepository.findLatestActiveByUserIdAndPurpose(any(), any()))
                    .thenReturn(Optional.of(token));
            when(otpHasher.verify(anyString(), any())).thenReturn(true);

            sut(List.of(mockHandler(OtpToken.Purpose.PHONE_VERIFICATION)))
                    .execute(new VerifyOtpCommand(
                            "test@example.com", "123456", OtpToken.Purpose.PHONE_VERIFICATION));

            assertThat(user.isPhoneVerified()).isTrue();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("PASSWORD_RESET should not update user status")
        void passwordResetNoUserUpdate() {
            User user = aUser();
            OtpToken token = Fixtures.anActiveOtpToken();

            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(otpTokenRepository.findLatestActiveByUserIdAndPurpose(any(), any()))
                    .thenReturn(Optional.of(token));
            when(otpHasher.verify(anyString(), any())).thenReturn(true);

            sut(List.of(mockHandler(OtpToken.Purpose.PASSWORD_RESET)))
                    .execute(new VerifyOtpCommand(
                            "test@example.com", "123456", OtpToken.Purpose.PASSWORD_RESET));

            assertThat(user.isEmailVerified()).isFalse();
        }
    }

    private static class VerifyOtpUseCaseTestable extends VerifyOtpUseCase {
        public VerifyOtpUseCaseTestable(List<OtpVerificationHandler> handlers, 
                                        OtpTokenRepository otpTokenRepository, 
                                        UserRepository userRepository, 
                                        OtpHasher otpHasher, 
                                        EventPublisher eventPublisher) {
            super(otpTokenRepository, userRepository, otpHasher, eventPublisher, handlers);
        }

        public void setMaxAttempts(int maxAttempts) {
            try {
                java.lang.reflect.Field field = VerifyOtpUseCase.class.getDeclaredField("maxAttempts");
                field.setAccessible(true);
                field.set(this, maxAttempts);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}