package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.LoginCommand;
import com.ecommerce.auth.application.command.SendOtpCommand;
import com.ecommerce.auth.application.dto.LoginResponse;
import com.ecommerce.auth.application.mapper.AuthApplicationMapper;
import com.ecommerce.auth.application.port.CaptchaChallengeService;
import com.ecommerce.auth.application.port.DummyPasswordChecker;
import com.ecommerce.auth.application.port.LoginAttemptTracking;
import com.ecommerce.auth.application.port.PasswordEncoder;
import com.ecommerce.auth.application.service.TokenService;
import com.ecommerce.auth.domain.entity.OtpToken;
import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;


@Service
@RequiredArgsConstructor
public class LoginUseCase {
    private static final Logger log = LoggerFactory.getLogger(LoginUseCase.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DummyPasswordChecker dummyPasswordChecker;
    private final LoginAttemptTracking loginAttemptTracking;
    private final CaptchaChallengeService captchaChallengeService;
    private final TokenService tokenService;
    private final AuthApplicationMapper mapper;
    private final SendOtpUseCase sendOtpUseCase;
    private final TransactionTemplate transactionTemplate;

    private static final Duration BASE_LOCKOUT_DURATION = Duration.ofMinutes(1);

    @Transactional
    public LoginResponse execute(LoginCommand command) {
        var userOpt = userRepository.findByEmail(new Email(command.email()));

        if (userOpt.isEmpty()) {
            dummyPasswordChecker.dummyCheck(command.password());
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        User user = userOpt.get();

        //  Account lockout check -
        if (loginAttemptTracking.isLocked(user.getId())) {
            long remaining = loginAttemptTracking.getRemainingLockoutSeconds(user.getId());
            throw new BusinessException(
                    ErrorCode.AUTH_ACCOUNT_LOCKED,
                    "Account temporarily locked due to failed attempts. Try again in "
                            + remaining + " seconds.");
        }

        // - Captcha check (if enabled)-
        if (captchaChallengeService.isCaptchaRequired(user.getId(), command.ipAddress(), command.email())) {
            // Verify captcha only when it's required
            if (command.captchaToken() == null || command.captchaToken().isBlank()) {
                throw new BusinessException(
                        ErrorCode.AUTH_CAPTCHA_REQUIRED,
                        "Captcha verification required. Please solve the challenge.");
            }
            if (!captchaChallengeService.verifyCaptcha(command.captchaToken(), command.ipAddress())) {
                throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
            }
        }

        // Credential verification - OAuth users have no password - reject email/password login for OAuth accounts
        if (user.isOAuthUser()) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash().value())) {
            loginAttemptTracking.recordFailedAttempt(user.getId(), BASE_LOCKOUT_DURATION);
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        user.ensureActive();

        // Successful login - reset failure counter
        loginAttemptTracking.recordSuccessfulLogin(user.getId());

        // Unverified email → send OTP in SEPARATE transaction before returning error.
        // Using TransactionTemplate to bypass self-invocation proxy bypass so that
        // SendOtpUseCase's own @Transactional boundary is respected.
        if (!user.isEmailVerified()) {
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    sendOtpUseCase.execute(new SendOtpCommand(
                            command.email(),
                            OtpToken.Channel.EMAIL,
                            OtpToken.Purpose.EMAIL_VERIFICATION));
                });
                log.info("OTP re-sent for unverified login: {}", command.email());
            } catch (BusinessException e) {
                log.warn("OTP send failed during unverified login: {}", e.getMessage());
            }
            throw new BusinessException(
                    ErrorCode.AUTH_EMAIL_NOT_VERIFIED,
                    "Please verify your email address. A verification code has been sent.");
        }

        return buildLoginResponse(user, command.deviceInfo(), command.ipAddress());
    }

    LoginResponse buildLoginResponse(User user, String deviceInfo, String ipAddress) {
        var tokens = tokenService.generateTokens(user, deviceInfo, ipAddress);
        return new LoginResponse(mapper.toUserResponse(user), tokens);
    }
}
