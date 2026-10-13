package com.uo289390.toco.repository;

import static com.uo289390.toco.repository.RepositoryTestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import com.uo289390.toco.repository.LessonRepository;
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
import com.uo289390.toco.model.LessonSection;

/**
 * Integration tests for LessonRepository derived queries: genre, instrument and
 * instrument-genre membership, popularity flags, level limits, exclusions and ordering.
 */
@DataJpaTest
class LessonRepositoryTest {

    private static final Pageable BY_ID = PageRequest.of(0, 50, Sort.by("id"));
    private static final List<Long> NONE = List.of(-1L);

    @Autowired
    private TestEntityManager em;

    @Autowired
    private LessonRepository lessonRepository;

    private Genre rock;
    private Genre jazz;
    private Instrument guitar;
    private Instrument piano;
    private InstrumentGenre rockGuitar;

    @BeforeEach
    void setUp() {
        rock = em.persist(genre(100L, "Rock"));
        jazz = em.persist(genre(101L, "Jazz"));
        guitar = em.persist(instrument(1L, "Guitar"));
        piano = em.persist(instrument(2L, "Piano"));
        rockGuitar = em.persist(instrumentGenre(200L, guitar, rock));

        // 1: rock genre lesson, level 1, popular, genrePopular
        Lesson l1 = lesson(1L, "Rock basics");
        l1.setGenre(rock);
        l1.setBelongsToGenre(true);
        l1.setLevel(1);
        l1.setPopular(true);
        l1.setGenrePopular(true);
        l1.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
        em.persist(l1);

        // 2: rock genre lesson, level 3
        Lesson l2 = lesson(2L, "Rock advanced");
        l2.setGenre(rock);
        l2.setBelongsToGenre(true);
        l2.setLevel(3);
        l2.setCreatedAt(LocalDateTime.of(2024, 3, 1, 0, 0));
        em.persist(l2);

        // 3: lesson of the rock-guitar instrument genre (has genre set but does not belong to the genre)
        Lesson l3 = lesson(3L, "Rock guitar riffs");
        l3.setGenre(rock);
        l3.setBelongsToGenre(false);
        l3.joinInstrumentGenre(rockGuitar, 0);
        l3.setGenrePopular(true);
        l3.setLevel(2);
        l3.setCreatedAt(LocalDateTime.of(2024, 3, 1, 0, 0));
        em.persist(l3);

        // 4: guitar instrument lesson, level 2, instrumentPopular
        Lesson l4 = lesson(4L, "Guitar posture");
        l4.setInstrument(guitar);
        l4.setBelongsToInstrument(true);
        l4.setInstrumentPopular(true);
        l4.setLevel(2);
        l4.setCreatedAt(LocalDateTime.of(2024, 2, 1, 0, 0));
        em.persist(l4);

        // 5: piano instrument lesson, level 4, popular
        Lesson l5 = lesson(5L, "Piano scales");
        l5.setInstrument(piano);
        l5.setBelongsToInstrument(true);
        l5.setLevel(4);
        l5.setPopular(true);
        l5.setCreatedAt(LocalDateTime.of(2023, 6, 1, 0, 0));
        em.persist(l5);

        // 6: generic lesson, level 1
        Lesson l6 = lesson(6L, "Reading music");
        l6.setLevel(1);
        l6.setCreatedAt(LocalDateTime.of(2022, 1, 1, 0, 0));
        em.persist(l6);

        // 7: generic lesson, level 5
        Lesson l7 = lesson(7L, "Music theory");
        l7.setLevel(5);
        l7.setCreatedAt(LocalDateTime.of(2022, 1, 1, 0, 0));
        em.persist(l7);

        // 8: jazz genre lesson, level 2
        Lesson l8 = lesson(8L, "Jazz swing");
        l8.setGenre(jazz);
        l8.setBelongsToGenre(true);
        l8.setLevel(2);
        l8.setCreatedAt(LocalDateTime.of(2021, 1, 1, 0, 0));
        em.persist(l8);

        em.flush();
        em.clear();
    }

