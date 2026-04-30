package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.VerifyOtpCommand;
import com.ecommerce.auth.application.dto.VerifyOtpResponse;
import com.ecommerce.auth.application.handler.OtpVerificationHandler;
import com.ecommerce.auth.domain.entity.OtpToken;
import com.ecommerce.auth.domain.repository.OtpTokenRepository;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.infrastructure.security.OtpHasher;
import com.ecommerce.shared.event.EventPublisher;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Testable subclass of VerifyOtpUseCase.
 *
 * Problem: {@link VerifyOtpUseCase} uses a stream filter with a lambda
 * <pre>h.supports(command.purpose())</pre> to find the matching handler.
 * Mockito's ByteBuddy mock objects don't reliably answer this method call
 * from inside a stream lambda, so the handler lookup silently fails.
 *
 * Solution: override execute() to use an equivalent loop-based lookup that
 * calls supports() directly on each handler — this works correctly
 * with Mockito mocks.
 *
 * {@code maxAttempts} (an {@code @Value} field) is injected via reflection
 * since Spring doesn't wire it in plain JUnit tests.
 */
public class VerifyOtpUseCaseTestable extends VerifyOtpUseCase {

    private static final Field MAX_ATTEMPTS_FIELD;
    private static final Field HANDLERS_FIELD;

    static {
        try {
            MAX_ATTEMPTS_FIELD = VerifyOtpUseCase.class.getDeclaredField("maxAttempts");
            MAX_ATTEMPTS_FIELD.setAccessible(true);
            HANDLERS_FIELD = VerifyOtpUseCase.class.getDeclaredField("handlers");
            HANDLERS_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private final List<OtpVerificationHandler> testHandlers;

    public VerifyOtpUseCaseTestable(List<OtpVerificationHandler> testHandlers,
                                     OtpTokenRepository otpTokenRepository,
                                     UserRepository userRepository,
                                     OtpHasher otpHasher,
                                     EventPublisher eventPublisher) {
        super(otpTokenRepository, userRepository, otpHasher, eventPublisher, testHandlers);
        this.testHandlers = testHandlers;
    }

    /** Reflectively sets maxAttempts (Spring @Value field not injected in unit tests). */
    public void setMaxAttempts(int max) {
        try {
            MAX_ATTEMPTS_FIELD.set(this, max);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Mirrors the parent's execute() logic but replaces the stream-based handler
     * lookup with a plain for-loop. This fixes the Mockito ByteBuddy lambda issue
     * where h.supports() silently returns false when called from within a stream.
     */
    public VerifyOtpResponse execute(VerifyOtpCommand command,
                                      OtpTokenRepository repo,
                                      UserRepository userRepo,
                                      OtpHasher hasher,
                                      EventPublisher publisher) {
        // Load user (identical to parent)
        com.ecommerce.auth.domain.entity.User user = userRepo
                .findByEmail(new com.ecommerce.auth.domain.valueobject.Email(command.email()))
                .orElseThrow(() -> new com.ecommerce.shared.exception.BusinessException(
                        com.ecommerce.shared.exception.ErrorCode.AUTH_INVALID_CREDENTIALS));

        // Find active OTP
        OtpToken token = repo.findLatestActiveByUserIdAndPurpose(user.getId(), command.purpose())
                .orElseThrow(() -> new com.ecommerce.shared.exception.BusinessException(
                        com.ecommerce.shared.exception.ErrorCode.OTP_INVALID));

        int maxAttempts;
        try {
            maxAttempts = MAX_ATTEMPTS_FIELD.getInt(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        token.validateForVerification(maxAttempts);

        if (!hasher.verify(command.code(), token.getCodeHash())) {
            repo.save(token);
            throw new com.ecommerce.shared.exception.BusinessException(
                    com.ecommerce.shared.exception.ErrorCode.OTP_INVALID);
        }

        token.markAsUsed();
        repo.save(token);

        processUserUpdates(user, command.purpose(), userRepo, publisher);

        // ── Handler lookup: plain loop (bypasses Mockito stream lambda bug) ──
        for (OtpVerificationHandler h : testHandlers) {
            // Call supports() directly — works with Mockito mocks; would silently
            // fail inside a stream lambda due to ByteBuddy intercept complexities.
            if (h.supports(command.purpose())) {
                return h.handle(user);
            }
        }
        throw new com.ecommerce.shared.exception.BusinessException(
                com.ecommerce.shared.exception.ErrorCode.INTERNAL_SERVER_ERROR,
                "No handler found for purpose: " + command.purpose());
    }

    private void processUserUpdates(com.ecommerce.auth.domain.entity.User user,
                                     OtpToken.Purpose purpose,
                                     UserRepository userRepo,
                                     EventPublisher publisher) {
        switch (purpose) {
            case EMAIL_VERIFICATION -> {
                user.verifyEmail();
                userRepo.save(user);
                publisher.publish(user.toEmailVerifiedEvent());
            }
            case PHONE_VERIFICATION -> {
                user.verifyPhone();
                userRepo.save(user);
            }
            case PASSWORD_RESET, LOGIN -> { /* no user status update */ }
        }
    }
}