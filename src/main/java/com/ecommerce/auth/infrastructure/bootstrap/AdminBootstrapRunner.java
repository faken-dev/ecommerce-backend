package com.ecommerce.auth.infrastructure.bootstrap;

import com.ecommerce.auth.application.port.PasswordEncoder;
import com.ecommerce.auth.domain.entity.Role;
import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.repository.RoleRepository;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.HashedPassword;
import com.ecommerce.user.domain.entity.UserProfile;
import com.ecommerce.user.domain.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a default admin account + user profile on first application startup.
 *
 * <p>Execution is idempotent Ä‚Â¢Ă¢â€Â¬Ă¢â‚¬Â it is skipped entirely if an admin account
 * with the configured email already exists in the database.</p>
 *
 * <p>Password is read from {@code app.bootstrap.admin-password} so it can be
 * supplied safely via environment variable in production:</p>
 * <pre>
 * ADMIN_BOOTSTRAP_ENABLED=true ADMIN_BOOTSTRAP_EMAIL=admin@shop.com ADMIN_BOOTSTRAP_PASSWORD=Secret123 java -jar app.jar
 * </pre>
 */

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin-email:}")
    private String adminEmail;

    @Value("${app.bootstrap.admin-password:}")
    private String adminPassword;

    @Value("${app.bootstrap.admin-enabled:false}")
    private boolean bootstrapEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!bootstrapEnabled) {
            log.debug("Admin bootstrap is disabled (app.bootstrap.admin-enabled=false). Skipping.");
            return;
        }

        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.warn("Admin bootstrap: admin-email or admin-password is blank. " +
                    "Set app.bootstrap.admin-email and app.bootstrap.admin-password. Skipping.");
            return;
        }

        Email email;
        try {
            email = new Email(adminEmail);
        } catch (Exception e) {
            log.warn("Admin bootstrap: invalid email '{}'. Skipping.", adminEmail);
            return;
        }

        if (userRepository.existsByEmail(email)) {
            log.debug("Admin account {} already exists. Skipping bootstrap.", adminEmail);
            return;
        }

        Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
        if (adminRole == null) {
            log.error("Admin bootstrap: ADMIN role not found. " +
                    "Ensure Flyway migration V1 has run. Cannot create admin account.");
            return;
        }

        // Create auth user
        User admin = User.create(email, HashedPassword.of(passwordEncoder.encode(adminPassword)), "Administrator");
        admin.setEmailVerified(true);
        admin.setActive(true);
        admin.addRole(adminRole);
        User saved = userRepository.save(admin);

        // Create user profile for the admin
        UserProfile profile = UserProfile.createFromRegistration(saved.getId(), "Administrator");
        userProfileRepository.save(profile);

        log.info(" Default admin account + profile created: {}", adminEmail);
        log.warn("  Change this password immediately in production!");
    }
}
