package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.RefreshTokenCommand;
import com.ecommerce.auth.application.dto.AuthTokenResponse;
import com.ecommerce.auth.application.port.TokenFamilyTracking;
import com.ecommerce.auth.application.service.TokenService;
import com.ecommerce.auth.domain.entity.RefreshToken;
import com.ecommerce.auth.domain.repository.RefreshTokenRepository;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class RefreshTokenUseCase {
    private static final Logger log = LoggerFactory.getLogger(RefreshTokenUseCase.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final TokenFamilyTracking tokenFamilyTracking;

    /**
     * Refresh token rotation with replay-attack detection.
     *
     * Each user has a token "family" tracked by a generation counter in Redis.
     * Tokens are issued with the current family generation. If an older-generation
     * token is presented, the entire family is revoked Ä‚Â¢Ă¢â€Â¬Ă¢â‚¬Â the legitimate token
     * issued after the attacker's token will also be invalidated, forcing a re-login.
     */
    @Transactional
    public AuthTokenResponse execute(RefreshTokenCommand command) {
        RefreshToken oldToken = refreshTokenRepository.findByTokenHashForUpdate(
                tokenService.hashForLookup(command.refreshToken()))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND));

        // Check expiry/revocation FIRST Ä‚Â¢Ă¢â€Â¬Ă¢â‚¬Â expired tokens must not be rotated
        oldToken.ensureValid();

        // Replay attack detection: if token.generation < current family generation Ä‚Â¢Ă¢â‚¬Â Ă¢â‚¬â„¢ attack
        if (!tokenFamilyTracking.isGenerationValid(oldToken.getUserId(), oldToken.getGeneration())) {
            log.warn("Replay attack detected for userId={}, token generation={}",
                    oldToken.getUserId(), oldToken.getGeneration());
            tokenFamilyTracking.revokeFamily(oldToken.getUserId());
            // Revoke the legitimate token issued after the attacker's use
            oldToken.revoke();
            oldToken.setUpdatedBy(RefreshToken.THEFT_DETECTOR);
            refreshTokenRepository.save(oldToken);
            throw new BusinessException(ErrorCode.AUTH_TOKEN_REPLAY_DETECTED);
        }

        var user = userRepository.findById(oldToken.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        user.ensureActive();

        // TokenService handles generation advancement + persistence + audit event
        AuthTokenResponse tokens = tokenService.generateTokens(
                user, command.deviceInfo(), command.ipAddress());

        // Revoke old token (rotation Ä‚Â¢Ă¢â€Â¬Ă¢â‚¬Â no replacement tracking needed)
        oldToken.revoke();
        refreshTokenRepository.save(oldToken);

        return tokens;
    }
}
