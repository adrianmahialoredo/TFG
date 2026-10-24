package com.uo289390.toco.repository;

import static com.uo289390.toco.repository.RepositoryTestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.Tune;

/**
 * Integration tests for TuneRepository: CRUD, the ordered notes collection (one per string),
 * the required instrument and the tunings of an instrument.
 */
@DataJpaTest
class TuneRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private TuneRepository tuneRepository;

    private Instrument guitar;
    private Instrument bass;

    @BeforeEach
    void setUp() {
        guitar = em.persist(instrument(1L, "Guitarra"));
        bass = em.persist(instrument(3L, "Bajo"));
        em.persist(instrument(5L, "Piano"));
        // Inserted out of id order to check the ordering of findByInstrumentIdOrderByIdAsc
        em.persist(tune(2L, "Drop D", guitar, "D2", "A2", "D3", "G3", "B3", "E4"));
        em.persist(tune(1L, "Estándar", guitar, "E2", "A2", "D3", "G3", "B3", "E4"));
        em.persist(tune(10L, "Estándar", bass, "E1", "A1", "D2", "G2"));
        em.flush();
        em.clear();
    }

    @Test
    void findById_returnsTuneWithInstrumentAndNotesInStringOrder() {
        Tune loaded = tuneRepository.findById(2L).orElseThrow();

        assertEquals("Drop D", loaded.getTitle());
        assertEquals(1L, loaded.getInstrument().getId());
        assertEquals(List.of("D2", "A2", "D3", "G3", "B3", "E4"), loaded.getNotes());
    }

    @Test
    void findById_missing_returnsEmpty() {
        assertTrue(tuneRepository.findById(99L).isEmpty());
    }

    @Test
    void findByInstrumentId_returnsTheTuningsOfTheInstrumentInIdOrder() {
        assertEquals(List.of(1L, 2L), tuneRepository.findByInstrumentIdOrderByIdAsc(1L).stream()
                .map(Tune::getId).toList());
        assertEquals(List.of(10L), tuneRepository.findByInstrumentIdOrderByIdAsc(3L).stream()
                .map(Tune::getId).toList());
    }

    @Test
    void findByInstrumentId_instrumentWithoutTunings_returnsEmpty() {
        assertTrue(tuneRepository.findByInstrumentIdOrderByIdAsc(5L).isEmpty());
        assertTrue(tuneRepository.findByInstrumentIdOrderByIdAsc(99L).isEmpty());
    }

    @Test
    void save_keepsRepeatedNotesAndTheirOrder() {
        // Order column: the order of the strings is kept, and nothing forbids two strings with the same note
        tuneRepository.saveAndFlush(tune(3L, "Open D", guitar, "D2", "A2", "D3", "F#3", "A3", "D4"));
        tuneRepository.saveAndFlush(tune(4L, "Unísono", guitar, "E2", "E2"));
        em.clear();

        assertEquals(List.of("D2", "A2", "D3", "F#3", "A3", "D4"), tuneRepository.findById(3L).orElseThrow().getNotes());
        assertEquals(List.of("E2", "E2"), tuneRepository.findById(4L).orElseThrow().getNotes());
    }

    @Test
    void updateNotes_replacesTheCollection() {
        Tune tune = tuneRepository.findById(10L).orElseThrow();
        tune.setNotes(new ArrayList<>(List.of("B0", "E1", "A1", "D2", "G2")));
        tuneRepository.saveAndFlush(tune);
        em.clear();

        assertEquals(List.of("B0", "E1", "A1", "D2", "G2"), tuneRepository.findById(10L).orElseThrow().getNotes());
    }

    @Test
    void save_withoutInstrument_isRejected() {
        Tune tune = tune(5L, "Sin instrumento", null, "E2");

        assertThrows(Exception.class, () -> tuneRepository.saveAndFlush(tune));
    }

    @Test
    void deleteById_removesTuneAndItsNotes() {
        tuneRepository.deleteById(2L);
        tuneRepository.flush();

        assertTrue(tuneRepository.findById(2L).isEmpty());
        assertEquals(2, tuneRepository.count());
        Number notes = (Number) em.getEntityManager()
                .createNativeQuery("SELECT COUNT(*) FROM tune_notes WHERE tune_id = 2").getSingleResult();
        assertEquals(0, notes.intValue());
    }

    @Test
    void deleteInstrumentWithTunings_violatesForeignKey() {
        // Native query: the exception is not translated by the repository proxy
        assertThrows(Exception.class, () -> {
            em.getEntityManager().createNativeQuery("DELETE FROM instruments WHERE id = 3").executeUpdate();
            em.flush();
        });
    }
}
