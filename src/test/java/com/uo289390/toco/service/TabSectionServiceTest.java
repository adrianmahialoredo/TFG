package com.uo289390.toco.service;

import static com.uo289390.toco.service.EntityResolverTestSupport.initResolver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.uo289390.toco.service.TabSectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uo289390.toco.dto.TabSectionDTO;
import com.uo289390.toco.model.SongLesson;
import com.uo289390.toco.model.TabSection;
import com.uo289390.toco.repository.SongLessonRepository;
import com.uo289390.toco.repository.TabSectionRepository;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.mapper.EntityMapper;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for {@link TabSectionServiceImpl}.
 * Covers CRUD operations and lookups by song lesson.
 */
@ExtendWith(MockitoExtension.class)
class TabSectionServiceTest {

    @Mock
    private TabSectionRepository tabSectionRepository;

    @Mock
    private SongLessonRepository songLessonRepository;

    @Spy
    private DtoMapper dtoMapper = new DtoMapper();

    @Spy
    private EntityMapper entityMapper = new EntityMapper();

    @InjectMocks
    private TabSectionServiceImpl tabSectionService;

    private SongLesson lesson1;
    private SongLesson lesson2;
    private TabSection testSection;

    @BeforeEach
    void setUp() {
        initResolver(tabSectionRepository, songLessonRepository);

        lesson1 = songLesson(10L);
        lesson2 = songLesson(20L);

        testSection = new TabSection();
        testSection.setId(1L);
        testSection.setIdentifier("Intro");
        testSection.setContent("e|---0---|");
        testSection.setSongLesson(lesson1);
    }

    private static SongLesson songLesson(Long id) {
        SongLesson lesson = new SongLesson();
        lesson.setId(id);
        return lesson;
    }

    private static TabSectionDTO dto(Long songLessonId) {
        TabSectionDTO dto = new TabSectionDTO();
        dto.setId(5L);
        dto.setIdentifier("Solo");
        dto.setContent("e|---12---|");
        dto.setSongLessonId(songLessonId);
        return dto;
    }

    // ---------- create ----------

