package com.uo289390.toco.repository;

import static com.uo289390.toco.repository.RepositoryTestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.uo289390.toco.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.Song;
import com.uo289390.toco.repository.SongRepository.TitleArtist;

/**
 * Integration tests for SongRepository derived queries and the specification-based
 * title/artist lookup, run against an in-memory database.
 */
@DataJpaTest
class SongRepositoryTest {

    private static final Pageable BY_ID = PageRequest.of(0, 50, Sort.by("id"));
    private static final List<Long> NONE = List.of(-1L);

    @Autowired
    private TestEntityManager em;

    @Autowired
    private SongRepository songRepository;

    private Genre rock;
    private Genre jazz;
    private Instrument guitar;
    private Instrument piano;
    private Instrument drums;

    private Song bohemian;   // rock, guitar+piano, popular, genrePopular, 1970s, level 3
    private Song yesterday;  // rock, guitar, genreClassic, 1960s, level 1
    private Song soWhat;     // jazz, piano, genreNew, newSong, level 5
    private Song blueInGreen; // jazz + rock, guitar + piano, level 2

    @BeforeEach
    void setUp() {
        rock = em.persist(genre(100L, "Rock"));
        jazz = em.persist(genre(101L, "Jazz"));
        guitar = em.persist(instrument(1L, "Guitar"));
        piano = em.persist(instrument(2L, "Piano"));
        drums = em.persist(instrument(3L, "Drums"));

        bohemian = song(1L, "Bohemian Rhapsody", "Queen");
        bohemian.setGenres(Set.of(rock));
        bohemian.setInstruments(Set.of(guitar, piano));
        bohemian.setPopular(true);
        bohemian.setGenrePopular(true);
        bohemian.setArtistPopular(true);
        bohemian.setDecade("1970s");
        bohemian.setLevel(3);
        em.persist(bohemian);

        yesterday = song(2L, "Yesterday", "The Beatles");
        yesterday.setGenres(Set.of(rock));
        yesterday.setInstruments(Set.of(guitar));
        yesterday.setGenreClassic(true);
        yesterday.setArtistClassic(true);
        yesterday.setDecade("1960s");
        yesterday.setLevel(1);
        em.persist(yesterday);

        soWhat = song(3L, "So What", "Miles Davis");
        soWhat.setGenres(Set.of(jazz));
        soWhat.setInstruments(Set.of(piano));
        soWhat.setGenreNew(true);
        soWhat.setNewSong(true);
        soWhat.setArtistNew(true);
        soWhat.setLevel(5);
        em.persist(soWhat);

        blueInGreen = song(4L, "Blue in Green", "Miles Davis");
        blueInGreen.setGenres(Set.of(jazz, rock));
        blueInGreen.setInstruments(Set.of(guitar, piano));
        blueInGreen.setArtistPopular(true);
        blueInGreen.setLevel(2);
        em.persist(blueInGreen);

        em.flush();
        em.clear();
    }

    private static List<Long> ids(Collection<Song> songs) {
        return songs.stream().map(Song::getId).sorted().toList();
    }

    // --- Simple flags ---

    @Test
    void findByDecade_returnsOnlySongsOfThatDecade() {
        assertEquals(List.of(1L), ids(songRepository.findByDecade("1970s")));
        assertEquals(List.of(2L), ids(songRepository.findByDecade("1960s")));
    }

    @Test
    void findByDecade_unknownDecade_returnsEmpty() {
        assertTrue(songRepository.findByDecade("1990s").isEmpty());
    }

    @Test
    void findByPopularTrue_returnsOnlyPopularSongs() {
        assertEquals(List.of(1L), ids(songRepository.findByPopularTrue(BY_ID)));
    }

    @Test
    void findByPopularTrue_unpaged_returnsAllPopular() {
        assertEquals(List.of(1L), ids(songRepository.findByPopularTrue(Pageable.unpaged())));
    }

    @Test
    void findByGenreNewTrue_returnsOnlyGenreNewSongs() {
        assertEquals(List.of(3L), ids(songRepository.findByGenreNewTrue(BY_ID)));
    }

    @Test
    void findByGenreNewTrue_respectsPageSize() {
        Song another = song(5L, "Another", "Someone");
        another.setGenreNew(true);
        em.persistAndFlush(another);

        assertEquals(1, songRepository.findByGenreNewTrue(PageRequest.of(0, 1)).size());
        assertEquals(2, songRepository.findByGenreNewTrue(PageRequest.of(0, 5)).size());
    }

    // --- Genre + flag ---

    @Test
    void findByGenresIdAndGenrePopularTrue_filtersByGenreAndFlag() {
        assertEquals(List.of(1L), ids(songRepository.findByGenresIdAndGenrePopularTrue(rock.getId())));
        assertTrue(songRepository.findByGenresIdAndGenrePopularTrue(jazz.getId()).isEmpty());
    }

