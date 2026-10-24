package com.uo289390.toco.service.recommendation;

import static com.uo289390.toco.service.recommendation.RecommendationFixtures.genre;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.ids;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.preferences;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.registerResolver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.Set;

import com.uo289390.toco.service.recommendation.GenreRecommendationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;

import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.ItemList;
import com.uo289390.toco.model.User;
import com.uo289390.toco.repository.GenreRepository;
import com.uo289390.toco.repository.ItemListRepository;
import com.uo289390.toco.repository.UserRepository;
import com.uo289390.toco.utils.RecommendationConstants;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for GenreRecommendationServiceImpl.
 * Tests the priority levels of the genre recommendations for an item list and for a user.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GenreRecommendationServiceImplTest {

    private static final Long ITEM_LIST_ID = 10L;
    private static final Long USER_ID = 20L;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private ItemListRepository itemListRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemList itemList;

    @Mock
    private User user;

    @InjectMocks
    private GenreRecommendationServiceImpl service;

    @Captor
    private ArgumentCaptor<Collection<Long>> excludedCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @BeforeEach
    void setUp() {
        registerResolver(itemListRepository, userRepository);
        when(itemListRepository.findById(ITEM_LIST_ID)).thenReturn(Optional.of(itemList));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(itemList.getUser()).thenReturn(user);
        when(user.getPreferences()).thenReturn(preferences(List.of(), List.of()));
        when(user.getIdsFromItemListsOfType(Genre.class)).thenReturn(ids());
        when(genreRepository.findByIdNotIn(any(), any())).thenReturn(List.of());
    }

    @Nested
    class FindForItemList {

        @Test
        void testFindForItemList_ItemListNotFound_ThrowsEntityNotFound() {
            // Arrange
            when(itemListRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> service.findForItemList(99L, 1));
            verifyNoInteractions(genreRepository);
        }

        @Test
        void testFindForItemList_NullId_ThrowsIllegalArgument() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.findForItemList(null, 1));
        }

        @Test
        void testFindForItemList_ZeroLimit_ReturnsEmptyWithoutQueries() {
            // Arrange
            when(itemList.getMainGenreFromSongs()).thenReturn(Optional.of(genre(1)));

            // Act
            SequencedSet<Genre> result = service.findForItemList(ITEM_LIST_ID, 0);

            // Assert
            assertTrue(result.isEmpty());
            verifyNoInteractions(genreRepository);
        }

        @Test
        void testFindForItemList_NullCandidate_IsSkipped() {
            // Arrange
            when(itemList.getTopGenresFromSongs(anyInt())).thenReturn(java.util.Arrays.asList(null, genre(2)));

            // Act
            SequencedSet<Genre> result = service.findForItemList(ITEM_LIST_ID, 1);

            // Assert
            assertEquals(List.of(genre(2)), List.copyOf(result));
            verifyNoInteractions(genreRepository);
        }

        @Test
        void testFindForItemList_MainGenreFromSongsFillsLimit_ReturnsOnlyIt() {
            // Arrange
            Genre fromSongs = genre(1);
            when(itemList.getMainGenreFromSongs()).thenReturn(Optional.of(fromSongs));
            when(itemList.getMainGenreFromInstrumentGenres()).thenReturn(Optional.of(genre(2)));
            when(itemList.getMainGenreFromLessons()).thenReturn(Optional.of(genre(3)));

            // Act
            SequencedSet<Genre> result = service.findForItemList(ITEM_LIST_ID, 1);

            // Assert
            assertEquals(List.of(fromSongs), List.copyOf(result));
            verify(itemList, never()).getTopGenresFromSongs(anyInt());
            verifyNoInteractions(genreRepository);
        }

        @Test
        void testFindForItemList_MainGenresKeepPriorityOrder() {
            // Arrange
            when(itemList.getMainGenreFromSongs()).thenReturn(Optional.of(genre(1)));
            when(itemList.getMainGenreFromInstrumentGenres()).thenReturn(Optional.of(genre(2)));
            when(itemList.getMainGenreFromLessons()).thenReturn(Optional.of(genre(3)));

            // Act
            SequencedSet<Genre> result = service.findForItemList(ITEM_LIST_ID, 3);

            // Assert
            assertEquals(List.of(genre(1), genre(2), genre(3)), List.copyOf(result));
            verifyNoInteractions(genreRepository);
        }

        @Test
        void testFindForItemList_MainGenreAlreadyInPlaylist_IsSkipped() {
            // Arrange
            Genre inPlaylist = genre(1);
            when(itemList.getMainGenreFromSongs()).thenReturn(Optional.of(inPlaylist));
            when(itemList.containsItem(inPlaylist)).thenReturn(true);
            when(itemList.getMainGenreFromLessons()).thenReturn(Optional.of(genre(3)));

            // Act
            SequencedSet<Genre> result = service.findForItemList(ITEM_LIST_ID, 1);

            // Assert
            assertEquals(List.of(genre(3)), List.copyOf(result));
        }

        @Test
        void testFindForItemList_DuplicatedMainGenres_AreAddedOnce() {
            // Arrange
            when(itemList.getMainGenreFromSongs()).thenReturn(Optional.of(genre(1)));
            when(itemList.getMainGenreFromInstrumentGenres()).thenReturn(Optional.of(genre(1)));
            when(itemList.getMainGenreFromLessons()).thenReturn(Optional.of(genre(1)));
            when(itemList.getTopGenresFromSongs(anyInt())).thenReturn(List.of(genre(1), genre(4)));

            // Act
            SequencedSet<Genre> result = service.findForItemList(ITEM_LIST_ID, 2);

            // Assert
            assertEquals(List.of(genre(1), genre(4)), List.copyOf(result));
        }

        @Test
        void testFindForItemList_Level2_UsesTopGenresInSourceOrder() {
            // Arrange
            int expectedTopN = RecommendationConstants.NUMBER_OF_GENRES_USED_IN_GENRE_RECOMMENDATION + 1;
            when(itemList.getTopGenresFromSongs(expectedTopN)).thenReturn(List.of(genre(1), genre(2)));
            when(itemList.getTopGenresFromInstrumentGenres(expectedTopN)).thenReturn(List.of(genre(3)));
            when(itemList.getTopGenresFromLessons(expectedTopN)).thenReturn(List.of(genre(4), genre(5)));

            // Act
            SequencedSet<Genre> result = service.findForItemList(ITEM_LIST_ID, 4);

            // Assert
            assertEquals(List.of(genre(1), genre(2), genre(3), genre(4)), List.copyOf(result));
            verify(itemList).getTopGenresFromSongs(expectedTopN);
            verify(itemList).getTopGenresFromInstrumentGenres(expectedTopN);
            verify(itemList).getTopGenresFromLessons(expectedTopN);
            verifyNoInteractions(genreRepository);
        }

        @Test
        void testFindForItemList_Level2_SkipsGenresAlreadyInPlaylist() {
            // Arrange
            Genre inPlaylist = genre(1);
            when(itemList.getTopGenresFromSongs(anyInt())).thenReturn(List.of(inPlaylist, genre(2)));
            when(itemList.containsItem(inPlaylist)).thenReturn(true);

            // Act
            SequencedSet<Genre> result = service.findForItemList(ITEM_LIST_ID, 1);

            // Assert
            assertEquals(List.of(genre(2)), List.copyOf(result));
        }

        @Test
        void testFindForItemList_Level3_CompletesWithUserFavouritesNotInPlaylist() {
            // Arrange
            Genre favouriteInPlaylist = genre(7);
            when(itemList.getMainGenreFromSongs()).thenReturn(Optional.of(genre(1)));
            when(user.getPreferences()).thenReturn(preferences(List.of(favouriteInPlaylist, genre(8), genre(9)), List.of()));
            when(itemList.containsItem(favouriteInPlaylist)).thenReturn(true);

            // Act
            SequencedSet<Genre> result = service.findForItemList(ITEM_LIST_ID, 3);

            // Assert
            assertEquals(List.of(genre(1), genre(8), genre(9)), List.copyOf(result));
            verifyNoInteractions(genreRepository);
        }

        @Test
        void testFindForItemList_Level4_CompletesWithRandomGenresExcludingRecommendedAndPlaylistGenres() {
            // Arrange
            when(itemList.getMainGenreFromSongs()).thenReturn(Optional.of(genre(1)));
            when(itemList.getGenres()).thenReturn(Set.of(genre(5)));
            when(genreRepository.findByIdNotIn(any(), any())).thenReturn(List.of(genre(30), genre(31)));

            // Act
            SequencedSet<Genre> result = service.findForItemList(ITEM_LIST_ID, 3);

            // Assert
            assertEquals(List.of(genre(1), genre(30), genre(31)), List.copyOf(result));
            verify(genreRepository).findByIdNotIn(excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(Set.of(1L, 5L), Set.copyOf(excludedCaptor.getValue()));
            assertEquals(2, pageableCaptor.getValue().getPageSize());
            assertEquals(0, pageableCaptor.getValue().getPageNumber());
        }

        @Test
        void testFindForItemList_EmptyPlaylist_UsesImpossibleIdToExclude() {
            // Act
            SequencedSet<Genre> result = service.findForItemList(ITEM_LIST_ID, 1);

            // Assert
            assertTrue(result.isEmpty());
            verify(genreRepository).findByIdNotIn(excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(List.of(-1L), List.copyOf(excludedCaptor.getValue()));
            assertEquals(1, pageableCaptor.getValue().getPageSize());
        }
    }

    @Nested
    class FindForUser {

        @Test
        void testFindForUser_UserNotFound_ThrowsEntityNotFound() {
            // Arrange
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> service.findForUser(99L, 1));
        }

        @Test
        void testFindForUser_NonPositiveLimit_ReturnsEmptyWithoutQueries() {
            // Act
            SequencedSet<Genre> zero = service.findForUser(USER_ID, 0);
            SequencedSet<Genre> negative = service.findForUser(USER_ID, -3);

            // Assert
            assertTrue(zero.isEmpty());
            assertTrue(negative.isEmpty());
            verifyNoInteractions(genreRepository);
        }

        @Test
        void testFindForUser_LimitOne_ReturnsFirstFavourite() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1), genre(2)), List.of()));
            when(user.getTopGenresFromItemLists(anyInt())).thenReturn(List.of(genre(10)));

            // Act
            SequencedSet<Genre> result = service.findForUser(USER_ID, 1);

            // Assert
            assertEquals(List.of(genre(1)), List.copyOf(result));
            verify(user).getTopGenresFromItemLists(Integer.MAX_VALUE);
            verifyNoInteractions(genreRepository);
        }

        @Test
        void testFindForUser_EvenLimit_HalfFavouritesHalfFromItemLists() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1), genre(2), genre(3)), List.of()));
            when(user.getTopGenresFromItemLists(anyInt())).thenReturn(List.of(genre(10), genre(11), genre(12)));

            // Act
            SequencedSet<Genre> result = service.findForUser(USER_ID, 4);

            // Assert
            assertEquals(List.of(genre(1), genre(2), genre(10), genre(11)), List.copyOf(result));
        }

        @Test
        void testFindForUser_OddLimit_FavouritesGetTheBiggerHalf() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1), genre(2), genre(3)), List.of()));
            when(user.getTopGenresFromItemLists(anyInt())).thenReturn(List.of(genre(10), genre(11)));

            // Act
            SequencedSet<Genre> result = service.findForUser(USER_ID, 3);

            // Assert
            assertEquals(List.of(genre(1), genre(2), genre(10)), List.copyOf(result));
        }

        @Test
        void testFindForUser_ExcludesGenresAlreadyInUserLists() {
            // Arrange
            when(user.getIdsFromItemListsOfType(Genre.class)).thenReturn(ids(1L, 10L));
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1), genre(2)), List.of()));
            when(user.getTopGenresFromItemLists(anyInt())).thenReturn(List.of(genre(10), genre(11)));

            // Act
            SequencedSet<Genre> result = service.findForUser(USER_ID, 2);

            // Assert
            assertEquals(List.of(genre(2), genre(11)), List.copyOf(result));
        }

        @Test
        void testFindForUser_NoFavourites_CompletesWithItemListGenres() {
            // Arrange
            when(user.getTopGenresFromItemLists(anyInt())).thenReturn(List.of(genre(10), genre(11), genre(12)));

            // Act
            SequencedSet<Genre> result = service.findForUser(USER_ID, 3);

            // Assert
            assertEquals(List.of(genre(10), genre(11), genre(12)), List.copyOf(result));
            verifyNoInteractions(genreRepository);
        }

        @Test
        void testFindForUser_NoItemListGenres_CompletesWithMoreFavourites() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1), genre(2), genre(3), genre(4)), List.of()));

            // Act
            SequencedSet<Genre> result = service.findForUser(USER_ID, 4);

            // Assert
            assertEquals(List.of(genre(1), genre(2), genre(3), genre(4)), List.copyOf(result));
            verifyNoInteractions(genreRepository);
        }

        @Test
        void testFindForUser_SameGenreInBothSources_IsAddedOnce() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1)), List.of()));
            when(user.getTopGenresFromItemLists(anyInt())).thenReturn(List.of(genre(1), genre(10)));

            // Act
            SequencedSet<Genre> result = service.findForUser(USER_ID, 2);

            // Assert
            assertEquals(List.of(genre(1), genre(10)), List.copyOf(result));
        }

        @Test
        void testFindForUser_NotEnoughCandidates_CompletesWithRandomExcludingListsAndRecommended() {
            // Arrange
            when(user.getIdsFromItemListsOfType(Genre.class)).thenReturn(ids(50L));
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1)), List.of()));
            when(genreRepository.findByIdNotIn(any(), any())).thenReturn(List.of(genre(40), genre(41)));

            // Act
            SequencedSet<Genre> result = service.findForUser(USER_ID, 3);

            // Assert
            assertEquals(List.of(genre(1), genre(40), genre(41)), List.copyOf(result));
            verify(genreRepository).findByIdNotIn(excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(Set.of(1L, 50L), Set.copyOf(excludedCaptor.getValue()));
            assertEquals(2, pageableCaptor.getValue().getPageSize());
        }

        @Test
        void testFindForUser_NoCandidatesAtAll_AllRandomWithImpossibleExclusion() {
            // Arrange
            when(genreRepository.findByIdNotIn(any(), any())).thenReturn(List.of(genre(40)));

            // Act
            SequencedSet<Genre> result = service.findForUser(USER_ID, 1);

            // Assert
            assertEquals(List.of(genre(40)), List.copyOf(result));
            verify(genreRepository).findByIdNotIn(excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(List.of(-1L), List.copyOf(excludedCaptor.getValue()));
            assertEquals(1, pageableCaptor.getValue().getPageSize());
        }
    }

    @Nested
    class FindMostUsedInItemListsForUser {

        @Test
        void testFindMostUsed_UnknownUser_Throws() {
            // Arrange
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> service.findMostUsedInItemListsForUser(99L, 2));
        }

        @Test
        void testFindMostUsed_NonPositiveLimit_ReturnsEmpty() {
            // Arrange
            when(user.getTopGenresFromItemLists(anyInt())).thenReturn(List.of(genre(10)));

            // Act
            SequencedSet<Genre> result = service.findMostUsedInItemListsForUser(USER_ID, 0);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        void testFindMostUsed_KeepsUsageOrderAndExcludesGenresInItemLists() {
            // Arrange
            when(user.getIdsFromItemListsOfType(Genre.class)).thenReturn(ids(11L));
            when(user.getTopGenresFromItemLists(anyInt())).thenReturn(List.of(genre(10), genre(11), genre(12)));

            // Act
            SequencedSet<Genre> result = service.findMostUsedInItemListsForUser(USER_ID, 5);

            // Assert
            assertEquals(List.of(10L, 12L), result.stream().map(Genre::getId).toList());
            verify(user).getTopGenresFromItemLists(Integer.MAX_VALUE);
        }

        @Test
        void testFindMostUsed_RespectsLimit() {
            // Arrange
            when(user.getTopGenresFromItemLists(anyInt())).thenReturn(List.of(genre(10), genre(11), genre(12)));

            // Act
            SequencedSet<Genre> result = service.findMostUsedInItemListsForUser(USER_ID, 2);

            // Assert
            assertEquals(List.of(10L, 11L), result.stream().map(Genre::getId).toList());
        }

        @Test
        void testFindMostUsed_NoLists_FallsBackToFavouritesNotInItemLists() {
            // Arrange
            when(user.getTopGenresFromItemLists(anyInt())).thenReturn(List.of());
            when(user.getIdsFromItemListsOfType(Genre.class)).thenReturn(ids(2L));
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1), genre(2), genre(3)), List.of()));

            // Act
            SequencedSet<Genre> result = service.findMostUsedInItemListsForUser(USER_ID, 5);

            // Assert
            assertEquals(List.of(1L, 3L), result.stream().map(Genre::getId).toList());
        }

        @Test
        void testFindMostUsed_ListGenresFirstThenFavouritesWithoutDuplicates() {
            // Arrange
            when(user.getTopGenresFromItemLists(anyInt())).thenReturn(List.of(genre(10), genre(1)));
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1), genre(2)), List.of()));

            // Act
            SequencedSet<Genre> result = service.findMostUsedInItemListsForUser(USER_ID, 5);

            // Assert
            assertEquals(List.of(10L, 1L, 2L), result.stream().map(Genre::getId).toList());
        }

        @Test
        void testFindMostUsed_ListGenresFillLimit_FavouritesNotUsed() {
            // Arrange
            when(user.getTopGenresFromItemLists(anyInt())).thenReturn(List.of(genre(10), genre(11)));
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1)), List.of()));

            // Act
            SequencedSet<Genre> result = service.findMostUsedInItemListsForUser(USER_ID, 2);

            // Assert
            assertEquals(List.of(10L, 11L), result.stream().map(Genre::getId).toList());
        }

        @Test
        void testFindMostUsed_NoListsNorFavourites_ReturnsEmptyWithoutRandomFill() {
            // Arrange
            when(user.getTopGenresFromItemLists(anyInt())).thenReturn(List.of());

            // Act
            SequencedSet<Genre> result = service.findMostUsedInItemListsForUser(USER_ID, 3);

            // Assert: never random genres
            assertTrue(result.isEmpty());
            verifyNoInteractions(genreRepository);
        }
    }
}
