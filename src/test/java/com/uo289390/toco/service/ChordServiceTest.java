package com.uo289390.toco.service;

import static com.uo289390.toco.service.EntityResolverTestSupport.initResolver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import com.uo289390.toco.service.ChordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uo289390.toco.dto.ChordDto;
import com.uo289390.toco.model.Chord;
import com.uo289390.toco.model.SongLesson;
import com.uo289390.toco.repository.ChordRepository;
import com.uo289390.toco.repository.SongLessonRepository;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.mapper.EntityMapper;

/**
 * Unit tests for {@link ChordServiceImpl}.
 * Covers CRUD operations, bulk lookups and chord ids by song lesson.
 */
@ExtendWith(MockitoExtension.class)
class ChordServiceTest {

    @Mock
    private ChordRepository chordRepository;

    @Mock
    private SongLessonRepository songLessonRepository;

    @Spy
    private DtoMapper dtoMapper = new DtoMapper();

    @Spy
    private EntityMapper entityMapper = new EntityMapper();

    @InjectMocks
    private ChordServiceImpl chordService;

    private Chord testChord;

    @BeforeEach
    void setUp() {
        initResolver(chordRepository, songLessonRepository);

        testChord = chord(1L, "C");
        testChord.setNotes(List.of("C", "E", "G"));
        testChord.setImageUrl("https://example.com/c.png");
        testChord.setOpen(true);
        testChord.setMajor(true);
        testChord.setCapoFret(0);
    }

    private static Chord chord(Long id, String name) {
        Chord chord = new Chord();
        chord.setId(id);
        chord.setName(name);
        return chord;
    }

    private static ChordDto fullDto() {
        ChordDto dto = new ChordDto();
        dto.setId(5L);
        dto.setName("F");
        dto.setNotes(List.of("F", "A", "C"));
        dto.setImageUrl("https://example.com/f.png");
        dto.setOpen(false);
        dto.setBarre(true);
        dto.setPowerChord(true);
        dto.setMinor(true);
        dto.setMajor(false);
        dto.setSeventh(true);
        dto.setCapo(true);
        dto.setCapoFret(3);
        return dto;
    }

    // ---------- findAll ----------

    @Test
    void testFindAll_ReturnsMappedChords() {
        // Arrange
        when(chordRepository.findAll()).thenReturn(List.of(testChord, chord(2L, "G")));

        // Act
        List<ChordDto> result = chordService.findAll();

        // Assert
        assertEquals(2, result.size());
        ChordDto first = result.get(0);
        assertEquals(1L, first.getId());
        assertEquals("C", first.getName());
        assertEquals(List.of("C", "E", "G"), first.getNotes());
        assertEquals("https://example.com/c.png", first.getImageUrl());
        assertTrue(first.isOpen());
        assertTrue(first.isMajor());
        assertFalse(first.isBarre());
        assertEquals("G", result.get(1).getName());
        verify(chordRepository).findAll();
    }

    @Test
    void testFindAll_Empty() {
        // Arrange
        when(chordRepository.findAll()).thenReturn(List.of());

        // Act
        List<ChordDto> result = chordService.findAll();

        // Assert
        assertTrue(result.isEmpty());
    }

    // ---------- findById ----------

