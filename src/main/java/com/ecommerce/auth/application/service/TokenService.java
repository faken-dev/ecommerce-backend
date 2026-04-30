package com.ecommerce.auth.application.service;

import com.ecommerce.auth.application.dto.AuthTokenResponse;
import com.ecommerce.auth.application.port.TokenBlacklistService;
import com.ecommerce.auth.application.port.TokenFamilyTracking;
import com.ecommerce.auth.domain.entity.RefreshToken;
import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.event.LoginCompletedEvent;
import com.ecommerce.auth.domain.repository.RefreshTokenRepository;
import com.ecommerce.auth.infrastructure.security.JwtTokenProvider;
import com.ecommerce.auth.infrastructure.security.TokenHasher;
import com.ecommerce.shared.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Centralized token generation service.
 * Consolidates the token generation logic that was previously duplicated across
 * LoginUseCase, AuthTokenOtpHandler, and EmailVerifiedHandler.
 *
 * <p>Redis and DB writes are wrapped in a Redis transaction (MULTI/EXEC) so that
 * BOTH either succeed or BOTH rollback - eliminating the orphaned Redis entry
 * that the previous try/catch approach could not fully guarantee.
 */

@Service
@RequiredArgsConstructor
public class TokenService {
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenHasher tokenHasher;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final TokenFamilyTracking tokenFamilyTracking;
    private final EventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;  // for transactional Redis ops

    @Value("${app.jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    /**
     * Generates access token + refresh token, persists refresh token to DB,
     * and publishes an audit event. Returns tokens directly to the caller.
     *
     * <p>Redis and DB writes are atomic: if either fails, neither is committed.
     * We use a Redis WATCH on the family key as an optimistic lock - if another
     * request advances the family between our read and write, the transaction
     * aborts and we retry (up to 3 times).
     *
     * @param user       the authenticated user
     * @param deviceInfo User-Agent or device description for audit
     * @param ipAddress  client IP for audit
     * @return auth token response containing access + raw refresh token
     */
    @Transactional
    public AuthTokenResponse generateTokens(User user, String deviceInfo, String ipAddress) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String rawRefreshToken = jwtTokenProvider.generateRawRefreshToken();
        String tokenHash = tokenHasher.hash(rawRefreshToken);

        // Advance token family generation; first token gets generation 1
        long newGeneration = tokenFamilyTracking.getCurrentGeneration(user.getId()) + 1;

        RefreshToken refreshToken = RefreshToken.create(
                user.getId(), tokenHash, deviceInfo, ipAddress, refreshExpirationMs, newGeneration);

        // - Atomic Redis + DB write via Redis MULTI/EXEC -----------
        // If DB save throws, the EXEC is never called → Redis side-effect rolls back.
        // If Redis WATCH fails (concurrent modification), we retry up to 3.
        executeAtomicTokenCreation(user.getId(), newGeneration, refreshToken);

        eventPublisher.publish(new LoginCompletedEvent(user.getId(), Instant.now()));
        log.debug("Tokens generated for userId={}, generation={}", user.getId(), newGeneration);

        return AuthTokenResponse.of(accessToken, rawRefreshToken, accessExpirationMs / 1000);
    }

    /**
     * Executes the Redis generation advance and DB refresh-token save inside a
     * Redis transaction, so that DB rollback automatically aborts the Redis write.
     *
     * <p>Uses WATCH as an optimistic lock on the family key. If another thread
     * modifies the key between our WATCH and EXEC, Redis aborts the transaction
     * (exec() returns null) and we retry with a fresh WATCH - up to 3 times.
     *
     * @param userId       user's UUID
     * @param generation   the new generation number
     * @param refreshToken the domain entity to persist in DB
     */
    private void executeAtomicTokenCreation(UUID userId, long generation, RefreshToken refreshToken) {
        final String familyKey = "token:family:" + userId;
        final long ttlSeconds = Duration.ofDays(7).toSeconds();

        for (int attempt = 1; attempt <= 3; attempt++) {
            RedisCallback<List<?>> callback = connection -> {
                connection.watch(familyKey.getBytes(StandardCharsets.UTF_8));

                connection.multi();
                setExInConnection(connection, familyKey, ttlSeconds, generation);
                // exec() returns null if WATCH was violated, List<?> otherwise
                return connection.exec();
            };

            List<?> result = redisTemplate.execute(callback);

            if (result != null) {
                // Redis WATCH/MULTI succeeded - proceed to DB save.
                // If DB fails, @Transactional rolls back the entire method.
                refreshTokenRepository.save(refreshToken);
                return; // success
            }

            // result == null → WATCH conflict → another thread modified the key → retry
            log.warn("Token family concurrent modification, retry {}/3 for userId={}",
                    attempt, userId);
            if (attempt == 3) {
                throw new RuntimeException(
                        "Token generation failed after 3 retries due to concurrent modification");
            }
        }
    }

    /**
     * Executes SETEX on a raw Redis connection inside a {@link RedisCallback}.
     *
     * <p>The {@code @SuppressWarnings("deprecation")} is intentional and necessary:
     * the byte-array overload of {@code setEx} on the low-level
     * {@link org.springframework.data.redis.connection.RedisConnection} interface
     * is the only option available inside a {@code RedisCallback}. Spring Data Redis
     * 3.x deprecated this overload in favour of String-based methods, but those
     * require {@code ValueOperations} which is not accessible from this scope.
     */
    @SuppressWarnings("deprecation")
    private void setExInConnection(
            RedisConnection connection,
            String key, long ttlSeconds, long generation) {
        connection.setEx(
                key.getBytes(StandardCharsets.UTF_8),
                ttlSeconds,
                String.valueOf(generation).getBytes(StandardCharsets.UTF_8));
    }

    /** Shortcut to blacklist an access token via the injected adapter. */
    public void blacklistAccessToken(String accessToken, long ttlMs) {
        tokenBlacklistService.blacklist(accessToken, ttlMs);
    }

    /** Returns remaining validity in ms (0 if expired or invalid). */
    public long getRemainingValidityMs(String accessToken) {
        return jwtTokenProvider.getRemainingValidityMs(accessToken);
    }

    /** Exposes TokenHasher for callers that need to hash a token before repository lookup. */
    public String hashForLookup(String rawToken) {
        return tokenHasher.hash(rawToken);
    }
}
