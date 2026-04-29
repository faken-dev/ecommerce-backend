package com.ecommerce.auth.infrastructure.security.oauth2;

import com.ecommerce.auth.domain.entity.Role;
import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.repository.RoleRepository;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.OAuth2Provider;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Provider provider = OAuth2Provider.valueOf(registrationId.toUpperCase());
        
        Map<String, Object> attributes = oauth2User.getAttributes();
        String emailStr = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerUserId = (String) attributes.get("id"); // Facebook
        if (providerUserId == null) {
            providerUserId = (String) attributes.get("sub"); // Google
        }
        if (emailStr == null || emailStr.isBlank()) {
            log.warn("Email not found from OAuth2 provider: {}. Generating placeholder email.", provider);
            emailStr = providerUserId + "@" + provider.name().toLowerCase() + ".ecommerce.internal";
        }

        Email email = new Email(emailStr);
        String picture = extractPicture(provider, attributes);
        Optional<User> userOptional = userRepository.findByProviderAndProviderUserId(provider, providerUserId);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            log.info("Found existing OAuth2 user: email={}, provider={}", user.getEmail().value(), provider);
            
            // Sync info if needed
            user.updateFullName(name);
            if (picture != null) {
                user.updateProfilePictureUrl(picture);
            }
            userRepository.save(user);
        } else {
            // 2. Fallback: Check if we have an email and if it's already in use
            if (emailStr != null && !emailStr.contains("ecommerce.internal")) {
                Optional<User> emailUser = userRepository.findByEmail(new Email(emailStr));
                if (emailUser.isPresent()) {
                    user = emailUser.get();
                    log.info("Linking existing account {} to provider {}", emailStr, provider);
                    user.setProvider(provider);
                    user.setProviderUserId(providerUserId);
                    userRepository.save(user);
                } else {
                    user = registerNewUser(provider, providerUserId, email, name, picture);
                }
            } else {
                // No email or dummy email -> must be a new registration
                user = registerNewUser(provider, providerUserId, email, name, picture);
            }
        }

        return new CustomOAuth2User(user.getId(), user.getEmail().value(), oauth2User);
    }

    private String extractPicture(OAuth2Provider provider, Map<String, Object> attributes) {
        if (provider == OAuth2Provider.GOOGLE) {
            return (String) attributes.get("picture");
        } else if (provider == OAuth2Provider.FACEBOOK) {
            @SuppressWarnings("unchecked")
            Map<String, Object> pictureObj = (Map<String, Object>) attributes.get("picture");
            if (pictureObj != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataObj = (Map<String, Object>) pictureObj.get("data");
                if (dataObj != null) {
                    return (String) dataObj.get("url");
                }
            }
        } else if (provider == OAuth2Provider.GITHUB) {
            return (String) attributes.get("avatar_url");
        }
        return null;
    }

    private User registerNewUser(OAuth2Provider provider, String providerUserId, Email email, String name, String picture) {
        User user = User.createOAuth(provider, providerUserId, email, name, picture);
        
        // Assign default role (BUYER)
        Role buyerRole = roleRepository.findByName("BUYER")
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_ROLE_NOT_FOUND));
        user.addRole(buyerRole);

        User savedUser = userRepository.save(user);
        
        // Publish event to trigger UserProfile creation
        eventPublisher.publish(savedUser.toRegistrationCompletedEvent());
        log.info("New OAuth2 user registered and event published: email={}, provider={}", email, provider);

        return savedUser;
    }
}
