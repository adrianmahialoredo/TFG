package com.uo289390.toco.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.uo289390.toco.model.Role;
import com.uo289390.toco.model.User;
import com.uo289390.toco.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Ensures there is an administrator account at startup.
 *
 * Catalog writes require the ADMIN role and registration always creates USER accounts, so the
 * first admin comes from configuration ({@code app.admin.*}, read from {@code ADMIN_*} in .env):
 * - If {@code app.admin.email} or {@code app.admin.password} is blank, nothing is done.
 * - If a user with that email exists, it is promoted to ADMIN (its password is not changed).
 * - Otherwise an ADMIN user is created with that email, username and password.
 *
 * The data loading scripts log in with these credentials to insert the catalog.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-10-13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    /**
     * Display name given to the administrator account when it is created.
     */
    static final String ADMIN_NAME = "Administrador";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String email;

    @Value("${app.admin.username:admin}")
    private String username;

    @Value("${app.admin.password:}")
    private String password;

    /**
     * Creates or promotes the configured administrator.
     *
     * @param args the application arguments (unused)
     */
    @Override
    public void run(ApplicationArguments args) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            log.info("No administrator configured (app.admin.email / app.admin.password): skipping");
            return;
        }

        try {
            userRepository.findByEmail(email).ifPresentOrElse(this::promote, this::createAdmin);
        } catch (RuntimeException e) {
            // A misconfigured admin must not prevent the API from starting
            log.error("Could not create or promote administrator {}: {}", email, e.getMessage(), e);
        }
    }

    private void promote(User user) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        log.info("User {} promoted to ADMIN", email);
    }

    private void createAdmin() {
        if (userRepository.findByUsername(username).isPresent()) {
            log.error("Cannot create administrator {}: username '{}' is already taken. Set ADMIN_USERNAME to another value",
                    email, username);
            return;
        }
        User admin = new User();
        admin.setEmail(email);
        admin.setUsername(username);
        admin.setName(ADMIN_NAME);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        // Preferences share the user's primary key (@MapsId): getPreferences() links them to the user
        admin.getPreferences();
        userRepository.save(admin);
        log.info("Administrator {} created", email);
    }

    // Package-private setters for unit tests (the values normally come from @Value)
    void setEmail(String email) {
        this.email = email;
    }

    void setUsername(String username) {
        this.username = username;
    }

    void setPassword(String password) {
        this.password = password;
    }
}
