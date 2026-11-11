package com.uo289390.toco.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import com.uo289390.toco.model.*;
import com.uo289390.toco.utils.RecommendationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

/**
 * Unit tests for the User domain logic: defaults, authorities, the preferences
 * back-reference and the aggregations over the user's item lists.
 */
class UserTest {

    private User user;

    private Genre rock;
    private Genre jazz;
    private Instrument guitar;
    private Instrument piano;

    @BeforeEach
    void setUp() {
        user = new User();
        rock = genre(100L);
        jazz = genre(101L);
        guitar = instrument(1L);
        piano = instrument(2L);
    }

    private static Genre genre(long id) {
        Genre genre = new Genre();
        genre.setId(id);
        return genre;
    }

    private static Instrument instrument(long id) {
        Instrument instrument = new Instrument();
        instrument.setId(id);
        return instrument;
    }

    private static Song song(long id, Set<Genre> genres, Set<Instrument> instruments) {
        Song song = new Song();
        song.setId(id);
        song.setGenres(genres);
        song.setInstruments(instruments);
        return song;
    }

    private ItemList addList(Item... items) {
        ItemList list = new ItemList();
        list.setUser(user);
        for (Item item : items) {
            list.addItem(item);
        }
        user.getItemLists().add(list);
        return list;
    }

    @Test
    void defaults_areUserRoleAndLevelOne() {
        assertEquals(Role.USER, user.getRole());
        assertEquals(1, user.getDifficultyLevel());
        assertNotNull(user.getItemLists());
        assertTrue(user.getItemLists().isEmpty());
        assertNull(user.getSpotifyUser());
    }

    @Test
    void getAuthorities_returnsRoleName() {
        user.setRole(Role.ADMIN);

        List<String> authorities = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        assertEquals(List.of("ADMIN"), authorities);
    }

