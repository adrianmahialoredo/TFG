package com.uo289390.toco.repository;

import static com.uo289390.toco.repository.RepositoryTestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

import com.uo289390.toco.model.Chord;
import com.uo289390.toco.model.Instrument;

/**
 * Integration tests for ChordRepository: CRUD, the notes element collection, the chord flags
 * and the unique (name, instrument) constraint.
 */
@DataJpaTest
class ChordRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ChordRepository chordRepository;

    private Instrument guitar;
    private Instrument piano;

    @BeforeEach
    void setUp() {
        guitar = em.persist(instrument(1L, "Guitar"));
        piano = em.persist(instrument(2L, "Piano"));
        em.persist(chord(1L, "C", guitar));
        em.persist(chord(2L, "G", guitar));
        em.persist(chord(3L, "Am", guitar));
        em.flush();
        em.clear();
    }

    @Test
    void findById_returnsChordWithAllFields() {
        Chord loaded = chordRepository.findById(1L).orElseThrow();

        assertEquals("C", loaded.getName());
        assertEquals(List.of("C", "E", "G"), loaded.getNotes());
        assertEquals("https://example.com/C.png", loaded.getImageUrl());
        assertEquals(1L, loaded.getInstrument().getId());
    }

    @Test
    void findById_missing_returnsEmpty() {
        assertTrue(chordRepository.findById(99L).isEmpty());
    }

    @Test
    void findAllById_returnsOnlyExistingChords() {
        List<String> names = chordRepository.findAllById(List.of(1L, 3L, 99L)).stream()
                .map(Chord::getName).sorted().toList();

        assertEquals(List.of("Am", "C"), names);
    }

    @Test
    void findAll_sortedByName() {
        assertEquals(List.of("Am", "C", "G"),
                chordRepository.findAll(Sort.by("name")).stream().map(Chord::getName).toList());
    }

    @Test
    void save_persistsFlagsAndCapoFret() {
        Chord chord = chord(4L, "F", guitar);
        chord.setBarre(true);
        chord.setMajor(true);
        chord.setSeventh(true);
        chord.setCapo(true);
        chord.setCapoFret(3);
        chordRepository.saveAndFlush(chord);
        em.clear();

        Chord loaded = chordRepository.findById(4L).orElseThrow();
        assertTrue(loaded.isBarre());
        assertTrue(loaded.isMajor());
        assertTrue(loaded.isSeventh());
        assertTrue(loaded.isCapo());
        assertEquals(3, loaded.getCapoFret());
        assertFalse(loaded.isOpen());
        assertFalse(loaded.isMinor());
        assertFalse(loaded.isPowerChord());
    }

    @Test
    void save_keepsNotesOrder() {
        Chord chord = chord(5L, "Dmaj7", guitar);
        chord.setNotes(List.of("D", "F#", "A", "C#"));
        chordRepository.saveAndFlush(chord);
        em.clear();

        assertEquals(List.of("D", "F#", "A", "C#"), chordRepository.findById(5L).orElseThrow().getNotes());
    }

    @Test
    void updateNotes_replacesTheCollection() {
        Chord chord = chordRepository.findById(2L).orElseThrow();
        chord.setNotes(new java.util.ArrayList<>(List.of("G", "B", "D", "F")));
        chordRepository.saveAndFlush(chord);
        em.clear();

        assertEquals(List.of("G", "B", "D", "F"), chordRepository.findById(2L).orElseThrow().getNotes());
    }

    @Test
    void duplicateNameForSameInstrument_violatesUniqueConstraint() {
        assertThrows(DataIntegrityViolationException.class,
                () -> chordRepository.saveAndFlush(chord(6L, "C", guitar)));
    }

    @Test
    void sameNameForAnotherInstrument_isAllowed() {
        chordRepository.saveAndFlush(chord(6L, "C", piano));
        em.clear();

        assertEquals(2L, chordRepository.findById(6L).orElseThrow().getInstrument().getId());
        assertEquals(4, chordRepository.count());
    }

    @Test
    void chordWithoutInstrument_isRejected() {
        assertThrows(Exception.class, () -> chordRepository.saveAndFlush(chord(7L, "D", null)));
    }

    @Test
    void deleteById_removesChordAndItsNotes() {
        chordRepository.deleteById(3L);
        chordRepository.flush();

        assertTrue(chordRepository.findById(3L).isEmpty());
        assertEquals(2, chordRepository.count());
        Number notes = (Number) em.getEntityManager()
                .createNativeQuery("SELECT COUNT(*) FROM chord_notes WHERE chord_id = 3").getSingleResult();
        assertEquals(0, notes.intValue());
    }
}
