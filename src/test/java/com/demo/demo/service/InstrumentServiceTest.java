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

import com.demo.demo.dto.InstrumentDTO;
import com.demo.demo.model.Instrument;
import com.demo.demo.repository.InstrumentRepository;
import com.demo.demo.repository.SongRepository;

/**
 * Unit tests for InstrumentService class.
 * Tests CRUD operations for instruments.
 */
@ExtendWith(MockitoExtension.class)
class InstrumentServiceTest {

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private InstrumentServiceImpl instrumentService;

    private Instrument testInstrument;
    private InstrumentDTO testInstrumentDTO;

    @BeforeEach
    void setUp() {
        testInstrument = new Instrument();
        testInstrument.setId(1L);
        testInstrument.setName("Guitar");
        testInstrument.setImageUrl("https://example.com/guitar.png");

        testInstrumentDTO = new InstrumentDTO();
        testInstrumentDTO.setName("Guitar");
        testInstrumentDTO.setImageUrl("https://example.com/guitar.png");
    }

    @Test
    void testGetAllInstruments_Success() {
        // Arrange
        List<Instrument> instruments = new ArrayList<>();
        instruments.add(testInstrument);
        when(instrumentRepository.findAll()).thenReturn(instruments);

        // Act
        List<InstrumentDTO> result = instrumentService.getAllInstruments();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(instrumentRepository, times(1)).findAll();
    }

    @Test
    void testGetInstrumentById_Success() {
        // Arrange
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(testInstrument));

        // Act
        Optional<InstrumentDTO> result = instrumentService.getInstrumentById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Guitar", result.get().getName());
        verify(instrumentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetInstrumentById_NotFound() {
        // Arrange
        when(instrumentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<InstrumentDTO> result = instrumentService.getInstrumentById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(instrumentRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateInstrument_Success() {
        // Arrange
        when(instrumentRepository.save(any(Instrument.class))).thenReturn(testInstrument);

        // Act
        InstrumentDTO result = instrumentService.createInstrument(testInstrumentDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Guitar", result.getName());
        verify(instrumentRepository, times(1)).save(any(Instrument.class));
    }

    @Test
    void testUpdateInstrument_Success() {
        // Arrange
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(testInstrument));
        when(instrumentRepository.save(any(Instrument.class))).thenReturn(testInstrument);

        InstrumentDTO updateDto = new InstrumentDTO();
        updateDto.setName("Electric Guitar");

        // Act
        Optional<InstrumentDTO> result = instrumentService.updateInstrument(1L, updateDto);

        // Assert
        assertTrue(result.isPresent());
        verify(instrumentRepository, times(1)).findById(1L);
        verify(instrumentRepository, times(1)).save(any(Instrument.class));
    }

    @Test
    void testDeleteInstrument_Success() {
        // Arrange
        when(instrumentRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = instrumentService.deleteInstrument(1L);

        // Assert
        assertTrue(result);
        verify(instrumentRepository, times(1)).existsById(1L);
        verify(instrumentRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteInstrument_NotFound() {
        // Arrange
        when(instrumentRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = instrumentService.deleteInstrument(999L);

        // Assert
        assertFalse(result);
        verify(instrumentRepository, times(1)).existsById(999L);
        verify(instrumentRepository, never()).deleteById(any());
    }
}