    private static List<Long> ids(Collection<Lesson> lessons) {
        return lessons.stream().map(Lesson::getId).sorted().toList();
    }

    private static List<Long> orderedIds(List<Lesson> lessons) {
        return lessons.stream().map(Lesson::getId).toList();
    }

    @Test
    void findByGenreIdAndBelongsToGenreTrue_ignoresInstrumentGenreLessons() {
        assertEquals(List.of(1L, 2L), ids(lessonRepository.findByGenreIdAndBelongsToGenreTrue(rock.getId())));
        assertEquals(List.of(8L), ids(lessonRepository.findByGenreIdAndBelongsToGenreTrue(jazz.getId())));
    }

    @Test
    void findByGenreIdAndBelongsToGenreTrue_unknownGenre_returnsEmpty() {
        assertTrue(lessonRepository.findByGenreIdAndBelongsToGenreTrue(999L).isEmpty());
    }

    @Test
    void findByPopularTrue_returnsPopularLessons() {
        assertEquals(List.of(1L, 5L), ids(lessonRepository.findByPopularTrue()));
    }

    @Test
    void findByGenreIdAndIdNotIn_includesAnyLessonWithThatGenre() {
        assertEquals(List.of(1L, 2L, 3L), ids(lessonRepository.findByGenreIdAndIdNotIn(rock.getId(), NONE, BY_ID)));
        assertEquals(List.of(3L), ids(lessonRepository.findByGenreIdAndIdNotIn(rock.getId(), List.of(1L, 2L), BY_ID)));
    }

    @Test
    void findByGenreIdAndIdNotIn_respectsPageSize() {
        assertEquals(2, lessonRepository.findByGenreIdAndIdNotIn(rock.getId(), NONE, PageRequest.of(0, 2)).size());
    }

    @Test
    void findAllByOrderByCreatedAtDescIdDesc_ordersByDateThenIdDescending() {
        assertEquals(List.of(3L, 2L, 4L, 1L, 5L, 7L, 6L, 8L),
                orderedIds(lessonRepository.findAllByOrderByCreatedAtDescIdDesc()));
    }

    @Test
    void findAllByOrderByLevelAscIdAsc_ordersByLevelThenId() {
        assertEquals(List.of(1L, 6L, 3L, 4L, 8L, 2L, 5L, 7L),
                orderedIds(lessonRepository.findAllByOrderByLevelAscIdAsc()));
    }

    @Test
    void findByIdNotIn_excludesIdsAndPaginates() {
        assertEquals(List.of(2L, 3L, 4L, 5L, 6L, 7L, 8L), ids(lessonRepository.findByIdNotIn(List.of(1L), BY_ID)));
        assertEquals(List.of(1L, 2L), orderedIds(lessonRepository.findByIdNotIn(NONE, PageRequest.of(0, 2, Sort.by("id")))));
    }

    @Test
    void findByGenreIdAndGenrePopularTrueAndBelongsToInstrumentGenreFalse_excludesInstrumentGenreLessons() {
        // Lesson 3 is genre-popular for rock but belongs to an instrument genre
        assertEquals(List.of(1L), ids(lessonRepository
                .findByGenreIdAndGenrePopularTrueAndBelongsToInstrumentGenreFalse(rock.getId(), BY_ID)));
        assertTrue(lessonRepository
                .findByGenreIdAndGenrePopularTrueAndBelongsToInstrumentGenreFalse(jazz.getId(), BY_ID).isEmpty());
    }

    @Test
    void findByInstrumentIdAndInstrumentPopularTrue_filtersByInstrumentAndFlag() {
        assertEquals(List.of(4L), ids(lessonRepository.findByInstrumentIdAndInstrumentPopularTrue(guitar.getId())));
        assertTrue(lessonRepository.findByInstrumentIdAndInstrumentPopularTrue(piano.getId()).isEmpty());
    }

    // --- Recommendation queries ---

