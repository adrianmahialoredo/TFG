package com.demo.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.demo.demo.dto.ChordDto;
import com.demo.demo.model.Chord;
import com.demo.demo.repository.ChordRepository;

/**
 * Unit tests for ChordService class.
 * Tests CRUD operations for chords.
 */
@ExtendWith(MockitoExtension.class)
class ChordServiceTest {

    @Mock
    private ChordRepository chordRepository;

    @InjectMocks
    private ChordServiceImpl chordService;

    private Chord testChord;
    private ChordDto testChordDto;

    @BeforeEach
    void setUp() {
        testChord = new Chord();
        testChord.setId(1L);
        testChord.setName("C Major");
        testChord.setImageUrl("https://example.com/cmajor.png");
        testChord.setOpen(true);
        testChord.setMajor(true);
        testChord.setNotes(List.of("C", "E", "G"));

        testChordDto = new ChordDto();
        testChordDto.setName("C Major");
        testChordDto.setImageUrl("https://example.com/cmajor.png");
    }

    @Test
    void testGetAllChords_Success() {
        // Arrange
        List<Chord> chords = new ArrayList<>();
        chords.add(testChord);
        when(chordRepository.findAll()).thenReturn(chords);

        // Act
        List<ChordDto> result = chordService.getAllChords();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(chordRepository, times(1)).findAll();
    }

    @Test
    void testGetChordById_Success() {
        // Arrange
        when(chordRepository.findById(1L)).thenReturn(Optional.of(testChord));

        // Act
        Optional<ChordDto> result = chordService.getChordById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("C Major", result.get().getName());
        verify(chordRepository, times(1)).findById(1L);
    }

    @Test
    void testGetChordById_NotFound() {
        // Arrange
        when(chordRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ChordDto> result = chordService.getChordById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(chordRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateChord_Success() {
        // Arrange
        when(chordRepository.save(any(Chord.class))).thenReturn(testChord);

        // Act
        ChordDto result = chordService.createChord(testChordDto);

        // Assert
        assertNotNull(result);
        assertEquals("C Major", result.getName());
        verify(chordRepository, times(1)).save(any(Chord.class));
    }

    @Test
    void testUpdateChord_Success() {
        // Arrange
        when(chordRepository.findById(1L)).thenReturn(Optional.of(testChord));
        when(chordRepository.save(any(Chord.class))).thenReturn(testChord);

        ChordDto updateDto = new ChordDto();
        updateDto.setName("C Minor");

        // Act
        Optional<ChordDto> result = chordService.updateChord(1L, updateDto);

        // Assert
        assertTrue(result.isPresent());
        verify(chordRepository, times(1)).findById(1L);
        verify(chordRepository, times(1)).save(any(Chord.class));
    }

    @Test
    void testUpdateChord_NotFound() {
        // Arrange
        when(chordRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ChordDto> result = chordService.updateChord(999L, testChordDto);

        // Assert
        assertFalse(result.isPresent());
        verify(chordRepository, times(1)).findById(999L);
    }

    @Test
    void testDeleteChord_Success() {
        // Arrange
        when(chordRepository.existsById(1L)).thenReturn(true);

        // Act
        chordService.deleteChord(1L);

        // Assert
        verify(chordRepository, times(1)).existsById(1L);
        verify(chordRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteChord_NotFound() {
        // Arrange
        when(chordRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> chordService.deleteChord(999L));
        verify(chordRepository, times(1)).existsById(999L);
    }
}

