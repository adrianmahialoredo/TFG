package com.uo289390.toco.utils.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.uo289390.toco.dto.*;
import com.uo289390.toco.model.*;
import com.uo289390.toco.utils.mapper.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EntityMapper}.
 * Checks that every DTO is mapped field by field to a new entity, using the
 * already-resolved related entities passed in by the services.
 */
class EntityMapperTest {

    private EntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EntityMapper();
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

    private static Lesson lesson(long id) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        return lesson;
    }

    private static Song song(long id) {
        Song song = new Song();
        song.setId(id);
        return song;
    }

    // -------- Chord --------

    @Test
    void testChordDtoToChord_MapsAllFields() {
        // Arrange
        ChordDto dto = new ChordDto();
        dto.setId(1L);
        dto.setName("C");
        dto.setNotes(List.of("C", "E", "G"));
        dto.setImageUrl("https://example.com/c.png");
        dto.setOpen(true);
        dto.setBarre(true);
        dto.setPowerChord(true);
        dto.setMinor(true);
        dto.setMajor(true);
        dto.setSeventh(true);
        dto.setCapo(true);
        dto.setCapoFret(5);

        // Act
        Chord chord = mapper.chordDtoToChord(dto);

        // Assert
        assertEquals(1L, chord.getId());
        assertEquals("C", chord.getName());
        assertEquals(List.of("C", "E", "G"), chord.getNotes());
        assertEquals("https://example.com/c.png", chord.getImageUrl());
        assertTrue(chord.isOpen());
        assertTrue(chord.isBarre());
        assertTrue(chord.isPowerChord());
        assertTrue(chord.isMinor());
        assertTrue(chord.isMajor());
        assertTrue(chord.isSeventh());
        assertTrue(chord.isCapo());
        assertEquals(5, chord.getCapoFret());
    }

    @Test
    void testChordDtoToChord_DefaultFlagsAreFalse() {
        // Arrange
        ChordDto dto = new ChordDto();
        dto.setName("D");

        // Act
        Chord chord = mapper.chordDtoToChord(dto);

        // Assert
        assertNull(chord.getId());
        assertFalse(chord.isOpen());
        assertFalse(chord.isBarre());
        assertFalse(chord.isPowerChord());
        assertFalse(chord.isMinor());
        assertFalse(chord.isMajor());
        assertFalse(chord.isSeventh());
        assertFalse(chord.isCapo());
        assertEquals(0, chord.getCapoFret());
    }

    // -------- Genre --------

    @Test
    void testGenreDtoToGenre_MapsAllFieldsAndType() {
        // Arrange
        GenreDTO dto = new GenreDTO();
        dto.setId(2L);
        dto.setTitle("Jazz");
        dto.setDescription("Jazz music");
        dto.setImageUrl("https://example.com/jazz.png");
        dto.setLevel(3);
        dto.setBannerUrl("https://example.com/banner.png");
        dto.setWelcomeIntroVideoId("intro");
        dto.setWelcomeSongVideoId("song");
        Set<Lesson> lessons = new LinkedHashSet<>(List.of(lesson(1), lesson(2)));

        // Act
        Genre genre = mapper.genreDtoToGenre(dto, lessons);

        // Assert
        assertEquals(2L, genre.getId());
        assertEquals("Jazz", genre.getTitle());
        assertEquals("Jazz music", genre.getDescription());
        assertEquals("https://example.com/jazz.png", genre.getImageUrl());
        assertEquals(3, genre.getLevel());
        assertEquals("https://example.com/banner.png", genre.getBannerUrl());
        assertEquals("intro", genre.getWelcomeIntroVideoId());
        assertEquals("song", genre.getWelcomeSongVideoId());
        assertEquals(Item.Type.Genre, genre.getType());
        assertEquals(lessons, genre.getLessons());
        // The owning side (Lesson.genre) must point to the genre so the link is persisted
        genre.getLessons().forEach(lesson -> assertSame(genre, lesson.getGenre()));
    }

    @Test
    void testGenreDtoToGenre_NullLessonsKeepsEmptyDefault() {
        // Arrange
        GenreDTO dto = new GenreDTO();
        dto.setTitle("Pop");

        // Act
        Genre genre = mapper.genreDtoToGenre(dto, null);

        // Assert
        assertNotNull(genre.getLessons());
        assertTrue(genre.getLessons().isEmpty());
    }

    // -------- Instrument --------

    @Test
    void testInstrumentDtoToInstrument() {
        // Arrange
        InstrumentDTO dto = new InstrumentDTO();
        dto.setId(3L);
        dto.setName("Guitar");
        dto.setImageUrl("https://example.com/guitar.png");

        // Act
        Instrument instrument = mapper.instrumentDtoToInstrument(dto);

        // Assert
        assertEquals(3L, instrument.getId());
        assertEquals("Guitar", instrument.getName());
        assertEquals("https://example.com/guitar.png", instrument.getImageUrl());
    }

    // -------- InstrumentGenre --------

    @Test
    void testInstrumentGenreDtoToInstrumentGenre_DoesNotMapLessons() {
        // Arrange
        InstrumentGenreDTO dto = new InstrumentGenreDTO();
        dto.setId(4L);
        dto.setTitle("Rock Guitar");
        dto.setImageUrl("https://example.com/rg.png");
        dto.setLevel(2);
        dto.setLessonIds(List.of(1L, 2L));
        Instrument guitar = instrument(1);
        Genre rock = genre(2);

        // Act
        InstrumentGenre ig = mapper.instrumentGenreDtoToInstrumentGenre(dto, guitar, rock);

        // Assert
        assertEquals(4L, ig.getId());
        assertEquals("Rock Guitar", ig.getTitle());
        assertEquals("https://example.com/rg.png", ig.getImageUrl());
        assertEquals(2, ig.getLevel());
        assertEquals(Item.Type.InstrumentGenre, ig.getType());
        assertSame(guitar, ig.getInstrument());
        assertSame(rock, ig.getGenre());
        assertTrue(ig.getLessons().isEmpty());
    }

    // -------- ItemList --------

    @Test
    void testItemListDtoToItemList() {
        // Arrange
        ItemListDTO dto = new ItemListDTO();
        dto.setId(5L);
        dto.setName("My list");
        dto.setImageUrl("https://example.com/list.png");
        User user = new User();
        user.setId(1L);
        Song song = song(1);
        Lesson lesson = lesson(2);

        // Act
        ItemList list = mapper.itemListDtoToItemList(dto, user, List.of(song, lesson, song));

        // Assert
        assertEquals(5L, list.getId());
        assertEquals("My list", list.getName());
        assertEquals("https://example.com/list.png", list.getImageUrl());
        assertSame(user, list.getUser());
        // Duplicates are dropped by ItemList.setItems
        assertEquals(List.of(song, lesson), list.getItems());
    }

    @Test
    void testItemListDtoToItemList_NullItemsGivesEmptyList() {
        // Act
        ItemList list = mapper.itemListDtoToItemList(new ItemListDTO(), new User(), null);

        // Assert
        assertNotNull(list.getItems());
        assertTrue(list.getItems().isEmpty());
    }

    // -------- LessonSection --------

    @Test
    void testLessonSectionDtoToLessonSection() {
        // Arrange
        LessonSectionDTO dto = new LessonSectionDTO();
        dto.setId(6L);
        dto.setTitle("Intro");
        dto.setHtmlContent("<p>x</p>");
        dto.setOrderIndex(3);
        dto.setLessonId(99L);
        Lesson lesson = lesson(1);

        // Act
        LessonSection section = mapper.lessonSectionDtoToLessonSection(dto, lesson);

        // Assert
        assertEquals(6L, section.getId());
        assertEquals("Intro", section.getTitle());
        assertEquals("<p>x</p>", section.getHtmlContent());
        assertEquals(3, section.getOrderIndex());
        assertSame(lesson, section.getLesson());
    }

    // -------- Song --------

    @Test
    void testSongDtoToSong() {
        // Arrange
        SongDTO dto = new SongDTO();
        dto.setId(7L);
        dto.setTitle("Imagine");
        dto.setArtist("John Lennon");
        Set<Genre> genres = Set.of(genre(1));
        Set<Instrument> instruments = Set.of(instrument(2));
        SongLesson songLesson = new SongLesson();
        songLesson.setId(3L);
        Set<SongLesson> lessons = Set.of(songLesson);

        // Act
        Song song = mapper.songDtoToSong(dto, genres, instruments, lessons);

        // Assert
        assertEquals(7L, song.getId());
        assertEquals("Imagine", song.getTitle());
        assertEquals("John Lennon", song.getArtist());
        assertSame(genres, song.getGenres());
        assertSame(instruments, song.getInstruments());
        assertSame(lessons, song.getLessons());
    }

    // -------- SongLesson --------

    @Test
    void testSongLessonDtoToSongLesson() {
        // Arrange
        SongLessonDTO dto = new SongLessonDTO();
        dto.setId(8L);
        dto.setDifficulty("ORIGINAL");
        dto.setChordedLyrics("[C] la");
        dto.setVideoUrl("https://youtube.com/v");
        Song song = song(1);
        Instrument guitar = instrument(2);
        Set<Chord> chords = new LinkedHashSet<>();

        // Act
        SongLesson lesson = mapper.songLessonDtoToSongLesson(dto, song, guitar, chords);

        // Assert
        assertEquals(8L, lesson.getId());
        assertEquals(SongLesson.DifficultyLevel.ORIGINAL, lesson.getDifficulty());
        assertEquals("[C] la", lesson.getChordedLyrics());
        assertEquals("https://youtube.com/v", lesson.getVideoUrl());
        assertSame(song, lesson.getSong());
        assertSame(guitar, lesson.getInstrument());
        assertSame(chords, lesson.getChords());
        assertNotNull(lesson.getTabSections());
        assertTrue(lesson.getTabSections().isEmpty());
    }

    @Test
    void testSongLessonDtoToSongLesson_EachDifficulty() {
        for (SongLesson.DifficultyLevel level : SongLesson.DifficultyLevel.values()) {
            // Arrange
            SongLessonDTO dto = new SongLessonDTO();
            dto.setDifficulty(level.name());

            // Act
            SongLesson lesson = mapper.songLessonDtoToSongLesson(dto, null, null, Set.of());

            // Assert
            assertEquals(level, lesson.getDifficulty());
        }
    }

    @Test
    void testSongLessonDtoToSongLesson_NullDifficultyThrowsIllegalArgument() {
        // Regression: used to be a NullPointerException (500); IllegalArgumentException maps to a 400
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mapper.songLessonDtoToSongLesson(new SongLessonDTO(), null, null, Set.of()));
        assertEquals("Difficulty is required", ex.getMessage());
    }

    @Test
    void testSongLessonDtoToSongLesson_InvalidDifficultyThrows() {
        // Arrange
        SongLessonDTO dto = new SongLessonDTO();
        dto.setDifficulty("HARD");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> mapper.songLessonDtoToSongLesson(dto, null, null, Set.of()));
    }

    // -------- TabSection --------

    @Test
    void testTabSectionDtoToTabSection() {
        // Arrange
        TabSectionDTO dto = new TabSectionDTO();
        dto.setId(9L);
        dto.setIdentifier("riff");
        dto.setContent("e|-0-|");
        SongLesson songLesson = new SongLesson();

        // Act
        TabSection section = mapper.tabSectionDtoToTabSection(dto, songLesson);

        // Assert
        assertEquals(9L, section.getId());
        assertEquals("riff", section.getIdentifier());
        assertEquals("e|-0-|", section.getContent());
        assertSame(songLesson, section.getSongLesson());
    }

    // -------- User --------

    @Test
    void testUserDtoToUser() {
        // Arrange
        UserDto dto = new UserDto();
        dto.setId(10L);
        dto.setEmail("a@b.com");
        dto.setUsername("alice");
        dto.setName("Alice");
        dto.setProfileImageUrl("https://example.com/alice.png");
        dto.setPassword("Secret1!");
        Set<Genre> genres = new LinkedHashSet<>(List.of(genre(1), genre(2)));
        Set<Instrument> instruments = new LinkedHashSet<>(List.of(instrument(3), instrument(4)));

        // Act
        User user = mapper.userDtoToUser(dto, genres, instruments);

        // Assert
        assertEquals(10L, user.getId());
        assertEquals("a@b.com", user.getEmail());
        assertEquals("alice", user.getUsername());
        assertEquals("Alice", user.getName());
        assertEquals("https://example.com/alice.png", user.getProfileImageUrl());
        // The mapper copies the raw password; hashing is the service's responsibility
        assertEquals("Secret1!", user.getPassword());
        assertSame(genres, user.getPreferences().getFavoriteGenres());
        assertSame(instruments, user.getPreferences().getFavoriteInstruments());
        assertSame(user, user.getPreferences().getUser());
        assertEquals(Role.USER, user.getRole());
    }
}
