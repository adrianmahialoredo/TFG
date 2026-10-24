package com.uo289390.toco.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.uo289390.toco.model.Item;
import com.uo289390.toco.model.Lesson;
import com.uo289390.toco.model.Song;
import com.uo289390.toco.repository.*;
import com.uo289390.toco.service.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uo289390.toco.dto.ItemSummaryDTO;
import com.uo289390.toco.utils.EntityResolver;
import com.uo289390.toco.utils.mapper.DtoMapper;


/**
 * Unit tests for {@link ItemServiceImpl}.
 * Tests item lookups, searches and recommendations for an item list.
 */
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemListRepository itemListRepository;

    @Spy
    private DtoMapper dtoMapper = new DtoMapper();

    private ItemServiceImpl itemService;

    private Song song;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        new EntityResolver(mock(ChordRepository.class), mock(GenreRepository.class), mock(InstrumentRepository.class),
                mock(InstrumentGenreRepository.class), itemRepository, itemListRepository, mock(LessonRepository.class),
                mock(LessonSectionRepository.class), mock(SongRepository.class), mock(SongLessonRepository.class),
                mock(SpotifyUserRepository.class), mock(TabSectionRepository.class), mock(TuneRepository.class), mock(UserRepository.class));
        itemService = new ItemServiceImpl(itemRepository, dtoMapper);

        song = new Song();
        song.setId(1L);
        song.setTitle("Wonderwall");
        song.setArtist("Oasis");
        song.setType(Item.Type.Song);
        song.setLevel(2);

        lesson = new Lesson();
        lesson.setId(2L);
        lesson.setTitle("Strumming basics");
        lesson.setType(Item.Type.Lesson);
    }

    @Test
    void testFindAll_ReturnsSummaries() {
        // Arrange
        when(itemRepository.findAll()).thenReturn(List.of(song, lesson));

        // Act
        List<ItemSummaryDTO> result = itemService.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Wonderwall", result.get(0).getTitle());
        assertEquals("Oasis", result.get(0).getArtist());
        assertEquals(Item.Type.Lesson, result.get(1).getType());
        assertNull(result.get(1).getArtist());
        verify(itemRepository).findAll();
    }

    @Test
    void testFindAll_Empty() {
        // Arrange
        when(itemRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        assertTrue(itemService.findAll().isEmpty());
    }

    @Test
    void testFindById_Found() {
        // Arrange
        when(itemRepository.findById(1L)).thenReturn(Optional.of(song));

        // Act
        Optional<ItemSummaryDTO> result = itemService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals(2, result.get().getLevel());
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(itemService.findById(99L).isEmpty());
    }

    @Test
    void testFindById_NullId() {
        // Act & Assert
        assertTrue(itemService.findById(null).isEmpty());
        verify(itemRepository, never()).findById(any());
    }

    @Test
    void testFindByIds() {
        // Arrange
        when(itemRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(song, lesson));

        // Act
        List<ItemSummaryDTO> result = itemService.findByIds(List.of(1L, 2L));

        // Assert
        assertEquals(List.of(1L, 2L), result.stream().map(ItemSummaryDTO::getId).toList());
    }

    @Test
    void testFindByIds_NoneFound() {
        // Arrange
        when(itemRepository.findAllById(List.of(5L))).thenReturn(List.of());

        // Act & Assert
        assertTrue(itemService.findByIds(List.of(5L)).isEmpty());
    }

    @Test
    void testSearch_DelegatesToCaseInsensitiveQuery() {
        // Arrange
        when(itemRepository.findByTitleContainingIgnoreCase("wonder")).thenReturn(List.of(song));

        // Act
        List<ItemSummaryDTO> result = itemService.search("wonder");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Wonderwall", result.getFirst().getTitle());
        verify(itemRepository).findByTitleContainingIgnoreCase("wonder");
    }

    @Test
    void testSearch_NoMatches() {
        // Arrange
        when(itemRepository.findByTitleContainingIgnoreCase("zzz")).thenReturn(List.of());

        // Act & Assert
        assertTrue(itemService.search("zzz").isEmpty());
    }




}
