package com.uo289390.toco.service;

import static com.uo289390.toco.service.EntityResolverTestSupport.initResolver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.uo289390.toco.service.GenreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uo289390.toco.dto.GenreDTO;
import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.Item;
import com.uo289390.toco.model.Lesson;
import com.uo289390.toco.repository.GenreRepository;
import com.uo289390.toco.repository.LessonRepository;
import com.uo289390.toco.repository.SongRepository;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.mapper.EntityMapper;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for {@link GenreServiceImpl}.
 * Covers CRUD operations, bulk lookups and lesson resolution.
 */
@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Spy
    private DtoMapper dtoMapper = new DtoMapper();

    @Spy
    private EntityMapper entityMapper = new EntityMapper();

    @InjectMocks
    private GenreServiceImpl genreService;

    private Genre testGenre;
    private Lesson lesson1;
    private Lesson lesson2;

    @BeforeEach
    void setUp() {
        initResolver(genreRepository, songRepository, lessonRepository);

        lesson1 = lesson(100L);
        lesson2 = lesson(200L);

        testGenre = genre(1L, "Rock");
        testGenre.setDescription("Rock music");
        testGenre.setImageUrl("https://example.com/rock.png");
        testGenre.setLevel(2);
        testGenre.setBannerUrl("https://example.com/rock-banner.png");
        testGenre.setWelcomeIntroVideoId("intro");
        testGenre.setWelcomeSongVideoId("song");
        testGenre.setLessons(new HashSet<>(Set.of(lesson1)));
    }

    private static Genre genre(Long id, String title) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setTitle(title);
        genre.setType(Item.Type.Genre);
        return genre;
    }

    private static Lesson lesson(Long id) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.setTitle("Lesson " + id);
        return lesson;
    }

    private static GenreDTO dto(List<Long> lessonIds) {
        GenreDTO dto = new GenreDTO();
        dto.setId(7L);
        dto.setTitle("Jazz");
        dto.setDescription("Jazz music");
        dto.setImageUrl("https://example.com/jazz.png");
        dto.setLevel(4);
        dto.setBannerUrl("https://example.com/jazz-banner.png");
        dto.setWelcomeIntroVideoId("jazz-intro");
        dto.setWelcomeSongVideoId("jazz-song");
        dto.setLessonIds(lessonIds);
        return dto;
    }

    // ---------- findAll ----------

    @Test
    void testFindAll_ReturnsMappedGenres() {
        // Arrange
        when(genreRepository.findAll()).thenReturn(List.of(testGenre, genre(2L, "Pop")));

        // Act
        List<GenreDTO> result = genreService.findAll();

        // Assert
        assertEquals(2, result.size());
        GenreDTO first = result.get(0);
        assertEquals(1L, first.getId());
        assertEquals("Rock", first.getTitle());
        assertEquals("Rock music", first.getDescription());
        assertEquals("https://example.com/rock.png", first.getImageUrl());
        assertEquals(2, first.getLevel());
        assertEquals("https://example.com/rock-banner.png", first.getBannerUrl());
        assertEquals("intro", first.getWelcomeIntroVideoId());
        assertEquals("song", first.getWelcomeSongVideoId());
        assertEquals(List.of(100L), first.getLessonIds());
        assertEquals("Pop", result.get(1).getTitle());
    }

    @Test
    void testFindAll_Empty() {
        // Arrange
        when(genreRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        assertTrue(genreService.findAll().isEmpty());
    }

    // ---------- findByIds ----------

    @Test
    void testFindByIds_NullIds_ReturnsEmptyWithoutQuerying() {
        // Act
        List<GenreDTO> result = genreService.findByIds(null);

        // Assert
        assertTrue(result.isEmpty());
        verifyNoInteractions(genreRepository);
    }

    @Test
    void testFindByIds_EmptyIds_ReturnsEmptyWithoutQuerying() {
        // Act
        List<GenreDTO> result = genreService.findByIds(List.of());

        // Assert
        assertTrue(result.isEmpty());
        verifyNoInteractions(genreRepository);
    }

    @Test
    void testFindByIds_ReturnsMappedGenres() {
        // Arrange
        when(genreRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(testGenre, genre(2L, "Pop")));

        // Act
        List<GenreDTO> result = genreService.findByIds(List.of(1L, 2L));

        // Assert
        assertEquals(List.of("Rock", "Pop"), result.stream().map(GenreDTO::getTitle).toList());
    }

    // ---------- findById ----------

    @Test
    void testFindById_Found() {
        // Arrange
        when(genreRepository.findById(1L)).thenReturn(Optional.of(testGenre));

        // Act
        Optional<GenreDTO> result = genreService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Rock", result.get().getTitle());
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(genreRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(genreService.findById(999L).isEmpty());
    }

    @Test
    void testFindById_NullId() {
        // Act & Assert
        assertTrue(genreService.findById(null).isEmpty());
        verify(genreRepository, never()).findById(any());
    }

    // ---------- create ----------

    @Test
    void testCreate_WithLessons_SavesGenreWithResolvedLessons() {
        // Arrange
        when(lessonRepository.findById(100L)).thenReturn(Optional.of(lesson1));
        when(lessonRepository.findById(200L)).thenReturn(Optional.of(lesson2));
        when(genreRepository.save(any(Genre.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        GenreDTO result = genreService.create(dto(List.of(100L, 200L)));

        // Assert
        ArgumentCaptor<Genre> captor = ArgumentCaptor.forClass(Genre.class);
        verify(genreRepository).save(captor.capture());
        Genre saved = captor.getValue();
        assertEquals(7L, saved.getId());
        assertEquals("Jazz", saved.getTitle());
        assertEquals("Jazz music", saved.getDescription());
        assertEquals("https://example.com/jazz.png", saved.getImageUrl());
        assertEquals(4, saved.getLevel());
        assertEquals("https://example.com/jazz-banner.png", saved.getBannerUrl());
        assertEquals("jazz-intro", saved.getWelcomeIntroVideoId());
        assertEquals("jazz-song", saved.getWelcomeSongVideoId());
        assertEquals(Item.Type.Genre, saved.getType());
        assertEquals(Set.of(lesson1, lesson2), saved.getLessons());
        assertEquals(7L, result.getId());
        assertEquals(Set.of(100L, 200L), new HashSet<>(result.getLessonIds()));
    }

    @Test
    void testCreate_WithoutLessons_SavesGenreWithNoLessons() {
        // Arrange
        when(genreRepository.save(any(Genre.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        GenreDTO result = genreService.create(dto(null));

        // Assert
        ArgumentCaptor<Genre> captor = ArgumentCaptor.forClass(Genre.class);
        verify(genreRepository).save(captor.capture());
        assertTrue(captor.getValue().getLessons().isEmpty());
        assertTrue(result.getLessonIds().isEmpty());
        verifyNoInteractions(lessonRepository);
    }

    @Test
    void testCreate_UnknownLesson_ThrowsAndDoesNotSave() {
        // Arrange
        when(lessonRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> genreService.create(dto(List.of(999L))));
        assertTrue(ex.getMessage().contains("Lesson"));
        verify(genreRepository, never()).save(any());
    }

    @Test
    void testCreate_NullLessonIdInList_ThrowsIllegalArgument() {
        // Arrange
        List<Long> ids = new ArrayList<>();
        ids.add(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> genreService.create(dto(ids)));
        verify(genreRepository, never()).save(any());
    }

    // ---------- update ----------

    @Test
    void testUpdate_Found_UpdatesFieldsAndReplacesLessons() {
        // Arrange
        when(genreRepository.findById(1L)).thenReturn(Optional.of(testGenre));
        when(lessonRepository.findById(200L)).thenReturn(Optional.of(lesson2));
        when(genreRepository.save(any(Genre.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Optional<GenreDTO> result = genreService.update(1L, dto(List.of(200L)));

        // Assert
        assertTrue(result.isPresent());
        verify(genreRepository).save(testGenre);
        assertEquals(1L, testGenre.getId(), "the id must not change on update");
        assertEquals("Jazz", testGenre.getTitle());
        assertEquals("Jazz music", testGenre.getDescription());
        assertEquals("https://example.com/jazz.png", testGenre.getImageUrl());
        assertEquals(4, testGenre.getLevel());
        assertEquals("https://example.com/jazz-banner.png", testGenre.getBannerUrl());
        assertEquals("jazz-intro", testGenre.getWelcomeIntroVideoId());
        assertEquals("jazz-song", testGenre.getWelcomeSongVideoId());
        assertEquals(Set.of(lesson2), testGenre.getLessons());
        assertEquals(List.of(200L), result.get().getLessonIds());
    }

    @Test
    void testUpdate_LevelZero_KeepsExistingLevel() {
        // Arrange
        when(genreRepository.findById(1L)).thenReturn(Optional.of(testGenre));
        when(genreRepository.save(any(Genre.class))).thenAnswer(inv -> inv.getArgument(0));
        GenreDTO dto = dto(null);
        dto.setLevel(0);

        // Act
        genreService.update(1L, dto);

        // Assert
        assertEquals(2, testGenre.getLevel());
    }

    @Test
    void testUpdate_NegativeLevel_KeepsExistingLevel() {
        // Arrange
        when(genreRepository.findById(1L)).thenReturn(Optional.of(testGenre));
        when(genreRepository.save(any(Genre.class))).thenAnswer(inv -> inv.getArgument(0));
        GenreDTO dto = dto(null);
        dto.setLevel(-3);

        // Act
        genreService.update(1L, dto);

        // Assert
        assertEquals(2, testGenre.getLevel());
    }

    @Test
    void testUpdate_NullLessonIds_KeepsExistingLessons() {
        // Arrange
        when(genreRepository.findById(1L)).thenReturn(Optional.of(testGenre));
        when(genreRepository.save(any(Genre.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        genreService.update(1L, dto(null));

        // Assert
        assertEquals(Set.of(lesson1), testGenre.getLessons());
        verifyNoInteractions(lessonRepository);
    }

    @Test
    void testUpdate_EmptyLessonIds_ClearsLessons() {
        // Arrange
        when(genreRepository.findById(1L)).thenReturn(Optional.of(testGenre));
        when(genreRepository.save(any(Genre.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        genreService.update(1L, dto(List.of()));

        // Assert
        assertTrue(testGenre.getLessons().isEmpty());
    }

    @Test
    void testUpdate_UnknownLesson_ThrowsAndDoesNotSave() {
        // Arrange
        when(genreRepository.findById(1L)).thenReturn(Optional.of(testGenre));
        when(lessonRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> genreService.update(1L, dto(List.of(999L))));
        verify(genreRepository, never()).save(any());
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        when(genreRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<GenreDTO> result = genreService.update(999L, dto(null));

        // Assert
        assertTrue(result.isEmpty());
        verify(genreRepository, never()).save(any());
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertTrue(genreService.update(null, dto(null)).isEmpty());
        verify(genreRepository, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    void testDelete_Exists() {
        // Arrange
        when(genreRepository.existsById(1L)).thenReturn(true);

        // Act & Assert
        assertTrue(genreService.delete(1L));
        verify(genreRepository).deleteById(1L);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        when(genreRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertFalse(genreService.delete(999L));
        verify(genreRepository, never()).deleteById(anyLong());
    }
}
