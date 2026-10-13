package com.uo289390.toco.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.Lesson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Genre.setLessons, which must keep both sides of the
 * Genre / Lesson relationship in sync (Lesson.genre owns it).
 */
class GenreLessonsTest {

    private Genre rock;
    private Genre jazz;

    @BeforeEach
    void setUp() {
        rock = new Genre();
        rock.setId(1L);
        jazz = new Genre();
        jazz.setId(2L);
    }

    private static Lesson lesson(long id) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        return lesson;
    }

    private static Set<Long> ids(Set<Lesson> lessons) {
        return lessons.stream().map(Lesson::getId).collect(Collectors.toSet());
    }

    @Test
    void setLessons_setsTheOwningSide() {
        Lesson riffs = lesson(10L);
        Lesson solos = lesson(11L);

        rock.setLessons(List.of(riffs, solos));

        assertSame(rock, riffs.getGenre());
        assertSame(rock, solos.getGenre());
        assertEquals(Set.of(10L, 11L), ids(rock.getLessons()));
    }

    @Test
    void setLessons_unlinksLessonsNoLongerIncluded() {
        Lesson riffs = lesson(10L);
        Lesson solos = lesson(11L);
        rock.setLessons(List.of(riffs, solos));

        rock.setLessons(List.of(riffs));

        assertSame(rock, riffs.getGenre());
        assertNull(solos.getGenre());
        assertEquals(Set.of(10L), ids(rock.getLessons()));
    }

    @Test
    void setLessons_movesLessonFromItsPreviousGenre() {
        Lesson riffs = lesson(10L);
        rock.setLessons(List.of(riffs));

        jazz.setLessons(List.of(riffs));

        assertSame(jazz, riffs.getGenre());
        assertTrue(rock.getLessons().isEmpty());
        assertEquals(Set.of(10L), ids(jazz.getLessons()));
    }

    @Test
    void setLessons_null_unlinksEveryLesson() {
        Lesson riffs = lesson(10L);
        rock.setLessons(List.of(riffs));

        rock.setLessons(null);

        assertNull(riffs.getGenre());
        assertTrue(rock.getLessons().isEmpty());
    }

    @Test
    void setLessons_duplicates_areStoredOnce() {
        Lesson riffs = lesson(10L);

        rock.setLessons(List.of(riffs, riffs));

        assertEquals(1, rock.getLessons().size());
    }

    @Test
    void setLessons_doesNotChangeBelongsToGenreFlag() {
        Lesson riffs = lesson(10L);

        rock.setLessons(List.of(riffs));

        assertFalse(riffs.isBelongsToGenre());
    }
}
