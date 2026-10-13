package com.uo289390.toco.utils.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.uo289390.toco.dto.*;
import com.uo289390.toco.model.*;
import com.uo289390.toco.utils.mapper.DtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DtoMapper}.
 * Checks that every entity is mapped field by field to its DTO, including relationship ids,
 * optional relationships and the polymorphic item mapping.
 */
class DtoMapperTest {

    private DtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DtoMapper();
    }

    // -------- Builders --------

    private static Genre genre(long id) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setTitle("Genre " + id);
        genre.setImageUrl("https://example.com/genre" + id + ".png");
        genre.setType(Item.Type.Genre);
        return genre;
    }

    private static Instrument instrument(long id) {
        Instrument instrument = new Instrument();
        instrument.setId(id);
        instrument.setName("Instrument " + id);
        instrument.setImageUrl("https://example.com/instrument" + id + ".png");
        return instrument;
    }

    private static Lesson lesson(long id) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.setTitle("Lesson " + id);
        lesson.setType(Item.Type.Lesson);
        return lesson;
    }

    private static Song song(long id) {
        Song song = new Song();
        song.setId(id);
        song.setTitle("Song " + id);
        song.setArtist("Artist " + id);
        song.setLevel(3);
        song.setImageUrl("https://example.com/song.png");
        song.setType(Item.Type.Song);
        return song;
    }

    private static InstrumentGenre instrumentGenre(long id) {
        InstrumentGenre ig = new InstrumentGenre();
        ig.setId(id);
        ig.setTitle("IG " + id);
        ig.setImageUrl("https://example.com/ig.png");
        ig.setLevel(2);
        ig.setType(Item.Type.InstrumentGenre);
        ig.setInstrument(instrument(7));
        ig.setGenre(genre(8));
        return ig;
    }

    private static User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setUsername("user");
        user.setName("User Name");
        user.setProfileImageUrl("https://example.com/me.png");
        user.setPassword("hashed");
        user.getPreferences().setFavoriteGenres(new LinkedHashSet<>(List.of(genre(1), genre(2))));
        user.getPreferences().setFavoriteInstruments(new LinkedHashSet<>(List.of(instrument(3), instrument(4))));
        return user;
    }

    // -------- Chord --------

    @Test
    void testChordToChordDto_MapsAllFields() {
        // Arrange
        Chord chord = new Chord(1L, "Am", List.of("A", "C", "E"), "https://example.com/am.png",
                true, false, false, true, false, false, true, 2);

        // Act
        ChordDto dto = mapper.chordToChordDto(chord);

        // Assert
        assertEquals(1L, dto.getId());
        assertEquals("Am", dto.getName());
        assertEquals(List.of("A", "C", "E"), dto.getNotes());
        assertEquals("https://example.com/am.png", dto.getImageUrl());
        assertTrue(dto.isOpen());
        assertFalse(dto.isBarre());
        assertFalse(dto.isPowerChord());
        assertTrue(dto.isMinor());
        assertFalse(dto.isMajor());
        assertFalse(dto.isSeventh());
        assertTrue(dto.isCapo());
        assertEquals(2, dto.getCapoFret());
    }

    @Test
    void testChordToChordDto_AllFlagsInverted() {
        // Arrange
        Chord chord = new Chord(2L, "G7", List.of(), "https://example.com/g7.png",
                false, true, true, false, true, true, false, 0);

        // Act
        ChordDto dto = mapper.chordToChordDto(chord);

        // Assert
        assertFalse(dto.isOpen());
        assertTrue(dto.isBarre());
        assertTrue(dto.isPowerChord());
        assertFalse(dto.isMinor());
        assertTrue(dto.isMajor());
        assertTrue(dto.isSeventh());
        assertFalse(dto.isCapo());
        assertEquals(0, dto.getCapoFret());
    }

    // -------- Genre --------

    @Test
    void testGenreToGenreDto_MapsAllFields() {
        // Arrange
        Genre genre = genre(1);
        genre.setLevel(4);
        genre.setDescription("Rock music");
        genre.setBannerUrl("https://example.com/banner.png");
        genre.setWelcomeIntroVideoId("intro");
        genre.setWelcomeSongVideoId("song");
        genre.setLessons(new LinkedHashSet<>(List.of(lesson(10), lesson(11))));

        // Act
        GenreDTO dto = mapper.genreToGenreDto(genre);

        // Assert
        assertEquals(1L, dto.getId());
        assertEquals("Genre 1", dto.getTitle());
        assertEquals("https://example.com/genre1.png", dto.getImageUrl());
        assertEquals(4, dto.getLevel());
        assertEquals("Rock music", dto.getDescription());
        assertEquals("https://example.com/banner.png", dto.getBannerUrl());
        assertEquals("intro", dto.getWelcomeIntroVideoId());
        assertEquals("song", dto.getWelcomeSongVideoId());
        assertEquals(List.of(10L, 11L), dto.getLessonIds());
        assertEquals(Item.Type.Genre, dto.getType());
    }

    @Test
    void testGenreToGenreDto_NullLessonsMapToEmptyLessonIds() {
        // Arrange: Genre.setLessons(null) unlinks every lesson and keeps an empty collection
        Genre genre = genre(1);
        genre.setLessons(null);

        // Act
        GenreDTO dto = mapper.genreToGenreDto(genre);

        // Assert
        assertEquals(List.of(), dto.getLessonIds());
    }

    @Test
    void testGenreToGenreDto_EmptyLessons() {
        // Act
        GenreDTO dto = mapper.genreToGenreDto(genre(1));

        // Assert
        assertNotNull(dto.getLessonIds());
        assertTrue(dto.getLessonIds().isEmpty());
    }

    // -------- Instrument --------

    @Test
    void testInstrumentToInstrumentDto() {
        // Act
        InstrumentDTO dto = mapper.instrumentToInstrumentDto(instrument(5));

        // Assert
        assertEquals(5L, dto.getId());
        assertEquals("Instrument 5", dto.getName());
        assertEquals("https://example.com/instrument5.png", dto.getImageUrl());
    }

    // -------- InstrumentGenre --------

    @Test
    void testInstrumentGenreToInstrumentGenreDto() {
        // Arrange
        InstrumentGenre ig = instrumentGenre(20);
        ig.setLessons(List.of(lesson(1), lesson(2), lesson(3)));

        // Act
        InstrumentGenreDTO dto = mapper.instrumentGenreToInstrumentGenreDto(ig);

        // Assert
        assertEquals(20L, dto.getId());
        assertEquals("IG 20", dto.getTitle());
        assertEquals("https://example.com/ig.png", dto.getImageUrl());
        assertEquals(2, dto.getLevel());
        assertEquals(7L, dto.getInstrumentId());
        assertEquals(8L, dto.getGenreId());
        assertEquals(List.of(1L, 2L, 3L), dto.getLessonIds());
        assertEquals(Item.Type.InstrumentGenre, dto.getType());
    }

    // -------- ItemList --------

    @Test
    void testItemListToItemListDto() {
        // Arrange
        ItemList list = new ItemList();
        list.setId(3L);
        list.setName("Favourites");
        list.setImageUrl("https://example.com/list.png");
        list.setUser(user());
        list.setItems(List.of(song(1), lesson(2)));

        // Act
        ItemListDTO dto = mapper.itemListToItemListDto(list);

        // Assert
        assertEquals(3L, dto.getId());
        assertEquals("Favourites", dto.getName());
        assertEquals("https://example.com/list.png", dto.getImageUrl());
        assertEquals(1L, dto.getUserId());
        assertEquals(List.of(1L, 2L), dto.getItemIds());
    }

    @Test
    void testItemListToItemListDto_EmptyItems() {
        // Arrange
        ItemList list = new ItemList();
        list.setUser(user());

        // Act
        ItemListDTO dto = mapper.itemListToItemListDto(list);

        // Assert
        assertTrue(dto.getItemIds().isEmpty());
    }

    // -------- ItemSummary --------

    @Test
    void testItemToItemSummaryDto_SongIncludesArtist() {
        // Act
        ItemSummaryDTO dto = mapper.itemToItemSummaryDto(song(1));

        // Assert
        assertEquals(1L, dto.getId());
        assertEquals("Song 1", dto.getTitle());
        assertEquals("https://example.com/song.png", dto.getImageUrl());
        assertEquals(Item.Type.Song, dto.getType());
        assertEquals(3, dto.getLevel());
        assertEquals("Artist 1", dto.getArtist());
    }

    @Test
    void testItemToItemSummaryDto_NonSongHasNoArtist() {
        // Arrange
        Lesson lesson = lesson(2);
        lesson.setLevel(1);

        // Act
        ItemSummaryDTO dto = mapper.itemToItemSummaryDto(lesson);

        // Assert
        assertEquals(Item.Type.Lesson, dto.getType());
        assertEquals(1, dto.getLevel());
        assertNull(dto.getArtist());
    }

    @Test
    void testItemsToItemSummaryDTOs() {
        // Act
        List<ItemSummaryDTO> dtos = mapper.itemsToItemSummaryDTOs(List.of(song(1), genre(2), lesson(3)));

        // Assert
        assertEquals(List.of(1L, 2L, 3L), dtos.stream().map(ItemSummaryDTO::getId).toList());
    }

    @Test
    void testItemsToItemSummaryDTOs_Empty() {
        // Act & Assert
        assertTrue(mapper.itemsToItemSummaryDTOs(List.of()).isEmpty());
    }

    // -------- LessonSection --------

    @Test
    void testLessonSectionToLessonSectionDto() {
        // Arrange
        LessonSection section = new LessonSection(4L, "Intro", "<p>Hi</p>", lesson(9), 2);

        // Act
        LessonSectionDTO dto = mapper.lessonSectionToLessonSectionDto(section);

        // Assert
        assertEquals(4L, dto.getId());
        assertEquals("Intro", dto.getTitle());
        assertEquals("<p>Hi</p>", dto.getHtmlContent());
        assertEquals(2, dto.getOrderIndex());
        assertEquals(9L, dto.getLessonId());
    }

    // -------- Lesson --------

    @Test
    void testLessonToLessonDto_WithAllRelationships() {
        // Arrange
        Lesson lesson = lesson(1);
        lesson.setLevel(2);
        lesson.setImageUrl("https://example.com/lesson.png");
        lesson.setBelongsToGenre(true);
        lesson.setPopular(true);
        lesson.setGenre(genre(5));
        lesson.setInstrument(instrument(6));
        InstrumentGenre ig = instrumentGenre(7);
        lesson.joinInstrumentGenre(ig, 3);
        lesson.addSection(new LessonSection(11L, "A", "a", null, 0));
        lesson.addSection(new LessonSection(12L, "B", "b", null, 1));

        // Act
        LessonDTO dto = mapper.lessonToLessonDto(lesson);

        // Assert
        assertEquals(1L, dto.getId());
        assertEquals("Lesson 1", dto.getTitle());
        assertEquals(2, dto.getLevel());
        // When the lesson has a genre, the genre cover is used
        assertEquals("https://example.com/genre5.png", dto.getImageUrl());
        assertTrue(dto.isBelongsToGenre());
        assertTrue(dto.isPopular());
        assertEquals(5L, dto.getGenreId());
        assertTrue(dto.isBelongsToInstrumentGenre());
        assertEquals(3, dto.getIndexInInstrumentGenre());
        assertEquals(7L, dto.getInstrumentGenreId());
        assertEquals(6L, dto.getInstrumentId());
        assertEquals(List.of(11L, 12L), dto.getSectionIds());
        assertEquals(Item.Type.Lesson, dto.getType());
    }

    @Test
    void testLessonToLessonDto_WithoutRelationshipsUsesOwnImage() {
        // Arrange
        Lesson lesson = lesson(1);
        lesson.setImageUrl("https://example.com/own.png");

        // Act
        LessonDTO dto = mapper.lessonToLessonDto(lesson);

        // Assert
        assertEquals("https://example.com/own.png", dto.getImageUrl());
        assertNull(dto.getGenreId());
        assertNull(dto.getInstrumentGenreId());
        assertNull(dto.getInstrumentId());
        assertFalse(dto.isBelongsToGenre());
        assertFalse(dto.isBelongsToInstrumentGenre());
        assertEquals(0, dto.getIndexInInstrumentGenre());
        assertEquals(List.of(), dto.getSectionIds());
    }

    @Test
    void testLessonToLessonDto_GenreWithoutImageFallsBackToNull() {
        // Arrange: Optional.map(getImageUrl) yields empty for a null image, so the lesson image is used
        Lesson lesson = lesson(1);
        lesson.setImageUrl("https://example.com/own.png");
        Genre genre = genre(5);
        genre.setImageUrl(null);
        lesson.setGenre(genre);

        // Act
        LessonDTO dto = mapper.lessonToLessonDto(lesson);

        // Assert
        assertEquals("https://example.com/own.png", dto.getImageUrl());
        assertEquals(5L, dto.getGenreId());
    }

    @Test
    void testLessonToLessonDto_NullSectionsLeavesSectionIdsNull() {
        // Arrange
        Lesson lesson = lesson(1);
        lesson.setSections(null);

        // Act
        LessonDTO dto = mapper.lessonToLessonDto(lesson);

        // Assert
        assertNull(dto.getSectionIds());
    }

    // -------- Song --------

    @Test
    void testSongToSongDto() {
        // Arrange
        Song song = song(1);
        song.setGenres(new LinkedHashSet<>(List.of(genre(2))));
        song.setInstruments(new LinkedHashSet<>(List.of(instrument(3), instrument(4))));
        SongLesson songLesson = new SongLesson();
        songLesson.setId(5L);
        song.setLessons(new LinkedHashSet<>(List.of(songLesson)));

        // Act
        SongDTO dto = mapper.songToSongDto(song);

        // Assert
        assertEquals(1L, dto.getId());
        assertEquals("Song 1", dto.getTitle());
        assertEquals("https://example.com/song.png", dto.getImageUrl());
        assertEquals(3, dto.getLevel());
        assertEquals("Artist 1", dto.getArtist());
        assertEquals(List.of(2L), dto.getGenreIds());
        assertEquals(List.of(3L, 4L), dto.getInstrumentIds());
        assertEquals(List.of(5L), dto.getLessonIds());
        assertEquals(Item.Type.Song, dto.getType());
    }

    @Test
    void testSongToSongDto_EmptyRelationships() {
        // Act
        SongDTO dto = mapper.songToSongDto(song(1));

        // Assert
        assertTrue(dto.getGenreIds().isEmpty());
        assertTrue(dto.getInstrumentIds().isEmpty());
        assertTrue(dto.getLessonIds().isEmpty());
    }

    // -------- SongLesson --------

    @Test
    void testSongLessonToSongLessonDto() {
        // Arrange
        Chord chord = new Chord();
        chord.setId(30L);
        SongLesson songLesson = new SongLesson();
        songLesson.setId(1L);
        songLesson.setDifficulty(SongLesson.DifficultyLevel.MEDIUM);
        songLesson.setChordedLyrics("[Am] hello");
        songLesson.setVideoUrl("https://youtube.com/x");
        songLesson.setSong(song(2));
        songLesson.setInstrument(instrument(3));
        songLesson.setChords(new LinkedHashSet<>(List.of(chord)));
        TabSection tab = new TabSection(40L, "intro", "e|---", songLesson);
        songLesson.setTabSections(new ArrayList<>(List.of(tab)));

        // Act
        SongLessonDTO dto = mapper.songLessonToSongLessonDto(songLesson);

        // Assert
        assertEquals(1L, dto.getId());
        assertEquals("MEDIUM", dto.getDifficulty());
        assertEquals("[Am] hello", dto.getChordedLyrics());
        assertEquals("https://youtube.com/x", dto.getVideoUrl());
        assertEquals(2L, dto.getSongId());
        assertEquals(3L, dto.getInstrumentId());
        assertEquals(List.of(30L), dto.getChordIds());
        assertEquals(List.of(40L), dto.getTabSectionsIds());
    }

    // -------- TabSection --------

    @Test
    void testTabSectionToTabSectionDto() {
        // Arrange
        SongLesson songLesson = new SongLesson();
        songLesson.setId(9L);
        TabSection section = new TabSection(1L, "solo", "e|--5--|", songLesson);

        // Act
        TabSectionDTO dto = mapper.tabSectionToTabSectionDto(section);

        // Assert
        assertEquals(1L, dto.getId());
        assertEquals("solo", dto.getIdentifier());
        assertEquals("e|--5--|", dto.getContent());
        assertEquals(9L, dto.getSongLessonId());
    }

    // -------- User --------

    @Test
    void testUserToUserDto_MapsFieldsAndNeverExposesPassword() {
        // Act
        UserDto dto = mapper.userToUserDto(user());

        // Assert
        assertEquals(1L, dto.getId());
        assertEquals("user@example.com", dto.getEmail());
        assertEquals("user", dto.getUsername());
        assertEquals("User Name", dto.getName());
        assertEquals("https://example.com/me.png", dto.getProfileImageUrl());
        assertEquals(Set.of(1L, 2L), Set.copyOf(dto.getFavouriteGenresIds()));
        assertEquals(Set.of(3L, 4L), Set.copyOf(dto.getFavouriteInstrumentsIds()));
        assertFalse(dto.isSpotifyConnected());
        assertNull(dto.getPassword());
    }

    @Test
    void testUserToUserDto_SpotifyConnected() {
        // Arrange
        User user = user();
        user.setSpotifyUser(new SpotifyUser(1L, "spotify", "a", "r", Instant.now(), user));

        // Act
        UserDto dto = mapper.userToUserDto(user);

        // Assert
        assertTrue(dto.isSpotifyConnected());
    }

    @Test
    void testUserToUserDto_NoFavourites() {
        // Arrange
        User user = new User();
        user.setId(2L);

        // Act
        UserDto dto = mapper.userToUserDto(user);

        // Assert
        assertTrue(dto.getFavouriteGenresIds().isEmpty());
        assertTrue(dto.getFavouriteInstrumentsIds().isEmpty());
    }

    @Test
    void testUserToUserResponseDto() {
        // Arrange
        User user = user();
        user.setSpotifyUser(new SpotifyUser());

        // Act
        UserResponseDTO dto = mapper.userToUserResponseDto(user);

        // Assert
        assertEquals(1L, dto.getId());
        assertEquals("user@example.com", dto.getEmail());
        assertEquals("user", dto.getUsername());
        assertEquals("User Name", dto.getName());
        assertEquals("https://example.com/me.png", dto.getProfileImageUrl());
        assertEquals(Set.of(1L, 2L), Set.copyOf(dto.getFavouriteGenresIds()));
        assertEquals(Set.of(3L, 4L), Set.copyOf(dto.getFavouriteInstrumentsIds()));
        assertTrue(dto.isSpotifyConnected());
    }

    @Test
    void testUserToUserResponseDto_NotConnected() {
        // Act
        UserResponseDTO dto = mapper.userToUserResponseDto(user());

        // Assert
        assertFalse(dto.isSpotifyConnected());
    }

    // -------- Polymorphic items --------

    @Test
    void testItemToItemDto_Song() {
        // Act
        ItemDTO dto = mapper.itemToItemDto(song(1));

        // Assert
        assertInstanceOf(SongDTO.class, dto);
        assertEquals("Artist 1", ((SongDTO) dto).getArtist());
    }

    @Test
    void testItemToItemDto_Lesson() {
        // Act & Assert
        assertInstanceOf(LessonDTO.class, mapper.itemToItemDto(lesson(1)));
    }

    @Test
    void testItemToItemDto_Genre() {
        // Act & Assert
        assertInstanceOf(GenreDTO.class, mapper.itemToItemDto(genre(1)));
    }

    @Test
    void testItemToItemDto_InstrumentGenre() {
        // Act
        ItemDTO dto = mapper.itemToItemDto(instrumentGenre(1));

        // Assert
        assertInstanceOf(InstrumentGenreDTO.class, dto);
        assertEquals(Item.Type.InstrumentGenre, dto.getType());
    }

    @Test
    void testItemToItemDto_UnknownSubtypeThrows() {
        // Arrange
        Item unknown = new Item() { };

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> mapper.itemToItemDto(unknown));
        assertTrue(ex.getMessage().startsWith("Unknown item type"));
    }

    @Test
    void testItemsToItemDTOs_KeepsOrderAndSubtypes() {
        // Act
        List<ItemDTO> dtos = mapper.itemsToItemDTOs(List.of(lesson(2), song(1), genre(3), instrumentGenre(4)));

        // Assert
        assertEquals(4, dtos.size());
        assertInstanceOf(LessonDTO.class, dtos.get(0));
        assertInstanceOf(SongDTO.class, dtos.get(1));
        assertInstanceOf(GenreDTO.class, dtos.get(2));
        assertInstanceOf(InstrumentGenreDTO.class, dtos.get(3));
        assertEquals(List.of(2L, 1L, 3L, 4L), dtos.stream().map(ItemDTO::getId).toList());
    }

    @Test
    void testItemsToItemDTOs_Empty() {
        // Act & Assert
        assertTrue(mapper.itemsToItemDTOs(List.of()).isEmpty());
    }

    @Test
    void testSongLessonToSongLessonDto_NullDifficulty_IsMappedAsNull() {
        // Arrange (regression: a null difficulty used to throw a NullPointerException)
        SongLesson lesson = new SongLesson();
        lesson.setId(1L);
        Song song = new Song();
        song.setId(2L);
        lesson.setSong(song);
        Instrument instrument = new Instrument();
        instrument.setId(3L);
        lesson.setInstrument(instrument);

        // Act
        SongLessonDTO dto = mapper.songLessonToSongLessonDto(lesson);

        // Assert
        assertNull(dto.getDifficulty());
    }
}