    @Test
    void testFindById_Found() {
        // Arrange
        when(chordRepository.findById(1L)).thenReturn(Optional.of(testChord));

        // Act
        Optional<ChordDto> result = chordService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("C", result.get().getName());
        verify(chordRepository).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(chordRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ChordDto> result = chordService.findById(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindById_NullId_ReturnsEmptyWithoutQuerying() {
        // Act
        Optional<ChordDto> result = chordService.findById(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(chordRepository, never()).findById(any());
    }

    // ---------- create ----------

    @Test
    void testCreate_SavesAllFieldsAndReturnsSavedDto() {
        // Arrange
        ChordDto dto = fullDto();
        when(chordRepository.save(any(Chord.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ChordDto result = chordService.create(dto);

        // Assert
        ArgumentCaptor<Chord> captor = ArgumentCaptor.forClass(Chord.class);
        verify(chordRepository).save(captor.capture());
        Chord saved = captor.getValue();
        assertEquals(5L, saved.getId());
        assertEquals("F", saved.getName());
        assertEquals(List.of("F", "A", "C"), saved.getNotes());
        assertEquals("https://example.com/f.png", saved.getImageUrl());
        assertFalse(saved.isOpen());
        assertTrue(saved.isBarre());
        assertTrue(saved.isPowerChord());
        assertTrue(saved.isMinor());
        assertFalse(saved.isMajor());
        assertTrue(saved.isSeventh());
        assertTrue(saved.isCapo());
        assertEquals(3, saved.getCapoFret());
        assertEquals(dto, result);
    }

    @Test
    void testCreate_ReturnsDtoOfPersistedEntity() {
        // Arrange
        when(chordRepository.save(any(Chord.class))).thenReturn(testChord);

        // Act
        ChordDto result = chordService.create(fullDto());

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("C", result.getName());
    }

    // ---------- update ----------

    @Test
    void testUpdate_Found_UpdatesEveryField() {
        // Arrange
        when(chordRepository.findById(1L)).thenReturn(Optional.of(testChord));
        when(chordRepository.save(any(Chord.class))).thenAnswer(inv -> inv.getArgument(0));
        ChordDto dto = fullDto();

        // Act
        Optional<ChordDto> result = chordService.update(1L, dto);

        // Assert
        assertTrue(result.isPresent());
        verify(chordRepository).save(testChord);
        assertEquals(1L, testChord.getId(), "the id must not change on update");
        assertEquals("F", testChord.getName());
        assertEquals(List.of("F", "A", "C"), testChord.getNotes());
        assertEquals("https://example.com/f.png", testChord.getImageUrl());
        assertFalse(testChord.isOpen());
        assertTrue(testChord.isBarre());
        assertTrue(testChord.isPowerChord());
        assertTrue(testChord.isMinor());
        assertFalse(testChord.isMajor());
        assertTrue(testChord.isSeventh());
        assertTrue(testChord.isCapo());
        assertEquals(3, testChord.getCapoFret());
        assertEquals(1L, result.get().getId());
        assertEquals("F", result.get().getName());
    }

    @Test
    void testUpdate_NotFound_DoesNotSave() {
        // Arrange
        when(chordRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ChordDto> result = chordService.update(999L, fullDto());

        // Assert
        assertTrue(result.isEmpty());
        verify(chordRepository, never()).save(any());
    }

    @Test
    void testUpdate_NullId_ReturnsEmpty() {
        // Act
        Optional<ChordDto> result = chordService.update(null, fullDto());

        // Assert
        assertTrue(result.isEmpty());
        verify(chordRepository, never()).findById(any());
        verify(chordRepository, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    void testDelete_Exists() {
        // Arrange
        when(chordRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = chordService.delete(1L);

        // Assert
        assertTrue(result);
        verify(chordRepository).deleteById(1L);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        when(chordRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = chordService.delete(999L);

        // Assert
        assertFalse(result);
        verify(chordRepository, never()).deleteById(anyLong());
    }

    // ---------- findByIds ----------

    @Test
    void testFindByIds_ReturnsMappedChords() {
        // Arrange
        when(chordRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(testChord, chord(2L, "G")));

        // Act
        List<ChordDto> result = chordService.findByIds(List.of(1L, 2L));

        // Assert
        assertEquals(List.of(1L, 2L), result.stream().map(ChordDto::getId).toList());
    }

    @Test
    void testFindByIds_NoMatches() {
        // Arrange
        when(chordRepository.findAllById(List.of(42L))).thenReturn(List.of());

        // Act
        List<ChordDto> result = chordService.findByIds(List.of(42L));

        // Assert
        assertTrue(result.isEmpty());
    }

    // ---------- findIdsForSongLesson ----------

    @Test
    void testFindIdsForSongLesson_ReturnsChordIdsInOrder() {
        // Arrange
        SongLesson lesson = new SongLesson();
        lesson.setId(10L);
        LinkedHashSet<Chord> chords = new LinkedHashSet<>();
        chords.add(chord(3L, "Am"));
        chords.add(chord(1L, "C"));
        chords.add(chord(2L, "G"));
        lesson.setChords(chords);
        when(songLessonRepository.findById(10L)).thenReturn(Optional.of(lesson));

        // Act
        List<Long> result = chordService.findIdsForSongLesson(10L);

        // Assert
        assertEquals(List.of(3L, 1L, 2L), result);
    }

    @Test
    void testFindIdsForSongLesson_LessonWithoutChords() {
        // Arrange
        SongLesson lesson = new SongLesson();
        lesson.setId(10L);
        when(songLessonRepository.findById(10L)).thenReturn(Optional.of(lesson));

        // Act
        List<Long> result = chordService.findIdsForSongLesson(10L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindIdsForSongLesson_LessonNotFound() {
        // Arrange
        when(songLessonRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        List<Long> result = chordService.findIdsForSongLesson(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindIdsForSongLesson_NullId() {
        // Act
        List<Long> result = chordService.findIdsForSongLesson(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(songLessonRepository, never()).findById(any());
    }
}
