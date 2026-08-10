package com.bookstore.config;

import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dev-only seed password initialiser.
 *
 * <p>Because static BCrypt hashes committed to source control are flagged by
 * secret scanners (e.g. IBM Vault Radar), {@code data.sql} inserts seed users
 * with a non-functional placeholder ({@code PLACEHOLDER_SET_BY_SEEDER}).
 * This bean runs after the SQL seed and replaces those placeholders with a
 * proper BCrypt hash computed at runtime from configurable env vars.
 *
 * <p>Passwords are read from:
 * <ul>
 *   <li>{@code SEED_ADMIN_PASSWORD}    — default {@code Admin@123}</li>
 *   <li>{@code SEED_CUSTOMER_PASSWORD} — default {@code Customer@123}</li>
 * </ul>
 *
 * <p>This bean is active <strong>only</strong> under the {@code dev} Spring profile
 * and is never present in a production build.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private static final String PLACEHOLDER = "PLACEHOLDER_SET_BY_SEEDER";

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.admin-password:Admin@123}")
    private String adminPassword;

    @Value("${seed.customer-password:Customer@123}")
    private String customerPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedPassword("admin@bookstore.com",    adminPassword);
        seedPassword("customer@bookstore.com", customerPassword);
    }

    private void seedPassword(String email, String rawPassword) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (PLACEHOLDER.equals(user.getPasswordHash())) {
                user.setPasswordHash(passwordEncoder.encode(rawPassword));
                userRepository.save(user);
                log.info("DevDataSeeder: password initialised for '{}'", email);
            }
            // If already a real BCrypt hash (re-run scenario), leave it untouched
        });
    }
}
