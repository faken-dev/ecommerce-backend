package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.port.TokenBlacklistService;
import com.ecommerce.auth.application.port.TokenFamilyTracking;
import com.ecommerce.auth.domain.entity.RefreshToken;
import com.ecommerce.auth.domain.repository.RefreshTokenRepository;
import com.ecommerce.auth.infrastructure.security.TokenHasher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Logout UseCase.
 *
 * Uses TokenBlacklistService and TokenFamilyTracking (ports) instead of raw RedisTemplate —
 * respects Dependency Inversion Principle.
 */
@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHasher tokenHasher;
    private final TokenBlacklistService tokenBlacklistService;
    private final TokenFamilyTracking tokenFamilyTracking;

    /** Logout from current device: revoke refresh token + blacklist access token. */
    @Transactional
    public void execute(String rawRefreshToken, String accessToken, long accessTokenRemainingMs) {
        String tokenHash = tokenHasher.hash(rawRefreshToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND));

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        if (accessTokenRemainingMs > 0) {
            tokenBlacklistService.blacklist(accessToken, accessTokenRemainingMs);
        }
    }

    /**
     * Logout from all devices: revoke entire token family (Redis) and all tokens (DB),
     * then blacklist current access token.
     */
    @Transactional
    public void executeAllDevices(UUID userId, String accessToken, long accessTokenRemainingMs) {
        tokenFamilyTracking.revokeFamily(userId);
        refreshTokenRepository.revokeAllByUserId(userId);
        if (accessTokenRemainingMs > 0) {
            tokenBlacklistService.blacklist(accessToken, accessTokenRemainingMs);
        }
    }
}
