package com.uo289390.toco.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.uo289390.toco.model.Item;
import com.uo289390.toco.model.ItemList;
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

import jakarta.persistence.EntityNotFoundException;

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
                mock(SpotifyUserRepository.class), mock(TabSectionRepository.class), mock(UserRepository.class));
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

    @Test
    void testFindRecommendedForItemList_EmptyList_ReturnsLatestItems() {
        // Arrange
        when(itemListRepository.findById(10L)).thenReturn(Optional.of(new ItemList()));
        when(itemRepository.findTop20ByIdNotInOrderByCreatedAtDesc(any())).thenReturn(List.of(lesson, song));

        // Act
        List<ItemSummaryDTO> result = itemService.findRecommendedForItemList(10L);

        // Assert
        assertEquals(List.of(2L, 1L), result.stream().map(ItemSummaryDTO::getId).toList());
        // Empty exclusions are replaced by a placeholder: "NOT IN ()" is not valid SQL
        verify(itemRepository).findTop20ByIdNotInOrderByCreatedAtDesc(List.of(-1L));
    }

    @Test
    void testFindRecommendedForItemList_ExcludesItemsAlreadyInTheList() {
        // Arrange (regression: the list used to be resolved but ignored)
        ItemList list = new ItemList();
        list.getItems().add(song);
        when(itemListRepository.findById(10L)).thenReturn(Optional.of(list));
        when(itemRepository.findTop20ByIdNotInOrderByCreatedAtDesc(any())).thenReturn(List.of(lesson));

        // Act
        List<ItemSummaryDTO> result = itemService.findRecommendedForItemList(10L);

        // Assert
        assertEquals(List.of(2L), result.stream().map(ItemSummaryDTO::getId).toList());
        verify(itemRepository).findTop20ByIdNotInOrderByCreatedAtDesc(List.of(song.getId()));
    }

    @Test
    void testFindRecommendedForItemList_MissingListThrows() {
        // Arrange
        when(itemListRepository.findById(10L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> itemService.findRecommendedForItemList(10L));
        verify(itemRepository, never()).findTop20ByIdNotInOrderByCreatedAtDesc(any());
    }

    @Test
    void testFindRecommendedForItemList_NullIdThrows() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> itemService.findRecommendedForItemList(null));
    }
}
