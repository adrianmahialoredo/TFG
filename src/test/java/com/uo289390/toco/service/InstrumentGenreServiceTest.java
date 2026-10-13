package com.uo289390.toco.service;

import static com.uo289390.toco.service.EntityResolverTestSupport.initResolver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.uo289390.toco.service.InstrumentGenreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uo289390.toco.dto.InstrumentGenreDTO;
import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.InstrumentGenre;
import com.uo289390.toco.model.Item;
import com.uo289390.toco.model.Lesson;
import com.uo289390.toco.repository.GenreRepository;
import com.uo289390.toco.repository.InstrumentGenreRepository;
import com.uo289390.toco.repository.InstrumentRepository;
import com.uo289390.toco.repository.LessonRepository;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.mapper.EntityMapper;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for {@link InstrumentGenreServiceImpl}.
 * Covers CRUD operations, lesson ordering/detaching and lookups by genre and instrument.
 */
@ExtendWith(MockitoExtension.class)
class InstrumentGenreServiceTest {

    @Mock
    private InstrumentGenreRepository instrumentGenreRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Spy
    private DtoMapper dtoMapper = new DtoMapper();

    @Spy
    private EntityMapper entityMapper = new EntityMapper();

    @InjectMocks
    private InstrumentGenreServiceImpl instrumentGenreService;

    private Instrument guitar;
    private Instrument piano;
    private Genre rock;
    private Genre jazz;
    private Lesson lesson1;
    private Lesson lesson2;
    private Lesson lesson3;
    private InstrumentGenre testInstrumentGenre;

    @BeforeEach
    void setUp() {
        initResolver(instrumentGenreRepository, instrumentRepository, genreRepository, lessonRepository);

        guitar = instrument(1L, "Guitar");
        piano = instrument(2L, "Piano");
        rock = genre(10L, "Rock");
        jazz = genre(20L, "Jazz");
        lesson1 = lesson(100L);
        lesson2 = lesson(200L);
        lesson3 = lesson(300L);

        testInstrumentGenre = new InstrumentGenre();
        testInstrumentGenre.setId(50L);
        testInstrumentGenre.setTitle("Rock Guitar");
        testInstrumentGenre.setImageUrl("https://example.com/rock-guitar.png");
        testInstrumentGenre.setLevel(2);
        testInstrumentGenre.setType(Item.Type.InstrumentGenre);
        testInstrumentGenre.setInstrument(guitar);
        testInstrumentGenre.setGenre(rock);
        testInstrumentGenre.setLessons(List.of(lesson1, lesson2));
    }

    private static Instrument instrument(Long id, String name) {
        Instrument instrument = new Instrument();
        instrument.setId(id);
        instrument.setName(name);
        return instrument;
    }

