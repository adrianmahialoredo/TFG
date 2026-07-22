package com.demo.demo.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

/**
 * Unit tests for User entity validation.
 * Tests all validation constraints on the User model.
 */
class UserValidationTest {

    private Validator validator;
    private User user;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setName("Test User");
        user.getPreferences().setDifficultyLevel(50);
        user.setRole(Role.USER);
    }

    @Test
    void testValidUser() {
        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(0, violations.size());
    }

    @Test
    void testEmailValidation_Invalid() {
        // Arrange
        user.setEmail("invalid-email");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size());
    }

    @Test
    void testEmailValidation_Null() {
        // Arrange
        user.setEmail(null);

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void testUsernameValidation_TooShort() {
        // Arrange
        user.setUsername("ab");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size());
    }

    @Test
    void testUsernameValidation_InvalidCharacters() {
        // Arrange
        user.setUsername("user@invalid!");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertTrue(violations.size() > 0);
    }

    @Test
    void testPasswordValidation_TooShort() {
        // Arrange
        user.setPassword("pass");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size());
    }

    @Test
    void testNameValidation_TooShort() {
        // Arrange
        user.setName("A");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size());
    }

    @Test
    void testDifficultyLevelValidation_BelowMin() {
        // Arrange
        user.getPreferences().setDifficultyLevel(-1);

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size());
    }

    @Test
    void testDifficultyLevelValidation_AboveMax() {
        // Arrange
        user.getPreferences().setDifficultyLevel(101);

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size());
    }

    @Test
    void testProfileImageUrlValidation_InvalidUrl() {
        // Arrange
        user.setProfileImageUrl("not-a-url");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size());
    }

    @Test
    void testProfileImageUrlValidation_ValidHttps() {
        // Arrange
        user.setProfileImageUrl("https://example.com/image.png");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(0, violations.size());
    }

    @Test
    void testProfileImageUrlValidation_Empty() {
        // Arrange
        user.setProfileImageUrl("");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(0, violations.size());
    }
}

