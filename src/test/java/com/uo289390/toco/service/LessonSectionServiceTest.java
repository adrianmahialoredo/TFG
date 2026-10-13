package com.uo289390.toco.service;

import static com.uo289390.toco.service.EntityResolverTestSupport.initResolver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.uo289390.toco.service.LessonSectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uo289390.toco.dto.LessonSectionDTO;
import com.uo289390.toco.model.Lesson;
import com.uo289390.toco.model.LessonSection;
import com.uo289390.toco.repository.LessonRepository;
import com.uo289390.toco.repository.LessonSectionRepository;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.mapper.EntityMapper;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for {@link LessonSectionServiceImpl}.
 * Covers CRUD operations and lookups by lesson.
 */
@ExtendWith(MockitoExtension.class)
class LessonSectionServiceTest {

    @Mock
    private LessonSectionRepository lessonSectionRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Spy
    private DtoMapper dtoMapper = new DtoMapper();

    @Spy
    private EntityMapper entityMapper = new EntityMapper();

    @InjectMocks
    private LessonSectionServiceImpl lessonSectionService;

    private Lesson lesson1;
    private Lesson lesson2;
    private LessonSection testSection;

    @BeforeEach
    void setUp() {
        initResolver(lessonSectionRepository, lessonRepository);

        lesson1 = lesson(10L);
        lesson2 = lesson(20L);

        testSection = new LessonSection();
        testSection.setId(1L);
        testSection.setTitle("Introduction");
        testSection.setHtmlContent("<p>Hello</p>");
        testSection.setOrderIndex(0);
        testSection.setLesson(lesson1);
    }

