package com.uo289390.toco.repository;

import static com.uo289390.toco.repository.RepositoryTestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.Song;
import com.uo289390.toco.model.SongLesson;
import com.uo289390.toco.model.TabSection;

/**
 * Integration tests for TabSectionRepository: the {@code findBySongLessonId} derived query,
 * generated IDs, long tab content and moving a section to another song lesson.
 */
@DataJpaTest
class TabSectionRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private TabSectionRepository tabSectionRepository;

    private SongLesson first;
    private SongLesson second;

    @BeforeEach
    void setUp() {
        Instrument guitar = em.persist(instrument(1L, "Guitar"));
        Song song = em.persist(song(10L, "Song", "Artist"));
        first = em.persist(songLesson(100L, song, guitar));
        second = em.persist(songLesson(101L, song, guitar));
        em.persist(songLesson(102L, song, guitar));

        em.persist(tabSection("Intro", first));
        em.persist(tabSection("Riff", first));
        em.persist(tabSection("Outro", first));
        em.persist(tabSection("Solo", second));
        em.flush();
        em.clear();
    }

    private List<String> identifiersOf(Long songLessonId) {
        return tabSectionRepository.findBySongLessonId(songLessonId).stream()
                .map(TabSection::getIdentifier).sorted().toList();
    }

    @Test
    void findBySongLessonId_returnsOnlySectionsOfThatLesson() {
        assertEquals(List.of("Intro", "Outro", "Riff"), identifiersOf(100L));
        assertEquals(List.of("Solo"), identifiersOf(101L));
    }

    @Test
    void findBySongLessonId_lessonWithoutSections_returnsEmpty() {
        assertTrue(tabSectionRepository.findBySongLessonId(102L).isEmpty());
    }

    @Test
    void findBySongLessonId_unknownOrNullLesson_returnsEmpty() {
        assertTrue(tabSectionRepository.findBySongLessonId(999L).isEmpty());
        assertTrue(tabSectionRepository.findBySongLessonId(null).isEmpty());
    }

    @Test
    void save_generatesIdAndKeepsSongLesson() {
        TabSection saved = tabSectionRepository.saveAndFlush(tabSection("Bridge", em.find(SongLesson.class, 102L)));
        em.clear();

        assertNotNull(saved.getId());
        TabSection loaded = tabSectionRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Bridge", loaded.getIdentifier());
        assertEquals(102L, loaded.getSongLesson().getId());
    }

    @Test
    void save_persistsLongMultilineContent() {
        String content = ("e|---0---3---5---|\n").repeat(500);
        TabSection section = tabSection("Long", em.find(SongLesson.class, 102L));
        section.setContent(content);
        Long id = tabSectionRepository.saveAndFlush(section).getId();
        em.clear();

        assertEquals(content, tabSectionRepository.findById(id).orElseThrow().getContent());
    }

    @Test
    void movingSectionToAnotherLesson_isReflectedInQuery() {
        TabSection solo = tabSectionRepository.findBySongLessonId(101L).get(0);
        solo.setSongLesson(em.find(SongLesson.class, 100L));
        tabSectionRepository.saveAndFlush(solo);
        em.clear();

        assertEquals(List.of("Intro", "Outro", "Riff", "Solo"), identifiersOf(100L));
        assertTrue(tabSectionRepository.findBySongLessonId(101L).isEmpty());
    }

    @Test
    void deleteById_removesOnlyThatSection() {
        Long introId = tabSectionRepository.findBySongLessonId(100L).stream()
                .filter(section -> section.getIdentifier().equals("Intro")).findFirst().orElseThrow().getId();

        tabSectionRepository.deleteById(introId);
        tabSectionRepository.flush();
        em.clear();

        assertEquals(List.of("Outro", "Riff"), identifiersOf(100L));
        assertEquals(3, tabSectionRepository.count());
    }
}
