package com.uo289390.toco.repository;

import static com.uo289390.toco.repository.RepositoryTestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import com.uo289390.toco.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.Item;
import com.uo289390.toco.model.Lesson;
import com.uo289390.toco.model.Song;

/**
 * Integration tests for ItemRepository, including polymorphic loading of the JOINED item hierarchy.
 */
@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemRepository itemRepository;

    private void seedMixedItems() {
        em.persist(song(1L, "Rock Anthem", "Band"));
        em.persist(genre(2L, "Rock"));
        em.persist(lesson(3L, "Intro to ROCK"));
        em.persist(lesson(4L, "Jazz chords"));
        em.flush();
        em.clear();
    }

    @Test
    void findByTitleContainingIgnoreCase_matchesAcrossItemTypes() {
        seedMixedItems();

        List<Item> result = itemRepository.findByTitleContainingIgnoreCase("rock");

        assertEquals(List.of(1L, 2L, 3L), result.stream().map(Item::getId).sorted().toList());
    }

    @Test
    void findByTitleContainingIgnoreCase_returnsConcreteSubclasses() {
        seedMixedItems();

        List<Item> result = itemRepository.findByTitleContainingIgnoreCase("rock");

        assertTrue(result.stream().anyMatch(Song.class::isInstance));
        assertTrue(result.stream().anyMatch(Genre.class::isInstance));
        assertTrue(result.stream().anyMatch(Lesson.class::isInstance));
    }

    @Test
    void findByTitleContainingIgnoreCase_noMatch_returnsEmpty() {
        seedMixedItems();

        assertTrue(itemRepository.findByTitleContainingIgnoreCase("metal").isEmpty());
    }

    @Test
    void findByTitleContainingIgnoreCase_emptyString_matchesEverything() {
        seedMixedItems();

        assertEquals(4, itemRepository.findByTitleContainingIgnoreCase("").size());
    }

    @Test
    void findTop20ByIdNotIn_returnsAtMost20NewestFirst() {
        LocalDateTime base = LocalDateTime.of(2024, 1, 1, 0, 0);
        for (long i = 1; i <= 25; i++) {
            Lesson lesson = lesson(i, "Lesson " + i);
            lesson.setCreatedAt(base.plusDays(i));
            em.persist(lesson);
        }
        em.flush();
        em.clear();

        List<Item> result = itemRepository.findTop20ByIdNotInOrderByCreatedAtDesc(List.of(-1L));

        assertEquals(20, result.size());
        assertEquals(25L, result.getFirst().getId());
        assertEquals(6L, result.getLast().getId());
    }

    @Test
    void findTop20ByIdNotIn_skipsExcludedItemsAndFillsWithOlderOnes() {
        LocalDateTime base = LocalDateTime.of(2024, 1, 1, 0, 0);
        for (long i = 1; i <= 25; i++) {
            Lesson lesson = lesson(i, "Lesson " + i);
            lesson.setCreatedAt(base.plusDays(i));
            em.persist(lesson);
        }
        em.flush();
        em.clear();

        List<Item> result = itemRepository.findTop20ByIdNotInOrderByCreatedAtDesc(List.of(25L, 24L));

        assertEquals(20, result.size());
        assertEquals(23L, result.getFirst().getId());
        assertEquals(4L, result.getLast().getId());
        assertTrue(result.stream().noneMatch(item -> item.getId() == 25L || item.getId() == 24L));
    }

    @Test
    void findTop20ByIdNotIn_fewerItems_returnsAll() {
        seedMixedItems();

        assertEquals(4, itemRepository.findTop20ByIdNotInOrderByCreatedAtDesc(List.of(-1L)).size());
    }

    @Test
    void findTop20ByIdNotIn_noItems_returnsEmpty() {
        assertTrue(itemRepository.findTop20ByIdNotInOrderByCreatedAtDesc(List.of(-1L)).isEmpty());
    }

    @Test
    void persistedItems_keepTheirType() {
        seedMixedItems();

        assertEquals(Item.Type.Song, itemRepository.findById(1L).orElseThrow().getType());
        assertEquals(Item.Type.Genre, itemRepository.findById(2L).orElseThrow().getType());
        assertEquals(Item.Type.Lesson, itemRepository.findById(3L).orElseThrow().getType());
    }
}