    @Test
    void testCreate_ResolvesSongLessonAndSaves() {
        // Arrange
        when(songLessonRepository.findById(20L)).thenReturn(Optional.of(lesson2));
        when(tabSectionRepository.save(any(TabSection.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        TabSectionDTO result = tabSectionService.create(dto(20L));

        // Assert
        ArgumentCaptor<TabSection> captor = ArgumentCaptor.forClass(TabSection.class);
        verify(tabSectionRepository).save(captor.capture());
        TabSection saved = captor.getValue();
        assertEquals(5L, saved.getId());
        assertEquals("Solo", saved.getIdentifier());
        assertEquals("e|---12---|", saved.getContent());
        assertSame(lesson2, saved.getSongLesson());
        assertEquals(dto(20L), result);
    }

    @Test
    void testCreate_UnknownSongLesson_ThrowsAndDoesNotSave() {
        // Arrange
        when(songLessonRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> tabSectionService.create(dto(999L)));
        assertTrue(ex.getMessage().contains("SongLesson"));
        verify(tabSectionRepository, never()).save(any());
    }

    @Test
    void testCreate_NullSongLessonId_ThrowsIllegalArgument() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> tabSectionService.create(dto(null)));
        verify(tabSectionRepository, never()).save(any());
    }

    // ---------- findAll / findByIds / findById / findForSongLesson ----------

    @Test
    void testFindAll_ReturnsMapped() {
        // Arrange
        when(tabSectionRepository.findAll()).thenReturn(List.of(testSection));

        // Act
        List<TabSectionDTO> result = tabSectionService.findAll();

        // Assert
        assertEquals(1, result.size());
        TabSectionDTO dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals("Intro", dto.getIdentifier());
        assertEquals("e|---0---|", dto.getContent());
        assertEquals(10L, dto.getSongLessonId());
    }

    @Test
    void testFindAll_Empty() {
        // Arrange
        when(tabSectionRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        assertTrue(tabSectionService.findAll().isEmpty());
    }

    @Test
    void testFindByIds_NullIds() {
        // Act & Assert
        assertTrue(tabSectionService.findByIds(null).isEmpty());
        verifyNoInteractions(tabSectionRepository);
    }

    @Test
    void testFindByIds_EmptyIds() {
        // Act & Assert
        assertTrue(tabSectionService.findByIds(List.of()).isEmpty());
        verifyNoInteractions(tabSectionRepository);
    }

    @Test
    void testFindByIds_ReturnsMapped() {
        // Arrange
        when(tabSectionRepository.findAllById(List.of(1L))).thenReturn(List.of(testSection));

        // Act
        List<TabSectionDTO> result = tabSectionService.findByIds(List.of(1L));

        // Assert
        assertEquals(List.of(1L), result.stream().map(TabSectionDTO::getId).toList());
    }

    @Test
    void testFindById_Found() {
        // Arrange
        when(tabSectionRepository.findById(1L)).thenReturn(Optional.of(testSection));

        // Act
        Optional<TabSectionDTO> result = tabSectionService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Intro", result.get().getIdentifier());
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(tabSectionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(tabSectionService.findById(999L).isEmpty());
    }

    @Test
    void testFindById_NullId() {
        // Act & Assert
        assertTrue(tabSectionService.findById(null).isEmpty());
        verify(tabSectionRepository, never()).findById(any());
    }

    @Test
    void testFindForSongLesson_ReturnsMapped() {
        // Arrange
        when(tabSectionRepository.findBySongLessonId(10L)).thenReturn(List.of(testSection));

        // Act
        List<TabSectionDTO> result = tabSectionService.findForSongLesson(10L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getSongLessonId());
        verify(tabSectionRepository).findBySongLessonId(10L);
    }

    @Test
    void testFindForSongLesson_Empty() {
        // Arrange
        when(tabSectionRepository.findBySongLessonId(99L)).thenReturn(List.of());

        // Act & Assert
        assertTrue(tabSectionService.findForSongLesson(99L).isEmpty());
    }

    // ---------- update ----------

    @Test
    void testUpdate_SameSongLesson_DoesNotResolveIt() {
        // Arrange
        when(tabSectionRepository.findById(1L)).thenReturn(Optional.of(testSection));
        when(tabSectionRepository.save(any(TabSection.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Optional<TabSectionDTO> result = tabSectionService.update(1L, dto(10L));

        // Assert
        assertTrue(result.isPresent());
        verify(tabSectionRepository).save(testSection);
        assertEquals(1L, testSection.getId(), "the id must not change on update");
        assertEquals("Solo", testSection.getIdentifier());
        assertEquals("e|---12---|", testSection.getContent());
        assertSame(lesson1, testSection.getSongLesson());
        verifyNoInteractions(songLessonRepository);
    }

    @Test
    void testUpdate_DifferentSongLesson_ResolvesNewOne() {
        // Arrange
        when(tabSectionRepository.findById(1L)).thenReturn(Optional.of(testSection));
        when(songLessonRepository.findById(20L)).thenReturn(Optional.of(lesson2));
        when(tabSectionRepository.save(any(TabSection.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Optional<TabSectionDTO> result = tabSectionService.update(1L, dto(20L));

        // Assert
        assertTrue(result.isPresent());
        assertSame(lesson2, testSection.getSongLesson());
        assertEquals(20L, result.get().getSongLessonId());
    }

    @Test
    void testUpdate_UnknownSongLesson_ThrowsAndDoesNotSave() {
        // Arrange
        when(tabSectionRepository.findById(1L)).thenReturn(Optional.of(testSection));
        when(songLessonRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> tabSectionService.update(1L, dto(999L)));
        verify(tabSectionRepository, never()).save(any());
    }

    @Test
    void testUpdate_NullSongLessonId_ThrowsIllegalArgument() {
        // Arrange
        when(tabSectionRepository.findById(1L)).thenReturn(Optional.of(testSection));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> tabSectionService.update(1L, dto(null)));
        verify(tabSectionRepository, never()).save(any());
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        when(tabSectionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(tabSectionService.update(999L, dto(10L)).isEmpty());
        verify(tabSectionRepository, never()).save(any());
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertTrue(tabSectionService.update(null, dto(10L)).isEmpty());
        verify(tabSectionRepository, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    void testDelete_Exists() {
        // Arrange
        when(tabSectionRepository.existsById(1L)).thenReturn(true);

        // Act & Assert
        assertTrue(tabSectionService.delete(1L));
        verify(tabSectionRepository).deleteById(1L);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        when(tabSectionRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertFalse(tabSectionService.delete(999L));
        verify(tabSectionRepository, never()).deleteById(anyLong());
    }
}