    private static Genre genre(Long id, String title) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setTitle(title);
        return genre;
    }

    private static Lesson lesson(Long id) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.setTitle("Lesson " + id);
        return lesson;
    }

    private static InstrumentGenreDTO dto(Long instrumentId, Long genreId, List<Long> lessonIds) {
        InstrumentGenreDTO dto = new InstrumentGenreDTO();
        dto.setId(60L);
        dto.setTitle("Jazz Piano");
        dto.setImageUrl("https://example.com/jazz-piano.png");
        dto.setLevel(3);
        dto.setInstrumentId(instrumentId);
        dto.setGenreId(genreId);
        dto.setLessonIds(lessonIds);
        return dto;
    }

    // ---------- findAll ----------

    @Test
    void testFindAll_ReturnsMappedInstrumentGenres() {
        // Arrange
        when(instrumentGenreRepository.findAll()).thenReturn(List.of(testInstrumentGenre));

        // Act
        List<InstrumentGenreDTO> result = instrumentGenreService.findAll();

        // Assert
        assertEquals(1, result.size());
        InstrumentGenreDTO dto = result.get(0);
        assertEquals(50L, dto.getId());
        assertEquals("Rock Guitar", dto.getTitle());
        assertEquals("https://example.com/rock-guitar.png", dto.getImageUrl());
        assertEquals(2, dto.getLevel());
        assertEquals(1L, dto.getInstrumentId());
        assertEquals(10L, dto.getGenreId());
        assertEquals(List.of(100L, 200L), dto.getLessonIds());
    }

    @Test
    void testFindAll_Empty() {
        // Arrange
        when(instrumentGenreRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        assertTrue(instrumentGenreService.findAll().isEmpty());
    }

    // ---------- findByIds ----------

    @Test
    void testFindByIds_NullIds() {
        // Act & Assert
        assertTrue(instrumentGenreService.findByIds(null).isEmpty());
        verifyNoInteractions(instrumentGenreRepository);
    }

    @Test
    void testFindByIds_EmptyIds() {
        // Act & Assert
        assertTrue(instrumentGenreService.findByIds(List.of()).isEmpty());
        verifyNoInteractions(instrumentGenreRepository);
    }

    @Test
    void testFindByIds_ReturnsMapped() {
        // Arrange
        when(instrumentGenreRepository.findAllById(List.of(50L))).thenReturn(List.of(testInstrumentGenre));

        // Act
        List<InstrumentGenreDTO> result = instrumentGenreService.findByIds(List.of(50L));

        // Assert
        assertEquals(1, result.size());
        assertEquals(50L, result.get(0).getId());
    }

    // ---------- findById ----------

    @Test
    void testFindById_Found() {
        // Arrange
        when(instrumentGenreRepository.findById(50L)).thenReturn(Optional.of(testInstrumentGenre));

        // Act
        Optional<InstrumentGenreDTO> result = instrumentGenreService.findById(50L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Rock Guitar", result.get().getTitle());
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(instrumentGenreRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(instrumentGenreService.findById(999L).isEmpty());
    }

    @Test
    void testFindById_NullId() {
        // Act & Assert
        assertTrue(instrumentGenreService.findById(null).isEmpty());
        verify(instrumentGenreRepository, never()).findById(any());
    }

    // ---------- create ----------

    @Test
    void testCreate_ResolvesRelationsAndOrdersLessons() {
        // Arrange
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(piano));
        when(genreRepository.findById(20L)).thenReturn(Optional.of(jazz));
        when(lessonRepository.findById(300L)).thenReturn(Optional.of(lesson3));
        when(lessonRepository.findById(100L)).thenReturn(Optional.of(lesson1));
        when(instrumentGenreRepository.save(any(InstrumentGenre.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        InstrumentGenreDTO result = instrumentGenreService.create(dto(2L, 20L, List.of(300L, 100L)));

        // Assert
        ArgumentCaptor<InstrumentGenre> captor = ArgumentCaptor.forClass(InstrumentGenre.class);
        verify(instrumentGenreRepository).save(captor.capture());
        InstrumentGenre saved = captor.getValue();
        assertEquals(60L, saved.getId());
        assertEquals("Jazz Piano", saved.getTitle());
        assertEquals("https://example.com/jazz-piano.png", saved.getImageUrl());
        assertEquals(3, saved.getLevel());
        assertEquals(Item.Type.InstrumentGenre, saved.getType());
        assertSame(piano, saved.getInstrument());
        assertSame(jazz, saved.getGenre());

        // Lessons are linked to the saved entity and indexed in the requested order
        assertSame(saved, lesson3.getInstrumentGenre());
        assertTrue(lesson3.isBelongsToInstrumentGenre());
        assertEquals(0, lesson3.getIndexInInstrumentGenre());
        assertSame(saved, lesson1.getInstrumentGenre());
        assertEquals(1, lesson1.getIndexInInstrumentGenre());

        assertEquals(60L, result.getId());
        assertEquals(2L, result.getInstrumentId());
        assertEquals(20L, result.getGenreId());
        assertEquals(List.of(300L, 100L), result.getLessonIds());
    }

    @Test
    void testCreate_WithoutLessons() {
        // Arrange
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(piano));
        when(genreRepository.findById(20L)).thenReturn(Optional.of(jazz));
        when(instrumentGenreRepository.save(any(InstrumentGenre.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        InstrumentGenreDTO result = instrumentGenreService.create(dto(2L, 20L, null));

        // Assert
        assertTrue(result.getLessonIds().isEmpty());
        verifyNoInteractions(lessonRepository);
    }

    @Test
    void testCreate_UnknownInstrument_ThrowsAndDoesNotSave() {
        // Arrange
        when(instrumentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> instrumentGenreService.create(dto(999L, 20L, null)));
        assertTrue(ex.getMessage().contains("Instrument"));
        verify(instrumentGenreRepository, never()).save(any());
    }

    @Test
    void testCreate_UnknownGenre_ThrowsAndDoesNotSave() {
        // Arrange
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(piano));
        when(genreRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> instrumentGenreService.create(dto(2L, 999L, null)));
        assertTrue(ex.getMessage().contains("Genre"));
        verify(instrumentGenreRepository, never()).save(any());
    }

    @Test
    void testCreate_NullInstrumentId_ThrowsIllegalArgument() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> instrumentGenreService.create(dto(null, 20L, null)));
        verify(instrumentGenreRepository, never()).save(any());
    }

    @Test
    void testCreate_NullGenreId_ThrowsIllegalArgument() {
        // Arrange
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(piano));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> instrumentGenreService.create(dto(2L, null, null)));
        verify(instrumentGenreRepository, never()).save(any());
    }

    @Test
    void testCreate_UnknownLesson_Throws() {
        // Arrange
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(piano));
        when(genreRepository.findById(20L)).thenReturn(Optional.of(jazz));
        when(instrumentGenreRepository.save(any(InstrumentGenre.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> instrumentGenreService.create(dto(2L, 20L, List.of(999L))));
    }

    // ---------- update ----------

    @Test
    void testUpdate_Found_UpdatesFieldsRelationsAndLessons() {
        // Arrange
        when(instrumentGenreRepository.findById(50L)).thenReturn(Optional.of(testInstrumentGenre));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(piano));
        when(genreRepository.findById(20L)).thenReturn(Optional.of(jazz));
        when(lessonRepository.findById(200L)).thenReturn(Optional.of(lesson2));
        when(lessonRepository.findById(300L)).thenReturn(Optional.of(lesson3));
        when(instrumentGenreRepository.save(any(InstrumentGenre.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Optional<InstrumentGenreDTO> result = instrumentGenreService.update(50L, dto(2L, 20L, List.of(300L, 200L)));

        // Assert
        assertTrue(result.isPresent());
        verify(instrumentGenreRepository).save(testInstrumentGenre);
        assertEquals(50L, testInstrumentGenre.getId(), "the id must not change on update");
        assertEquals("Jazz Piano", testInstrumentGenre.getTitle());
        assertEquals("https://example.com/jazz-piano.png", testInstrumentGenre.getImageUrl());
        assertEquals(3, testInstrumentGenre.getLevel());
        assertEquals(Item.Type.InstrumentGenre, testInstrumentGenre.getType());
        assertSame(piano, testInstrumentGenre.getInstrument());
        assertSame(jazz, testInstrumentGenre.getGenre());
        assertEquals(List.of(300L, 200L), result.get().getLessonIds());

        // Lesson removed from the list is detached
        assertNull(lesson1.getInstrumentGenre());
        assertFalse(lesson1.isBelongsToInstrumentGenre());
        assertEquals(0, lesson1.getIndexInInstrumentGenre());

        // Remaining/new lessons are re-indexed
        assertEquals(0, lesson3.getIndexInInstrumentGenre());
        assertEquals(1, lesson2.getIndexInInstrumentGenre());
        assertSame(testInstrumentGenre, lesson3.getInstrumentGenre());
    }

    @Test
    void testUpdate_OmittedLessonIds_KeepsLessons() {
        // Arrange (regression: omitting lessonIds used to detach every lesson)
        List<Long> lessonsBefore = testInstrumentGenre.getLessons().stream().map(Lesson::getId).toList();
        when(instrumentGenreRepository.findById(50L)).thenReturn(Optional.of(testInstrumentGenre));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(piano));
        when(genreRepository.findById(20L)).thenReturn(Optional.of(jazz));
        when(instrumentGenreRepository.save(any(InstrumentGenre.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Optional<InstrumentGenreDTO> result = instrumentGenreService.update(50L, dto(2L, 20L, null));

        // Assert
        assertFalse(lessonsBefore.isEmpty());
        assertEquals(lessonsBefore, result.orElseThrow().getLessonIds());
        assertSame(testInstrumentGenre, lesson1.getInstrumentGenre());
        verifyNoInteractions(lessonRepository);
    }

    @Test
    void testUpdate_EmptyLessonIds_DetachesEveryLesson() {
        // Arrange
        when(instrumentGenreRepository.findById(50L)).thenReturn(Optional.of(testInstrumentGenre));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(piano));
        when(genreRepository.findById(20L)).thenReturn(Optional.of(jazz));
        when(instrumentGenreRepository.save(any(InstrumentGenre.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        instrumentGenreService.update(50L, dto(2L, 20L, List.of()));

        // Assert
        assertTrue(testInstrumentGenre.getLessons().isEmpty());
        assertNull(lesson1.getInstrumentGenre());
    }

    @Test
    void testUpdate_LessonMovedFromAnotherInstrumentGenre_IsRemovedFromPreviousOne() {
        // Arrange
        InstrumentGenre other = new InstrumentGenre();
        other.setId(70L);
        other.setLessons(List.of(lesson3));
        when(instrumentGenreRepository.findById(50L)).thenReturn(Optional.of(testInstrumentGenre));
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(guitar));
        when(genreRepository.findById(10L)).thenReturn(Optional.of(rock));
        when(lessonRepository.findById(300L)).thenReturn(Optional.of(lesson3));
        when(instrumentGenreRepository.save(any(InstrumentGenre.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        instrumentGenreService.update(50L, dto(1L, 10L, List.of(300L)));

        // Assert
        assertTrue(other.getLessons().isEmpty());
        assertSame(testInstrumentGenre, lesson3.getInstrumentGenre());
        assertEquals(1, testInstrumentGenre.getLessons().size());
    }

    @Test
    void testUpdate_UnknownGenre_ThrowsAndDoesNotSave() {
        // Arrange
        when(instrumentGenreRepository.findById(50L)).thenReturn(Optional.of(testInstrumentGenre));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(piano));
        when(genreRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> instrumentGenreService.update(50L, dto(2L, 999L, List.of())));
        verify(instrumentGenreRepository, never()).save(any());
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        when(instrumentGenreRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(instrumentGenreService.update(999L, dto(2L, 20L, null)).isEmpty());
        verify(instrumentGenreRepository, never()).save(any());
        verifyNoInteractions(instrumentRepository, genreRepository, lessonRepository);
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertTrue(instrumentGenreService.update(null, dto(2L, 20L, null)).isEmpty());
        verify(instrumentGenreRepository, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    void testDelete_Found_DetachesLessonsAndDeletes() {
        // Arrange
        when(instrumentGenreRepository.findById(50L)).thenReturn(Optional.of(testInstrumentGenre));

        // Act
        boolean result = instrumentGenreService.delete(50L);

        // Assert
        assertTrue(result);
        assertTrue(testInstrumentGenre.getLessons().isEmpty());
        assertNull(lesson1.getInstrumentGenre());
        assertFalse(lesson1.isBelongsToInstrumentGenre());
        assertNull(lesson2.getInstrumentGenre());
        verify(instrumentGenreRepository).delete(testInstrumentGenre);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        when(instrumentGenreRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertFalse(instrumentGenreService.delete(999L));
        verify(instrumentGenreRepository, never()).delete(any());
    }

    @Test
    void testDelete_NullId() {
        // Act & Assert
        assertFalse(instrumentGenreService.delete(null));
        verify(instrumentGenreRepository, never()).delete(any());
    }

    // ---------- findForGenre / findForInstrument ----------

    @Test
    void testFindForGenre_ReturnsMapped() {
        // Arrange
        when(instrumentGenreRepository.findByGenreId(10L)).thenReturn(List.of(testInstrumentGenre));

        // Act
        List<InstrumentGenreDTO> result = instrumentGenreService.findForGenre(10L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getGenreId());
        verify(instrumentGenreRepository).findByGenreId(10L);
    }

    @Test
    void testFindForGenre_Empty() {
        // Arrange
        when(instrumentGenreRepository.findByGenreId(99L)).thenReturn(List.of());

        // Act & Assert
        assertTrue(instrumentGenreService.findForGenre(99L).isEmpty());
    }

    @Test
    void testFindForInstrument_ReturnsMapped() {
        // Arrange
        when(instrumentGenreRepository.findByInstrumentId(1L)).thenReturn(List.of(testInstrumentGenre));

        // Act
        List<InstrumentGenreDTO> result = instrumentGenreService.findForInstrument(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getInstrumentId());
        verify(instrumentGenreRepository).findByInstrumentId(1L);
    }

    @Test
    void testFindForInstrument_Empty() {
        // Arrange
        when(instrumentGenreRepository.findByInstrumentId(99L)).thenReturn(List.of());

        // Act & Assert
        assertTrue(instrumentGenreService.findForInstrument(99L).isEmpty());
    }
}
