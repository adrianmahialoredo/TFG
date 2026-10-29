package com.uo289390.toco.service;

import static com.uo289390.toco.service.EntityResolverTestSupport.initResolver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.uo289390.toco.service.SongLessonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uo289390.toco.dto.SongLessonDTO;
import com.uo289390.toco.model.Chord;
import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.Song;
import com.uo289390.toco.model.SongLesson;
import com.uo289390.toco.model.TabSection;
import com.uo289390.toco.repository.ChordRepository;
import com.uo289390.toco.repository.InstrumentRepository;
import com.uo289390.toco.repository.SongLessonRepository;
import com.uo289390.toco.repository.SongRepository;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.mapper.EntityMapper;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for {@link SongLessonServiceImpl}.
 * Covers CRUD operations and the resolution of song, instrument and chords
 * (which must belong to the lesson's instrument).
 */
@ExtendWith(MockitoExtension.class)
class SongLessonServiceTest {

    @Mock
    private SongLessonRepository songLessonRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private ChordRepository chordRepository;

    @Spy
    private DtoMapper dtoMapper = new DtoMapper();

    @Spy
    private EntityMapper entityMapper = new EntityMapper();

    @InjectMocks
    private SongLessonServiceImpl songLessonService;

    private Song song1;
    private Song song2;
    private Instrument guitar;
    private Instrument piano;
    private Chord chordC;
    private Chord chordG;
    private SongLesson testLesson;

    @BeforeEach
    void setUp() {
        initResolver(songLessonRepository, songRepository, instrumentRepository, chordRepository);

        song1 = song(1L);
        song2 = song(2L);
        guitar = instrument(10L);
        piano = instrument(20L);
        chordC = chord(100L, guitar);
        chordG = chord(200L, guitar);

        testLesson = new SongLesson();
        testLesson.setId(50L);
        testLesson.setDifficulty(SongLesson.DifficultyLevel.EASY);
        testLesson.setChordedLyrics("[C] Hello");
        testLesson.setVideoUrl("https://example.com/v1");
        testLesson.setSong(song1);
        testLesson.setInstrument(guitar);
        testLesson.setChords(new LinkedHashSet<>(List.of(chordC)));
        TabSection tab = new TabSection();
        tab.setId(900L);
        tab.setSongLesson(testLesson);
        testLesson.setTabSections(new ArrayList<>(List.of(tab)));
    }

    private static Song song(Long id) {
        Song song = new Song();
        song.setId(id);
        song.setTitle("Song " + id);
        return song;
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

    private static SongLessonDTO dto(Long songId, Long instrumentId, List<Long> chordIds) {
        SongLessonDTO dto = new SongLessonDTO();
        dto.setId(60L);
        dto.setDifficulty("ORIGINAL");
        dto.setChordedLyrics("[G] World");
        dto.setVideoUrl("https://example.com/v2");
        dto.setSongId(songId);
        dto.setInstrumentId(instrumentId);
        dto.setChordIds(chordIds);
        return dto;
    }

    // ---------- create ----------

    @Test
    void testCreate_ResolvesRelationsAndSaves() {
        // Arrange
        when(songRepository.findById(2L)).thenReturn(Optional.of(song2));
        when(instrumentRepository.findById(20L)).thenReturn(Optional.of(piano));
        when(chordRepository.findById(200L)).thenReturn(Optional.of(chord(200L, piano)));
        when(chordRepository.findById(100L)).thenReturn(Optional.of(chord(100L, piano)));
        when(songLessonRepository.save(any(SongLesson.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SongLessonDTO result = songLessonService.create(dto(2L, 20L, List.of(200L, 100L)));

        // Assert
        ArgumentCaptor<SongLesson> captor = ArgumentCaptor.forClass(SongLesson.class);
        verify(songLessonRepository).save(captor.capture());
        SongLesson saved = captor.getValue();
        assertEquals(60L, saved.getId());
        assertEquals(SongLesson.DifficultyLevel.ORIGINAL, saved.getDifficulty());
        assertEquals("[G] World", saved.getChordedLyrics());
        assertEquals("https://example.com/v2", saved.getVideoUrl());
        assertSame(song2, saved.getSong());
        assertSame(piano, saved.getInstrument());
        assertEquals(List.of(200L, 100L), saved.getChords().stream().map(Chord::getId).toList());
        assertTrue(saved.getTabSections().isEmpty());

        assertEquals(60L, result.getId());
        assertEquals("ORIGINAL", result.getDifficulty());
        assertEquals(2L, result.getSongId());
        assertEquals(20L, result.getInstrumentId());
        assertEquals(List.of(200L, 100L), result.getChordIds());
        assertTrue(result.getTabSectionsIds().isEmpty());
    }

    @Test
    void testCreate_WithoutChords() {
        // Arrange
        when(songRepository.findById(2L)).thenReturn(Optional.of(song2));
        when(instrumentRepository.findById(20L)).thenReturn(Optional.of(piano));
        when(songLessonRepository.save(any(SongLesson.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SongLessonDTO result = songLessonService.create(dto(2L, 20L, null));

        // Assert
        assertTrue(result.getChordIds().isEmpty());
        verifyNoInteractions(chordRepository);
    }

    @Test
    void testCreate_UnknownSong_ThrowsAndDoesNotSave() {
        // Arrange
        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> songLessonService.create(dto(999L, 20L, null)));
        assertTrue(ex.getMessage().contains("Song"));
        verify(songLessonRepository, never()).save(any());
    }

    @Test
    void testCreate_UnknownInstrument_ThrowsAndDoesNotSave() {
        // Arrange
        when(songRepository.findById(2L)).thenReturn(Optional.of(song2));
        when(instrumentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> songLessonService.create(dto(2L, 999L, null)));
        verify(songLessonRepository, never()).save(any());
    }

    @Test
    void testCreate_UnknownChord_ThrowsAndDoesNotSave() {
        // Arrange
        when(songRepository.findById(2L)).thenReturn(Optional.of(song2));
        when(instrumentRepository.findById(20L)).thenReturn(Optional.of(piano));
        when(chordRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> songLessonService.create(dto(2L, 20L, List.of(999L))));
        verify(songLessonRepository, never()).save(any());
    }

    @Test
    void testCreate_ChordOfAnotherInstrument_ThrowsAndDoesNotSave() {
        // Arrange
        when(songRepository.findById(2L)).thenReturn(Optional.of(song2));
        when(instrumentRepository.findById(20L)).thenReturn(Optional.of(piano));
        when(chordRepository.findById(100L)).thenReturn(Optional.of(chordC));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> songLessonService.create(dto(2L, 20L, List.of(100L))));
        assertTrue(ex.getMessage().contains("instrument"));
        verify(songLessonRepository, never()).save(any());
    }

    @Test
    void testCreate_NullSongId_ThrowsIllegalArgument() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> songLessonService.create(dto(null, 20L, null)));
        verify(songLessonRepository, never()).save(any());
    }

    @Test
    void testCreate_InvalidDifficulty_ThrowsIllegalArgument() {
        // Arrange
        when(songRepository.findById(2L)).thenReturn(Optional.of(song2));
        when(instrumentRepository.findById(20L)).thenReturn(Optional.of(piano));
        SongLessonDTO dto = dto(2L, 20L, null);
        dto.setDifficulty("HARD");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> songLessonService.create(dto));
        verify(songLessonRepository, never()).save(any());
    }

    // ---------- findAll / findById / findByIds / findForSong ----------

    @Test
    void testFindAll_ReturnsMapped() {
        // Arrange
        when(songLessonRepository.findAll()).thenReturn(List.of(testLesson));

        // Act
        List<SongLessonDTO> result = songLessonService.findAll();

        // Assert
        assertEquals(1, result.size());
        SongLessonDTO dto = result.get(0);
        assertEquals(50L, dto.getId());
        assertEquals("EASY", dto.getDifficulty());
        assertEquals("[C] Hello", dto.getChordedLyrics());
        assertEquals("https://example.com/v1", dto.getVideoUrl());
        assertEquals(1L, dto.getSongId());
        assertEquals(10L, dto.getInstrumentId());
        assertEquals(List.of(100L), dto.getChordIds());
        assertEquals(List.of(900L), dto.getTabSectionsIds());
    }

    @Test
    void testFindAll_Empty() {
        // Arrange
        when(songLessonRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        assertTrue(songLessonService.findAll().isEmpty());
    }

    @Test
    void testFindById_Found() {
        // Arrange
        when(songLessonRepository.findById(50L)).thenReturn(Optional.of(testLesson));

        // Act
        Optional<SongLessonDTO> result = songLessonService.findById(50L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(50L, result.get().getId());
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(songLessonRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(songLessonService.findById(999L).isEmpty());
    }

    @Test
    void testFindById_NullId() {
        // Act & Assert
        assertTrue(songLessonService.findById(null).isEmpty());
        verify(songLessonRepository, never()).findById(any());
    }

    @Test
    void testFindByIds_NullIds() {
        // Act & Assert
        assertTrue(songLessonService.findByIds(null).isEmpty());
        verifyNoInteractions(songLessonRepository);
    }

    @Test
    void testFindByIds_EmptyIds() {
        // Act & Assert
        assertTrue(songLessonService.findByIds(List.of()).isEmpty());
        verifyNoInteractions(songLessonRepository);
    }

    @Test
    void testFindByIds_ReturnsMapped() {
        // Arrange
        when(songLessonRepository.findAllById(List.of(50L))).thenReturn(List.of(testLesson));

        // Act
        List<SongLessonDTO> result = songLessonService.findByIds(List.of(50L));

        // Assert
        assertEquals(List.of(50L), result.stream().map(SongLessonDTO::getId).toList());
    }

    @Test
    void testFindForSong_ReturnsMapped() {
        // Arrange
        when(songLessonRepository.findBySongId(1L)).thenReturn(List.of(testLesson));

        // Act
        List<SongLessonDTO> result = songLessonService.findForSong(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getSongId());
        verify(songLessonRepository).findBySongId(1L);
    }

    @Test
    void testFindForSong_Empty() {
        // Arrange
        when(songLessonRepository.findBySongId(99L)).thenReturn(List.of());

        // Act & Assert
        assertTrue(songLessonService.findForSong(99L).isEmpty());
    }

    // ---------- update ----------

    @Test
    void testUpdate_SameSongAndInstrument_DoesNotResolveThemAgain() {
        // Arrange
        when(songLessonRepository.findById(50L)).thenReturn(Optional.of(testLesson));
        when(chordRepository.findById(200L)).thenReturn(Optional.of(chordG));
        when(songLessonRepository.save(any(SongLesson.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Optional<SongLessonDTO> result = songLessonService.update(50L, dto(1L, 10L, List.of(200L)));

        // Assert
        assertTrue(result.isPresent());
        verify(songLessonRepository).save(testLesson);
        assertEquals(50L, testLesson.getId(), "the id must not change on update");
        assertEquals(SongLesson.DifficultyLevel.ORIGINAL, testLesson.getDifficulty());
        assertEquals("[G] World", testLesson.getChordedLyrics());
        assertEquals("https://example.com/v2", testLesson.getVideoUrl());
        assertSame(song1, testLesson.getSong());
        assertSame(guitar, testLesson.getInstrument());
        assertEquals(Set.of(chordG), testLesson.getChords());
        verifyNoInteractions(songRepository, instrumentRepository);
        assertEquals(List.of(200L), result.get().getChordIds());
    }

    @Test
    void testUpdate_DifferentSongAndInstrument_ResolvesNewOnes() {
        // Arrange
        when(songLessonRepository.findById(50L)).thenReturn(Optional.of(testLesson));
        when(songRepository.findById(2L)).thenReturn(Optional.of(song2));
        when(instrumentRepository.findById(20L)).thenReturn(Optional.of(piano));
        when(songLessonRepository.save(any(SongLesson.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Optional<SongLessonDTO> result = songLessonService.update(50L, dto(2L, 20L, List.of()));

        // Assert
        assertTrue(result.isPresent());
        assertSame(song2, testLesson.getSong());
        assertSame(piano, testLesson.getInstrument());
        assertTrue(testLesson.getChords().isEmpty());
        assertEquals(2L, result.get().getSongId());
        assertEquals(20L, result.get().getInstrumentId());
    }

    @Test
    void testUpdate_NewInstrumentWithChordsOfTheOldOne_ThrowsAndDoesNotSave() {
        // Arrange
        when(songLessonRepository.findById(50L)).thenReturn(Optional.of(testLesson));
        when(instrumentRepository.findById(20L)).thenReturn(Optional.of(piano));
        when(chordRepository.findById(100L)).thenReturn(Optional.of(chordC));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> songLessonService.update(50L, dto(1L, 20L, List.of(100L))));
        verify(songLessonRepository, never()).save(any());
    }

    @Test
    void testUpdate_NullChordIds_ClearsChords() {
        // Arrange
        when(songLessonRepository.findById(50L)).thenReturn(Optional.of(testLesson));
        when(songLessonRepository.save(any(SongLesson.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        songLessonService.update(50L, dto(1L, 10L, null));

        // Assert
        assertTrue(testLesson.getChords().isEmpty());
    }

    @Test
    void testUpdate_UnknownNewSong_ThrowsAndDoesNotSave() {
        // Arrange
        when(songLessonRepository.findById(50L)).thenReturn(Optional.of(testLesson));
        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> songLessonService.update(50L, dto(999L, 10L, null)));
        verify(songLessonRepository, never()).save(any());
    }

    @Test
    void testUpdate_NullSongId_ThrowsIllegalArgument() {
        // Arrange
        when(songLessonRepository.findById(50L)).thenReturn(Optional.of(testLesson));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> songLessonService.update(50L, dto(null, 10L, null)));
        verify(songLessonRepository, never()).save(any());
    }

    @Test
    void testUpdate_InvalidDifficulty_Throws() {
        // Arrange
        when(songLessonRepository.findById(50L)).thenReturn(Optional.of(testLesson));
        SongLessonDTO dto = dto(1L, 10L, null);
        dto.setDifficulty("unknown");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> songLessonService.update(50L, dto));
        verify(songLessonRepository, never()).save(any());
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        when(songLessonRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(songLessonService.update(999L, dto(1L, 10L, null)).isEmpty());
        verify(songLessonRepository, never()).save(any());
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertTrue(songLessonService.update(null, dto(1L, 10L, null)).isEmpty());
        verify(songLessonRepository, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    void testDelete_Exists() {
        // Arrange
        when(songLessonRepository.existsById(50L)).thenReturn(true);

        // Act & Assert
        assertTrue(songLessonService.delete(50L));
        verify(songLessonRepository).deleteById(50L);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        when(songLessonRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertFalse(songLessonService.delete(999L));
        verify(songLessonRepository, never()).deleteById(anyLong());
    }
}
