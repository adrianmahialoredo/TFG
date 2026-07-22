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

import com.demo.demo.dto.GenreDTO;
import com.demo.demo.model.Genre;
import com.demo.demo.repository.GenreRepository;
import com.demo.demo.repository.SongRepository;
import com.demo.demo.repository.LessonRepository;

/**
 * Unit tests for GenreService class.
 * Tests CRUD operations for genres.
 */
@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private GenreServiceImpl genreService;

    private Genre testGenre;
    private GenreDTO testGenreDTO;

    @BeforeEach
    void setUp() {
        testGenre = new Genre();
        testGenre.setId(1L);
        testGenre.setTitle("Rock");
        testGenre.setDescription("Rock music genre");
        testGenre.setBannerUrl("https://example.com/rock-banner.png");
        testGenre.setWelcomeIntroVideoId("https://example.com/rock-intro.mp4");
        testGenre.setWelcomeSongVideoId("https://example.com/rock-song.mp4");

        testGenreDTO = new GenreDTO();
        testGenreDTO.setTitle("Rock");
        testGenreDTO.setDescription("Rock music genre");
        testGenreDTO.setBannerUrl("https://example.com/rock-banner.png");
    }

    @Test
    void testGetAllGenres_Success() {
        // Arrange
        List<Genre> genres = new ArrayList<>();
        genres.add(testGenre);
        when(genreRepository.findAll()).thenReturn(genres);

        // Act
        List<GenreDTO> result = genreService.getAllGenres();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(genreRepository, times(1)).findAll();
    }

    @Test
    void testGetGenreById_Success() {
        // Arrange
        when(genreRepository.findById(1L)).thenReturn(Optional.of(testGenre));

        // Act
        Optional<GenreDTO> result = genreService.getGenreById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Rock", result.get().getTitle());
        verify(genreRepository, times(1)).findById(1L);
    }

    @Test
    void testGetGenreById_NotFound() {
        // Arrange
        when(genreRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<GenreDTO> result = genreService.getGenreById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(genreRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateGenre_Success() {
        // Arrange
        when(genreRepository.save(any(Genre.class))).thenReturn(testGenre);

        // Act
        GenreDTO result = genreService.createGenre(testGenreDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Rock", result.getTitle());
        verify(genreRepository, times(1)).save(any(Genre.class));
    }

    @Test
    void testUpdateGenre_Success() {
        // Arrange
        when(genreRepository.findById(1L)).thenReturn(Optional.of(testGenre));
        when(genreRepository.save(any(Genre.class))).thenReturn(testGenre);

        GenreDTO updateDto = new GenreDTO();
        updateDto.setTitle("Rock Updated");

        // Act
        Optional<GenreDTO> result = genreService.updateGenre(1L, updateDto);

        // Assert
        assertTrue(result.isPresent());
        verify(genreRepository, times(1)).findById(1L);
        verify(genreRepository, times(1)).save(any(Genre.class));
    }

    @Test
    void testDeleteGenre_Success() {
        // Arrange
        when(genreRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = genreService.deleteGenre(1L);

        // Assert
        assertTrue(result);
        verify(genreRepository, times(1)).existsById(1L);
        verify(genreRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteGenre_NotFound() {
        // Arrange
        when(genreRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = genreService.deleteGenre(999L);

        // Assert
        assertFalse(result);
        verify(genreRepository, times(1)).existsById(999L);
        verify(genreRepository, never()).deleteById(any());
    }
}

