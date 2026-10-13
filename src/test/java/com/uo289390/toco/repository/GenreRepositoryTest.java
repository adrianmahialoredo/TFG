package com.uo289390.toco.repository;

import static com.uo289390.toco.repository.RepositoryTestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import com.uo289390.toco.repository.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.Lesson;

/**
 * Integration tests for GenreRepository.
 */
@DataJpaTest
class GenreRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private GenreRepository genreRepository;

    @BeforeEach
    void setUp() {
        em.persist(genre(1L, "Rock"));
        em.persist(genre(2L, "Jazz"));
        em.persist(genre(3L, "Blues"));
        em.flush();
        em.clear();
    }

    @Test
    void findByIdNotIn_excludesGivenIds() {
        List<Genre> result = genreRepository.findByIdNotIn(List.of(2L), PageRequest.of(0, 10, Sort.by("id")));

        assertEquals(List.of(1L, 3L), result.stream().map(Genre::getId).toList());
    }

    @Test
    void findByIdNotIn_respectsPageSizeAndSort() {
        List<Genre> result = genreRepository.findByIdNotIn(List.of(-1L), PageRequest.of(0, 2, Sort.by("title")));

        assertEquals(List.of("Blues", "Jazz"), result.stream().map(Genre::getTitle).toList());
    }

    @Test
    void findByIdNotIn_allExcluded_returnsEmpty() {
        assertTrue(genreRepository.findByIdNotIn(List.of(1L, 2L, 3L), PageRequest.of(0, 10)).isEmpty());
    }

    @Test
    void findById_returnsGenreWithAllFields() {
        Genre loaded = genreRepository.findById(1L).orElseThrow();

        assertEquals("Rock", loaded.getTitle());
        assertEquals("Rock description", loaded.getDescription());
        assertEquals("https://example.com/1-banner.png", loaded.getBannerUrl());
        assertEquals("intro1", loaded.getWelcomeIntroVideoId());
        assertEquals("song1", loaded.getWelcomeSongVideoId());
    }

    @Test
    void setLessons_persistsTheLinkOnTheOwningSide() {
        // Regression: lessons were only added to the inverse (mappedBy) collection, so genre_id was never written
        em.persist(lesson(10L, "Riffs"));
        em.persist(lesson(11L, "Solos"));
        em.flush();
        em.clear();

        Genre rock = genreRepository.findById(1L).orElseThrow();
        rock.setLessons(Set.of(em.find(Lesson.class, 10L), em.find(Lesson.class, 11L)));
        em.flush();
        em.clear();

        assertEquals(1L, em.find(Lesson.class, 10L).getGenre().getId());
        assertEquals(1L, em.find(Lesson.class, 11L).getGenre().getId());
        assertEquals(Set.of(10L, 11L), genreRepository.findById(1L).orElseThrow().getLessons().stream()
                .map(Lesson::getId).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void setLessons_unlinksRemovedLessonsAndMovesLessonsBetweenGenres() {
        Genre rock = em.find(Genre.class, 1L);
        Lesson riffs = lesson(10L, "Riffs");
        Lesson solos = lesson(11L, "Solos");
        riffs.setGenre(rock);
        solos.setGenre(rock);
        em.persist(riffs);
        em.persist(solos);
        em.flush();
        em.clear();

        Genre loadedRock = genreRepository.findById(1L).orElseThrow();
        Genre jazz = genreRepository.findById(2L).orElseThrow();
        loadedRock.setLessons(Set.of(em.find(Lesson.class, 10L)));
        jazz.setLessons(Set.of(em.find(Lesson.class, 10L)));
        em.flush();
        em.clear();

        assertEquals(2L, em.find(Lesson.class, 10L).getGenre().getId());
        assertNull(em.find(Lesson.class, 11L).getGenre());
        assertTrue(genreRepository.findById(1L).orElseThrow().getLessons().isEmpty());
    }
}
