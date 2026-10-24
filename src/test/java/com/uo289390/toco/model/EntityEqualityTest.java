package com.uo289390.toco.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import com.uo289390.toco.model.*;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the ID-based equals/hashCode contract of the entities that override it
 * (Item hierarchy, Chord, Instrument, SongLesson), plus small entity defaults.
 */
class EntityEqualityTest {

    private static Song song(Long id) {
        Song song = new Song();
        song.setId(id);
        return song;
    }

    private static Genre genre(Long id) {
        Genre genre = new Genre();
        genre.setId(id);
        return genre;
    }

    // --- Item hierarchy ---

    @Test
    void items_withSameId_areEqualAndShareHashCode() {
        Song a = song(1L);
        Song b = song(1L);
        a.setTitle("A");
        b.setTitle("B");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void items_withDifferentIds_areNotEqual() {
        assertNotEquals(song(1L), song(2L));
    }

    @Test
    void items_withNullId_areOnlyEqualToThemselves() {
        Song a = song(null);
        Song b = song(null);

        assertEquals(a, a);
        assertNotEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void items_ofDifferentSubtypesWithSameId_areEqual() {
        // Items share a single identifier space (JOINED inheritance), so equality is by ID only
        assertEquals(song(5L), genre(5L));
    }

    @Test
    void item_isNotEqualToNullOrOtherClass() {
        Song song = song(1L);

        assertNotEquals(null, song);
        assertNotEquals("1", song);
    }

    @Test
    void items_inHashSet_areDeduplicatedById() {
        Set<Item> set = new HashSet<>();
        set.add(song(1L));
        set.add(song(1L));
        set.add(song(2L));

        assertEquals(2, set.size());
    }

    // --- Chord ---

    @Test
    void chords_equalityIsById() {
        Chord a = new Chord();
        a.setId(1L);
        a.setName("C");
        Chord b = new Chord();
        b.setId(1L);
        b.setName("G");
        Chord c = new Chord();
        c.setId(2L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(new Chord(), new Chord());
        assertNotEquals(a, song(1L));
    }

    // --- Instrument ---

    @Test
    void instruments_equalityIsById() {
        Instrument a = new Instrument(1L, "https://x.com/a.png", "Guitar");
        Instrument b = new Instrument(1L, "https://x.com/b.png", "Bass");
        Instrument c = new Instrument(2L, "https://x.com/a.png", "Guitar");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(new Instrument(), new Instrument());
    }

    // --- SongLesson ---

    @Test
    @SuppressWarnings("EqualsWithItself")
    void entitiesWithoutId_areEqualToThemselves() {
        // Reflexivity must hold even before an ID is assigned (the id-based comparison alone would fail)
        Song song = song(null);
        Instrument instrument = new Instrument();
        SongLesson songLesson = new SongLesson();
        Chord chord = new Chord();

        assertTrue(song.equals(song));
        assertTrue(instrument.equals(instrument));
        assertTrue(songLesson.equals(songLesson));
        assertTrue(chord.equals(chord));
    }

    @Test
    void instrumentAndSongLesson_areNotEqualToOtherTypes() {
        Instrument instrument = new Instrument();
        instrument.setId(1L);
        SongLesson songLesson = new SongLesson();
        songLesson.setId(1L);

        assertNotEquals(instrument, songLesson);
        assertNotEquals(songLesson, instrument);
        assertNotEquals(null, instrument);
        assertFalse(song(1L).equals(instrument));
        assertFalse(song(1L).equals(null));
    }

    @Test
    void songLessons_equalityIsById() {
        SongLesson a = new SongLesson();
        a.setId(1L);
        SongLesson b = new SongLesson();
        b.setId(1L);
        SongLesson c = new SongLesson();
        c.setId(2L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(new SongLesson(), new SongLesson());
    }

    @Test
    void songLesson_defaults() {
        SongLesson songLesson = new SongLesson();

        assertNotNull(songLesson.getChords());
        assertNotNull(songLesson.getTabSections());
        assertTrue(songLesson.getChords().isEmpty());
        assertTrue(songLesson.getTabSections().isEmpty());
    }

    // --- Misc defaults ---

    @Test
    void song_toStringIncludesIdTitleAndArtist() {
        Song song = song(7L);
        song.setTitle("Imagine");
        song.setArtist("John Lennon");

        assertEquals("Song{id=7, title='Imagine', artist='John Lennon'}", song.toString());
    }

    @Test
    void song_defaultsHaveEmptyCollections() {
        Song song = new Song();

        assertTrue(song.getGenres().isEmpty());
        assertTrue(song.getInstruments().isEmpty());
        assertTrue(song.getLessons().isEmpty());
        assertFalse(song.isPopular());
        assertFalse(song.isNewSong());
    }

    @Test
    void preferences_defaultsAndConstants() {
        Preferences preferences = new Preferences();

        assertEquals(0, preferences.getDifficultyLevel());
        assertTrue(preferences.getFavoriteGenres().isEmpty());
        assertTrue(preferences.getFavoriteInstruments().isEmpty());
        assertEquals(2, Preferences.MIN_NUMBER_OF_FAVOURITE_GENRES);
        assertEquals(2, Preferences.MIN_NUMBER_OF_FAVOURITE_INSTRUMENTS);
    }

    @Test
    void genreAndInstrumentGenre_defaultLessonsAreEmpty() {
        assertTrue(new Genre().getLessons().isEmpty());
        assertTrue(new InstrumentGenre().getLessons().isEmpty());
    }

    @Test
    void settings_holdsFlags() {
        Settings settings = new Settings(1L, true, false);

        assertEquals(1L, settings.getId());
        assertTrue(settings.isNotifications());
        assertFalse(settings.isSound());
    }

    @Test
    void enums_exposeExpectedValues() {
        assertArrayEquals(new Role[] { Role.ADMIN, Role.USER }, Role.values());
        assertArrayEquals(new SongLesson.DifficultyLevel[] {
                SongLesson.DifficultyLevel.EASY, SongLesson.DifficultyLevel.MEDIUM, SongLesson.DifficultyLevel.ORIGINAL },
                SongLesson.DifficultyLevel.values());
        assertEquals(4, Item.Type.values().length);
    }
}
