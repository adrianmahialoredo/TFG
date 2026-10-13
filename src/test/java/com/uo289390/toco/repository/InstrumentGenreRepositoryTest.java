package com.uo289390.toco.repository;

import static com.uo289390.toco.repository.RepositoryTestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;

import com.uo289390.toco.repository.InstrumentGenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.InstrumentGenre;
import com.uo289390.toco.model.Lesson;

/**
 * Integration tests for InstrumentGenreRepository derived queries.
 */
@DataJpaTest
class InstrumentGenreRepositoryTest {

    private static final Pageable BY_ID = PageRequest.of(0, 50, Sort.by("id"));
    private static final List<Long> NONE = List.of(-1L);

    @Autowired
    private TestEntityManager em;

    @Autowired
    private InstrumentGenreRepository repository;

    private Genre rock;
    private Genre jazz;
    private Instrument guitar;
    private Instrument piano;

    @BeforeEach
    void setUp() {
        rock = em.persist(genre(100L, "Rock"));
        jazz = em.persist(genre(101L, "Jazz"));
        guitar = em.persist(instrument(1L, "Guitar"));
        piano = em.persist(instrument(2L, "Piano"));

        InstrumentGenre rockGuitar = instrumentGenre(200L, guitar, rock);
        rockGuitar.setLevel(1);
        em.persist(rockGuitar);
        InstrumentGenre rockPiano = instrumentGenre(201L, piano, rock);
        rockPiano.setLevel(3);
        em.persist(rockPiano);
        InstrumentGenre jazzPiano = instrumentGenre(202L, piano, jazz);
        jazzPiano.setLevel(5);
        em.persist(jazzPiano);

        em.flush();
        em.clear();
    }

    private static List<Long> ids(Collection<InstrumentGenre> items) {
        return items.stream().map(InstrumentGenre::getId).sorted().toList();
    }

    @Test
    void findByGenreId_returnsInstrumentGenresOfThatGenre() {
        assertEquals(List.of(200L, 201L), ids(repository.findByGenreId(rock.getId())));
        assertEquals(List.of(202L), ids(repository.findByGenreId(jazz.getId())));
        assertTrue(repository.findByGenreId(999L).isEmpty());
    }

    @Test
    void findByInstrumentId_returnsInstrumentGenresOfThatInstrument() {
        assertEquals(List.of(201L, 202L), ids(repository.findByInstrumentId(piano.getId())));
        assertEquals(List.of(200L), ids(repository.findByInstrumentId(guitar.getId())));
        assertTrue(repository.findByInstrumentId(999L).isEmpty());
    }

    @Test
    void findByGenreIdAndInstrumentId_findsExactPair() {
        assertEquals(201L, repository.findByGenreIdAndInstrumentId(rock.getId(), piano.getId()).orElseThrow().getId());
        assertTrue(repository.findByGenreIdAndInstrumentId(jazz.getId(), guitar.getId()).isEmpty());
    }

    @Test
    void findWithLevel_filtersByGenresInstrumentsAndInclusiveLevel() {
        List<Long> genres = List.of(rock.getId(), jazz.getId());
        List<Long> instruments = List.of(guitar.getId(), piano.getId());

        assertEquals(List.of(200L), ids(repository
                .findByGenreIdInAndInstrumentIdInAndLevelLessThanEqualAndIdNotIn(genres, instruments, 2, NONE, BY_ID)));
        assertEquals(List.of(200L, 201L), ids(repository
                .findByGenreIdInAndInstrumentIdInAndLevelLessThanEqualAndIdNotIn(genres, instruments, 3, NONE, BY_ID)));
        assertEquals(List.of(201L, 202L), ids(repository
                .findByGenreIdInAndInstrumentIdInAndLevelLessThanEqualAndIdNotIn(genres, instruments, 5, List.of(200L), BY_ID)));
    }

    @Test
    void findWithLevel_requiresBothGenreAndInstrumentToMatch() {
        assertEquals(List.of(201L), ids(repository.findByGenreIdInAndInstrumentIdInAndLevelLessThanEqualAndIdNotIn(
                List.of(rock.getId()), List.of(piano.getId()), 5, NONE, BY_ID)));
    }

    @Test
    void findWithoutLevel_filtersByGenresInstrumentsAndExclusions() {
        assertEquals(List.of(201L, 202L), ids(repository.findByGenreIdInAndInstrumentIdInAndIdNotIn(
                List.of(rock.getId(), jazz.getId()), List.of(piano.getId()), NONE, BY_ID)));
        assertEquals(List.of(202L), ids(repository.findByGenreIdInAndInstrumentIdInAndIdNotIn(
                List.of(rock.getId(), jazz.getId()), List.of(piano.getId()), List.of(201L), BY_ID)));
    }

    @Test
    void findWithoutLevel_respectsPageSize() {
        assertEquals(1, repository.findByGenreIdInAndInstrumentIdInAndIdNotIn(
                List.of(rock.getId(), jazz.getId()), List.of(guitar.getId(), piano.getId()), NONE, PageRequest.of(0, 1)).size());
    }

    @Test
    void lessons_areLoadedOrderedByIndexInInstrumentGenre() {
        InstrumentGenre rockGuitar = repository.findById(200L).orElseThrow();
        Lesson a = em.persist(lesson(10L, "A"));
        Lesson b = em.persist(lesson(11L, "B"));
        Lesson c = em.persist(lesson(12L, "C"));
        rockGuitar.setLessons(List.of(c, a, b));
        em.flush();
        em.clear();

        InstrumentGenre loaded = repository.findById(200L).orElseThrow();
        assertEquals(List.of(12L, 10L, 11L), loaded.getLessons().stream().map(Lesson::getId).toList());
    }
}
