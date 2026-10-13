package com.uo289390.toco.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uo289390.toco.model.Role;
import com.uo289390.toco.model.User;
import com.uo289390.toco.repository.UserRepository;

/**
 * Unit tests for {@link AdminBootstrap}: the administrator is created, promoted or left alone
 * depending on the configuration and on what is already in the database.
 */
@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {

    private static final String EMAIL = "admin@toco.dev";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "S3cret!pass";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminBootstrap adminBootstrap;

    @BeforeEach
    void setUp() {
        adminBootstrap.setEmail(EMAIL);
        adminBootstrap.setUsername(USERNAME);
        adminBootstrap.setPassword(PASSWORD);
    }

    @ParameterizedTest
    @CsvSource(value = {"'', S3cret!pass", "admin@toco.dev, ''", "'   ', S3cret!pass", "NULL, NULL"}, nullValues = "NULL")
    void run_withoutEmailOrPassword_doesNothing(String email, String password) {
        adminBootstrap.setEmail(email);
        adminBootstrap.setPassword(password);

        adminBootstrap.run(null);

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void run_withoutExistingUser_createsAdmin() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn("hashed");

        adminBootstrap.run(null);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertEquals(EMAIL, saved.getValue().getEmail());
        assertEquals(USERNAME, saved.getValue().getUsername());
        assertEquals(AdminBootstrap.ADMIN_NAME, saved.getValue().getName());
        assertEquals("hashed", saved.getValue().getPassword());
        assertEquals(Role.ADMIN, saved.getValue().getRole());
    }

    @Test
    void run_createsAdminWithPreferencesLinkedToIt() {
        // Preferences share the user's id (@MapsId): an unlinked Preferences makes Hibernate fail on save
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        adminBootstrap.run(null);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertSame(saved.getValue(), saved.getValue().getPreferences().getUser());
    }

    @Test
    void run_whenSaveFails_doesNotPropagate() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenThrow(new IllegalStateException("db down"));

        assertDoesNotThrow(() -> adminBootstrap.run(null));
    }

    @Test
    void run_withExistingRegularUser_promotesItWithoutTouchingPassword() {
        User existing = new User();
        existing.setEmail(EMAIL);
        existing.setPassword("old-hash");
        existing.setRole(Role.USER);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));

        adminBootstrap.run(null);

        verify(userRepository).save(existing);
        assertEquals(Role.ADMIN, existing.getRole());
        assertEquals("old-hash", existing.getPassword());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void run_withExistingAdmin_doesNotSave() {
        User existing = new User();
        existing.setEmail(EMAIL);
        existing.setRole(Role.ADMIN);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));

        adminBootstrap.run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void run_withUsernameTaken_doesNotCreateAdmin() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(new User()));

        adminBootstrap.run(null);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }
}
