package com.uo289390.toco.model;

import static org.junit.jupiter.api.Assertions.*;

import com.uo289390.toco.model.Chord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

/**
 * Unit tests for Chord entity validation.
 * Tests all validation constraints on the Chord model.
 */
class ChordValidationTest {

    private Validator validator;
    private Chord chord;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        chord = new Chord();
        chord.setName("C Major");
        chord.setImageUrl("https://example.com/cmajor.png");
        chord.setNotes(java.util.List.of("C", "E", "G"));
        chord.setInstrument(new Instrument());
    }

    @Test
    void testValidChord() {
        // Act
        Set<ConstraintViolation<Chord>> violations = validator.validate(chord);

        // Assert
        assertEquals(0, violations.size());
    }

    @Test
    void testInstrumentValidation_Null() {
        // Arrange
        chord.setInstrument(null);

        // Act
        Set<ConstraintViolation<Chord>> violations = validator.validate(chord);

        // Assert
        assertEquals(1, violations.size());
        assertEquals("instrument", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void testChordNameValidation_Blank() {
        // Arrange
        chord.setName("");

        // Act
        Set<ConstraintViolation<Chord>> violations = validator.validate(chord);

        // Assert
        assertEquals(1, violations.size());
    }

    @Test
    void testChordNameValidation_Null() {
        // Arrange
        chord.setName(null);

        // Act
        Set<ConstraintViolation<Chord>> violations = validator.validate(chord);

        // Assert
        assertEquals(1, violations.size());
    }

    @Test
    void testImageUrlValidation_Blank() {
        // Arrange
        chord.setImageUrl("");

        // Act
        Set<ConstraintViolation<Chord>> violations = validator.validate(chord);

        // Assert
        assertTrue(violations.size() > 0);
    }

    @Test
    void testImageUrlValidation_InvalidUrl() {
        // Arrange
        chord.setImageUrl("not-a-url");

        // Act
        Set<ConstraintViolation<Chord>> violations = validator.validate(chord);

        // Assert
        assertEquals(1, violations.size());
    }

    @Test
    void testNotesValidation_Empty() {
        // Arrange
        chord.setNotes(java.util.List.of());

        // Act
        Set<ConstraintViolation<Chord>> violations = validator.validate(chord);

        // Assert
        assertEquals(1, violations.size());
    }

    @Test
    void testNotesValidation_Null() {
        // Arrange
        chord.setNotes(null);

        // Act
        Set<ConstraintViolation<Chord>> violations = validator.validate(chord);

        // Assert
        assertEquals(1, violations.size());
    }

    @Test
    void testChordWithMultipleNotes() {
        // Arrange
        chord.setNotes(java.util.List.of("C", "E", "G", "B"));

        // Act
        Set<ConstraintViolation<Chord>> violations = validator.validate(chord);

        // Assert
        assertEquals(0, violations.size());
    }

    @Test
    void testChordProperties() {
        // Arrange
        chord.setOpen(true);
        chord.setBarre(false);
        chord.setMajor(true);
        chord.setMinor(false);

        // Assert
        assertTrue(chord.isOpen());
        assertFalse(chord.isBarre());
        assertTrue(chord.isMajor());
        assertFalse(chord.isMinor());
    }
}