    @Test
    void findByGenresIdAndGenreClassicTrue_filtersByGenreAndFlag() {
        assertEquals(List.of(2L), ids(songRepository.findByGenresIdAndGenreClassicTrue(rock.getId())));
        assertTrue(songRepository.findByGenresIdAndGenreClassicTrue(jazz.getId()).isEmpty());
    }

    @Test
    void findByGenresIdAndGenreNewTrue_filtersByGenreAndFlag() {
        assertEquals(List.of(3L), ids(songRepository.findByGenresIdAndGenreNewTrue(jazz.getId())));
        assertTrue(songRepository.findByGenresIdAndGenreNewTrue(rock.getId()).isEmpty());
    }

    @Test
    void findByGenresIdAndFlag_unknownGenre_returnsEmpty() {
        assertTrue(songRepository.findByGenresIdAndGenrePopularTrue(999L).isEmpty());
    }

    // --- Artist + flag ---

    @Test
    void findByArtistIgnoreCaseAndArtistPopularTrue_isCaseInsensitive() {
        assertEquals(List.of(4L),
                ids(songRepository.findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn("mILES dAVIS", NONE, BY_ID)));
        assertEquals(List.of(1L),
                ids(songRepository.findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn("QUEEN", NONE, BY_ID)));
    }

    @Test
    void findByArtistIgnoreCaseAndArtistPopularTrue_excludesIds() {
        assertTrue(songRepository.findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn("Miles Davis", List.of(4L), BY_ID).isEmpty());
    }

    @Test
    void findByArtistIgnoreCaseAndArtistClassicTrue_filtersByFlag() {
        assertEquals(List.of(2L),
                ids(songRepository.findByArtistIgnoreCaseAndArtistClassicTrueAndIdNotIn("the beatles", NONE, BY_ID)));
        assertTrue(songRepository.findByArtistIgnoreCaseAndArtistClassicTrueAndIdNotIn("Queen", NONE, BY_ID).isEmpty());
    }

    @Test
    void findByArtistIgnoreCaseAndArtistNewTrue_filtersByFlag() {
        assertEquals(List.of(3L),
                ids(songRepository.findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn("MILES DAVIS", NONE, BY_ID)));
        assertTrue(songRepository.findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn("Miles Davis", List.of(3L), BY_ID).isEmpty());
    }

    @Test
    void findByArtist_doesNotMatchPartialName() {
        assertTrue(songRepository.findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn("Miles", NONE, BY_ID).isEmpty());
    }

    // --- Genre / instrument combinations ---

    @Test
    void findDistinctByGenresIdAndInstrumentsIn_returnsDistinctSongs() {
        List<Song> result = songRepository.findDistinctByGenresIdAndInstrumentsInAndIdNotIn(
                rock.getId(), List.of(guitar, piano), NONE, BY_ID);

        // Bohemian has both instruments but must appear only once
        assertEquals(List.of(1L, 2L, 4L), ids(result));
        assertEquals(3, result.size());
    }

    @Test
    void findDistinctByGenresIdAndInstrumentsIn_excludesIds() {
        assertEquals(List.of(4L), ids(songRepository.findDistinctByGenresIdAndInstrumentsInAndIdNotIn(
                rock.getId(), List.of(guitar, piano), List.of(1L, 2L), BY_ID)));
    }

    @Test
    void findDistinctByGenresIdAndInstrumentsIn_instrumentWithoutSongs_returnsEmpty() {
        assertTrue(songRepository.findDistinctByGenresIdAndInstrumentsInAndIdNotIn(
                rock.getId(), List.of(drums), NONE, BY_ID).isEmpty());
    }

    @Test
    void findByGenresIdAndIdNotIn_filtersByGenreAndExclusions() {
        assertEquals(List.of(3L, 4L), ids(songRepository.findByGenresIdAndIdNotIn(jazz.getId(), NONE, BY_ID)));
        assertEquals(List.of(4L), ids(songRepository.findByGenresIdAndIdNotIn(jazz.getId(), List.of(3L), BY_ID)));
    }

    @Test
    void findByGenresIdAndIdNotIn_respectsPageSize() {
        assertEquals(1, songRepository.findByGenresIdAndIdNotIn(rock.getId(), NONE, PageRequest.of(0, 1)).size());
    }

    @Test
    void findDistinctByInstrumentsInAndIdNotIn_returnsDistinctSongs() {
        List<Song> result = songRepository.findDistinctByInstrumentsInAndIdNotIn(List.of(guitar, piano), NONE, BY_ID);

        assertEquals(List.of(1L, 2L, 3L, 4L), ids(result));
        assertEquals(4, result.size());
    }

