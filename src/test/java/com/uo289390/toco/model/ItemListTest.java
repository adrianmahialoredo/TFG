package com.uo289390.toco.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.uo289390.toco.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the ItemList domain logic: item management (no duplicates, order),
 * type filtering and the genre/instrument occurrence rankings derived from its items.
 */
class ItemListTest {

    private ItemList itemList;

    private Genre rock;
    private Genre jazz;
    private Genre blues;
    private Instrument guitar;
    private Instrument piano;

    @BeforeEach
    void setUp() {
        itemList = new ItemList();
        rock = genre(100L, "Rock");
        jazz = genre(101L, "Jazz");
        blues = genre(102L, "Blues");
        guitar = instrument(1L, "Guitar");
        piano = instrument(2L, "Piano");
    }

    // --- Builders ---

    private static Genre genre(long id, String title) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setTitle(title);
        return genre;
    }

    private static Instrument instrument(long id, String name) {
        Instrument instrument = new Instrument();
        instrument.setId(id);
        instrument.setName(name);
        return instrument;
    }

    private static Song song(long id, Set<Genre> genres, Set<Instrument> instruments) {
        Song song = new Song();
        song.setId(id);
        song.setGenres(new LinkedHashSet<>(genres));
        song.setInstruments(new LinkedHashSet<>(instruments));
        return song;
    }

    private static InstrumentGenre instrumentGenre(long id, Instrument instrument, Genre genre) {
        InstrumentGenre instrumentGenre = new InstrumentGenre();
        instrumentGenre.setId(id);
        instrumentGenre.setInstrument(instrument);
        instrumentGenre.setGenre(genre);
        return instrumentGenre;
    }

    private static Lesson genreLesson(long id, Genre genre) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.setGenre(genre);
        lesson.setBelongsToGenre(true);
        return lesson;
    }

    private static Lesson instrumentGenreLesson(long id, InstrumentGenre instrumentGenre) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.joinInstrumentGenre(instrumentGenre, 0);
        return lesson;
    }

    private void addAll(Item... items) {
        Arrays.stream(items).forEach(itemList::addItem);
    }

    @Nested
    class ItemManagement {

        @Test
        void newList_isEmpty() {
            assertEquals(0, itemList.getSize());
            assertTrue(itemList.getItems().isEmpty());
        }

        @Test
        void addItem_addsNewItem() {
            Song song = song(1L, Set.of(), Set.of());

            assertTrue(itemList.addItem(song));
            assertEquals(1, itemList.getSize());
            assertTrue(itemList.containsItem(song));
        }

        @Test
        void addItem_null_isRejected() {
            assertFalse(itemList.addItem(null));
            assertEquals(0, itemList.getSize());
        }

        @Test
        void addItem_duplicate_isRejected() {
            Song song = song(1L, Set.of(), Set.of());
            itemList.addItem(song);

            assertFalse(itemList.addItem(song));
            assertEquals(1, itemList.getSize());
        }

        @Test
        void addItem_differentInstanceWithSameId_isRejected() {
            itemList.addItem(song(1L, Set.of(), Set.of()));

            assertFalse(itemList.addItem(song(1L, Set.of(), Set.of())));
        }

        @Test
        void addItem_keepsInsertionOrder() {
            Song a = song(3L, Set.of(), Set.of());
            Song b = song(1L, Set.of(), Set.of());
            Song c = song(2L, Set.of(), Set.of());
            addAll(a, b, c);

            assertEquals(List.of(a, b, c), itemList.getItems());
        }

        @Test
        void removeItem_removesExistingItem() {
            Song song = song(1L, Set.of(), Set.of());
            itemList.addItem(song);

            itemList.removeItem(song);

            assertFalse(itemList.containsItem(song));
            assertEquals(0, itemList.getSize());
        }

        @Test
        void removeItem_missingItem_isNoOp() {
            itemList.addItem(song(1L, Set.of(), Set.of()));

            itemList.removeItem(song(2L, Set.of(), Set.of()));

            assertEquals(1, itemList.getSize());
        }

        @Test
        void setItems_removesDuplicatesAndKeepsOrder() {
            Song a = song(1L, Set.of(), Set.of());
            Song b = song(2L, Set.of(), Set.of());

            itemList.setItems(List.of(b, a, b));

            assertEquals(List.of(b, a), itemList.getItems());
        }

        @Test
        void setItems_null_resultsInEmptyMutableList() {
            itemList.setItems(null);

            assertTrue(itemList.getItems().isEmpty());
            assertTrue(itemList.addItem(song(1L, Set.of(), Set.of())));
        }

        @Test
        void setItems_copiesTheGivenCollection() {
            List<Item> source = new ArrayList<>(List.of(song(1L, Set.of(), Set.of())));
            itemList.setItems(source);

            source.add(song(2L, Set.of(), Set.of()));

            assertEquals(1, itemList.getSize());
        }

        @Test
        void containsItem_unknownItem_returnsFalse() {
            assertFalse(itemList.containsItem(song(1L, Set.of(), Set.of())));
        }
    }

    @Nested
    class TypeFiltering {

        @Test
        void getItemsOfType_returnsOnlyMatchingType() {
            Song song = song(1L, Set.of(), Set.of());
            Lesson lesson = genreLesson(2L, rock);
            addAll(song, rock, lesson);

            assertEquals(Set.of(song), itemList.getItemsOfType(Song.class));
            assertEquals(Set.of(lesson), itemList.getItemsOfType(Lesson.class));
            assertEquals(3, itemList.getItemsOfType(Item.class).size());
        }

        @Test
        void getSongsAndGenres_delegateToTypeFilter() {
            Song song = song(1L, Set.of(), Set.of());
            addAll(song, rock, jazz);

            assertEquals(Set.of(song), itemList.getSongs());
            assertEquals(Set.of(rock, jazz), itemList.getGenres());
        }

        @Test
        void getOnlyItemOfType_singleMatch_returnsIt() {
            addAll(rock, song(1L, Set.of(), Set.of()));

            assertEquals(Optional.of(rock), itemList.getOnlyGenre());
        }

        @Test
        void getOnlyItemOfType_noMatch_returnsEmpty() {
            addAll(song(1L, Set.of(), Set.of()));

            assertTrue(itemList.getOnlyGenre().isEmpty());
            assertTrue(itemList.getOnlyInstrumentGenre().isEmpty());
        }

        @Test
        void getOnlyItemOfType_severalMatches_returnsEmpty() {
            addAll(rock, jazz);

            assertTrue(itemList.getOnlyGenre().isEmpty());
        }

        @Test
        void getOnlyInstrumentGenre_singleMatch_returnsIt() {
            InstrumentGenre rockGuitar = instrumentGenre(200L, guitar, rock);
            addAll(rock, rockGuitar);

            assertEquals(Optional.of(rockGuitar), itemList.getOnlyInstrumentGenre());
        }
    }

    @Nested
    class GenreRankings {

        @Test
        void getTopGenresFromSongs_ranksByOccurrences() {
            addAll(song(1L, Set.of(rock), Set.of()),
                    song(2L, Set.of(rock, jazz), Set.of()),
                    song(3L, Set.of(rock, jazz, blues), Set.of()),
                    song(4L, Set.of(jazz), Set.of()));
            // rock: 3, jazz: 3 -> tie; blues: 1

            List<Genre> top = itemList.getTopGenresFromSongs(3);

            assertEquals(3, top.size());
            assertEquals(Set.of(rock, jazz), Set.copyOf(top.subList(0, 2)));
            assertEquals(blues, top.get(2));
        }

        @Test
        void getTopGenresFromSongs_limitsResult() {
            addAll(song(1L, Set.of(rock), Set.of()),
                    song(2L, Set.of(rock, jazz), Set.of()));

            assertEquals(List.of(rock), itemList.getTopGenresFromSongs(1));
        }

        @Test
        void getTopGenresFromSongs_ignoresNonSongItems() {
            addAll(jazz, genreLesson(5L, jazz), song(1L, Set.of(rock), Set.of()));

            assertEquals(List.of(rock), itemList.getTopGenresFromSongs(5));
        }

        @Test
        void getTopGenres_zeroOrNegativeN_returnsEmpty() {
            addAll(song(1L, Set.of(rock), Set.of()));

            assertTrue(itemList.getTopGenresFromSongs(0).isEmpty());
            assertTrue(itemList.getTopGenresFromSongs(-1).isEmpty());
        }

        @Test
        void getTopGenres_emptyList_returnsEmpty() {
            assertTrue(itemList.getTopGenresFromSongs(3).isEmpty());
            assertTrue(itemList.getTopGenresFromItems(3).isEmpty());
            assertTrue(itemList.getMainGenreFromItems().isEmpty());
        }

        @Test
        void getTopGenresFromInstrumentGenres_usesTheirGenre() {
            addAll(instrumentGenre(200L, guitar, jazz),
                    instrumentGenre(201L, piano, jazz),
                    instrumentGenre(202L, guitar, rock));

            assertEquals(List.of(jazz), itemList.getTopGenresFromInstrumentGenres(1));
            assertEquals(Optional.of(jazz), itemList.getMainGenreFromInstrumentGenres());
        }

        @Test
        void getTopGenresFromInstrumentGenres_ignoresNullGenre() {
            addAll(instrumentGenre(200L, guitar, null));

            assertTrue(itemList.getTopGenresFromInstrumentGenres(3).isEmpty());
        }

        @Test
        void getTopGenresFromLessons_countsGenreLessons() {
            addAll(genreLesson(1L, blues), genreLesson(2L, blues), genreLesson(3L, rock));

            assertEquals(List.of(blues), itemList.getTopGenresFromLessons(1));
            assertEquals(Optional.of(blues), itemList.getMainGenreFromLessons());
        }

        @Test
        void getTopGenresFromLessons_ignoresGenreWhenLessonDoesNotBelongToIt() {
            Lesson lesson = genreLesson(1L, rock);
            lesson.setBelongsToGenre(false);
            addAll(lesson);

            assertTrue(itemList.getTopGenresFromLessons(3).isEmpty());
        }

        @Test
        void getTopGenresFromLessons_belongsToGenreButWithoutGenre_contributesNothing() {
            Lesson lesson = genreLesson(1L, null);
            addAll(lesson, genreLesson(2L, rock));

            assertEquals(List.of(rock), itemList.getTopGenresFromLessons(3));
        }

        @Test
        void getTopGenresFromSongs_songWithNullGenres_isSkipped() {
            Song withoutGenres = song(1L, Set.of(), Set.of());
            withoutGenres.setGenres(null);
            addAll(withoutGenres, song(2L, Set.of(jazz), Set.of()));

            assertEquals(List.of(jazz), itemList.getTopGenresFromSongs(3));
        }

        @Test
        void getTopGenresFromLessons_usesInstrumentGenreGenre() {
            addAll(instrumentGenreLesson(1L, instrumentGenre(200L, guitar, jazz)));

            assertEquals(List.of(jazz), itemList.getTopGenresFromLessons(3));
        }

        @Test
        void getTopGenresFromLessons_sameGenreViaBothPaths_countsOnce() {
            Lesson lesson = instrumentGenreLesson(1L, instrumentGenre(200L, guitar, rock));
            lesson.setGenre(rock);
            lesson.setBelongsToGenre(true);
            addAll(lesson, genreLesson(2L, jazz), genreLesson(3L, jazz));

            // rock counted once from lesson 1, jazz twice -> jazz first
            assertEquals(List.of(jazz, rock), itemList.getTopGenresFromLessons(2));
        }

        @Test
        void getTopGenresFromLessons_ignoresInstrumentGenreWhenFlagIsOff() {
            Lesson lesson = new Lesson();
            lesson.setId(1L);
            lesson.setInstrumentGenre(instrumentGenre(200L, guitar, rock));
            lesson.setBelongsToInstrumentGenre(false);
            addAll(lesson);

            assertTrue(itemList.getTopGenresFromLessons(3).isEmpty());
        }

        @Test
        void getGenreOccurrencesFromItems_combinesAllSources() {
            addAll(song(1L, Set.of(rock, jazz), Set.of()),
                    instrumentGenre(200L, guitar, rock),
                    genreLesson(2L, blues),
                    rock); // a plain Genre item contributes no occurrence

            List<Genre> occurrences = itemList.getGenreOccurrencesFromItems();

            assertEquals(4, occurrences.size());
            assertEquals(2, occurrences.stream().filter(rock::equals).count());
        }

        @Test
        void getTopGenresFromItems_andMainGenre_useAllSources() {
            addAll(song(1L, Set.of(jazz), Set.of()),
                    instrumentGenre(200L, guitar, rock),
                    genreLesson(2L, rock),
                    song(3L, Set.of(rock), Set.of()));

            assertEquals(List.of(rock, jazz), itemList.getTopGenresFromItems(2));
            assertEquals(Optional.of(rock), itemList.getMainGenreFromItems());
        }

        @Test
        void getMainGenreFromSongs_returnsMostFrequent() {
            addAll(song(1L, Set.of(blues), Set.of()),
                    song(2L, Set.of(blues, rock), Set.of()));

            assertEquals(Optional.of(blues), itemList.getMainGenreFromSongs());
        }

        @Test
        void getMainGenre_noSources_returnsEmpty() {
            addAll(rock, jazz);

            assertTrue(itemList.getMainGenreFromSongs().isEmpty());
            assertTrue(itemList.getMainGenreFromLessons().isEmpty());
            assertTrue(itemList.getMainGenreFromInstrumentGenres().isEmpty());
        }
    }

    @Nested
    class InstrumentRankings {

        @Test
        void getTopInstrumentsFromSongs_ranksByOccurrences() {
            addAll(song(1L, Set.of(), Set.of(piano)),
                    song(2L, Set.of(), Set.of(piano, guitar)),
                    song(3L, Set.of(), Set.of(piano)));

            assertEquals(List.of(piano, guitar), itemList.getTopInstrumentsFromSongs(5));
            assertEquals(List.of(piano), itemList.getTopInstrumentsFromSongs(1));
        }

        @Test
        void getTopInstrumentsFromLessons_usesInstrumentGenreInstrument() {
            addAll(instrumentGenreLesson(1L, instrumentGenre(200L, guitar, rock)),
                    instrumentGenreLesson(2L, instrumentGenre(201L, guitar, jazz)),
                    instrumentGenreLesson(3L, instrumentGenre(202L, piano, jazz)));

            assertEquals(List.of(guitar, piano), itemList.getTopInstrumentsFromLessons(2));
        }

        @Test
        void getTopInstrumentsFromLessons_genreLessonsContributeNothing() {
            addAll(genreLesson(1L, rock));

            assertTrue(itemList.getTopInstrumentsFromLessons(3).isEmpty());
        }

        @Test
        void getInstrumentOccurrencesFromItems_combinesAllSources() {
            addAll(song(1L, Set.of(), Set.of(guitar, piano)),
                    instrumentGenre(200L, guitar, rock),
                    instrumentGenreLesson(2L, instrumentGenre(201L, guitar, jazz)));

            List<Instrument> occurrences = itemList.getInstrumentOccurrencesFromItems();

            assertEquals(4, occurrences.size());
            assertEquals(3, occurrences.stream().filter(guitar::equals).count());
        }

        @Test
        void getInstrumentOccurrencesFromItems_ignoresInstrumentGenreWithoutInstrument() {
            addAll(instrumentGenre(200L, null, rock));

            assertTrue(itemList.getInstrumentOccurrencesFromItems().isEmpty());
        }

        @Test
        void getTopInstrumentsFromItems_andMainInstrument() {
            addAll(song(1L, Set.of(), Set.of(piano)),
                    instrumentGenre(200L, guitar, rock),
                    instrumentGenre(201L, guitar, jazz));

            assertEquals(List.of(guitar, piano), itemList.getTopInstrumentsFromItems(2));
            assertEquals(Optional.of(guitar), itemList.getMainInstrumentFromItems());
        }

        @Test
        void getMainInstrumentFromItems_emptyList_returnsEmpty() {
            assertTrue(itemList.getMainInstrumentFromItems().isEmpty());
        }
    }
}
