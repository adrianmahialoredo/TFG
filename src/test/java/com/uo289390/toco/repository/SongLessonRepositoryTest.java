package com.uo289390.toco.repository;

import static com.uo289390.toco.repository.RepositoryTestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashSet;
import java.util.List;

import com.uo289390.toco.repository.ChordRepository;
import com.uo289390.toco.repository.SongLessonRepository;
import com.uo289390.toco.repository.TabSectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.uo289390.toco.model.Chord;
import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.Song;
import com.uo289390.toco.model.SongLesson;
import com.uo289390.toco.model.TabSection;

/**
 * Integration tests for the song lesson aggregate: SongLessonRepository, TabSectionRepository
 * and ChordRepository, including the song-chord and song-lesson-tab relations.
 */
@DataJpaTest
class SongLessonRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private SongLessonRepository songLessonRepository;

    @Autowired
    private TabSectionRepository tabSectionRepository;

    @Autowired
    private ChordRepository chordRepository;

    private Song song;
    private Song otherSong;
    private Instrument guitar;
    private Chord c;
    private Chord g;

    @BeforeEach
    void setUp() {
        guitar = em.persist(instrument(1L, "Guitar"));
        song = em.persist(song(10L, "Song", "Artist"));
        otherSong = em.persist(song(11L, "Other", "Artist"));
        c = em.persist(chord(1L, "C"));
        g = em.persist(chord(2L, "G"));

        SongLesson easy = songLesson(100L, song, guitar);
        easy.setChords(new LinkedHashSet<>(List.of(g, c)));
        em.persist(easy);

        SongLesson original = songLesson(101L, song, guitar);
        original.setDifficulty(SongLesson.DifficultyLevel.ORIGINAL);
        em.persist(original);

        em.persist(songLesson(102L, otherSong, guitar));

        em.persist(tabSection("Intro", easy));
        em.persist(tabSection("Solo", easy));
        em.persist(tabSection("Verse", original));

        em.flush();
        em.clear();
    }

    @Test
    void findBySongId_returnsLessonsOfThatSong() {
        List<Long> ids = songLessonRepository.findBySongId(song.getId()).stream().map(SongLesson::getId).sorted().toList();

        assertEquals(List.of(100L, 101L), ids);
    }

    @Test
    void findBySongId_unknownSong_returnsEmpty() {
        assertTrue(songLessonRepository.findBySongId(999L).isEmpty());
    }

    @Test
    void songLesson_persistsDifficultyAsEnum() {
        assertEquals(SongLesson.DifficultyLevel.ORIGINAL, songLessonRepository.findById(101L).orElseThrow().getDifficulty());
    }

    @Test
    void songLesson_chordsAreOrderedById() {
        SongLesson loaded = songLessonRepository.findById(100L).orElseThrow();

        assertEquals(List.of(1L, 2L), loaded.getChords().stream().map(Chord::getId).toList());
    }

    @Test
    void findBySongLessonId_returnsTabSectionsOfThatLesson() {
        List<String> identifiers = tabSectionRepository.findBySongLessonId(100L).stream()
                .map(TabSection::getIdentifier).sorted().toList();

        assertEquals(List.of("Intro", "Solo"), identifiers);
        assertEquals(1, tabSectionRepository.findBySongLessonId(101L).size());
        assertTrue(tabSectionRepository.findBySongLessonId(102L).isEmpty());
    }

    @Test
    void deletingSongLesson_cascadesToTabSections() {
        SongLesson easy = songLessonRepository.findById(100L).orElseThrow();
        easy.getSong().getLessons().remove(easy);
        songLessonRepository.delete(easy);
        songLessonRepository.flush();

        assertTrue(tabSectionRepository.findBySongLessonId(100L).isEmpty());
        assertEquals(1, tabSectionRepository.count());
    }

    @Test
    void deletingSong_cascadesToItsSongLessons() {
        em.getEntityManager().remove(em.find(Song.class, otherSong.getId()));
        em.flush();

        assertTrue(songLessonRepository.findBySongId(otherSong.getId()).isEmpty());
        assertEquals(2, songLessonRepository.count());
    }

    @Test
    void chordRepository_persistsNotesCollection() {
        Chord loaded = chordRepository.findById(1L).orElseThrow();

        assertEquals(List.of("C", "E", "G"), loaded.getNotes());
        assertEquals(2, chordRepository.count());
    }
}
