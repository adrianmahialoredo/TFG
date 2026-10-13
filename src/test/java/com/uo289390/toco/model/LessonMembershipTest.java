package com.uo289390.toco.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.uo289390.toco.model.InstrumentGenre;
import com.uo289390.toco.model.Lesson;
import com.uo289390.toco.model.LessonSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Lesson / InstrumentGenre relationship helpers: sections,
 * joining and leaving an instrument genre, and InstrumentGenre.setLessons reordering.
 */
class LessonMembershipTest {

    private InstrumentGenre rockGuitar;
    private InstrumentGenre jazzGuitar;

    @BeforeEach
    void setUp() {
        rockGuitar = new InstrumentGenre();
        rockGuitar.setId(200L);
        jazzGuitar = new InstrumentGenre();
        jazzGuitar.setId(201L);
    }

    private static Lesson lesson(long id) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        return lesson;
    }

    private static List<Long> ids(java.util.Collection<Lesson> lessons) {
        return lessons.stream().map(Lesson::getId).toList();
    }

    // --- Lesson ---

    @Test
    void newLesson_defaults() {
        Lesson lesson = new Lesson();

        assertFalse(lesson.isBelongsToGenre());
        assertFalse(lesson.isBelongsToInstrumentGenre());
        assertFalse(lesson.isBelongsToInstrument());
        assertEquals(0, lesson.getIndexInInstrumentGenre());
        assertNotNull(lesson.getSections());
        assertTrue(lesson.getSections().isEmpty());
        assertNotNull(lesson.getCreatedAt());
    }

    @Test
    void addSection_appendsAndLinksBackReference() {
        Lesson lesson = lesson(1L);
        LessonSection first = new LessonSection();
        LessonSection second = new LessonSection();

        lesson.addSection(first);
        lesson.addSection(second);

        assertEquals(List.of(first, second), lesson.getSections());
        assertSame(lesson, first.getLesson());
        assertSame(lesson, second.getLesson());
    }

    @Test
    void joinInstrumentGenre_setsReferenceFlagAndIndex() {
        Lesson lesson = lesson(1L);

        lesson.joinInstrumentGenre(rockGuitar, 3);

        assertSame(rockGuitar, lesson.getInstrumentGenre());
        assertTrue(lesson.isBelongsToInstrumentGenre());
        assertEquals(3, lesson.getIndexInInstrumentGenre());
    }

    @Test
    void leaveInstrumentGenre_resetsMembership() {
        Lesson lesson = lesson(1L);
        lesson.joinInstrumentGenre(rockGuitar, 3);

        lesson.leaveInstrumentGenre();

        assertNull(lesson.getInstrumentGenre());
        assertFalse(lesson.isBelongsToInstrumentGenre());
        assertEquals(0, lesson.getIndexInInstrumentGenre());
    }

    // --- InstrumentGenre.setLessons ---

    @Test
    void setLessons_assignsIndexesInGivenOrder() {
        Lesson a = lesson(1L);
        Lesson b = lesson(2L);
        Lesson c = lesson(3L);

        rockGuitar.setLessons(List.of(c, a, b));

        assertEquals(List.of(3L, 1L, 2L), ids(rockGuitar.getLessons()));
        assertEquals(0, c.getIndexInInstrumentGenre());
        assertEquals(1, a.getIndexInInstrumentGenre());
        assertEquals(2, b.getIndexInInstrumentGenre());
        assertTrue(List.of(a, b, c).stream().allMatch(l -> l.getInstrumentGenre() == rockGuitar && l.isBelongsToInstrumentGenre()));
    }

    @Test
    void setLessons_ignoresDuplicates() {
        Lesson a = lesson(1L);
        Lesson b = lesson(2L);

        rockGuitar.setLessons(Arrays.asList(a, b, a));

        assertEquals(List.of(1L, 2L), ids(rockGuitar.getLessons()));
        assertEquals(0, a.getIndexInInstrumentGenre());
        assertEquals(1, b.getIndexInInstrumentGenre());
    }

    @Test
    void setLessons_releasesLessonsNoLongerIncluded() {
        Lesson a = lesson(1L);
        Lesson b = lesson(2L);
        rockGuitar.setLessons(List.of(a, b));

        rockGuitar.setLessons(List.of(b));

        assertEquals(List.of(2L), ids(rockGuitar.getLessons()));
        assertNull(a.getInstrumentGenre());
        assertFalse(a.isBelongsToInstrumentGenre());
        assertEquals(0, b.getIndexInInstrumentGenre());
    }

    @Test
    void setLessons_reorderingUpdatesIndexes() {
        Lesson a = lesson(1L);
        Lesson b = lesson(2L);
        rockGuitar.setLessons(List.of(a, b));

        rockGuitar.setLessons(List.of(b, a));

        assertEquals(List.of(2L, 1L), ids(rockGuitar.getLessons()));
        assertEquals(1, a.getIndexInInstrumentGenre());
        assertEquals(0, b.getIndexInInstrumentGenre());
    }

    @Test
    void setLessons_null_clearsAndReleasesAll() {
        Lesson a = lesson(1L);
        rockGuitar.setLessons(List.of(a));

        rockGuitar.setLessons(null);

        assertTrue(rockGuitar.getLessons().isEmpty());
        assertFalse(a.isBelongsToInstrumentGenre());
    }

    @Test
    void setLessons_empty_clearsAndReleasesAll() {
        Lesson a = lesson(1L);
        rockGuitar.setLessons(List.of(a));

        rockGuitar.setLessons(new ArrayList<>());

        assertTrue(rockGuitar.getLessons().isEmpty());
        assertNull(a.getInstrumentGenre());
    }

    @Test
    void setLessons_movesLessonFromPreviousInstrumentGenre() {
        Lesson a = lesson(1L);
        Lesson b = lesson(2L);
        jazzGuitar.setLessons(List.of(a, b));

        rockGuitar.setLessons(List.of(a));

        assertSame(rockGuitar, a.getInstrumentGenre());
        assertEquals(List.of(1L), ids(rockGuitar.getLessons()));
        assertEquals(List.of(2L), ids(jazzGuitar.getLessons()));
    }
}
