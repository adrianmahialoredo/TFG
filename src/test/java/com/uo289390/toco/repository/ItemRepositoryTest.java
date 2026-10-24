package com.uo289390.toco.repository;

import static com.uo289390.toco.repository.RepositoryTestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

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
    void persistedItems_keepTheirType() {
        seedMixedItems();

        assertEquals(Item.Type.Song, itemRepository.findById(1L).orElseThrow().getType());
        assertEquals(Item.Type.Genre, itemRepository.findById(2L).orElseThrow().getType());
        assertEquals(Item.Type.Lesson, itemRepository.findById(3L).orElseThrow().getType());
    }
}
