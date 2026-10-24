package com.uo289390.toco.service.recommendation;

import static com.uo289390.toco.service.recommendation.RecommendationFixtures.genre;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.instrument;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.instrumentGenre;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.lesson;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.song;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.uo289390.toco.service.recommendation.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uo289390.toco.dto.ItemDTO;
import com.uo289390.toco.dto.LessonDTO;
import com.uo289390.toco.dto.SongDTO;
import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.InstrumentGenre;
import com.uo289390.toco.model.Item;
import com.uo289390.toco.model.Lesson;
import com.uo289390.toco.model.Song;
import com.uo289390.toco.utils.RecommendationConstants;
import com.uo289390.toco.utils.mapper.DtoMapper;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for ItemRecommendationServiceImpl.
 * Tests that the item recommendations aggregate every type of recommendation,
 * with the configured counts and in the expected order.
 */
@ExtendWith(MockitoExtension.class)
class ItemRecommendationServiceImplTest {

    private static final Long ID = 7L;

    @Mock
    private SongRecommendationService songRecommendationService;

    @Mock
    private GenreRecommendationService genreRecommendationService;

    @Mock
    private InstrumentGenreRecommendationService instrumentGenreRecommendationService;

    @Mock
    private LessonRecommendationService lessonRecommendationService;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private ItemRecommendationServiceImpl service;

    @Captor
    private ArgumentCaptor<Collection<? extends Item>> itemsCaptor;

    private final Song song = song(1);
    private final Genre genre = genre(2);
    private final InstrumentGenre instrumentGenre = instrumentGenre(3, genre(2), instrument(30));
    private final Lesson lesson = lesson(4);

    private static <T> LinkedHashSet<T> setOf(List<T> items) {
        return new LinkedHashSet<>(items);
    }

    @Test
    void testFindForItemList_AggregatesAllTypesShuffledWithConfiguredCounts() {
        // Arrange
        List<ItemDTO> mapped = List.of(new SongDTO(), new LessonDTO());
        when(songRecommendationService.findForItemList(ID, RecommendationConstants.RECOMMENDED_SONGS_COUNT)).thenReturn(setOf(List.of(song)));
        when(genreRecommendationService.findForItemList(ID, RecommendationConstants.RECOMMENDED_GENRES_COUNT)).thenReturn(setOf(List.of(genre)));
        when(instrumentGenreRecommendationService.findForItemList(ID, RecommendationConstants.RECOMMENDED_INSTRUMENT_GENRES_COUNT)).thenReturn(setOf(List.of(instrumentGenre)));
        when(lessonRecommendationService.findForItemList(ID, RecommendationConstants.RECOMMENDED_LESSONS_COUNT)).thenReturn(setOf(List.of(lesson)));
        when(dtoMapper.itemsToItemDTOs(any())).thenReturn(mapped);

        // Act
        List<ItemDTO> result = service.findForItemList(ID);

        // Assert
        assertSame(mapped, result);
        verify(dtoMapper).itemsToItemDTOs(itemsCaptor.capture());
        // Every type is there, each once, in a random order (shuffled to mix the types)
        List<Item> mappedItems = new ArrayList<>(itemsCaptor.getValue());
        assertEquals(4, mappedItems.size());
        assertEquals(new LinkedHashSet<>(List.of(song, genre, instrumentGenre, lesson)), new LinkedHashSet<>(mappedItems));
        InOrder inOrder = inOrder(songRecommendationService, genreRecommendationService, instrumentGenreRecommendationService, lessonRecommendationService);
        inOrder.verify(songRecommendationService).findForItemList(ID, RecommendationConstants.RECOMMENDED_SONGS_COUNT);
        inOrder.verify(genreRecommendationService).findForItemList(ID, RecommendationConstants.RECOMMENDED_GENRES_COUNT);
        inOrder.verify(instrumentGenreRecommendationService).findForItemList(ID, RecommendationConstants.RECOMMENDED_INSTRUMENT_GENRES_COUNT);
        inOrder.verify(lessonRecommendationService).findForItemList(ID, RecommendationConstants.RECOMMENDED_LESSONS_COUNT);
    }

