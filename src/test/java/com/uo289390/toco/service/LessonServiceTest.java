package com.uo289390.toco.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uo289390.toco.model.*;
import com.uo289390.toco.repository.*;
import com.uo289390.toco.service.LessonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uo289390.toco.dto.LessonDTO;
import com.uo289390.toco.dto.LessonSectionDTO;
import com.uo289390.toco.utils.EntityResolver;
import com.uo289390.toco.utils.mapper.DtoMapper;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for {@link LessonServiceImpl}.
 * Tests CRUD operations, relationship assignment (genre, instrument-genre, instrument),
 * section handling and the id listings used by the feeds.
 */
@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private InstrumentGenreRepository instrumentGenreRepository;

    @Spy
    private DtoMapper dtoMapper = new DtoMapper();

    private LessonServiceImpl lessonService;

    private Lesson lesson;
    private Genre genre;
    private Instrument instrument;
    private InstrumentGenre instrumentGenre;

    @BeforeEach
    void setUp() {
        new EntityResolver(mock(ChordRepository.class), genreRepository, instrumentRepository,
                instrumentGenreRepository, mock(ItemRepository.class), mock(ItemListRepository.class), lessonRepository,
                mock(LessonSectionRepository.class), mock(SongRepository.class), mock(SongLessonRepository.class),
                mock(SpotifyUserRepository.class), mock(TabSectionRepository.class), mock(TuneRepository.class), mock(UserRepository.class));
        lessonService = new LessonServiceImpl(lessonRepository, dtoMapper);

        lesson = new Lesson();
        lesson.setId(1L);
        lesson.setTitle("Power chords");
        lesson.setLevel(2);
        lesson.setType(Item.Type.Lesson);
        lesson.setImageUrl("https://example.com/lesson.png");

        genre = new Genre();
        genre.setId(5L);
        genre.setTitle("Rock");
        genre.setImageUrl("https://example.com/rock.png");

        instrument = new Instrument();
        instrument.setId(6L);
        instrument.setName("Guitar");

        instrumentGenre = new InstrumentGenre();
        instrumentGenre.setId(7L);
        instrumentGenre.setTitle("Rock guitar");
    }

    private static LessonSectionDTO sectionDto(String title, int order) {
        LessonSectionDTO dto = new LessonSectionDTO();
        dto.setTitle(title);
        dto.setHtmlContent("<p>" + title + "</p>");
        dto.setVideoUrl("https://www.youtube.com/watch?v=" + title.replace(" ", ""));
        dto.setOrderIndex(order);
        return dto;
    }

    private static LessonDTO lessonDto() {
        LessonDTO dto = new LessonDTO();
        dto.setId(1L);
        dto.setTitle("Power chords");
        dto.setLevel(3);
        dto.setImageUrl("https://example.com/own.png");
        return dto;
    }

    private Lesson captureSaved() {
        ArgumentCaptor<Lesson> captor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).save(captor.capture());
        return captor.getValue();
    }

    // -------- Reads --------

    @Test
    void testFindAll() {
        // Arrange
        Lesson other = new Lesson();
        other.setId(2L);
        when(lessonRepository.findAll()).thenReturn(List.of(lesson, other));

        // Act
        List<LessonDTO> result = lessonService.findAll();

        // Assert
        assertEquals(List.of(1L, 2L), result.stream().map(LessonDTO::getId).toList());
    }

    @Test
    void testFindAll_Empty() {
        // Arrange
        when(lessonRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        assertTrue(lessonService.findAll().isEmpty());
    }

    @Test
    void testFindById_Found() {
        // Arrange
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));

        // Act
        Optional<LessonDTO> result = lessonService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Power chords", result.get().getTitle());
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(lessonRepository.findById(9L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(lessonService.findById(9L).isEmpty());
    }

    @Test
    void testFindById_NullId() {
        // Act & Assert
        assertTrue(lessonService.findById(null).isEmpty());
        verify(lessonRepository, never()).findById(any());
    }

    @Test
    void testFindForGenre() {
        // Arrange
        when(lessonRepository.findByGenreIdAndBelongsToGenreTrue(5L)).thenReturn(List.of(lesson));

        // Act
        List<LessonDTO> result = lessonService.findForGenre(5L);

        // Assert
        assertEquals(1, result.size());
        verify(lessonRepository).findByGenreIdAndBelongsToGenreTrue(5L);
    }

    @Test
    void testFindForGenre_Empty() {
        // Arrange
        when(lessonRepository.findByGenreIdAndBelongsToGenreTrue(5L)).thenReturn(List.of());

        // Act & Assert
        assertTrue(lessonService.findForGenre(5L).isEmpty());
    }

    @Test
    void testFindByIds() {
        // Arrange
        when(lessonRepository.findAllById(List.of(1L))).thenReturn(List.of(lesson));

        // Act
        List<LessonDTO> result = lessonService.findByIds(List.of(1L));

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void testFindByIds_NullReturnsEmptyWithoutQuery() {
        // Act
        List<LessonDTO> result = lessonService.findByIds(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(lessonRepository, never()).findAllById(any());
    }

    @Test
    void testFindByIds_EmptyReturnsEmptyWithoutQuery() {
        // Act
        List<LessonDTO> result = lessonService.findByIds(List.of());

        // Assert
        assertTrue(result.isEmpty());
        verify(lessonRepository, never()).findAllById(any());
    }

    @Test
    void testFindIdsPopular() {
        // Arrange
        Lesson other = new Lesson();
        other.setId(3L);
        when(lessonRepository.findByPopularTrue()).thenReturn(List.of(lesson, other));

        // Act & Assert
        assertEquals(List.of(1L, 3L), lessonService.findIdsPopular());
    }

    @Test
    void testFindIdsNew() {
        // Arrange
        Lesson other = new Lesson();
        other.setId(3L);
        when(lessonRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(other, lesson));

        // Act & Assert
        assertEquals(List.of(3L, 1L), lessonService.findIdsNew());
    }

    @Test
    void testFindIdsRecommended() {
        // Arrange
        when(lessonRepository.findAllByOrderByLevelAscIdAsc()).thenReturn(List.of(lesson));

        // Act & Assert
        assertEquals(List.of(1L), lessonService.findIdsRecommended());
    }

    @Test
    void testFindIdsInstrumentPopular() {
        // Arrange
        when(lessonRepository.findByInstrumentIdAndInstrumentPopularTrue(6L)).thenReturn(List.of(lesson));

        // Act & Assert
        assertEquals(List.of(1L), lessonService.findIdsInstrumentPopular(6L));
    }

    @Test
    void testFindIdsInstrumentPopular_Empty() {
        // Arrange
        when(lessonRepository.findByInstrumentIdAndInstrumentPopularTrue(6L)).thenReturn(List.of());

        // Act & Assert
        assertTrue(lessonService.findIdsInstrumentPopular(6L).isEmpty());
    }

    // -------- Create --------

    @Test
    void testCreate_BasicFieldsWithoutRelationships() {
        // Arrange
        LessonDTO dto = lessonDto();
        dto.setPopular(true);
        dto.setSections(List.of(sectionDto("Intro", 0)));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        LessonDTO result = lessonService.create(dto);

        // Assert
        Lesson saved = captureSaved();
        assertEquals(1L, saved.getId());
        assertEquals(Item.Type.Lesson, saved.getType());
        assertEquals("Power chords", saved.getTitle());
        assertEquals(3, saved.getLevel());
        assertEquals("https://example.com/own.png", saved.getImageUrl());
        assertTrue(saved.isPopular());
        assertFalse(saved.isBelongsToGenre());
        assertNull(saved.getGenre());
        assertNull(saved.getInstrumentGenre());
        assertNull(saved.getInstrument());
        assertFalse(saved.isBelongsToInstrumentGenre());
        assertEquals(1L, result.getId());
        verifyNoInteractions(genreRepository, instrumentRepository, instrumentGenreRepository);
    }

    @Test
    void testCreate_SectionsAreLinkedToTheLesson() {
        // Arrange
        LessonDTO dto = lessonDto();
        dto.setSections(List.of(sectionDto("Intro", 0), sectionDto("Practice", 1)));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        lessonService.create(dto);

        // Assert
        Lesson saved = captureSaved();
        assertEquals(2, saved.getSections().size());
        LessonSection first = saved.getSections().get(0);
        assertEquals("Intro", first.getTitle());
        assertEquals("<p>Intro</p>", first.getHtmlContent());
        assertEquals("https://www.youtube.com/watch?v=Intro", first.getVideoUrl());
        assertEquals(0, first.getOrderIndex());
        assertSame(saved, first.getLesson());
        assertEquals("Practice", saved.getSections().get(1).getTitle());
        assertEquals(1, saved.getSections().get(1).getOrderIndex());
    }

    @Test
    void testCreate_WithoutSectionsStillSaves() {
        // Arrange
        LessonDTO dto = lessonDto();
        dto.setSections(null);
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        lessonService.create(dto);

        // Assert
        assertTrue(captureSaved().getSections().isEmpty());
    }

    @Test
    void testCreate_EmptySectionsStillSaves() {
        // Arrange
        LessonDTO dto = lessonDto();
        dto.setSections(new ArrayList<>());
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        lessonService.create(dto);

        // Assert
        assertTrue(captureSaved().getSections().isEmpty());
    }

    @Test
    void testCreate_BelongsToGenreUsesGenreCover() {
        // Arrange
        LessonDTO dto = lessonDto();
        dto.setGenreId(5L);
        dto.setBelongsToGenre(true);
        when(genreRepository.findById(5L)).thenReturn(Optional.of(genre));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        LessonDTO result = lessonService.create(dto);

        // Assert
        Lesson saved = captureSaved();
        assertSame(genre, saved.getGenre());
        assertTrue(saved.isBelongsToGenre());
        assertEquals("https://example.com/rock.png", saved.getImageUrl());
        assertEquals(5L, result.getGenreId());
    }

    @Test
    void testCreate_GenreWithoutBelongsToGenreKeepsOwnImage() {
        // Arrange
        LessonDTO dto = lessonDto();
        dto.setGenreId(5L);
        dto.setBelongsToGenre(false);
        when(genreRepository.findById(5L)).thenReturn(Optional.of(genre));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        lessonService.create(dto);

        // Assert
        Lesson saved = captureSaved();
        assertSame(genre, saved.getGenre());
        assertEquals("https://example.com/own.png", saved.getImageUrl());
    }

    @Test
    void testCreate_BelongsToGenreWithGenreWithoutImageKeepsOwnImage() {
        // Arrange
        genre.setImageUrl(null);
        LessonDTO dto = lessonDto();
        dto.setGenreId(5L);
        dto.setBelongsToGenre(true);
        when(genreRepository.findById(5L)).thenReturn(Optional.of(genre));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        lessonService.create(dto);

        // Assert
        assertEquals("https://example.com/own.png", captureSaved().getImageUrl());
    }

    @Test
    void testCreate_UnknownGenreThrows() {
        // Arrange
        LessonDTO dto = lessonDto();
        dto.setGenreId(5L);
        when(genreRepository.findById(5L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> lessonService.create(dto));
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void testCreate_InstrumentGenreJoinedWithIndex() {
        // Arrange
        LessonDTO dto = lessonDto();
        dto.setInstrumentGenreId(7L);
        dto.setIndexInInstrumentGenre(4);
        when(instrumentGenreRepository.findById(7L)).thenReturn(Optional.of(instrumentGenre));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        LessonDTO result = lessonService.create(dto);

        // Assert
        Lesson saved = captureSaved();
        assertSame(instrumentGenre, saved.getInstrumentGenre());
        assertTrue(saved.isBelongsToInstrumentGenre());
        assertEquals(4, saved.getIndexInInstrumentGenre());
        assertEquals(7L, result.getInstrumentGenreId());
    }

    @Test
    void testCreate_UnknownInstrumentGenreThrows() {
        // Arrange
        LessonDTO dto = lessonDto();
        dto.setInstrumentGenreId(7L);
        when(instrumentGenreRepository.findById(7L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> lessonService.create(dto));
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void testCreate_InstrumentAssigned() {
        // Arrange
        LessonDTO dto = lessonDto();
        dto.setInstrumentId(6L);
        when(instrumentRepository.findById(6L)).thenReturn(Optional.of(instrument));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        LessonDTO result = lessonService.create(dto);

        // Assert
        assertSame(instrument, captureSaved().getInstrument());
        assertEquals(6L, result.getInstrumentId());
    }

    @Test
    void testCreate_UnknownInstrumentThrows() {
        // Arrange
        LessonDTO dto = lessonDto();
        dto.setInstrumentId(6L);
        when(instrumentRepository.findById(6L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> lessonService.create(dto));
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void testCreate_ReturnsMappedSavedEntity() {
        // Arrange
        Lesson persisted = new Lesson();
        persisted.setId(1L);
        persisted.setTitle("Persisted title");
        when(lessonRepository.save(any(Lesson.class))).thenReturn(persisted);

        // Act
        LessonDTO result = lessonService.create(lessonDto());

        // Assert
        assertEquals("Persisted title", result.getTitle());
        verify(dtoMapper).lessonToLessonDto(persisted);
    }

    // -------- Update --------

    @Test
    void testUpdate_BasicFields() {
        // Arrange
        LessonDTO dto = lessonDto();
        dto.setTitle("Updated");
        dto.setLevel(5);
        dto.setPopular(true);
        dto.setBelongsToGenre(true);
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(lesson)).thenReturn(lesson);

        // Act
        Optional<LessonDTO> result = lessonService.update(1L, dto);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Updated", lesson.getTitle());
        assertEquals(5, lesson.getLevel());
        assertEquals("https://example.com/own.png", lesson.getImageUrl());
        assertTrue(lesson.isPopular());
        assertTrue(lesson.isBelongsToGenre());
        assertEquals("Updated", result.get().getTitle());
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        when(lessonRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(lessonService.update(1L, lessonDto()).isEmpty());
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void testUpdate_AssignsRelationships() {
        // Arrange
        LessonDTO dto = lessonDto();
        dto.setGenreId(5L);
        dto.setInstrumentId(6L);
        dto.setInstrumentGenreId(7L);
        dto.setIndexInInstrumentGenre(2);
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(genreRepository.findById(5L)).thenReturn(Optional.of(genre));
        when(instrumentRepository.findById(6L)).thenReturn(Optional.of(instrument));
        when(instrumentGenreRepository.findById(7L)).thenReturn(Optional.of(instrumentGenre));
        when(lessonRepository.save(lesson)).thenReturn(lesson);

        // Act
        lessonService.update(1L, dto);

        // Assert
        assertSame(genre, lesson.getGenre());
        assertSame(instrument, lesson.getInstrument());
        assertSame(instrumentGenre, lesson.getInstrumentGenre());
        assertTrue(lesson.isBelongsToInstrumentGenre());
        assertEquals(2, lesson.getIndexInInstrumentGenre());
    }

    @Test
    void testUpdate_NullRelationshipIdsKeepExistingRelationships() {
        // Arrange
        lesson.setGenre(genre);
        lesson.setInstrument(instrument);
        lesson.joinInstrumentGenre(instrumentGenre, 1);
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(lesson)).thenReturn(lesson);

        // Act
        lessonService.update(1L, lessonDto());

        // Assert
        assertSame(genre, lesson.getGenre());
        assertSame(instrument, lesson.getInstrument());
        assertSame(instrumentGenre, lesson.getInstrumentGenre());
        assertEquals(1, lesson.getIndexInInstrumentGenre());
        verifyNoInteractions(genreRepository, instrumentRepository, instrumentGenreRepository);
    }

    @Test
    void testUpdate_WithSectionsReplacesThem() {
        // Arrange
        lesson.addSection(new LessonSection(50L, "Old", "old", null, null, 0));
        LessonDTO dto = lessonDto();
        dto.setSections(List.of(sectionDto("New A", 0), sectionDto("New B", 1)));
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(lesson)).thenReturn(lesson);

        // Act
        lessonService.update(1L, dto);

        // Assert
        assertEquals(List.of("New A", "New B"), lesson.getSections().stream().map(LessonSection::getTitle).toList());
        assertEquals(List.of("https://www.youtube.com/watch?v=NewA", "https://www.youtube.com/watch?v=NewB"),
                lesson.getSections().stream().map(LessonSection::getVideoUrl).toList());
        assertTrue(lesson.getSections().stream().allMatch(s -> s.getLesson() == lesson));
    }

    @Test
    void testUpdate_EmptySectionsClearsThem() {
        // Arrange
        lesson.addSection(new LessonSection(50L, "Old", "old", null, null, 0));
        LessonDTO dto = lessonDto();
        dto.setSections(List.of());
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(lesson)).thenReturn(lesson);

        // Act
        lessonService.update(1L, dto);

        // Assert
        assertTrue(lesson.getSections().isEmpty());
    }

    @Test
    void testUpdate_NullSectionsKeepsExisting() {
        // Arrange
        lesson.addSection(new LessonSection(50L, "Old", "old", null, null, 0));
        LessonDTO dto = lessonDto();
        dto.setSections(null);
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(lesson)).thenReturn(lesson);

        // Act
        lessonService.update(1L, dto);

        // Assert
        assertEquals(1, lesson.getSections().size());
        assertEquals("Old", lesson.getSections().getFirst().getTitle());
    }

    @Test
    void testUpdate_UnknownGenreThrows() {
        // Arrange
        LessonDTO dto = lessonDto();
        dto.setGenreId(5L);
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(genreRepository.findById(5L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> lessonService.update(1L, dto));
        verify(lessonRepository, never()).save(any());
    }

    // -------- Delete --------

    @Test
    void testDelete_Exists() {
        // Arrange
        when(lessonRepository.existsById(1L)).thenReturn(true);

        // Act & Assert
        assertTrue(lessonService.delete(1L));
        verify(lessonRepository).deleteById(1L);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        when(lessonRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertFalse(lessonService.delete(1L));
        verify(lessonRepository, never()).deleteById(any());
    }
}
