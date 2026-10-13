package com.uo289390.toco.model;

import static org.junit.jupiter.api.Assertions.*;

import com.uo289390.toco.model.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

/**
 * Unit tests for Genre entity validation.
 * Tests all validation constraints on the Genre model.
 */
class GenreValidationTest {

    private Validator validator;
    private Genre genre;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        genre = new Genre();
        genre.setTitle("Rock");
        genre.setDescription("Rock music genre");
        genre.setBannerUrl("https://example.com/rock-banner.png");
        genre.setWelcomeIntroVideoId("https://example.com/rock-intro.mp4");
        genre.setWelcomeSongVideoId("https://example.com/rock-song.mp4");
    }

    @Test
    void testValidGenre() {
        // Act
        Set<ConstraintViolation<Genre>> violations = validator.validate(genre);

        // Assert
        assertEquals(0, violations.size());
    }

    @Test
    void testDescriptionValidation_Blank() {
        // Arrange
        genre.setDescription("");

        // Act
        Set<ConstraintViolation<Genre>> violations = validator.validate(genre);

        // Assert
        assertTrue(violations.size() > 0);
    }

    @Test
    void testBannerUrlValidation_Blank() {
        // Arrange
        genre.setBannerUrl("");

        // Act
        Set<ConstraintViolation<Genre>> violations = validator.validate(genre);

        // Assert: a blank banner URL breaks both @NotBlank and @Pattern
        assertEquals(2, violations.size());
    }

    @Test
    void testBannerUrlValidation_InvalidUrl() {
        // Arrange
        genre.setBannerUrl("not-a-url");

        // Act
        Set<ConstraintViolation<Genre>> violations = validator.validate(genre);

        // Assert
        assertEquals(1, violations.size());
    }

    @Test
    void testWelcomeIntroVideoValidation_InvalidUrl() {
        // Arrange: welcomeIntroVideoId holds a video identifier, not a URL,
        // so any non-blank value is accepted.
        genre.setWelcomeIntroVideoId("invalid-url");

        // Act
        Set<ConstraintViolation<Genre>> violations = validator.validate(genre);

        // Assert
        assertEquals(0, violations.size());
    }

    @Test
    void testWelcomeSongVideoValidation_InvalidUrl() {
        // Arrange: welcomeSongVideoId holds a video identifier, not a URL,
        // so any non-blank value is accepted.
        genre.setWelcomeSongVideoId("invalid-url");

        // Act
        Set<ConstraintViolation<Genre>> violations = validator.validate(genre);

        // Assert
        assertEquals(0, violations.size());
    }

    @Test
    void testGenreWithValidHttpUrls() {
        // Arrange
        genre.setBannerUrl("http://example.com/banner.png");
        genre.setWelcomeIntroVideoId("http://example.com/intro.mp4");
        genre.setWelcomeSongVideoId("http://example.com/song.mp4");

        // Act
        Set<ConstraintViolation<Genre>> violations = validator.validate(genre);

        // Assert
        assertEquals(0, violations.size());
    }

    @Test
    void testGenreWithValidHttpsUrls() {
        // Arrange
        genre.setBannerUrl("https://example.com/banner.png");
        genre.setWelcomeIntroVideoId("https://example.com/intro.mp4");
        genre.setWelcomeSongVideoId("https://example.com/song.mp4");

        // Act
        Set<ConstraintViolation<Genre>> violations = validator.validate(genre);

        // Assert
        assertEquals(0, violations.size());
    }
}

