package com.uo289390.toco.service;

import static com.uo289390.toco.service.EntityResolverTestSupport.initResolver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uo289390.toco.dto.TuneDTO;
import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.Tune;
import com.uo289390.toco.repository.InstrumentRepository;
import com.uo289390.toco.repository.TuneRepository;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.mapper.EntityMapper;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for {@link TuneServiceImpl}.
 * Covers CRUD operations, bulk lookups and the tunings of an instrument.
 */
@ExtendWith(MockitoExtension.class)
class TuneServiceTest {

    @Mock
    private TuneRepository tuneRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Spy
    private DtoMapper dtoMapper = new DtoMapper();

    @Spy
    private EntityMapper entityMapper = new EntityMapper();

    @InjectMocks
    private TuneServiceImpl tuneService;

    private Instrument guitar;
    private Instrument bass;
    private Tune standard;

    @BeforeEach
    void setUp() {
        initResolver(tuneRepository, instrumentRepository);

        guitar = instrument(1L, "Guitarra");
        bass = instrument(3L, "Bajo");
        standard = tune(1L, "Estándar", guitar, "E2", "A2", "D3", "G3", "B3", "E4");
    }

    private static Instrument instrument(Long id, String name) {
        Instrument instrument = new Instrument();
        instrument.setId(id);
        instrument.setName(name);
        return instrument;
    }

    private static Tune tune(Long id, String title, Instrument instrument, String... notes) {
        return new Tune(id, title, instrument, new ArrayList<>(List.of(notes)));
    }

    private static TuneDTO dto(Long instrumentId) {
        TuneDTO dto = new TuneDTO();
        dto.setId(7L);
        dto.setTitle("Drop D");
        dto.setInstrumentId(instrumentId);
        dto.setNotes(List.of("D2", "A2", "D3", "G3", "B3", "E4"));
        return dto;
    }

    // ---------- findAll / findByIds / findById ----------

    @Test
    void testFindAll_ReturnsMappedTunes() {
        // Arrange
        when(tuneRepository.findAll()).thenReturn(List.of(standard, tune(10L, "Estándar", bass, "E1", "A1", "D2", "G2")));

        // Act
        List<TuneDTO> result = tuneService.findAll();

        // Assert
        assertEquals(2, result.size());
        TuneDTO first = result.get(0);
        assertEquals(1L, first.getId());
        assertEquals("Estándar", first.getTitle());
        assertEquals(1L, first.getInstrumentId());
        assertEquals(List.of("E2", "A2", "D3", "G3", "B3", "E4"), first.getNotes());
        assertEquals(3L, result.get(1).getInstrumentId());
    }

    @Test
    void testFindAll_Empty() {
        // Arrange
        when(tuneRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        assertTrue(tuneService.findAll().isEmpty());
    }

    @Test
    void testFindByIds_ReturnsMappedTunes() {
        // Arrange
        when(tuneRepository.findAllById(List.of(1L, 99L))).thenReturn(List.of(standard));

        // Act
        List<TuneDTO> result = tuneService.findByIds(List.of(1L, 99L));

        // Assert
        assertEquals(List.of(1L), result.stream().map(TuneDTO::getId).toList());
    }

    @Test
    void testFindById_Found() {
        // Arrange
        when(tuneRepository.findById(1L)).thenReturn(Optional.of(standard));

        // Act
        Optional<TuneDTO> result = tuneService.findById(1L);

        // Assert
        assertEquals("Estándar", result.orElseThrow().getTitle());
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(tuneRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(tuneService.findById(99L).isEmpty());
    }

    @Test
    void testFindById_NullId_ReturnsEmptyWithoutQuerying() {
        // Act & Assert
        assertTrue(tuneService.findById(null).isEmpty());
        verify(tuneRepository, never()).findById(any());
    }

    // ---------- findForInstrument ----------

    @Test
    void testFindForInstrument_ReturnsTheTuningsInRepositoryOrder() {
        // Arrange
        Tune dropD = tune(2L, "Drop D", guitar, "D2", "A2", "D3", "G3", "B3", "E4");
        when(tuneRepository.findByInstrumentIdOrderByIdAsc(1L)).thenReturn(List.of(standard, dropD));

        // Act
        List<TuneDTO> result = tuneService.findForInstrument(1L);

        // Assert
        assertEquals(List.of("Estándar", "Drop D"), result.stream().map(TuneDTO::getTitle).toList());
    }

    @Test
    void testFindForInstrument_NoTunings() {
        // Arrange
        when(tuneRepository.findByInstrumentIdOrderByIdAsc(5L)).thenReturn(List.of());

        // Act & Assert
        assertTrue(tuneService.findForInstrument(5L).isEmpty());
    }

    // ---------- create ----------

    @Test
    void testCreate_ResolvesInstrumentAndSaves() {
        // Arrange
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(guitar));
        when(tuneRepository.save(any(Tune.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        TuneDTO result = tuneService.create(dto(1L));

        // Assert
        ArgumentCaptor<Tune> captor = ArgumentCaptor.forClass(Tune.class);
        verify(tuneRepository).save(captor.capture());
        Tune saved = captor.getValue();
        assertEquals(7L, saved.getId());
        assertEquals("Drop D", saved.getTitle());
        assertSame(guitar, saved.getInstrument());
        assertEquals(List.of("D2", "A2", "D3", "G3", "B3", "E4"), saved.getNotes());
        assertEquals(dto(1L), result);
    }

    @Test
    void testCreate_MissingInstrument_ThrowsNotFoundAndDoesNotSave() {
        // Arrange
        when(instrumentRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> tuneService.create(dto(99L)));
        verify(tuneRepository, never()).save(any());
    }

    @Test
    void testCreate_NullInstrumentId_ThrowsIllegalArgument() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> tuneService.create(dto(null)));
        verify(tuneRepository, never()).save(any());
    }

    // ---------- update ----------

    @Test
    void testUpdate_Found_UpdatesEveryField() {
        // Arrange
        when(tuneRepository.findById(1L)).thenReturn(Optional.of(standard));
        when(instrumentRepository.findById(3L)).thenReturn(Optional.of(bass));
        when(tuneRepository.save(any(Tune.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Optional<TuneDTO> result = tuneService.update(1L, dto(3L));

        // Assert
        assertTrue(result.isPresent());
        verify(tuneRepository).save(standard);
        assertEquals(1L, standard.getId(), "the id must not change on update");
        assertEquals("Drop D", standard.getTitle());
        assertSame(bass, standard.getInstrument());
        assertEquals(List.of("D2", "A2", "D3", "G3", "B3", "E4"), standard.getNotes());
        assertEquals(3L, result.get().getInstrumentId());
    }

    @Test
    void testUpdate_NotFound_DoesNotSave() {
        // Arrange
        when(tuneRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(tuneService.update(99L, dto(1L)).isEmpty());
        verify(tuneRepository, never()).save(any());
    }

    @Test
    void testUpdate_MissingInstrument_ThrowsNotFound() {
        // Arrange
        when(tuneRepository.findById(1L)).thenReturn(Optional.of(standard));
        when(instrumentRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> tuneService.update(1L, dto(99L)));
        verify(tuneRepository, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    void testDelete_Exists() {
        // Arrange
        when(tuneRepository.existsById(1L)).thenReturn(true);

        // Act & Assert
        assertTrue(tuneService.delete(1L));
        verify(tuneRepository).deleteById(1L);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        when(tuneRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertFalse(tuneService.delete(99L));
        verify(tuneRepository, never()).deleteById(anyLong());
    }
}
