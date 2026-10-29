package com.uo289390.toco.service;

import static com.uo289390.toco.service.EntityResolverTestSupport.initResolver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.uo289390.toco.service.SongServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.uo289390.toco.dto.SongDTO;
import com.uo289390.toco.dto.SongLessonDTO;
import com.uo289390.toco.dto.TabSectionDTO;
import com.uo289390.toco.model.Chord;
import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.Item;
import com.uo289390.toco.model.Song;
import com.uo289390.toco.model.SongLesson;
import com.uo289390.toco.model.TabSection;
import com.uo289390.toco.repository.ChordRepository;
import com.uo289390.toco.repository.GenreRepository;
import com.uo289390.toco.repository.InstrumentRepository;
import com.uo289390.toco.repository.ItemListRepository;
import com.uo289390.toco.repository.SongLessonRepository;
import com.uo289390.toco.repository.SongRepository;
import com.uo289390.toco.repository.UserRepository;
import com.uo289390.toco.utils.mapper.DtoMapper;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for {@link SongServiceImpl}.
 * Covers CRUD operations (including the cascading creation of song lessons and
 * tab sections) and the filtered song queries.
 */
@ExtendWith(MockitoExtension.class)
class SongServiceTest {

    @Mock
    private SongRepository songRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private ChordRepository chordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemListRepository itemListRepository;

    @Mock
    private SongLessonRepository songLessonRepository;

    @Spy
    private DtoMapper dtoMapper = new DtoMapper();

    @InjectMocks
    private SongServiceImpl songService;

    private Song testSong;
    private Genre rock;
    private Genre pop;
    private Instrument guitar;
    private Instrument piano;

    @BeforeEach
    void setUp() {
        initResolver(songRepository, genreRepository, instrumentRepository, chordRepository,
                userRepository, itemListRepository, songLessonRepository);

        rock = genre(10L);
        pop = genre(11L);
        guitar = instrument(1L);
        piano = instrument(2L);

        testSong = song(1L, "Bohemian Rhapsody");
        testSong.setArtist("Queen");
        testSong.setImageUrl("https://example.com/bohemian.png");
        testSong.setLevel(3);
        testSong.setDecade("1970s");
        testSong.getGenres().add(rock);
        testSong.getInstruments().add(guitar);
    }

    private static Song song(Long id, String title) {
        Song song = new Song();
        song.setId(id);
        song.setTitle(title);
        song.setType(Item.Type.Song);
        return song;
    }

