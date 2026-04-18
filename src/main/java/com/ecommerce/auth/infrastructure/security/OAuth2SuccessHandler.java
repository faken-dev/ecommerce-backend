package com.ecommerce.auth.infrastructure.security;

import com.ecommerce.auth.application.dto.AuthTokenResponse;
import com.ecommerce.auth.application.usecase.OAuth2LoginUseCase;
import com.ecommerce.auth.domain.valueobject.OAuth2Provider;
import com.ecommerce.shared.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom success handler for OAuth2 login — issues JWT tokens instead of redirecting.
 *
 * Receives the authenticated OAuth2AuthenticationToken after Spring exchanges
 * the authorization code for tokens with Google. Extracts the user's email/name
 * and delegates to OAuth2LoginUseCase to produce a standard AuthTokenResponse.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2LoginUseCase oauth2LoginUseCase;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            log.error("OAuth2SuccessHandler received non-OAuth2 authentication type: {}",
                    authentication.getClass().getName());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unexpected authentication type");
            return;
        }

        OAuth2User principal = oauthToken.getPrincipal();
        String providerId = oauthToken.getAuthorizedClientRegistrationId();

        OAuth2Provider provider = switch (providerId) {
            case "google" -> OAuth2Provider.GOOGLE;
            case "apple"  -> OAuth2Provider.APPLE;
            default       -> {
                log.error("Unsupported OAuth2 provider: {}", providerId);
                yield null;
            }
        };

        if (provider == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Unsupported OAuth2 provider: " + providerId);
            return;
        }

        String email     = principal.getAttribute("email");
        String fullName  = principal.getAttribute("name");
        String avatarUrl = principal.getAttribute("picture");
        String providerUserId = principal.getName(); // Google's "sub" claim

        if (email == null || email.isBlank()) {
            log.error("OAuth2 user has no email: provider={}, providerUserId={}",
                    provider, providerUserId);
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "OAuth2 provider did not return an email address");
            return;
        }

        String deviceInfo = request.getHeader("X-Device-Info");
        String ipAddress  = extractClientIp(request);

        AuthTokenResponse tokenResponse;
        try {
            tokenResponse = oauth2LoginUseCase.execute(
                    provider, providerUserId, email, fullName, avatarUrl,
                    deviceInfo, ipAddress);
        } catch (Exception e) {
            log.error("OAuth2 token generation failed: provider={}, email={}",
                    provider, email, e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Authentication succeeded but token generation failed");
            return;
        }

        writeJsonResponse(response, HttpServletResponse.SC_OK, ApiResponse.ok(tokenResponse));
    }

    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private void writeJsonResponse(HttpServletResponse response, int status, Object body)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private void sendError(HttpServletResponse response, int status, String message)
            throws IOException {
        writeJsonResponse(response, status, ApiResponse.error(message));
    }
}
