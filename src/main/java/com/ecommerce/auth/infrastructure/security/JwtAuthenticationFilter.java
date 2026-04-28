package com.ecommerce.auth.infrastructure.security;

import com.ecommerce.shared.infrastructure.security.AuthenticatedUser;

import com.ecommerce.auth.application.port.TokenBlacklistService;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.shared.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * JWT authentication filter — validates access tokens and sets the SecurityContext.
 *
 * <p>Blacklist check uses {@link TokenBlacklistService} (application port) instead of
 * raw {@code StringRedisTemplate} — respects Dependency Inversion Principle.
 * The concrete implementation ({@code RedisTokenBlacklistService}) handles the Redis details.
 */

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            try {
                // 1. Check Blacklist via port (infrastructure handles Redis details)
                if (tokenBlacklistService.isBlacklisted(token)) {
                    writeError(response, ErrorCode.AUTH_TOKEN_INVALID);
                    return;
                }

                // 2. Validate + Parse Token
                if (!jwtTokenProvider.validateAccessToken(token)) {
                    writeError(response, ErrorCode.AUTH_TOKEN_INVALID);
                    return;
                }

                Claims claims = jwtTokenProvider.parseAccessToken(token);
                UUID userId = UUID.fromString(claims.getSubject());

                // 3. Extract Info
                String email = claims.get("email", String.class);
                String fullName = claims.get("fullName", String.class);

                // 4. Extract Permissions (Authorities)
                @SuppressWarnings("unchecked")
                List<String> permissions = claims.get("permissions", List.class);

                List<SimpleGrantedAuthority> authorities = (permissions != null)
                        ? permissions.stream().map(SimpleGrantedAuthority::new).toList()
                        : List.of();

                // 5. Set SecurityContext
                AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                        .id(userId)
                        .email(email)
                        .fullName(fullName)
                        .build();

                var authentication = new UsernamePasswordAuthenticationToken(authenticatedUser, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (JwtException | IllegalArgumentException e) {
                log.debug("JWT processing failed: {}", e.getMessage());
                writeError(response, ErrorCode.AUTH_TOKEN_INVALID);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<?> apiResponse = ApiResponse.error(errorCode);

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