    @Test
    void testFindForItemList_MixesTheTypes() {
        // Arrange: 10 songs then 6 lessons; in priority order the first 5 would always be songs
        List<Song> songs = new ArrayList<>();
        for (int i = 1; i <= 10; i++) songs.add(song(i));
        List<Lesson> lessons = new ArrayList<>();
        for (int i = 11; i <= 16; i++) lessons.add(lesson(i));
        when(songRecommendationService.findForItemList(anyLong(), anyInt())).thenReturn(setOf(songs));
        when(genreRecommendationService.findForItemList(anyLong(), anyInt())).thenReturn(new LinkedHashSet<>());
        when(instrumentGenreRecommendationService.findForItemList(anyLong(), anyInt())).thenReturn(new LinkedHashSet<>());
        when(lessonRecommendationService.findForItemList(anyLong(), anyInt())).thenReturn(setOf(lessons));
        when(dtoMapper.itemsToItemDTOs(any())).thenReturn(List.of());

        // Act: the chance of 20 shuffles all leaving songs first is negligible (~(1/8008)^20)
        boolean mixed = false;
        for (int attempt = 0; attempt < 20 && !mixed; attempt++) {
            service.findForItemList(ID);
            verify(dtoMapper, atLeastOnce()).itemsToItemDTOs(itemsCaptor.capture());
            List<Item> result = new ArrayList<>(itemsCaptor.getValue());
            assertEquals(16, result.size());
            mixed = result.subList(0, 5).stream().anyMatch(Lesson.class::isInstance);
        }

        // Assert
        assertTrue(mixed, "Some lesson should appear among the first 5 items");
    }

    @Test
    void testFindForItemList_NoRecommendations_MapsEmptyCollection() {
        // Arrange
        when(songRecommendationService.findForItemList(anyLong(), anyInt())).thenReturn(new LinkedHashSet<>());
        when(genreRecommendationService.findForItemList(anyLong(), anyInt())).thenReturn(new LinkedHashSet<>());
        when(instrumentGenreRecommendationService.findForItemList(anyLong(), anyInt())).thenReturn(new LinkedHashSet<>());
        when(lessonRecommendationService.findForItemList(anyLong(), anyInt())).thenReturn(new LinkedHashSet<>());
        when(dtoMapper.itemsToItemDTOs(any())).thenReturn(List.of());

        // Act
        List<ItemDTO> result = service.findForItemList(ID);

        // Assert
        assertTrue(result.isEmpty());
        verify(dtoMapper).itemsToItemDTOs(itemsCaptor.capture());
        assertTrue(itemsCaptor.getValue().isEmpty());
    }

    @Test
    void testFindForItemList_DelegateFails_PropagatesException() {
        // Arrange
        when(songRecommendationService.findForItemList(anyLong(), anyInt())).thenThrow(new EntityNotFoundException("ItemList not found: 7"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.findForItemList(ID));
        verifyNoInteractions(genreRecommendationService, instrumentGenreRecommendationService, lessonRecommendationService, dtoMapper);
    }

    @Test
    void testFindForUser_AggregatesAllTypesInOrderWithHomeCounts() {
        // Arrange
        List<ItemDTO> mapped = List.of(new SongDTO());
        when(songRecommendationService.findForUser(ID, RecommendationConstants.HOME_RECOMMENDED_SONGS_COUNT)).thenReturn(setOf(List.of(song)));
        when(genreRecommendationService.findForUser(ID, RecommendationConstants.HOME_RECOMMENDED_GENRES_COUNT)).thenReturn(setOf(List.of(genre)));
        when(instrumentGenreRecommendationService.findForUser(ID, RecommendationConstants.HOME_RECOMMENDED_INSTRUMENT_GENRES_COUNT)).thenReturn(setOf(List.of(instrumentGenre)));
        when(lessonRecommendationService.findForUser(ID, RecommendationConstants.HOME_RECOMMENDED_LESSONS_COUNT)).thenReturn(setOf(List.of(lesson)));
        when(dtoMapper.itemsToItemDTOs(any())).thenReturn(mapped);

        // Act
        List<ItemDTO> result = service.findForUser(ID);

        // Assert
        assertSame(mapped, result);
        verify(dtoMapper).itemsToItemDTOs(itemsCaptor.capture());
        assertEquals(List.of(song, genre, instrumentGenre, lesson), new ArrayList<>(itemsCaptor.getValue()));
        verify(songRecommendationService, never()).findForItemList(anyLong(), anyInt());
    }

    @Test
    void testFindForUser_ItemsWithSameId_AreDeduplicated() {
        // Arrange: items are equal by id, whatever their concrete type
        Lesson lessonWithSongId = lesson(1);
        when(songRecommendationService.findForUser(anyLong(), anyInt())).thenReturn(setOf(List.of(song)));
        when(genreRecommendationService.findForUser(anyLong(), anyInt())).thenReturn(new LinkedHashSet<>());
        when(instrumentGenreRecommendationService.findForUser(anyLong(), anyInt())).thenReturn(new LinkedHashSet<>());
        when(lessonRecommendationService.findForUser(anyLong(), anyInt())).thenReturn(setOf(List.of(lessonWithSongId, lesson)));
        when(dtoMapper.itemsToItemDTOs(any())).thenReturn(List.of());

        // Act
        service.findForUser(ID);

        // Assert
        verify(dtoMapper).itemsToItemDTOs(itemsCaptor.capture());
        assertEquals(List.of(song, lesson), new ArrayList<>(itemsCaptor.getValue()));
    }
}
