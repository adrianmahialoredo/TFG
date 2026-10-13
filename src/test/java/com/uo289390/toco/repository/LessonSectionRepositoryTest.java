package com.uo289390.toco.repository;

import static com.uo289390.toco.repository.RepositoryTestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import com.uo289390.toco.repository.LessonRepository;
import com.uo289390.toco.repository.LessonSectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.uo289390.toco.model.Lesson;
import com.uo289390.toco.model.LessonSection;

/**
 * Integration tests for LessonSectionRepository.
 */
@DataJpaTest
class LessonSectionRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private LessonSectionRepository lessonSectionRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @BeforeEach
    void setUp() {
        Lesson first = lesson(1L, "First");
        first.addSection(lessonSection("A", first, 0));
        first.addSection(lessonSection("B", first, 1));
        em.persist(first);

        Lesson second = lesson(2L, "Second");
        second.addSection(lessonSection("C", second, 0));
        em.persist(second);

        em.persist(lesson(3L, "Empty"));
        em.flush();
        em.clear();
    }

    @Test
    void findByLessonId_returnsSectionsOfThatLesson() {
        List<String> titles = lessonSectionRepository.findByLessonId(1L).stream()
                .map(LessonSection::getTitle).sorted().toList();

        assertEquals(List.of("A", "B"), titles);
        assertEquals(1, lessonSectionRepository.findByLessonId(2L).size());
    }

    @Test
    void findByLessonId_lessonWithoutSections_returnsEmpty() {
        assertTrue(lessonSectionRepository.findByLessonId(3L).isEmpty());
        assertTrue(lessonSectionRepository.findByLessonId(999L).isEmpty());
    }

    @Test
    void sectionsGetGeneratedIds() {
        assertTrue(lessonSectionRepository.findAll().stream().allMatch(section -> section.getId() != null));
    }

    @Test
    void removingSectionFromLesson_deletesOrphan() {
        Lesson lesson = lessonRepository.findById(1L).orElseThrow();
        lesson.getSections().removeIf(section -> section.getTitle().equals("A"));
        lessonRepository.saveAndFlush(lesson);

        assertEquals(List.of("B"), lessonSectionRepository.findByLessonId(1L).stream().map(LessonSection::getTitle).toList());
        assertEquals(2, lessonSectionRepository.count());
    }

    @Test
    void deletingLesson_cascadesToSections() {
        lessonRepository.deleteById(1L);
        lessonRepository.flush();

        assertTrue(lessonSectionRepository.findByLessonId(1L).isEmpty());
        assertEquals(1, lessonSectionRepository.count());
    }
}
