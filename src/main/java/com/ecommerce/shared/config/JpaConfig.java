package com.ecommerce.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA auditing configuration.
 *
 * <p>The {@link AuditorAware} resolves the current actor's UUID from the
 * {@code SecurityContext}. Since the principal set by {@link JwtAuthenticationFilter}
 * is a {@link UUID}, this implementation safely casts it.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.of(UUID0.INSTANCE);
            }
            Object principal = auth.getPrincipal();
            if (principal instanceof UUID uuid) {
                return Optional.of(uuid);
            }
            // Fallback: if someone sets a String principal (e.g. "system")
            if ("system".equals(principal)) {
                return Optional.of(UUID0.INSTANCE);
            }
            return Optional.of(UUID0.INSTANCE);
        };
    }

    /**
     * Sentinel UUID for the "system" actor (no authenticated user).
     * All- zeroes so it is identifiable in audit logs and DB.
     */
    private static final class UUID0 {
        static final UUID INSTANCE = UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
}