package com.uo289390.toco.repository;

import static com.uo289390.toco.repository.RepositoryTestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import com.uo289390.toco.repository.InstrumentRepository;
import com.uo289390.toco.repository.ItemListRepository;
import com.uo289390.toco.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.Item;
import com.uo289390.toco.model.ItemList;
import com.uo289390.toco.model.Song;
import com.uo289390.toco.model.User;

/**
 * Integration tests for ItemListRepository and InstrumentRepository: polymorphic list contents,
 * item order, cascades from the owning user and unique instrument names.
 */
@DataJpaTest
class ItemListRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemListRepository itemListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    private User owner;
    private Song song;
    private Genre genre;

    @BeforeEach
    void setUp() {
        owner = em.persist(user("owner"));
        song = em.persist(song(1L, "Song", "Artist"));
        genre = em.persist(genre(2L, "Rock"));
        em.flush();
    }

    private ItemList list(String name) {
        ItemList list = new ItemList();
        list.setName(name);
        list.setUser(owner);
        return list;
    }

    @Test
    void save_generatesIdAndKeepsItemOrderAndTypes() {
        ItemList list = list("Favourites");
        list.addItem(genre);
        list.addItem(song);
        Long id = itemListRepository.saveAndFlush(list).getId();
        em.clear();

        ItemList loaded = itemListRepository.findById(id).orElseThrow();
        assertNotNull(id);
        assertEquals(List.of(2L, 1L), loaded.getItems().stream().map(Item::getId).toList());
        assertEquals(1, loaded.getSongs().size());
        assertEquals(1, loaded.getGenres().size());
        assertEquals(owner.getId(), loaded.getUser().getId());
    }

    @Test
    void removingAndAddingItems_keepsInsertionOrderInDatabase() {
        Genre jazz = em.persist(genre(3L, "Jazz"));
        ItemList list = list("Practice");
        list.addItem(song);
        list.addItem(jazz);
        list.addItem(genre);
        Long id = itemListRepository.saveAndFlush(list).getId();
        em.clear();

        ItemList loaded = itemListRepository.findById(id).orElseThrow();
        loaded.getItems().removeIf(item -> item.getId().equals(3L));
        itemListRepository.saveAndFlush(loaded);
        em.clear();

        ItemList reloaded = itemListRepository.findById(id).orElseThrow();
        reloaded.addItem(em.find(Genre.class, 3L));
        itemListRepository.saveAndFlush(reloaded);
        em.clear();

        assertEquals(List.of(1L, 2L, 3L),
                itemListRepository.findById(id).orElseThrow().getItems().stream().map(Item::getId).toList());
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.getEntityManager().createNativeQuery(
                "select item_id, item_position from itemlist_items where itemlist_id = :id order by item_position")
                .setParameter("id", id).getResultList();
        assertEquals(List.of("1:0", "2:1", "3:2"),
                rows.stream().map(r -> ((Number) r[0]).longValue() + ":" + ((Number) r[1]).intValue()).toList());
    }

    @Test
    void save_withoutUser_fails() {
        ItemList list = new ItemList();
        list.setName("Orphan");

        assertThrows(Exception.class, () -> itemListRepository.saveAndFlush(list));
    }

    @Test
    void deletingList_doesNotDeleteItems() {
        ItemList list = list("Temp");
        list.addItem(song);
        Long id = itemListRepository.saveAndFlush(list).getId();

        itemListRepository.deleteById(id);
        itemListRepository.flush();

        assertFalse(itemListRepository.existsById(id));
        assertNotNull(em.find(Song.class, song.getId()));
    }

    @Test
    void removingListFromUser_deletesOrphanList() {
        ItemList list = list("Owned");
        owner.getItemLists().add(list);
        userRepository.saveAndFlush(owner);
        assertEquals(1, itemListRepository.count());

        owner.getItemLists().clear();
        userRepository.saveAndFlush(owner);

        assertEquals(0, itemListRepository.count());
    }

    @Test
    void instrumentRepository_crudAndUniqueName() {
        instrumentRepository.saveAndFlush(instrument(1L, "Guitar"));

        assertEquals("Guitar", instrumentRepository.findById(1L).orElseThrow().getName());
        Instrument duplicate = instrument(2L, "Guitar");
        assertThrows(DataIntegrityViolationException.class, () -> instrumentRepository.saveAndFlush(duplicate));
    }
}
