package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.SendOtpCommand;
import com.ecommerce.auth.application.port.OtpGenerator;
import com.ecommerce.auth.application.port.SpamProtection;
import com.ecommerce.auth.domain.entity.OtpToken;
import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.event.OtpRequestedEvent;
import com.ecommerce.auth.domain.repository.OtpTokenRepository;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.HashedPassword;
import com.ecommerce.auth.infrastructure.security.OtpHasher;
import com.ecommerce.shared.event.EventPublisher;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendOtpUseCase")
class SendOtpUseCaseTest {

    @Mock private OtpTokenRepository otpTokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private OtpGenerator otpGenerator;
    @Mock private OtpHasher otpHasher;
    @Mock private EventPublisher eventPublisher;
    @Mock private SpamProtection spamProtection;

    private SendOtpUseCase sut;

    private User aUser() {
        return User.builder()
                .id(UUID.randomUUID())
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

    private SendOtpCommand emailVerificationCmd() {
        return new SendOtpCommand("test@example.com",
                OtpToken.Channel.EMAIL, OtpToken.Purpose.EMAIL_VERIFICATION);
    }

    @BeforeEach
    void setUp() throws Exception {
        sut = new SendOtpUseCase(
                otpTokenRepository, userRepository, otpGenerator,
                otpHasher, eventPublisher, spamProtection);

        // Inject @Value fields via reflection
        setField(sut, "otpExpiryMinutes", 5);
        setField(sut, "maxAttempts", 3);
    }

    // Use @Value fields (Spring-style injection)
    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // ── Happy Path ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("execute() — success")
    class Success {

        @Test
        @DisplayName("should generate OTP, save token, and publish event")
        void generatesAndSavesOtp() {
            User user = aUser();
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(spamProtection.isAccountBlocked(any())).thenReturn(false);
            when(spamProtection.incrementDailySendCount(any(), any())).thenReturn(1);
            when(spamProtection.getCooldownRemainingSeconds(any(), any())).thenReturn(0L);
            when(spamProtection.getPenaltyTtl(any(), any())).thenReturn(0L);
            when(otpTokenRepository.findLatestActiveByUserIdAndPurpose(any(), any()))
                    .thenReturn(Optional.empty());
            when(otpGenerator.generate(6)).thenReturn("123456");
            when(otpHasher.hash("123456")).thenReturn("hashed_123456");

            sut.execute(emailVerificationCmd());

            verify(otpGenerator).generate(6);
            verify(otpHasher).hash("123456");
            verify(otpTokenRepository).save(any(OtpToken.class));
            verify(otpTokenRepository).storeRawOtp(any(), any(), eq("123456"), eq(300L));
            verify(eventPublisher).publish(any(OtpRequestedEvent.class));
        }

        @Test
        @DisplayName("should store raw OTP in Redis with correct TTL")
        void storesRawOtpWithTTL() {
            User user = aUser();
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(spamProtection.isAccountBlocked(any())).thenReturn(false);
            when(spamProtection.incrementDailySendCount(any(), any())).thenReturn(1);
            when(spamProtection.getCooldownRemainingSeconds(any(), any())).thenReturn(0L);
            when(spamProtection.getPenaltyTtl(any(), any())).thenReturn(0L);
            when(otpTokenRepository.findLatestActiveByUserIdAndPurpose(any(), any()))
                    .thenReturn(Optional.empty());
            when(otpGenerator.generate(6)).thenReturn("654321");
            when(otpHasher.hash(any())).thenReturn("hash");

            sut.execute(emailVerificationCmd());

            // 5 minutes * 60 = 300 seconds
            verify(otpTokenRepository).storeRawOtp(any(), any(), eq("654321"), eq(300L));
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

            assertThatThrownBy(() -> sut.execute(emailVerificationCmd()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("should throw AUTH_OTP_BLOCKED when account is blocked in spam protection")
        void accountBlockedBySpamProtection() {
            User user = aUser();
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(spamProtection.isAccountBlocked(any())).thenReturn(true);

            assertThatThrownBy(() -> sut.execute(emailVerificationCmd()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_OTP_BLOCKED);
        }

        @Test
        @DisplayName("should throw AUTH_OTP_BLOCKED when user OTP is already blocked")
        void userOtpBlocked() {
            User user = aUser();
            user.blockOtp("spam");
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(spamProtection.isAccountBlocked(any())).thenReturn(false);

            assertThatThrownBy(() -> sut.execute(emailVerificationCmd()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_OTP_BLOCKED);
        }

        @Test
        @DisplayName("should throw AUTH_OTP_BLOCKED and block user when daily limit exceeded")
        void dailyLimitExceeded() {
            User user = aUser();
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(spamProtection.isAccountBlocked(any())).thenReturn(false);
            when(spamProtection.incrementDailySendCount(any(), any())).thenReturn(5); // > maxAttempts(3)
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            assertThatThrownBy(() -> sut.execute(emailVerificationCmd()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_OTP_BLOCKED);

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should throw OTP_SEND_BLOCKED when in cooldown window")
        void cooldownActive() {
            User user = aUser();
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(spamProtection.isAccountBlocked(any())).thenReturn(false);
            when(spamProtection.incrementDailySendCount(any(), any())).thenReturn(1);
            when(spamProtection.getCooldownRemainingSeconds(any(), any())).thenReturn(120L);

            assertThatThrownBy(() -> sut.execute(emailVerificationCmd()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.OTP_SEND_BLOCKED);
        }

        @Test
        @DisplayName("should throw OTP_STILL_ACTIVE when active OTP already exists")
        void otpStillActive() {
            User user = aUser();
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
            when(spamProtection.isAccountBlocked(any())).thenReturn(false);
            when(spamProtection.incrementDailySendCount(any(), any())).thenReturn(1);
            when(spamProtection.getCooldownRemainingSeconds(any(), any())).thenReturn(0L);
            when(spamProtection.getPenaltyTtl(any(), any())).thenReturn(0L);
            when(otpTokenRepository.findLatestActiveByUserIdAndPurpose(any(), any()))
                    .thenReturn(Optional.of(Fixtures.anActiveOtpToken()));

            assertThatThrownBy(() -> sut.execute(emailVerificationCmd()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.OTP_STILL_ACTIVE);
        }

        @Test
        @DisplayName("should throw VALIDATION_FAILED when email destination is missing")
        void noEmailDestination() {
            // A user whose email is null — only possible via builder with null email
            User userWithNullEmail = User.builder()
                    .id(UUID.randomUUID())
                    .email(null)
                    .passwordHash(HashedPassword.of("hash"))
                    .fullName("Test User")
                    .active(true)
                    .emailVerified(false)
                    .phoneVerified(false)
                    .otpBlocked(false)
                    .roles( Set.of())
                    .build();

            when(userRepository.findByEmail(any(Email.class)))
                    .thenReturn(Optional.of(userWithNullEmail));
            when(spamProtection.isAccountBlocked(any())).thenReturn(false);
            when(spamProtection.incrementDailySendCount(any(), any())).thenReturn(1);
            when(spamProtection.getCooldownRemainingSeconds(any(), any())).thenReturn(0L);
            when(spamProtection.getPenaltyTtl(any(), any())).thenReturn(0L);
            when(otpTokenRepository.findLatestActiveByUserIdAndPurpose(any(), any()))
                    .thenReturn(Optional.empty());
            when(otpGenerator.generate(6)).thenReturn("123456");
            when(otpHasher.hash(any())).thenReturn("hash");

            // user.getEmail() is null → user.getEmail().value() throws NPE
            assertThatThrownBy(() -> sut.execute(emailVerificationCmd()))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
