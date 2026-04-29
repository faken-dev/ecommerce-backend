package com.ecommerce.auth.infrastructure.scheduler;

import com.ecommerce.auth.domain.repository.OtpTokenRepository;
import com.ecommerce.auth.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;


@Component
@RequiredArgsConstructor
public class ScheduledCleanupService {
    private static final Logger log = LoggerFactory.getLogger(ScheduledCleanupService.class);

    private final OtpTokenRepository otpTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // Run every hour to clean up expired OTP tokens
    @Scheduled(fixedRateString = "PT1H")
    @Transactional
    public void cleanupExpiredOtpTokens() {
        otpTokenRepository.deleteExpiredTokens(Instant.now());
        log.info("Cleaned up expired OTP tokens at {}", Instant.now());
    }

    // Run daily at 2 AM to clean up expired and revoked refresh tokens
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        int deleted = refreshTokenRepository.deleteExpiredAndRevoked(Instant.now());
        log.info("Cleaned up {} expired/revoked refresh tokens at {}", deleted, Instant.now());
    }
}