    private static Lesson lesson(Long id) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.setTitle("Lesson " + id);
        return lesson;
    }

    private static LessonSectionDTO dto(Long lessonId) {
        LessonSectionDTO dto = new LessonSectionDTO();
        dto.setId(5L);
        dto.setTitle("Practice");
        dto.setHtmlContent("<p>Practice</p>");
        dto.setOrderIndex(2);
        dto.setLessonId(lessonId);
        return dto;
    }

    // ---------- create ----------

    @Test
    void testCreate_ResolvesLessonAndSaves() {
        // Arrange
        when(lessonRepository.findById(20L)).thenReturn(Optional.of(lesson2));
        when(lessonSectionRepository.save(any(LessonSection.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        LessonSectionDTO result = lessonSectionService.create(dto(20L));

        // Assert
        ArgumentCaptor<LessonSection> captor = ArgumentCaptor.forClass(LessonSection.class);
        verify(lessonSectionRepository).save(captor.capture());
        LessonSection saved = captor.getValue();
        assertEquals(5L, saved.getId());
        assertEquals("Practice", saved.getTitle());
        assertEquals("<p>Practice</p>", saved.getHtmlContent());
        assertEquals(2, saved.getOrderIndex());
        assertSame(lesson2, saved.getLesson());
        assertEquals(dto(20L), result);
    }

    @Test
    void testCreate_UnknownLesson_ThrowsAndDoesNotSave() {
        // Arrange
        when(lessonRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> lessonSectionService.create(dto(999L)));
        assertTrue(ex.getMessage().contains("Lesson"));
        verify(lessonSectionRepository, never()).save(any());
    }

    @Test
    void testCreate_NullLessonId_ThrowsIllegalArgument() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> lessonSectionService.create(dto(null)));
        verify(lessonSectionRepository, never()).save(any());
    }

    // ---------- findAll / findByIds / findById / findForLesson ----------

    @Test
    void testFindAll_ReturnsMapped() {
        // Arrange
        when(lessonSectionRepository.findAll()).thenReturn(List.of(testSection));

        // Act
        List<LessonSectionDTO> result = lessonSectionService.findAll();

        // Assert
        assertEquals(1, result.size());
        LessonSectionDTO dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals("Introduction", dto.getTitle());
        assertEquals("<p>Hello</p>", dto.getHtmlContent());
        assertEquals(0, dto.getOrderIndex());
        assertEquals(10L, dto.getLessonId());
    }

    @Test
    void testFindAll_Empty() {
        // Arrange
        when(lessonSectionRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        assertTrue(lessonSectionService.findAll().isEmpty());
    }

    @Test
    void testFindByIds_NullIds() {
        // Act & Assert
        assertTrue(lessonSectionService.findByIds(null).isEmpty());
        verifyNoInteractions(lessonSectionRepository);
    }

    @Test
    void testFindByIds_EmptyIds() {
        // Act & Assert
        assertTrue(lessonSectionService.findByIds(List.of()).isEmpty());
        verifyNoInteractions(lessonSectionRepository);
    }

    @Test
    void testFindByIds_ReturnsMapped() {
        // Arrange
        when(lessonSectionRepository.findAllById(List.of(1L))).thenReturn(List.of(testSection));

        // Act
        List<LessonSectionDTO> result = lessonSectionService.findByIds(List.of(1L));

        // Assert
        assertEquals(List.of(1L), result.stream().map(LessonSectionDTO::getId).toList());
    }

    @Test
    void testFindById_Found() {
        // Arrange
        when(lessonSectionRepository.findById(1L)).thenReturn(Optional.of(testSection));

        // Act
        Optional<LessonSectionDTO> result = lessonSectionService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Introduction", result.get().getTitle());
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(lessonSectionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(lessonSectionService.findById(999L).isEmpty());
    }

    @Test
    void testFindById_NullId() {
        // Act & Assert
        assertTrue(lessonSectionService.findById(null).isEmpty());
        verify(lessonSectionRepository, never()).findById(any());
    }

    @Test
    void testFindForLesson_ReturnsMapped() {
        // Arrange
        when(lessonSectionRepository.findByLessonId(10L)).thenReturn(List.of(testSection));

        // Act
        List<LessonSectionDTO> result = lessonSectionService.findForLesson(10L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getLessonId());
        verify(lessonSectionRepository).findByLessonId(10L);
    }

    @Test
    void testFindForLesson_Empty() {
        // Arrange
        when(lessonSectionRepository.findByLessonId(99L)).thenReturn(List.of());

        // Act & Assert
        assertTrue(lessonSectionService.findForLesson(99L).isEmpty());
    }

    // ---------- update ----------

    @Test
    void testUpdate_SameLesson_DoesNotResolveIt() {
        // Arrange
        when(lessonSectionRepository.findById(1L)).thenReturn(Optional.of(testSection));
        when(lessonSectionRepository.save(any(LessonSection.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Optional<LessonSectionDTO> result = lessonSectionService.update(1L, dto(10L));

        // Assert
        assertTrue(result.isPresent());
        verify(lessonSectionRepository).save(testSection);
        assertEquals(1L, testSection.getId(), "the id must not change on update");
        assertEquals("Practice", testSection.getTitle());
        assertEquals("<p>Practice</p>", testSection.getHtmlContent());
        assertEquals(2, testSection.getOrderIndex());
        assertSame(lesson1, testSection.getLesson());
        verifyNoInteractions(lessonRepository);
    }

    @Test
    void testUpdate_DifferentLesson_ResolvesNewOne() {
        // Arrange
        when(lessonSectionRepository.findById(1L)).thenReturn(Optional.of(testSection));
        when(lessonRepository.findById(20L)).thenReturn(Optional.of(lesson2));
        when(lessonSectionRepository.save(any(LessonSection.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Optional<LessonSectionDTO> result = lessonSectionService.update(1L, dto(20L));

        // Assert
        assertTrue(result.isPresent());
        assertSame(lesson2, testSection.getLesson());
        assertEquals(20L, result.get().getLessonId());
    }

    @Test
    void testUpdate_UnknownLesson_ThrowsAndDoesNotSave() {
        // Arrange
        when(lessonSectionRepository.findById(1L)).thenReturn(Optional.of(testSection));
        when(lessonRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> lessonSectionService.update(1L, dto(999L)));
        verify(lessonSectionRepository, never()).save(any());
    }

    @Test
    void testUpdate_NullLessonId_ThrowsIllegalArgument() {
        // Arrange
        when(lessonSectionRepository.findById(1L)).thenReturn(Optional.of(testSection));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> lessonSectionService.update(1L, dto(null)));
        verify(lessonSectionRepository, never()).save(any());
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        when(lessonSectionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(lessonSectionService.update(999L, dto(10L)).isEmpty());
        verify(lessonSectionRepository, never()).save(any());
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertTrue(lessonSectionService.update(null, dto(10L)).isEmpty());
        verify(lessonSectionRepository, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    void testDelete_Exists() {
        // Arrange
        when(lessonSectionRepository.existsById(1L)).thenReturn(true);

        // Act & Assert
        assertTrue(lessonSectionService.delete(1L));
        verify(lessonSectionRepository).deleteById(1L);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        when(lessonSectionRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertFalse(lessonSectionService.delete(999L));
        verify(lessonSectionRepository, never()).deleteById(anyLong());
    }
}