    private static Genre genre(Long id) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setTitle("Genre " + id);
        return genre;
    }

    private static Instrument instrument(Long id) {
        Instrument instrument = new Instrument();
        instrument.setId(id);
        instrument.setName("Instrument " + id);
        return instrument;
    }

    private static Chord chord(Long id, Instrument instrument) {
        Chord chord = new Chord();
        chord.setId(id);
        chord.setName("Chord " + id);
        chord.setInstrument(instrument);
        return chord;
    }

    private static SongLesson songLesson(Long id) {
        SongLesson lesson = new SongLesson();
        lesson.setId(id);
        lesson.setDifficulty(SongLesson.DifficultyLevel.EASY);
        lesson.setSong(song(1L, "x"));
        lesson.setInstrument(instrument(1L));
        return lesson;
    }

    private static SongDTO basicDto() {
        SongDTO dto = new SongDTO();
        dto.setId(5L);
        dto.setTitle("Imagine");
        dto.setArtist("John Lennon");
        dto.setImageUrl("https://example.com/imagine.png");
        return dto;
    }

    private static Song savedArgument(org.mockito.invocation.InvocationOnMock inv) {
        return inv.getArgument(0);
    }

    // ---------- findAll ----------

    @Test
    void testFindAll_ReturnsMappedSongs() {
        // Arrange
        when(songRepository.findAll()).thenReturn(List.of(testSong, song(2L, "Yesterday")));

        // Act
        List<SongDTO> result = songService.findAll();

        // Assert
        assertEquals(2, result.size());
        SongDTO first = result.get(0);
        assertEquals(1L, first.getId());
        assertEquals("Bohemian Rhapsody", first.getTitle());
        assertEquals("Queen", first.getArtist());
        assertEquals("https://example.com/bohemian.png", first.getImageUrl());
        assertEquals(3, first.getLevel());
        assertEquals(List.of(10L), first.getGenreIds());
        assertEquals(List.of(1L), first.getInstrumentIds());
        assertTrue(first.getLessonIds().isEmpty());
    }

    @Test
    void testFindAll_Empty() {
        // Arrange
        when(songRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        assertTrue(songService.findAll().isEmpty());
    }

    // ---------- findById ----------

    @Test
    void testFindById_Found() {
        // Arrange
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));

        // Act
        Optional<SongDTO> result = songService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Bohemian Rhapsody", result.get().getTitle());
        verify(songRepository).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(songService.findById(999L).isEmpty());
    }

    @Test
    void testFindById_NullId() {
        // Act & Assert
        assertTrue(songService.findById(null).isEmpty());
        verify(songRepository, never()).findById(any());
    }

    // ---------- findByIds ----------

    @Test
    void testFindByIds_NullIds() {
        // Act
        List<SongDTO> result = songService.findByIds(null);

        // Assert
        assertTrue(result.isEmpty());
        verifyNoInteractions(songRepository);
    }

    @Test
    void testFindByIds_EmptyIds() {
        // Act & Assert
        assertTrue(songService.findByIds(List.of()).isEmpty());
        verifyNoInteractions(songRepository);
    }

    @Test
    void testFindByIds_ReturnsMapped() {
        // Arrange
        when(songRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(testSong, song(2L, "Yesterday")));

        // Act
        List<SongDTO> result = songService.findByIds(List.of(1L, 2L));

        // Assert
        assertEquals(List.of(1L, 2L), result.stream().map(SongDTO::getId).toList());
    }

    // ---------- create ----------

    @Test
    void testCreate_BasicFields() {
        // Arrange
        SongDTO dto = basicDto();
        dto.setGenreIds(null);
        dto.setInstrumentIds(null);
        dto.setLessons(null);
        when(songRepository.saveAndFlush(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);

        // Act
        SongDTO result = songService.create(dto);

        // Assert
        ArgumentCaptor<Song> captor = ArgumentCaptor.forClass(Song.class);
        verify(songRepository).saveAndFlush(captor.capture());
        Song saved = captor.getValue();
        assertEquals(5L, saved.getId());
        assertEquals("Imagine", saved.getTitle());
        assertEquals("John Lennon", saved.getArtist());
        assertEquals("https://example.com/imagine.png", saved.getImageUrl());
        assertEquals(Item.Type.Song, saved.getType());
        assertTrue(saved.getGenres().isEmpty());
        assertTrue(saved.getInstruments().isEmpty());
        assertTrue(saved.getLessons().isEmpty());
        verifyNoInteractions(genreRepository, instrumentRepository, chordRepository);
        assertEquals(5L, result.getId());
        assertEquals("Imagine", result.getTitle());
    }

    @Test
    void testCreate_CopiesCarouselFlags() {
        // Arrange
        SongDTO dto = basicDto();
        dto.setGenreIds(null);
        dto.setInstrumentIds(null);
        dto.setLessons(null);
        dto.setPopular(true);
        dto.setNewSong(true);
        dto.setGenrePopular(true);
        dto.setGenreNew(true);
        dto.setArtistPopular(true);
        dto.setArtistNew(true);
        when(songRepository.saveAndFlush(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);

        // Act
        SongDTO result = songService.create(dto);

        // Assert
        ArgumentCaptor<Song> captor = ArgumentCaptor.forClass(Song.class);
        verify(songRepository).saveAndFlush(captor.capture());
        Song saved = captor.getValue();
        assertTrue(saved.isPopular());
        assertTrue(saved.isNewSong());
        assertTrue(saved.isGenrePopular());
        assertTrue(saved.isGenreNew());
        assertFalse(saved.isGenreClassic());
        assertTrue(saved.isArtistPopular());
        assertTrue(saved.isArtistNew());
        assertFalse(saved.isArtistClassic());
        assertTrue(result.isPopular());
        assertTrue(result.isGenreNew());
    }

    @Test
    void testCreate_ResolvesGenresAndInstruments() {
        // Arrange
        SongDTO dto = basicDto();
        dto.setGenreIds(List.of(10L, 11L));
        dto.setInstrumentIds(List.of(1L, 2L));
        when(genreRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(rock, pop));
        when(instrumentRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(guitar, piano));
        when(songRepository.saveAndFlush(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);

        // Act
        SongDTO result = songService.create(dto);

        // Assert
        ArgumentCaptor<Song> captor = ArgumentCaptor.forClass(Song.class);
        verify(songRepository).saveAndFlush(captor.capture());
        assertEquals(Set.of(rock, pop), captor.getValue().getGenres());
        assertEquals(Set.of(guitar, piano), captor.getValue().getInstruments());
        assertEquals(Set.of(10L, 11L), Set.copyOf(result.getGenreIds()));
        assertEquals(Set.of(1L, 2L), Set.copyOf(result.getInstrumentIds()));
    }

    @Test
    void testCreate_EmptyRelationLists_QueryRepositoriesWithEmptyLists() {
        // Arrange
        SongDTO dto = basicDto();
        dto.setGenreIds(List.of());
        dto.setInstrumentIds(List.of());
        when(genreRepository.findAllById(List.of())).thenReturn(List.of());
        when(instrumentRepository.findAllById(List.of())).thenReturn(List.of());
        when(songRepository.saveAndFlush(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);

        // Act
        SongDTO result = songService.create(dto);

        // Assert
        assertTrue(result.getGenreIds().isEmpty());
        assertTrue(result.getInstrumentIds().isEmpty());
        assertTrue(result.getLessonIds().isEmpty());
    }

    @Test
    void testCreate_CascadesSongLessonsAndTabSections() {
        // Arrange
        SongDTO dto = basicDto();
        dto.setGenreIds(null);
        dto.setInstrumentIds(null);

        TabSectionDTO intro = new TabSectionDTO();
        intro.setIdentifier("Intro");
        intro.setContent("e|---0---|");
        TabSectionDTO solo = new TabSectionDTO();
        solo.setIdentifier("Solo");
        solo.setContent("e|---5---|");

        SongLessonDTO lessonDto = new SongLessonDTO();
        lessonDto.setId(500L);
        lessonDto.setDifficulty("MEDIUM");
        lessonDto.setChordedLyrics("[C] Imagine");
        lessonDto.setVideoUrl("https://example.com/video");
        lessonDto.setInstrumentId(1L);
        lessonDto.setChordIds(List.of(7L, 8L));
        lessonDto.setTabSections(List.of(intro, solo));
        dto.setLessons(List.of(lessonDto));

        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(guitar));
        when(chordRepository.findById(7L)).thenReturn(Optional.of(chord(7L, guitar)));
        when(chordRepository.findById(8L)).thenReturn(Optional.of(chord(8L, guitar)));
        when(songRepository.saveAndFlush(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);

        // Act
        SongDTO result = songService.create(dto);

        // Assert
        ArgumentCaptor<Song> captor = ArgumentCaptor.forClass(Song.class);
        verify(songRepository).saveAndFlush(captor.capture());
        Song saved = captor.getValue();
        assertEquals(1, saved.getLessons().size());
        SongLesson lesson = saved.getLessons().iterator().next();
        assertEquals(500L, lesson.getId());
        assertEquals(SongLesson.DifficultyLevel.MEDIUM, lesson.getDifficulty());
        assertEquals("[C] Imagine", lesson.getChordedLyrics());
        assertEquals("https://example.com/video", lesson.getVideoUrl());
        assertSame(guitar, lesson.getInstrument());
        assertSame(saved, lesson.getSong());
        assertEquals(List.of(7L, 8L), lesson.getChords().stream().map(Chord::getId).toList());

        List<TabSection> tabs = lesson.getTabSections();
        assertEquals(2, tabs.size());
        assertEquals("Intro", tabs.get(0).getIdentifier());
        assertEquals("e|---0---|", tabs.get(0).getContent());
        assertSame(lesson, tabs.get(0).getSongLesson());
        assertEquals("Solo", tabs.get(1).getIdentifier());
        assertSame(lesson, tabs.get(1).getSongLesson());

        assertEquals(List.of(500L), result.getLessonIds());
    }

    @Test
    void testCreate_LessonWithChordOfAnotherInstrument_ThrowsAndDoesNotSave() {
        // Arrange
        SongDTO dto = basicDto();
        dto.setGenreIds(null);
        dto.setInstrumentIds(null);
        SongLessonDTO lessonDto = new SongLessonDTO();
        lessonDto.setId(502L);
        lessonDto.setDifficulty("EASY");
        lessonDto.setInstrumentId(2L);
        lessonDto.setChordIds(List.of(7L));
        dto.setLessons(List.of(lessonDto));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(piano));
        when(chordRepository.findById(7L)).thenReturn(Optional.of(chord(7L, guitar)));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> songService.create(dto));
        verify(songRepository, never()).saveAndFlush(any());
    }

    @Test
    void testCreate_LessonWithoutChordsOrTabs() {
        // Arrange
        SongDTO dto = basicDto();
        dto.setGenreIds(null);
        dto.setInstrumentIds(null);
        SongLessonDTO lessonDto = new SongLessonDTO();
        lessonDto.setId(501L);
        lessonDto.setDifficulty("ORIGINAL");
        lessonDto.setInstrumentId(2L);
        dto.setLessons(List.of(lessonDto));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(piano));
        when(songRepository.saveAndFlush(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);

        // Act
        songService.create(dto);

        // Assert
        ArgumentCaptor<Song> captor = ArgumentCaptor.forClass(Song.class);
        verify(songRepository).saveAndFlush(captor.capture());
        SongLesson lesson = captor.getValue().getLessons().iterator().next();
        assertEquals(SongLesson.DifficultyLevel.ORIGINAL, lesson.getDifficulty());
        assertTrue(lesson.getChords().isEmpty());
        assertTrue(lesson.getTabSections().isEmpty());
        verifyNoInteractions(chordRepository);
    }

    @Test
    void testCreate_MultipleLessons() {
        // Arrange
        SongDTO dto = basicDto();
        dto.setGenreIds(null);
        dto.setInstrumentIds(null);
        SongLessonDTO easy = new SongLessonDTO();
        easy.setId(1L);
        easy.setDifficulty("EASY");
        easy.setInstrumentId(1L);
        SongLessonDTO original = new SongLessonDTO();
        original.setId(2L);
        original.setDifficulty("ORIGINAL");
        original.setInstrumentId(2L);
        dto.setLessons(List.of(easy, original));
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(guitar));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(piano));
        when(songRepository.saveAndFlush(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);

        // Act
        SongDTO result = songService.create(dto);

        // Assert
        assertEquals(List.of(1L, 2L), result.getLessonIds());
    }

    @Test
    void testCreate_InvalidDifficulty_ThrowsAndDoesNotSave() {
        // Arrange
        SongDTO dto = basicDto();
        dto.setGenreIds(null);
        dto.setInstrumentIds(null);
        SongLessonDTO lessonDto = new SongLessonDTO();
        lessonDto.setId(1L);
        lessonDto.setDifficulty("IMPOSSIBLE");
        lessonDto.setInstrumentId(1L);
        dto.setLessons(List.of(lessonDto));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> songService.create(dto));
        verify(songRepository, never()).saveAndFlush(any());
    }

    @Test
    void testCreate_UnknownLessonInstrument_ThrowsAndDoesNotSave() {
        // Arrange
        SongDTO dto = basicDto();
        dto.setGenreIds(null);
        dto.setInstrumentIds(null);
        SongLessonDTO lessonDto = new SongLessonDTO();
        lessonDto.setId(1L);
        lessonDto.setDifficulty("EASY");
        lessonDto.setInstrumentId(999L);
        dto.setLessons(List.of(lessonDto));
        when(instrumentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> songService.create(dto));
        verify(songRepository, never()).saveAndFlush(any());
    }

    @Test
    void testCreate_LessonWithoutInstrument_ThrowsIllegalArgument() {
        // Arrange
        SongDTO dto = basicDto();
        dto.setGenreIds(null);
        dto.setInstrumentIds(null);
        SongLessonDTO lessonDto = new SongLessonDTO();
        lessonDto.setId(1L);
        lessonDto.setDifficulty("EASY");
        dto.setLessons(List.of(lessonDto));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> songService.create(dto));
        verify(songRepository, never()).saveAndFlush(any());
    }

    // ---------- update ----------

    @Test
    void testUpdate_Found_UpdatesFieldsAndReplacesRelations() {
        // Arrange
        SongLesson sl = songLesson(300L);
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(genreRepository.findById(11L)).thenReturn(Optional.of(pop));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(piano));
        when(songLessonRepository.findById(300L)).thenReturn(Optional.of(sl));
        when(songRepository.save(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);

        SongDTO dto = basicDto();
        dto.setGenreIds(List.of(11L));
        dto.setInstrumentIds(List.of(2L));
        dto.setLessonIds(List.of(300L));

        // Act
        Optional<SongDTO> result = songService.update(1L, dto);

        // Assert
        assertTrue(result.isPresent());
        verify(songRepository).save(testSong);
        assertEquals(1L, testSong.getId(), "the id must not change on update");
        assertEquals("Imagine", testSong.getTitle());
        assertEquals("John Lennon", testSong.getArtist());
        assertEquals(Set.of(pop), testSong.getGenres());
        assertEquals(Set.of(piano), testSong.getInstruments());
        assertEquals(Set.of(sl), testSong.getLessons());
        assertEquals(List.of(11L), result.get().getGenreIds());
        assertEquals(List.of(2L), result.get().getInstrumentIds());
        assertEquals(List.of(300L), result.get().getLessonIds());
    }

    @Test
    void testUpdate_EmptyRelationLists_ClearsRelations() {
        // Arrange
        testSong.getLessons().add(songLesson(300L));
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(songRepository.save(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);
        SongDTO dto = basicDto();
        dto.setGenreIds(List.of());
        dto.setInstrumentIds(List.of());
        dto.setLessonIds(List.of());

        // Act
        songService.update(1L, dto);

        // Assert
        assertTrue(testSong.getGenres().isEmpty());
        assertTrue(testSong.getInstruments().isEmpty());
        assertTrue(testSong.getLessons().isEmpty());
    }

    @Test
    void testUpdate_OmittedRelationLists_KeepsRelations() {
        // Arrange (regression: omitted lists used to wipe the relations)
        SongLesson sl = songLesson(300L);
        testSong.getLessons().add(sl);
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(songRepository.save(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);

        // Act: basicDto() leaves genreIds, instrumentIds and lessonIds as null
        songService.update(1L, basicDto());

        // Assert
        assertEquals(Set.of(rock), testSong.getGenres());
        assertEquals(Set.of(guitar), testSong.getInstruments());
        assertEquals(Set.of(sl), testSong.getLessons());
        verifyNoInteractions(genreRepository, instrumentRepository, songLessonRepository);
    }

    @Test
    void testUpdate_UpdatesImageUrlAndLevel() {
        // Arrange (regression: imageUrl and level were never updated)
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(songRepository.save(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);
        SongDTO dto = basicDto();
        dto.setLevel(5);

        // Act
        SongDTO result = songService.update(1L, dto).orElseThrow();

        // Assert
        assertEquals("https://example.com/imagine.png", testSong.getImageUrl());
        assertEquals(5, testSong.getLevel());
        assertEquals(5, result.getLevel());
    }

    @Test
    void testUpdate_LevelZero_KeepsCurrentLevel() {
        // Arrange
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(songRepository.save(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);

        // Act: level 0 means the field was omitted
        songService.update(1L, basicDto());

        // Assert
        assertEquals(3, testSong.getLevel());
    }

    @Test
    void testUpdate_OverwritesCarouselFlags() {
        // Arrange
        testSong.setPopular(true);
        testSong.setGenreClassic(true);
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(songRepository.save(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);
        SongDTO dto = basicDto();
        dto.setGenreNew(true);
        dto.setArtistClassic(true);

        // Act
        songService.update(1L, dto);

        // Assert
        assertFalse(testSong.isPopular());
        assertFalse(testSong.isGenreClassic());
        assertTrue(testSong.isGenreNew());
        assertTrue(testSong.isArtistClassic());
    }

    @Test
    void testUpdate_WithDecade_ReplacesDecade() {
        // Arrange
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(songRepository.save(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);
        SongDTO dto = basicDto();
        dto.setDecade("1980s");

        // Act
        songService.update(1L, dto);

        // Assert
        assertEquals("1980s", testSong.getDecade());
    }

    @Test
    void testUpdate_NullDecade_KeepsCurrentDecade() {
        // Arrange
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(songRepository.save(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);

        // Act: basicDto() leaves the decade as null (omitted)
        songService.update(1L, basicDto());

        // Assert
        assertEquals("1970s", testSong.getDecade());
    }

    @Test
    void testUpdate_LessonIds_ModifiesManagedCollectionInPlace() {
        // Arrange (regression: replacing an orphanRemoval collection breaks Hibernate)
        SongLesson kept = songLesson(300L);
        SongLesson removed = songLesson(301L);
        SongLesson added = songLesson(302L);
        testSong.getLessons().add(kept);
        testSong.getLessons().add(removed);
        Set<SongLesson> managedCollection = testSong.getLessons();
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(songLessonRepository.findById(300L)).thenReturn(Optional.of(kept));
        when(songLessonRepository.findById(302L)).thenReturn(Optional.of(added));
        when(songRepository.save(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);
        SongDTO dto = basicDto();
        dto.setLessonIds(List.of(300L, 302L));

        // Act
        songService.update(1L, dto);

        // Assert
        assertSame(managedCollection, testSong.getLessons());
        assertEquals(Set.of(kept, added), testSong.getLessons());
        assertSame(testSong, added.getSong());
    }

    @Test
    void testCreate_SetsLevel() {
        // Arrange (regression: level was ignored on create)
        when(songRepository.saveAndFlush(any(Song.class))).thenAnswer(SongServiceTest::savedArgument);
        SongDTO dto = basicDto();
        dto.setLevel(4);

        // Act
        SongDTO result = songService.create(dto);

        // Assert
        assertEquals(4, result.getLevel());
    }

    @Test
    void testCreate_NestedLessonWithoutDifficulty_ThrowsBadRequestAndDoesNotSave() {
        // Arrange (regression: a missing difficulty used to end in a NullPointerException / 500)
        SongDTO dto = basicDto();
        SongLessonDTO lessonDto = new SongLessonDTO();
        lessonDto.setInstrumentId(1L);
        dto.setLessons(List.of(lessonDto));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> songService.create(dto));
        assertEquals("Difficulty is required", ex.getMessage());
        verify(songRepository, never()).saveAndFlush(any());
    }

    @Test
    void testUpdate_UnknownGenre_ThrowsAndDoesNotSave() {
        // Arrange
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(genreRepository.findById(999L)).thenReturn(Optional.empty());
        SongDTO dto = basicDto();
        dto.setGenreIds(List.of(999L));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> songService.update(1L, dto));
        verify(songRepository, never()).save(any());
    }

    @Test
    void testUpdate_UnknownSongLesson_ThrowsAndDoesNotSave() {
        // Arrange
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(songLessonRepository.findById(999L)).thenReturn(Optional.empty());
        SongDTO dto = basicDto();
        dto.setLessonIds(List.of(999L));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> songService.update(1L, dto));
        verify(songRepository, never()).save(any());
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(songService.update(999L, basicDto()).isEmpty());
        verify(songRepository, never()).save(any());
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertTrue(songService.update(null, basicDto()).isEmpty());
        verify(songRepository, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    void testDelete_Exists() {
        // Arrange
        when(songRepository.existsById(1L)).thenReturn(true);

        // Act & Assert
        assertTrue(songService.delete(1L));
        verify(songRepository).deleteById(1L);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        when(songRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertFalse(songService.delete(999L));
        verify(songRepository, never()).deleteById(anyLong());
    }

    // ---------- filtered queries ----------

    @Test
    void testFindByDecade() {
        // Arrange
        when(songRepository.findByDecade("1970s")).thenReturn(List.of(testSong));

        // Act
        List<SongDTO> result = songService.findByDecade("1970s");

        // Assert
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testFindByDecade_NoMatches() {
        // Arrange
        when(songRepository.findByDecade("1950s")).thenReturn(List.of());

        // Act & Assert
        assertTrue(songService.findByDecade("1950s").isEmpty());
    }

    @Test
    void testFindPopular_WithLimit_UsesFirstPageOfThatSize() {
        // Arrange
        when(songRepository.findByPopularTrue(any(Pageable.class))).thenReturn(List.of(testSong));

        // Act
        List<SongDTO> result = songService.findPopular(5);

        // Assert
        assertEquals(1, result.size());
        verify(songRepository).findByPopularTrue(PageRequest.of(0, 5));
    }

    @Test
    void testFindPopular_NullLimit_IsUnpaged() {
        // Arrange
        when(songRepository.findByPopularTrue(any(Pageable.class))).thenReturn(List.of(testSong));

        // Act
        songService.findPopular(null);

        // Assert
        verify(songRepository).findByPopularTrue(Pageable.unpaged());
    }

    @Test
    void testFindPopular_ZeroLimit_Throws() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> songService.findPopular(0));
        verifyNoInteractions(songRepository);
    }

    @Test
    void testFindPopular_NegativeLimit_Throws() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> songService.findPopular(-1));
        verifyNoInteractions(songRepository);
    }

    @Test
    void testFindNew_WithLimit_UsesFirstPageOfThatSize() {
        // Arrange
        when(songRepository.findByGenreNewTrue(any(Pageable.class))).thenReturn(List.of(testSong));

        // Act
        List<SongDTO> result = songService.findNew(3);

        // Assert
        assertEquals(1, result.size());
        verify(songRepository).findByGenreNewTrue(PageRequest.of(0, 3));
    }

    @Test
    void testFindNew_NullLimit_IsUnpaged() {
        // Arrange
        when(songRepository.findByGenreNewTrue(any(Pageable.class))).thenReturn(new ArrayList<>());

        // Act
        List<SongDTO> result = songService.findNew(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(songRepository).findByGenreNewTrue(Pageable.unpaged());
    }

    @Test
    void testFindNew_InvalidLimit_Throws() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> songService.findNew(0));
        verifyNoInteractions(songRepository);
    }

    @Test
    void testFindPopularForGenre() {
        // Arrange
        when(songRepository.findByGenresIdAndGenrePopularTrue(10L)).thenReturn(List.of(testSong));

        // Act
        List<SongDTO> result = songService.findPopularForGenre(10L);

        // Assert
        assertEquals(List.of(1L), result.stream().map(SongDTO::getId).toList());
        verify(songRepository).findByGenresIdAndGenrePopularTrue(10L);
    }

    @Test
    void testFindClassicForGenre() {
        // Arrange
        when(songRepository.findByGenresIdAndGenreClassicTrue(10L)).thenReturn(List.of(testSong));

        // Act
        List<SongDTO> result = songService.findClassicForGenre(10L);

        // Assert
        assertEquals(List.of(1L), result.stream().map(SongDTO::getId).toList());
        verify(songRepository).findByGenresIdAndGenreClassicTrue(10L);
    }

    @Test
    void testFindNewForGenre() {
        // Arrange
        when(songRepository.findByGenresIdAndGenreNewTrue(10L)).thenReturn(List.of(testSong));

        // Act
        List<SongDTO> result = songService.findNewForGenre(10L);

        // Assert
        assertEquals(List.of(1L), result.stream().map(SongDTO::getId).toList());
        verify(songRepository).findByGenresIdAndGenreNewTrue(10L);
    }

    @Test
    void testGenreQueries_NoMatches() {
        // Arrange
        when(songRepository.findByGenresIdAndGenrePopularTrue(99L)).thenReturn(List.of());
        when(songRepository.findByGenresIdAndGenreClassicTrue(99L)).thenReturn(List.of());
        when(songRepository.findByGenresIdAndGenreNewTrue(99L)).thenReturn(List.of());

        // Act & Assert
        assertTrue(songService.findPopularForGenre(99L).isEmpty());
        assertTrue(songService.findClassicForGenre(99L).isEmpty());
        assertTrue(songService.findNewForGenre(99L).isEmpty());
    }

    // ---------- findNewForArtists / findPopularForArtists ----------

    private static Song songBy(Long id, String artist) {
        Song song = song(id, "Song " + id);
        song.setArtist(artist);
        return song;
    }

    private static List<Long> ids(List<SongDTO> songs) {
        return songs.stream().map(SongDTO::getId).toList();
    }

    @Test
    void testFindNewForArtists_MixesTheArtistsInTurns() {
        // Arrange
        when(songRepository.findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn(eq("Queen"), anyCollection(), any(Pageable.class)))
                .thenReturn(List.of(songBy(1L, "Queen"), songBy(2L, "Queen"), songBy(3L, "Queen")));
        when(songRepository.findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn(eq("Bowie"), anyCollection(), any(Pageable.class)))
                .thenReturn(List.of(songBy(10L, "Bowie")));

        // Act
        List<SongDTO> result = songService.findNewForArtists(List.of("Queen", "Bowie"), 10);

        // Assert: one of each artist in turns; when Bowie runs out, Queen fills the rest
        assertEquals(List.of(1L, 10L, 2L, 3L), ids(result));
    }

    @Test
    void testFindNewForArtists_StopsAtTheLimitAndAsksEachArtistForUpToTheLimit() {
        // Arrange
        when(songRepository.findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn(eq("Queen"), anyCollection(), any(Pageable.class)))
                .thenReturn(List.of(songBy(1L, "Queen"), songBy(2L, "Queen")));
        when(songRepository.findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn(eq("Bowie"), anyCollection(), any(Pageable.class)))
                .thenReturn(List.of(songBy(10L, "Bowie"), songBy(11L, "Bowie")));

        // Act
        List<SongDTO> result = songService.findNewForArtists(List.of("Queen", "Bowie"), 3);

        // Assert
        assertEquals(List.of(1L, 10L, 2L), ids(result));
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(songRepository, times(2)).findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn(anyString(), anyCollection(), pageable.capture());
        assertTrue(pageable.getAllValues().stream().allMatch(p -> p.getPageSize() == 3));
    }

    @Test
    void testFindNewForArtists_DefaultLimitIsTenAndDuplicatedArtistsAreAskedOnce() {
        // Arrange
        when(songRepository.findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn(eq("Queen"), anyCollection(), any(Pageable.class)))
                .thenReturn(List.of());

        // Act
        List<SongDTO> result = songService.findNewForArtists(java.util.Arrays.asList(" Queen ", "Queen", "", null), null);

        // Assert
        assertTrue(result.isEmpty());
        verify(songRepository).findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn(eq("Queen"), anyCollection(), eq(PageRequest.of(0, 10)));
    }

    @Test
    void testFindPopularForArtists_UsesThePopularQuery() {
        // Arrange
        when(songRepository.findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn(eq("Queen"), anyCollection(), any(Pageable.class)))
                .thenReturn(List.of(songBy(5L, "Queen")));

        // Act & Assert
        assertEquals(List.of(5L), ids(songService.findPopularForArtists(List.of("Queen"), 12)));
        verify(songRepository, never()).findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn(anyString(), anyCollection(), any());
    }

    @Test
    void testFindForArtists_InvalidArgumentsAreRejectedWithoutQuerying() {
        List<String> eleven = java.util.stream.IntStream.rangeClosed(1, 11).mapToObj(i -> "Artist " + i).toList();

        assertThrows(IllegalArgumentException.class, () -> songService.findNewForArtists(List.of(), 10));
        assertThrows(IllegalArgumentException.class, () -> songService.findNewForArtists(null, 10));
        assertThrows(IllegalArgumentException.class, () -> songService.findNewForArtists(List.of(" "), 10));
        assertThrows(IllegalArgumentException.class, () -> songService.findNewForArtists(eleven, 10));
        assertThrows(IllegalArgumentException.class, () -> songService.findPopularForArtists(List.of("Queen"), 51));
        assertThrows(IllegalArgumentException.class, () -> songService.findPopularForArtists(List.of("Queen"), 0));
        verifyNoInteractions(songRepository);
    }
}
