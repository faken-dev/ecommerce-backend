package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.dto.AuthTokenResponse;
import com.ecommerce.auth.application.port.OAuth2UserRepository;
import com.ecommerce.auth.application.service.TokenService;
import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.OAuth2Provider;
import com.ecommerce.shared.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth2 login use case Ä‚Â¢Ă¢â€Â¬Ă¢â‚¬Â bridges Spring Security OAuth2 with the existing JWT infrastructure.
 *
 * Flow: OAuth2SuccessHandler (Spring Security) extracts the OAuth2User principal,
 * extracts email/name from the provider's userinfo, then delegates here.
 * This use case loads/creates the User, publishes RegistrationCompletedEvent
 * (to trigger UserProfile creation via the existing UserProfileCreatedHandler),
 * and issues JWT tokens via TokenService.
 */
@Service
@RequiredArgsConstructor

public class OAuth2LoginUseCase {
    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginUseCase.class);

    private final OAuth2UserRepository oauth2UserRepository;
    private final TokenService tokenService;
    private final EventPublisher eventPublisher;

    /**
     * Handles a successful OAuth2 authentication: loads/creates the user and issues JWT tokens.
     *
     * @param provider        identity provider (GOOGLE, APPLE, etc.)
     * @param providerUserId  provider's subject ID
     * @param email           user's email from provider userinfo
     * @param fullName        display name from provider (may be null Ä‚Â¢Ă¢â€Â¬Ă¢â‚¬Â defaults to email prefix)
     * @param avatarUrl       profile picture URL (may be null)
     * @param deviceInfo      raw device info string for audit
     * @param ipAddress       client IP for audit
     * @return JWT token pair identical to email/password login
     */
    @Transactional
    public AuthTokenResponse execute(OAuth2Provider provider, String providerUserId,
                                    String email, String fullName, String avatarUrl,
                                    String deviceInfo, String ipAddress) {
        User user = oauth2UserRepository.findOrCreate(
                provider, providerUserId, new Email(email), fullName, avatarUrl);

        // Blocked/deleted OAuth users must not receive tokens
        user.ensureActive();

        // Publish same event as email/password registration to trigger UserProfile creation
        eventPublisher.publish(user.toRegistrationCompletedEvent());

        log.info("OAuth2 login successful: provider={}, email={}, userId={}",
                provider, email, user.getId());

        return tokenService.generateTokens(user, deviceInfo, ipAddress);
    }
}
