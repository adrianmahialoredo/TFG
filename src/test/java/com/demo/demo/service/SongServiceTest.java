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

import com.demo.demo.dto.SongDTO;
import com.demo.demo.model.Song;
import com.demo.demo.repository.SongRepository;
import com.demo.demo.repository.GenreRepository;
import com.demo.demo.repository.InstrumentRepository;
import com.demo.demo.repository.SongLessonRepository;

/**
 * Unit tests for SongService class.
 * Tests CRUD operations and filtering for songs.
 */
@ExtendWith(MockitoExtension.class)
class SongServiceTest {

    @Mock
    private SongRepository songRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private SongLessonRepository songLessonRepository;

    @InjectMocks
    private SongServiceImpl songService;

    private Song testSong;
    private SongDTO testSongDTO;

    @BeforeEach
    void setUp() {
        testSong = new Song();
        testSong.setId(1L);
        testSong.setTitle("Bohemian Rhapsody");
        testSong.setArtist("Queen");
        testSong.setImageUrl("https://example.com/bohemian.png");
        testSong.setDecade("1970s");
        testSong.setPopular(true);
        testSong.setGenreClassic(true);

        testSongDTO = new SongDTO();
        testSongDTO.setTitle("Bohemian Rhapsody");
        testSongDTO.setArtist("Queen");
    }

    @Test
    void testGetAllSongs_Success() {
        // Arrange
        List<Song> songs = new ArrayList<>();
        songs.add(testSong);
        when(songRepository.findAll()).thenReturn(songs);

        // Act
        List<SongDTO> result = songService.getAllSongs();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(songRepository, times(1)).findAll();
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetSongById_Success() {
        // Arrange
        doReturn(Optional.of(testSong)).when(songRepository).findById(anyLong());

        // Act
        Optional<SongDTO> result = songService.getSongById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Bohemian Rhapsody", result.get().getTitle());
        verify(songRepository, times(1)).findById(1L);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetSongById_NotFound() {
        // Arrange
        doReturn(Optional.empty()).when(songRepository).findById(anyLong());

        // Act
        Optional<SongDTO> result = songService.getSongById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(songRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateSong_Success() {
        // Arrange
        when(songRepository.save(any(Song.class))).thenReturn(testSong);

        // Act
        SongDTO result = songService.createSong(testSongDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Bohemian Rhapsody", result.getTitle());
        verify(songRepository, times(1)).save(any(Song.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testUpdateSong_Success() {
        // Arrange
        doReturn(Optional.of(testSong)).when(songRepository).findById(anyLong());
        when(songRepository.save(any(Song.class))).thenReturn(testSong);

        SongDTO updateDto = new SongDTO();
        updateDto.setTitle("Bohemian Rhapsody (Updated)");
        updateDto.setArtist("Queen");

        // Act
        Optional<SongDTO> result = songService.updateSong(1L, updateDto);

        // Assert
        assertTrue(result.isPresent());
        verify(songRepository, times(1)).findById(1L);
        verify(songRepository, times(1)).save(any(Song.class));
    }

    @Test
    void testDeleteSong_Success() {
        // Arrange
        when(songRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = songService.deleteSong(1L);

        // Assert
        assertTrue(result);
        verify(songRepository, times(1)).existsById(1L);
        verify(songRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteSong_NotFound() {
        // Arrange
        when(songRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = songService.deleteSong(999L);

        // Assert
        assertFalse(result);
        verify(songRepository, times(1)).existsById(999L);
        verify(songRepository, never()).deleteById(any());
    }
}






