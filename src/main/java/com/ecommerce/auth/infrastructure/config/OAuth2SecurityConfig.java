package com.ecommerce.auth.infrastructure.config;

import com.ecommerce.auth.infrastructure.security.OAuth2AuthenticationEntryPoint;
import com.ecommerce.auth.infrastructure.security.OAuth2FailureHandler;
import com.ecommerce.auth.infrastructure.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * OAuth2 login security filter chain.
 *
 * Uses @Order(1) to take precedence over the main SecurityConfig chain (Order 2).
 * Handles all /login/oauth2/* routes — the authorization code callback from Google —
 * using Spring's OAuth2AuthorizationRequestRedirectFilter and OAuth2LoginAuthenticationFilter.
 *
 * After successful OAuth2 authentication, OAuth2SuccessHandler issues a standard JWT
 * token response (instead of Spring's default redirect), keeping the API-first design.
 */
@Configuration
@RequiredArgsConstructor
public class OAuth2SecurityConfig {

    private final OAuth2SuccessHandler oauth2SuccessHandler;
    private final OAuth2FailureHandler oauth2FailureHandler;
    private final OAuth2AuthenticationEntryPoint oauth2EntryPoint;

    @Bean
    @Order(1)
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/login/oauth2/**", "/api/v1/auth/oauth2/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2SuccessHandler)
                        .failureHandler(oauth2FailureHandler)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(oauth2EntryPoint)
                )
                .build();
    }
}
