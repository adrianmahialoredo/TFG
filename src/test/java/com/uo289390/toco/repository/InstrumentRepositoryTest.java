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

import com.uo289390.toco.model.Instrument;

/**
 * Integration tests for InstrumentRepository: CRUD with manually assigned IDs and the unique
 * name constraint.
 */
@DataJpaTest
class InstrumentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @BeforeEach
    void setUp() {
        em.persist(instrument(1L, "Guitar"));
        em.persist(instrument(2L, "Bass"));
        em.persist(instrument(3L, "Drums"));
        em.flush();
        em.clear();
    }

    @Test
    void findById_returnsInstrumentWithAllFields() {
        Instrument loaded = instrumentRepository.findById(1L).orElseThrow();

        assertEquals("Guitar", loaded.getName());
        assertEquals("https://example.com/Guitar.png", loaded.getImageUrl());
    }

    @Test
    void findById_missing_returnsEmpty() {
        assertTrue(instrumentRepository.findById(99L).isEmpty());
    }

    @Test
    void findAllById_returnsOnlyExistingInstruments() {
        List<Long> ids = instrumentRepository.findAllById(List.of(2L, 3L, 99L)).stream()
                .map(Instrument::getId).sorted().toList();

        assertEquals(List.of(2L, 3L), ids);
    }

    @Test
    void findAll_sortedByName() {
        assertEquals(List.of("Bass", "Drums", "Guitar"),
                instrumentRepository.findAll(Sort.by("name")).stream().map(Instrument::getName).toList());
    }

    @Test
    void save_withManualId_keepsThatId() {
        instrumentRepository.saveAndFlush(instrument(42L, "Piano"));
        em.clear();

        assertEquals("Piano", instrumentRepository.findById(42L).orElseThrow().getName());
        assertEquals(4, instrumentRepository.count());
    }

    @Test
    void save_existingId_updatesInstrument() {
        Instrument guitar = instrumentRepository.findById(1L).orElseThrow();
        guitar.setName("Electric guitar");
        instrumentRepository.saveAndFlush(guitar);
        em.clear();

        assertEquals("Electric guitar", instrumentRepository.findById(1L).orElseThrow().getName());
        assertEquals(3, instrumentRepository.count());
    }

    @Test
    void duplicateName_violatesUniqueConstraint() {
        assertThrows(DataIntegrityViolationException.class,
                () -> instrumentRepository.saveAndFlush(instrument(4L, "Guitar")));
    }

    @Test
    void deleteById_removesInstrument() {
        instrumentRepository.deleteById(2L);
        instrumentRepository.flush();

        assertFalse(instrumentRepository.existsById(2L));
        assertEquals(2, instrumentRepository.count());
    }
}
