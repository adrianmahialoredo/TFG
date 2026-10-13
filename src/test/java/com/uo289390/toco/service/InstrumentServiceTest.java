package com.uo289390.toco.service;

import static com.uo289390.toco.service.EntityResolverTestSupport.initResolver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.uo289390.toco.service.InstrumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uo289390.toco.dto.InstrumentDTO;
import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.repository.InstrumentRepository;
import com.uo289390.toco.repository.SongRepository;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.mapper.EntityMapper;

/**
 * Unit tests for {@link InstrumentServiceImpl}.
 * Covers CRUD operations and bulk lookups for instruments.
 */
@ExtendWith(MockitoExtension.class)
class InstrumentServiceTest {

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private SongRepository songRepository;

    @Spy
    private DtoMapper dtoMapper = new DtoMapper();

    @Spy
    private EntityMapper entityMapper = new EntityMapper();

    @InjectMocks
    private InstrumentServiceImpl instrumentService;

    private Instrument testInstrument;

    @BeforeEach
    void setUp() {
        initResolver(instrumentRepository, songRepository);
        testInstrument = instrument(1L, "Guitar");
        testInstrument.setImageUrl("https://example.com/guitar.png");
    }

    private static Instrument instrument(Long id, String name) {
        Instrument instrument = new Instrument();
        instrument.setId(id);
        instrument.setName(name);
        return instrument;
    }

    private static InstrumentDTO dto() {
        InstrumentDTO dto = new InstrumentDTO();
        dto.setId(9L);
        dto.setName("Piano");
        dto.setImageUrl("https://example.com/piano.png");
        return dto;
    }

    // ---------- findAll ----------

    @Test
    void testFindAll_ReturnsMappedInstruments() {
        // Arrange
        when(instrumentRepository.findAll()).thenReturn(List.of(testInstrument, instrument(2L, "Bass")));

        // Act
        List<InstrumentDTO> result = instrumentService.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Guitar", result.get(0).getName());
        assertEquals("https://example.com/guitar.png", result.get(0).getImageUrl());
        assertEquals("Bass", result.get(1).getName());
    }

    @Test
    void testFindAll_Empty() {
        // Arrange
        when(instrumentRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        assertTrue(instrumentService.findAll().isEmpty());
    }

    // ---------- findByIds ----------

    @Test
    void testFindByIds_NullIds() {
        // Act & Assert
        assertTrue(instrumentService.findByIds(null).isEmpty());
        verifyNoInteractions(instrumentRepository);
    }

    @Test
    void testFindByIds_EmptyIds() {
        // Act & Assert
        assertTrue(instrumentService.findByIds(List.of()).isEmpty());
        verifyNoInteractions(instrumentRepository);
    }

    @Test
    void testFindByIds_ReturnsMappedInstruments() {
        // Arrange
        when(instrumentRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(testInstrument, instrument(2L, "Bass")));

        // Act
        List<InstrumentDTO> result = instrumentService.findByIds(List.of(1L, 2L));

        // Assert
        assertEquals(List.of("Guitar", "Bass"), result.stream().map(InstrumentDTO::getName).toList());
    }

    // ---------- findById ----------

    @Test
    void testFindById_Found() {
        // Arrange
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(testInstrument));

        // Act
        Optional<InstrumentDTO> result = instrumentService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Guitar", result.get().getName());
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(instrumentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(instrumentService.findById(999L).isEmpty());
    }

    @Test
    void testFindById_NullId() {
        // Act & Assert
        assertTrue(instrumentService.findById(null).isEmpty());
        verify(instrumentRepository, never()).findById(any());
    }

    // ---------- create ----------

    @Test
    void testCreate_SavesMappedInstrument() {
        // Arrange
        when(instrumentRepository.save(any(Instrument.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        InstrumentDTO result = instrumentService.create(dto());

        // Assert
        ArgumentCaptor<Instrument> captor = ArgumentCaptor.forClass(Instrument.class);
        verify(instrumentRepository).save(captor.capture());
        assertEquals(9L, captor.getValue().getId());
        assertEquals("Piano", captor.getValue().getName());
        assertEquals("https://example.com/piano.png", captor.getValue().getImageUrl());
        assertEquals(dto(), result);
    }

    @Test
    void testCreate_ReturnsDtoOfPersistedEntity() {
        // Arrange
        when(instrumentRepository.save(any(Instrument.class))).thenReturn(testInstrument);

        // Act
        InstrumentDTO result = instrumentService.create(dto());

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("Guitar", result.getName());
    }

    // ---------- update ----------

    @Test
    void testUpdate_Found_UpdatesNameAndImage() {
        // Arrange
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(testInstrument));
        when(instrumentRepository.save(any(Instrument.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Optional<InstrumentDTO> result = instrumentService.update(1L, dto());

        // Assert
        assertTrue(result.isPresent());
        verify(instrumentRepository).save(testInstrument);
        assertEquals(1L, testInstrument.getId(), "the id must not change on update");
        assertEquals("Piano", testInstrument.getName());
        assertEquals("https://example.com/piano.png", testInstrument.getImageUrl());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        when(instrumentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(instrumentService.update(999L, dto()).isEmpty());
        verify(instrumentRepository, never()).save(any());
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertTrue(instrumentService.update(null, dto()).isEmpty());
        verify(instrumentRepository, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    void testDelete_Exists() {
        // Arrange
        when(instrumentRepository.existsById(1L)).thenReturn(true);

        // Act & Assert
        assertTrue(instrumentService.delete(1L));
        verify(instrumentRepository).deleteById(1L);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        when(instrumentRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertFalse(instrumentService.delete(999L));
        verify(instrumentRepository, never()).deleteById(anyLong());
    }
}