    @Test
    void findDistinctByInstrumentsInAndIdNotIn_excludesIds() {
        assertEquals(List.of(3L), ids(songRepository.findDistinctByInstrumentsInAndIdNotIn(
                List.of(piano), List.of(1L, 4L), BY_ID)));
    }

    @Test
    void findByInstrumentsIdAndIdNotIn_filtersByInstrument() {
        assertEquals(List.of(1L, 2L, 4L), ids(songRepository.findByInstrumentsIdAndIdNotIn(guitar.getId(), NONE, BY_ID)));
        assertTrue(songRepository.findByInstrumentsIdAndIdNotIn(drums.getId(), NONE, BY_ID).isEmpty());
    }

    @Test
    void findDistinctByGenresInAndInstrumentsIn_matchesAnyGenreAndAnyInstrument() {
        List<Song> result = songRepository.findDistinctByGenresInAndInstrumentsInAndIdNotIn(
                List.of(rock, jazz), List.of(piano), NONE, BY_ID);

        assertEquals(List.of(1L, 3L, 4L), ids(result));
        assertEquals(3, result.size());
    }

    @Test
    void findDistinctByGenresInAndInstrumentsIn_excludesIds() {
        assertEquals(List.of(2L), ids(songRepository.findDistinctByGenresInAndInstrumentsInAndIdNotIn(
                List.of(rock), List.of(guitar), List.of(1L, 4L), BY_ID)));
    }

    // --- Level ---

    @Test
    void findByGenresIdAndLevelLessThanEqual_includesBoundary() {
        assertEquals(List.of(2L, 4L), ids(songRepository.findByGenresIdAndLevelLessThanEqualAndIdNotIn(rock.getId(), 2, NONE, BY_ID)));
        assertEquals(List.of(1L, 2L, 4L), ids(songRepository.findByGenresIdAndLevelLessThanEqualAndIdNotIn(rock.getId(), 3, NONE, BY_ID)));
    }

    @Test
    void findByGenresIdAndLevelLessThanEqual_levelBelowAll_returnsEmpty() {
        assertTrue(songRepository.findByGenresIdAndLevelLessThanEqualAndIdNotIn(jazz.getId(), 1, NONE, BY_ID).isEmpty());
    }

    @Test
    void findByInstrumentsIdAndLevelLessThanEqual_filtersByLevelAndExclusions() {
        assertEquals(List.of(4L), ids(songRepository.findByInstrumentsIdAndLevelLessThanEqualAndIdNotIn(piano.getId(), 4, List.of(1L), BY_ID)));
        assertEquals(List.of(1L, 3L, 4L), ids(songRepository.findByInstrumentsIdAndLevelLessThanEqualAndIdNotIn(piano.getId(), 5, NONE, BY_ID)));
    }

    // --- Title / artist specification ---

    @Test
    void findByTitleAndArtistIn_emptyPairs_returnsEmptyWithoutQuerying() {
        assertTrue(songRepository.findByTitleAndArtistIn(List.of()).isEmpty());
    }

    @Test
    void findByTitleAndArtistIn_matchesCaseInsensitively() {
        List<Song> result = songRepository.findByTitleAndArtistIn(List.of(
                new TitleArtist("bohemian rhapsody", "QUEEN"),
                new TitleArtist("SO WHAT", "miles davis")));

        assertEquals(List.of(1L, 3L), ids(result));
    }

    @Test
    void findByTitleAndArtistIn_requiresBothTitleAndArtistToMatch() {
        List<Song> result = songRepository.findByTitleAndArtistIn(List.of(
                new TitleArtist("Bohemian Rhapsody", "Miles Davis"),
                new TitleArtist("Yesterday", "Queen")));

        assertTrue(result.isEmpty());
    }

    @Test
    void findByTitleAndArtistIn_unknownPairs_returnsEmpty() {
        assertTrue(songRepository.findByTitleAndArtistIn(List.of(new TitleArtist("Nope", "Nobody"))).isEmpty());
    }

    // --- Relations & CRUD ---

    @Test
    void savedSong_keepsGenresAndInstruments() {
        Song loaded = songRepository.findById(4L).orElseThrow();

        assertEquals(Set.of(100L, 101L), Set.copyOf(loaded.getGenres().stream().map(Genre::getId).toList()));
        assertEquals(Set.of(1L, 2L), Set.copyOf(loaded.getInstruments().stream().map(Instrument::getId).toList()));
    }

    @Test
    void deleteById_removesSong() {
        songRepository.deleteById(2L);
        songRepository.flush();

        assertFalse(songRepository.existsById(2L));
        assertEquals(3, songRepository.count());
    }
}