    @Test
    void getAuthorities_defaultRole_isUser() {
        assertEquals(List.of("USER"), user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }

    @Test
    void getPreferences_linksBackReference() {
        Preferences preferences = user.getPreferences();

        assertNotNull(preferences);
        assertSame(user, preferences.getUser());
    }

    @Test
    void getPreferences_whenNull_createsNewLinkedPreferences() {
        user = new User(null, null, null, null, null, null, Role.USER, 1, null, new java.util.ArrayList<>(), null, null, null);

        Preferences preferences = user.getPreferences();

        assertNotNull(preferences);
        assertSame(user, preferences.getUser());
        assertSame(preferences, user.getPreferences());
    }

    @Test
    void setPreferences_linksBackReference() {
        Preferences preferences = new Preferences();

        user.setPreferences(preferences);

        assertSame(preferences, user.getPreferences());
        assertSame(user, preferences.getUser());
    }

    @Test
    void setPreferences_null_replacesWithEmptyLinkedPreferences() {
        user.setPreferences(null);

        assertNotNull(user.getPreferences());
        assertSame(user, user.getPreferences().getUser());
        assertTrue(user.getPreferences().getFavoriteGenres().isEmpty());
    }

    @Test
    void hasItemInLists_findsItemInAnyList() {
        Song song = song(1L, Set.of(), Set.of());
        addList(rock);
        addList(song);

        assertTrue(user.hasItemInLists(song));
        assertTrue(user.hasItemInLists(rock));
        assertFalse(user.hasItemInLists(jazz));
    }

    @Test
    void hasItemInLists_noLists_returnsFalse() {
        assertFalse(user.hasItemInLists(rock));
    }

    @Test
    void getAllFromItemListsOfType_mergesListsWithoutDuplicates() {
        Song a = song(1L, Set.of(), Set.of());
        Song b = song(2L, Set.of(), Set.of());
        addList(a, rock);
        addList(a, b);

        assertEquals(Set.of(a, b), user.getAllFromItemListsOfType(Song.class));
        assertEquals(Set.of(rock), user.getAllFromItemListsOfType(Genre.class));
        assertTrue(user.getAllFromItemListsOfType(Lesson.class).isEmpty());
    }

    @Test
    void getIdsExcludedFromRecommendationsOfType_onlyReturnsItemsInEnoughLists() {
        assertEquals(3, RecommendationConstants.N_ITEM_LISTS_TO_EXCLUDE_FROM_RECOMMENDATIONS);
        Song inThree = song(1L, Set.of(), Set.of());
        Song inTwo = song(2L, Set.of(), Set.of());
        addList(inThree, inTwo, rock);
        addList(inThree, inTwo, rock);
        addList(inThree, rock);

        assertEquals(Set.of(1L), user.getIdsExcludedFromRecommendationsOfType(Song.class));
        assertEquals(Set.of(100L), user.getIdsExcludedFromRecommendationsOfType(Genre.class));
        assertTrue(user.getIdsExcludedFromRecommendationsOfType(Lesson.class).isEmpty());
    }

    @Test
    void getIdsExcludedFromRecommendationsOfType_withItemsInFewerListsIsEmpty() {
        addList(song(1L, Set.of(), Set.of()), rock);
        addList(song(1L, Set.of(), Set.of()));

        assertTrue(user.getIdsExcludedFromRecommendationsOfType(Song.class).isEmpty());
        assertTrue(user.getIdsExcludedFromRecommendationsOfType(Genre.class).isEmpty());
    }

    @Test
    void getTopGenresFromItemLists_aggregatesAcrossLists() {
        addList(song(1L, Set.of(rock), Set.of()), song(2L, Set.of(jazz), Set.of()));
        addList(song(3L, Set.of(jazz), Set.of()));

        assertEquals(List.of(jazz, rock), user.getTopGenresFromItemLists(2));
        assertEquals(List.of(jazz), user.getTopGenresFromItemLists(1));
    }

    @Test
    void getTopGenresFromItemLists_sameItemInTwoLists_countsTwice() {
        Song shared = song(1L, Set.of(rock), Set.of());
        addList(shared);
        addList(shared, song(2L, Set.of(jazz), Set.of()));

        assertEquals(List.of(rock), user.getTopGenresFromItemLists(1));
    }

    @Test
    void getTopInstrumentsFromItemLists_aggregatesAcrossLists() {
        addList(song(1L, Set.of(), Set.of(guitar)));
        addList(song(2L, Set.of(), Set.of(piano, guitar)));

        assertEquals(List.of(guitar, piano), user.getTopInstrumentsFromItemLists(2));
    }

    @Test
    void topRankings_noLists_returnEmpty() {
        assertTrue(user.getTopGenresFromItemLists(3).isEmpty());
        assertTrue(user.getTopInstrumentsFromItemLists(3).isEmpty());
    }

    // -------- Top artists --------

    private static Song songBy(long id, String artist) {
        Song song = new Song();
        song.setId(id);
        song.setArtist(artist);
        return song;
    }

    @Test
    void getTopArtistsFromItemLists_ordersByRepetitionsAcrossLists() {
        addList(songBy(1L, "Queen"), songBy(2L, "Bowie"), songBy(3L, "Queen"));
        addList(songBy(4L, "Bowie"), songBy(5L, "Bowie"), songBy(6L, "Oasis"));

        assertEquals(List.of("Bowie", "Queen", "Oasis"), user.getTopArtistsFromItemLists(5));
        assertEquals(List.of("Bowie"), user.getTopArtistsFromItemLists(1));
    }

    @Test
    void getTopArtistsFromItemLists_tiesKeepTheOrderOfFirstAppearance() {
        addList(songBy(1L, "Oasis"), songBy(2L, "Blur"), songBy(3L, "Pulp"));

        assertEquals(List.of("Oasis", "Blur"), user.getTopArtistsFromItemLists(2));
    }

    @Test
    void getTopArtistsFromItemLists_sameSongInTwoLists_countsTwice() {
        Song shared = songBy(1L, "Queen");
        addList(shared, songBy(2L, "Bowie"));
        addList(shared, songBy(3L, "Bowie"), songBy(4L, "Queen"));

        // Queen: 3 (the shared song counts once per list); Bowie: 2
        assertEquals(List.of("Queen", "Bowie"), user.getTopArtistsFromItemLists(2));
    }

    @Test
    void getTopArtistsFromItemLists_ignoresBlankArtistsAndNonSongItems() {
        Lesson lesson = new Lesson();
        lesson.setId(9L);
        addList(songBy(1L, " "), songBy(2L, null), songBy(3L, " Queen "), lesson);

        assertEquals(List.of("Queen"), user.getTopArtistsFromItemLists(5));
    }

    @Test
    void getTopArtistsFromItemLists_withoutSongsOrNonPositiveN_returnsEmpty() {
        assertTrue(user.getTopArtistsFromItemLists(5).isEmpty());
        addList(songBy(1L, "Queen"));
        assertTrue(user.getTopArtistsFromItemLists(0).isEmpty());
    }
}