    @Test
    void genreLessonsWithLevel_filterByGenresLevelAndExclusions() {
        assertEquals(List.of(1L, 8L), ids(lessonRepository
                .findByGenreIdInAndBelongsToGenreTrueAndLevelLessThanEqualAndIdNotIn(List.of(rock.getId(), jazz.getId()), 2, NONE, BY_ID)));
        assertEquals(List.of(8L), ids(lessonRepository
                .findByGenreIdInAndBelongsToGenreTrueAndLevelLessThanEqualAndIdNotIn(List.of(rock.getId(), jazz.getId()), 2, List.of(1L), BY_ID)));
    }

    @Test
    void genreLessonsWithLevel_levelIsInclusive() {
        assertEquals(List.of(1L, 2L), ids(lessonRepository
                .findByGenreIdInAndBelongsToGenreTrueAndLevelLessThanEqualAndIdNotIn(List.of(rock.getId()), 3, NONE, BY_ID)));
    }

    @Test
    void genreLessonsWithoutLevel_neverIncludeInstrumentGenreLessons() {
        assertEquals(List.of(1L, 2L), ids(lessonRepository
                .findByGenreIdInAndBelongsToGenreTrueAndIdNotIn(List.of(rock.getId()), NONE, BY_ID)));
    }

    @Test
    void genreLessons_emptyGenreList_returnsEmpty() {
        assertTrue(lessonRepository.findByGenreIdInAndBelongsToGenreTrueAndIdNotIn(List.of(), NONE, BY_ID).isEmpty());
    }

    @Test
    void instrumentLessonsWithLevel_filterByInstrumentsAndLevel() {
        assertEquals(List.of(4L), ids(lessonRepository
                .findByInstrumentIdInAndBelongsToInstrumentTrueAndLevelLessThanEqualAndIdNotIn(List.of(guitar.getId(), piano.getId()), 3, NONE, BY_ID)));
        assertEquals(List.of(4L, 5L), ids(lessonRepository
                .findByInstrumentIdInAndBelongsToInstrumentTrueAndLevelLessThanEqualAndIdNotIn(List.of(guitar.getId(), piano.getId()), 4, NONE, BY_ID)));
    }

    @Test
    void instrumentLessonsWithoutLevel_excludeIds() {
        assertEquals(List.of(5L), ids(lessonRepository
                .findByInstrumentIdInAndBelongsToInstrumentTrueAndIdNotIn(List.of(guitar.getId(), piano.getId()), List.of(4L), BY_ID)));
    }

    @Test
    void genericLessonsWithLevel_returnOnlyLessonsWithoutOwner() {
        assertEquals(List.of(6L), ids(lessonRepository
                .findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndLevelLessThanEqualAndIdNotIn(3, NONE, BY_ID)));
    }

    @Test
    void genericLessonsWithoutLevel_returnOnlyLessonsWithoutOwner() {
        assertEquals(List.of(6L, 7L), ids(lessonRepository
                .findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndIdNotIn(NONE, BY_ID)));
        assertEquals(List.of(7L), ids(lessonRepository
                .findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndIdNotIn(List.of(6L), BY_ID)));
    }

    // --- Relations ---

    @Test
    void savingLessonWithSections_cascadesAndKeepsOrder() {
        Lesson lesson = lesson(50L, "With sections");
        lesson.addSection(lessonSection("Second", lesson, 1));
        lesson.addSection(lessonSection("First", lesson, 0));
        lessonRepository.saveAndFlush(lesson);
        em.clear();

        Lesson loaded = lessonRepository.findById(50L).orElseThrow();
        assertEquals(List.of("First", "Second"), loaded.getSections().stream().map(LessonSection::getTitle).toList());
    }

    @Test
    void lessonOfInstrumentGenre_keepsReferenceAndIndex() {
        Lesson loaded = lessonRepository.findById(3L).orElseThrow();

        assertTrue(loaded.isBelongsToInstrumentGenre());
        assertEquals(rockGuitar.getId(), loaded.getInstrumentGenre().getId());
        assertEquals(0, loaded.getIndexInInstrumentGenre());
    }
}
