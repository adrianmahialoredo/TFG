package com.uo289390.toco.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uo289390.toco.model.ItemList;
import com.uo289390.toco.model.Lesson;
import com.uo289390.toco.model.Song;
import com.uo289390.toco.model.User;
import com.uo289390.toco.repository.*;
import com.uo289390.toco.service.ItemListServiceImpl;
import com.uo289390.toco.service.SongService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uo289390.toco.dto.ItemListDTO;
import com.uo289390.toco.utils.EntityResolver;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.mapper.EntityMapper;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for {@link ItemListServiceImpl}.
 * Tests CRUD operations on item lists and adding/removing items.
 */
@ExtendWith(MockitoExtension.class)
class ItemListServiceTest {

    @Mock
    private ItemListRepository itemListRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SongService songService;

    @Spy
    private DtoMapper dtoMapper = new DtoMapper();

    @Spy
    private EntityMapper entityMapper = new EntityMapper();

    private ItemListServiceImpl itemListService;

    private User user;
    private Song song;
    private Lesson lesson;
    private ItemList list;

    @BeforeEach
    void setUp() {
        new EntityResolver(mock(ChordRepository.class), mock(GenreRepository.class), mock(InstrumentRepository.class),
                mock(InstrumentGenreRepository.class), itemRepository, itemListRepository, mock(LessonRepository.class),
                mock(LessonSectionRepository.class), mock(SongRepository.class), mock(SongLessonRepository.class),
                mock(SpotifyUserRepository.class), mock(TabSectionRepository.class), userRepository);
        itemListService = new ItemListServiceImpl(itemListRepository, songService, dtoMapper, entityMapper);

        user = new User();
        user.setId(1L);

        song = new Song();
        song.setId(10L);
        song.setTitle("Song");
        lesson = new Lesson();
        lesson.setId(20L);
        lesson.setTitle("Lesson");

        list = new ItemList();
        list.setId(100L);
        list.setName("Favourites");
        list.setImageUrl("https://example.com/list.png");
        list.setUser(user);
        list.setItems(List.of(song));
    }

    private ItemListDTO dto(String name, Long userId, List<Long> itemIds) {
        ItemListDTO dto = new ItemListDTO();
        dto.setName(name);
        dto.setImageUrl("https://example.com/new.png");
        dto.setUserId(userId);
        dto.setItemIds(itemIds);
        return dto;
    }

    // -------- Reads --------

    @Test
    void testFindAll() {
        // Arrange
        when(itemListRepository.findAll()).thenReturn(List.of(list));

        // Act
        List<ItemListDTO> result = itemListService.findAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Favourites", result.getFirst().getName());
        assertEquals(1L, result.getFirst().getUserId());
        assertEquals(List.of(10L), result.getFirst().getItemIds());
    }

