package com.uo289390.toco.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * Unit tests for Tune entity validation and equality.
 */
class TuneValidationTest {

    private Validator validator;
    private Tune tune;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();

        Instrument guitar = new Instrument();
        guitar.setId(1L);
        tune = new Tune(1L, "Estándar", guitar, new ArrayList<>(List.of("E2", "A2", "D3", "G3", "B3", "E4")));
    }

    @Test
    void validTune_hasNoViolations() {
        assertTrue(validator.validate(tune).isEmpty());
    }

    @Test
    void blankTitle_isRejected() {
        tune.setTitle(" ");

        Set<ConstraintViolation<Tune>> violations = validator.validate(tune);

        assertEquals(1, violations.size());
        assertEquals("Tune title cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void missingInstrument_isRejected() {
        tune.setInstrument(null);

        assertEquals("Tune must belong to an instrument",
                validator.validate(tune).iterator().next().getMessage());
    }

    @Test
    void emptyNotes_areRejected() {
        tune.setNotes(new ArrayList<>());

        assertEquals("Tune must have at least one note",
                validator.validate(tune).iterator().next().getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"C0", "E1", "F#3", "A4", "B8"})
    void wellFormedNote_isAccepted(String note) {
        tune.setNotes(new ArrayList<>(List.of(note)));

        assertTrue(validator.validate(tune).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"E", "e2", "H2", "Bb2", "E9", "E#", "A#10", ""})
    void malformedNote_isRejected(String note) {
        tune.setNotes(new ArrayList<>(List.of("E2", note)));

        Set<ConstraintViolation<Tune>> violations = validator.validate(tune);

        assertEquals(1, violations.size());
        assertEquals("Notes must look like E2 or F#3", violations.iterator().next().getMessage());
    }

    @Test
    void equality_isById() {
        Tune same = new Tune();
        same.setId(1L);
        Tune other = new Tune();
        other.setId(2L);

        assertEquals(tune, same);
        assertEquals(tune.hashCode(), same.hashCode());
        assertNotEquals(tune, other);
        assertNotEquals(new Tune(), new Tune());
    }
}