    @Test
    void testFindAll_Empty() {
        // Arrange
        when(itemListRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        assertTrue(itemListService.findAll().isEmpty());
    }

    @Test
    void testFindIdsForUser_ReturnsListIds() {
        // Arrange
        ItemList other = new ItemList();
        other.setId(200L);
        user.setItemLists(new ArrayList<>(List.of(list, other)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        List<Long> result = itemListService.findIdsForUser(1L);

        // Assert
        assertEquals(List.of(100L, 200L), result);
    }

    @Test
    void testFindIdsForUser_UserWithoutLists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        assertTrue(itemListService.findIdsForUser(1L).isEmpty());
    }

    @Test
    void testFindIdsForUser_UnknownUserReturnsEmpty() {
        // Arrange
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(itemListService.findIdsForUser(9L).isEmpty());
    }

    @Test
    void testFindIdsForUser_NullUserIdReturnsEmpty() {
        // Act & Assert
        assertTrue(itemListService.findIdsForUser(null).isEmpty());
        verifyNoInteractions(userRepository);
    }

    @Test
    void testFindByIds() {
        // Arrange
        when(itemListRepository.findAllById(List.of(100L))).thenReturn(List.of(list));

        // Act
        List<ItemListDTO> result = itemListService.findByIds(List.of(100L));

        // Assert
        assertEquals(List.of(100L), result.stream().map(ItemListDTO::getId).toList());
    }

    @Test
    void testFindById_Found() {
        // Arrange
        when(itemListRepository.findById(100L)).thenReturn(Optional.of(list));

        // Act
        Optional<ItemListDTO> result = itemListService.findById(100L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("https://example.com/list.png", result.get().getImageUrl());
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(itemListRepository.findById(5L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(itemListService.findById(5L).isEmpty());
    }

    // -------- Create --------

    @Test
    void testCreate_ResolvesUserAndItemsAndSaves() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(song));
        when(itemRepository.findById(20L)).thenReturn(Optional.of(lesson));
        when(itemListRepository.save(any(ItemList.class))).thenAnswer(inv -> {
            ItemList saved = inv.getArgument(0);
            saved.setId(300L);
            return saved;
        });

        // Act
        ItemListDTO result = itemListService.create(dto("New list", 1L, List.of(20L, 10L, 20L)));

        // Assert
        ArgumentCaptor<ItemList> captor = ArgumentCaptor.forClass(ItemList.class);
        verify(itemListRepository).save(captor.capture());
        ItemList saved = captor.getValue();
        assertEquals("New list", saved.getName());
        assertEquals("https://example.com/new.png", saved.getImageUrl());
        assertSame(user, saved.getUser());
        assertEquals(List.of(lesson, song), saved.getItems());

        assertEquals(300L, result.getId());
        assertEquals(List.of(20L, 10L), result.getItemIds());
    }

    @Test
    void testCreate_WithoutItems() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemListRepository.save(any(ItemList.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ItemListDTO result = itemListService.create(dto("Empty", 1L, List.of()));

        // Assert
        assertTrue(result.getItemIds().isEmpty());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void testCreate_UnknownUserThrows() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> itemListService.create(dto("x", 1L, List.of())));
        verify(itemListRepository, never()).save(any());
    }

    @Test
    void testCreate_NullUserThrows() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> itemListService.create(dto("x", null, List.of())));
        verify(itemListRepository, never()).save(any());
    }

    @Test
    void testCreate_UnknownItemThrows() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> itemListService.create(dto("x", 1L, List.of(10L))));
        verify(itemListRepository, never()).save(any());
    }

    // -------- Update --------

    @Test
    void testUpdate_ReplacesAllFields() {
        // Arrange
        User newOwner = new User();
        newOwner.setId(2L);
        when(itemListRepository.findById(100L)).thenReturn(Optional.of(list));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newOwner));
        when(itemRepository.findById(20L)).thenReturn(Optional.of(lesson));
        when(itemListRepository.save(any(ItemList.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Optional<ItemListDTO> result = itemListService.update(100L, dto("Renamed", 2L, List.of(20L)));

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Renamed", result.get().getName());
        assertEquals("https://example.com/new.png", result.get().getImageUrl());
        assertEquals(2L, result.get().getUserId());
        assertEquals(List.of(20L), result.get().getItemIds());
        assertSame(newOwner, list.getUser());
        verify(itemListRepository).save(list);
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        when(itemListRepository.findById(5L)).thenReturn(Optional.empty());

        // Act
        Optional<ItemListDTO> result = itemListService.update(5L, dto("x", 1L, List.of()));

        // Assert
        assertTrue(result.isEmpty());
        verify(itemListRepository, never()).save(any());
    }

    @Test
    void testUpdate_NullItemIdsClearsItems() {
        // Arrange
        when(itemListRepository.findById(100L)).thenReturn(Optional.of(list));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemListRepository.save(any(ItemList.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Optional<ItemListDTO> result = itemListService.update(100L, dto("x", 1L, null));

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().getItemIds().isEmpty());
    }

    @Test
    void testUpdate_UnknownUserThrows() {
        // Arrange
        when(itemListRepository.findById(100L)).thenReturn(Optional.of(list));
        when(userRepository.findById(7L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> itemListService.update(100L, dto("x", 7L, List.of())));
        verify(itemListRepository, never()).save(any());
    }

    // -------- Delete --------

    @Test
    void testDelete_Exists() {
        // Arrange
        when(itemListRepository.existsById(100L)).thenReturn(true);

        // Act & Assert
        assertTrue(itemListService.delete(100L));
        verify(itemListRepository).deleteById(100L);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        when(itemListRepository.existsById(100L)).thenReturn(false);

        // Act & Assert
        assertFalse(itemListService.delete(100L));
        verify(itemListRepository, never()).deleteById(any());
    }

    // -------- Add / remove items --------

    @Test
    void testAddItem_AppendsItem() {
        // Arrange
        when(itemListRepository.findById(100L)).thenReturn(Optional.of(list));
        when(itemRepository.findById(20L)).thenReturn(Optional.of(lesson));
        when(itemListRepository.save(list)).thenReturn(list);

        // Act
        Optional<ItemListDTO> result = itemListService.addItem(100L, 20L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(List.of(10L, 20L), result.get().getItemIds());
        verify(itemListRepository).save(list);
    }

    @Test
    void testAddItem_DuplicateIsIgnored() {
        // Arrange
        when(itemListRepository.findById(100L)).thenReturn(Optional.of(list));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(song));
        when(itemListRepository.save(list)).thenReturn(list);

        // Act
        Optional<ItemListDTO> result = itemListService.addItem(100L, 10L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(List.of(10L), result.get().getItemIds());
    }

    @Test
    void testAddItem_ListNotFound() {
        // Arrange
        when(itemListRepository.findById(5L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(itemListService.addItem(5L, 10L).isEmpty());
        verifyNoInteractions(itemRepository);
        verify(itemListRepository, never()).save(any());
    }

    @Test
    void testAddItem_ItemNotFoundThrows() {
        // Arrange
        when(itemListRepository.findById(100L)).thenReturn(Optional.of(list));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> itemListService.addItem(100L, 99L));
        verify(itemListRepository, never()).save(any());
    }

    @Test
    void testRemoveItem_RemovesById() {
        // Arrange
        list.setItems(List.of(song, lesson));
        when(itemListRepository.findById(100L)).thenReturn(Optional.of(list));
        when(itemListRepository.save(list)).thenReturn(list);

        // Act
        Optional<ItemListDTO> result = itemListService.removeItem(100L, 10L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(List.of(20L), result.get().getItemIds());
    }

    @Test
    void testRemoveItem_ItemNotInListLeavesListUnchanged() {
        // Arrange
        when(itemListRepository.findById(100L)).thenReturn(Optional.of(list));
        when(itemListRepository.save(list)).thenReturn(list);

        // Act
        Optional<ItemListDTO> result = itemListService.removeItem(100L, 999L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(List.of(10L), result.get().getItemIds());
    }

    @Test
    void testRemoveItem_ListNotFound() {
        // Arrange
        when(itemListRepository.findById(5L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(itemListService.removeItem(5L, 10L).isEmpty());
        verify(itemListRepository, never()).save(any());
    }
}
