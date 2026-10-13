package com.uo289390.toco.service.recommendation;

import static com.uo289390.toco.service.recommendation.RecommendationFixtures.genre;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.ids;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.instrument;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.instrumentGenre;
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

import com.uo289390.toco.service.recommendation.InstrumentGenreRecommendationServiceImpl;
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
import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.InstrumentGenre;
import com.uo289390.toco.model.ItemList;
import com.uo289390.toco.model.User;
import com.uo289390.toco.repository.InstrumentGenreRepository;
import com.uo289390.toco.repository.ItemListRepository;
import com.uo289390.toco.repository.UserRepository;
import com.uo289390.toco.utils.RecommendationConstants;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for InstrumentGenreRecommendationServiceImpl.
 * Tests the genre/instrument combinations recommended for an item list and for a user.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InstrumentGenreRecommendationServiceImplTest {

    private static final Long ITEM_LIST_ID = 10L;
    private static final Long USER_ID = 20L;
    private static final int USER_LEVEL = 3;

    @Mock
    private InstrumentGenreRepository instrumentGenreRepository;

    @Mock
    private ItemListRepository itemListRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemList itemList;

    @Mock
    private User user;

    @InjectMocks
    private InstrumentGenreRecommendationServiceImpl service;

    @Captor
    private ArgumentCaptor<Collection<Long>> genreIdsCaptor;

    @Captor
    private ArgumentCaptor<Collection<Long>> instrumentIdsCaptor;

    @Captor
    private ArgumentCaptor<Collection<Long>> excludedCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @BeforeEach
    void setUp() {
        registerResolver(itemListRepository, userRepository);
        when(itemListRepository.findById(ITEM_LIST_ID)).thenReturn(Optional.of(itemList));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(user.getPreferences()).thenReturn(preferences(List.of(), List.of()));
        when(user.getDifficultyLevel()).thenReturn(USER_LEVEL);
        when(user.getIdsFromItemListsOfType(InstrumentGenre.class)).thenReturn(ids());
        when(instrumentGenreRepository.findByGenreIdAndInstrumentId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(instrumentGenreRepository.findByGenreIdInAndInstrumentIdInAndLevelLessThanEqualAndIdNotIn(any(), any(), anyInt(), any(), any()))
                .thenReturn(List.of());
        when(instrumentGenreRepository.findByGenreIdInAndInstrumentIdInAndIdNotIn(any(), any(), any(), any()))
                .thenReturn(List.of());
    }

    /**
     * Makes the repository return an InstrumentGenre for the given genre and instrument.
     */
    private InstrumentGenre existing(long id, Genre genre, Instrument instrument) {
        InstrumentGenre instrumentGenre = instrumentGenre(id, genre, instrument);
        when(instrumentGenreRepository.findByGenreIdAndInstrumentId(genre.getId(), instrument.getId()))
                .thenReturn(Optional.of(instrumentGenre));
        return instrumentGenre;
    }

    @Nested
    class FindForItemList {

        @Test
        void testFindForItemList_ItemListNotFound_ThrowsEntityNotFound() {
            // Arrange
            when(itemListRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> service.findForItemList(99L, 1));
        }

        @Test
        void testFindForItemList_NonPositiveLimit_ReturnsEmptyWithoutQueries() {
            // Act
            SequencedSet<InstrumentGenre> result = service.findForItemList(ITEM_LIST_ID, 0);

            // Assert
            assertTrue(result.isEmpty());
            verifyNoInteractions(instrumentGenreRepository);
        }

        @Test
        void testFindForItemList_Level1_UsesOnlyGenreOfThePlaylist() {
            // Arrange
            Genre onlyGenre = genre(1);
            Instrument mainInstrument = instrument(2);
            InstrumentGenre expected = existing(100, onlyGenre, mainInstrument);
            when(itemList.getOnlyGenre()).thenReturn(Optional.of(onlyGenre));
            when(itemList.getMainGenreFromItems()).thenReturn(Optional.of(genre(9)));
            when(itemList.getMainInstrumentFromItems()).thenReturn(Optional.of(mainInstrument));

            // Act
            SequencedSet<InstrumentGenre> result = service.findForItemList(ITEM_LIST_ID, 1);

            // Assert
            assertEquals(List.of(expected), List.copyOf(result));
            verify(itemList, never()).getMainGenreFromItems();
            verify(itemList, never()).getTopGenresFromItems(anyInt());
        }

        @Test
        void testFindForItemList_Level1_FallsBackToMainGenreFromItems() {
            // Arrange
            Genre mainGenre = genre(1);
            Instrument mainInstrument = instrument(2);
            InstrumentGenre expected = existing(100, mainGenre, mainInstrument);
            when(itemList.getMainGenreFromItems()).thenReturn(Optional.of(mainGenre));
            when(itemList.getMainInstrumentFromItems()).thenReturn(Optional.of(mainInstrument));

            // Act
            SequencedSet<InstrumentGenre> result = service.findForItemList(ITEM_LIST_ID, 1);

            // Assert
            assertEquals(List.of(expected), List.copyOf(result));
        }

        @Test
        void testFindForItemList_Level1_NoMainInstrument_SkipsLookup() {
            // Arrange
            when(itemList.getMainGenreFromItems()).thenReturn(Optional.of(genre(1)));

            // Act
            SequencedSet<InstrumentGenre> result = service.findForItemList(ITEM_LIST_ID, 1);

            // Assert
            assertTrue(result.isEmpty());
            verify(instrumentGenreRepository, never()).findByGenreIdAndInstrumentId(anyLong(), anyLong());
        }

        @Test
        void testFindForItemList_Level1_InstrumentGenreAlreadyInPlaylist_IsSkipped() {
            // Arrange
            Genre mainGenre = genre(1);
            Instrument mainInstrument = instrument(2);
            InstrumentGenre inPlaylist = existing(100, mainGenre, mainInstrument);
            when(itemList.getMainGenreFromItems()).thenReturn(Optional.of(mainGenre));
            when(itemList.getMainInstrumentFromItems()).thenReturn(Optional.of(mainInstrument));
            when(itemList.containsItem(inPlaylist)).thenReturn(true);

            // Act
            SequencedSet<InstrumentGenre> result = service.findForItemList(ITEM_LIST_ID, 1);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        void testFindForItemList_Level2_CombinesTopGenresAndInstrumentsInOrder() {
            // Arrange
            int topN = RecommendationConstants.NUMBER_OF_MAX_TOP_INSTRUMENTS_AND_GENRES_USED_IN_INSTRUMENT_GENRE_RECOMMENDATION;
            Genre g1 = genre(1);
            Genre g2 = genre(2);
            Instrument i1 = instrument(11);
            Instrument i2 = instrument(12);
            InstrumentGenre g1i1 = existing(101, g1, i1);
            InstrumentGenre g1i2 = existing(102, g1, i2);
            InstrumentGenre g2i1 = existing(201, g2, i1);
            when(itemList.getTopGenresFromItems(topN)).thenReturn(List.of(g1, g2));
            when(itemList.getTopInstrumentsFromItems(topN)).thenReturn(List.of(i1, i2));

            // Act
            SequencedSet<InstrumentGenre> result = service.findForItemList(ITEM_LIST_ID, 5);

            // Assert
            assertEquals(List.of(g1i1, g1i2, g2i1), List.copyOf(result));
            verify(itemList).getTopGenresFromItems(topN);
            verify(itemList).getTopInstrumentsFromItems(topN);
            verify(instrumentGenreRepository).findByGenreIdAndInstrumentId(2L, 12L);
        }

        @Test
        void testFindForItemList_Level2_StopsWhenLimitIsReached() {
            // Arrange
            Genre g1 = genre(1);
            Instrument i1 = instrument(11);
            Instrument i2 = instrument(12);
            InstrumentGenre g1i1 = existing(101, g1, i1);
            existing(102, g1, i2);
            when(itemList.getTopGenresFromItems(anyInt())).thenReturn(List.of(g1));
            when(itemList.getTopInstrumentsFromItems(anyInt())).thenReturn(List.of(i1, i2));

            // Act
            SequencedSet<InstrumentGenre> result = service.findForItemList(ITEM_LIST_ID, 1);

            // Assert
            assertEquals(List.of(g1i1), List.copyOf(result));
            verify(instrumentGenreRepository, never()).findByGenreIdAndInstrumentId(1L, 12L);
        }

        @Test
        void testFindForItemList_Level2_DoesNotRepeatLevel1Result() {
            // Arrange
            Genre g1 = genre(1);
            Instrument i1 = instrument(11);
            Instrument i2 = instrument(12);
            InstrumentGenre g1i1 = existing(101, g1, i1);
            InstrumentGenre g1i2 = existing(102, g1, i2);
            when(itemList.getMainGenreFromItems()).thenReturn(Optional.of(g1));
            when(itemList.getMainInstrumentFromItems()).thenReturn(Optional.of(i1));
            when(itemList.getTopGenresFromItems(anyInt())).thenReturn(List.of(g1));
            when(itemList.getTopInstrumentsFromItems(anyInt())).thenReturn(List.of(i1, i2));

            // Act
            SequencedSet<InstrumentGenre> result = service.findForItemList(ITEM_LIST_ID, 3);

            // Assert
            assertEquals(List.of(g1i1, g1i2), List.copyOf(result));
        }

        @Test
        void testFindForItemList_NoTopGenresOrInstruments_ReturnsEmpty() {
            // Arrange
            when(itemList.getTopGenresFromItems(anyInt())).thenReturn(List.of(genre(1)));

            // Act
            SequencedSet<InstrumentGenre> result = service.findForItemList(ITEM_LIST_ID, 3);

            // Assert
            assertTrue(result.isEmpty());
            verify(instrumentGenreRepository, never()).findByGenreIdAndInstrumentId(anyLong(), anyLong());
        }
    }

    @Nested
    class FindForUser {

        @Test
        void testFindForUser_UserNotFound_ThrowsEntityNotFound() {
            // Arrange
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> service.findForUser(99L, 2));
        }

        @Test
        void testFindForUser_NonPositiveLimit_ReturnsEmptyWithoutQueries() {
            // Act
            SequencedSet<InstrumentGenre> result = service.findForUser(USER_ID, 0);

            // Assert
            assertTrue(result.isEmpty());
            verifyNoInteractions(instrumentGenreRepository);
        }

        @Test
        void testFindForUser_NoGenres_ReturnsEmptyWithoutQueries() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(), List.of(instrument(11))));

            // Act
            SequencedSet<InstrumentGenre> result = service.findForUser(USER_ID, 2);

            // Assert
            assertTrue(result.isEmpty());
            verifyNoInteractions(instrumentGenreRepository);
        }

        @Test
        void testFindForUser_NoInstruments_ReturnsEmptyWithoutQueries() {
            // Arrange
            when(user.getTopGenresFromItemLists(anyInt())).thenReturn(List.of(genre(1)));

            // Act
            SequencedSet<InstrumentGenre> result = service.findForUser(USER_ID, 2);

            // Assert
            assertTrue(result.isEmpty());
            verifyNoInteractions(instrumentGenreRepository);
        }

        @Test
        void testFindForUser_CandidatesAreItemListTopsFollowedByLimitedFavourites() {
            // Arrange
            when(user.getTopGenresFromItemLists(RecommendationConstants.NUMBER_OF_ITEM_LISTS_GENRES_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION))
                    .thenReturn(List.of(genre(5), genre(1)));
            when(user.getTopInstrumentsFromItemLists(RecommendationConstants.NUMBER_OF_ITEM_LISTS_INSTRUMENTS_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION))
                    .thenReturn(List.of(instrument(15)));
            when(user.getPreferences()).thenReturn(preferences(
                    List.of(genre(1), genre(2), genre(3)),
                    List.of(instrument(11), instrument(12), instrument(13))));
            List<InstrumentGenre> found = List.of(instrumentGenre(100, genre(1), instrument(11)), instrumentGenre(101, genre(5), instrument(15)));
            when(instrumentGenreRepository.findByGenreIdInAndInstrumentIdInAndLevelLessThanEqualAndIdNotIn(any(), any(), anyInt(), any(), any()))
                    .thenReturn(found);

            // Act
            SequencedSet<InstrumentGenre> result = service.findForUser(USER_ID, 2);

            // Assert
            assertEquals(found, List.copyOf(result));
            verify(instrumentGenreRepository).findByGenreIdInAndInstrumentIdInAndLevelLessThanEqualAndIdNotIn(
                    genreIdsCaptor.capture(), instrumentIdsCaptor.capture(), eq(USER_LEVEL), excludedCaptor.capture(), pageableCaptor.capture());
            // Favourites limited to the configured count (2) and duplicates removed, item list tops first
            assertEquals(List.of(5L, 1L, 2L), List.copyOf(genreIdsCaptor.getValue()));
            assertEquals(List.of(15L, 11L, 12L), List.copyOf(instrumentIdsCaptor.getValue()));
            assertEquals(List.of(-1L), List.copyOf(excludedCaptor.getValue()));
            assertEquals(2, pageableCaptor.getValue().getPageSize());
            verify(instrumentGenreRepository, never()).findByGenreIdInAndInstrumentIdInAndIdNotIn(any(), any(), any(), any());
        }

        @Test
        void testFindForUser_Level1Short_Level2CompletesWithoutLevelFilter() {
            // Arrange
            when(user.getIdsFromItemListsOfType(InstrumentGenre.class)).thenReturn(ids(77L));
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1)), List.of(instrument(11))));
            InstrumentGenre byLevel = instrumentGenre(100, genre(1), instrument(11));
            InstrumentGenre anyLevel = instrumentGenre(101, genre(1), instrument(11));
            when(instrumentGenreRepository.findByGenreIdInAndInstrumentIdInAndLevelLessThanEqualAndIdNotIn(any(), any(), anyInt(), any(), any()))
                    .thenReturn(List.of(byLevel));
            when(instrumentGenreRepository.findByGenreIdInAndInstrumentIdInAndIdNotIn(any(), any(), any(), any()))
                    .thenReturn(List.of(anyLevel));

            // Act
            SequencedSet<InstrumentGenre> result = service.findForUser(USER_ID, 3);

            // Assert
            assertEquals(List.of(byLevel, anyLevel), List.copyOf(result));
            verify(instrumentGenreRepository).findByGenreIdInAndInstrumentIdInAndLevelLessThanEqualAndIdNotIn(
                    any(), any(), eq(USER_LEVEL), excludedCaptor.capture(), any());
            assertEquals(Set.of(77L, 100L), Set.copyOf(excludedCaptor.getValue()),
                    "the set passed to the first query is reused and accumulates the recommended ids");
            verify(instrumentGenreRepository).findByGenreIdInAndInstrumentIdInAndIdNotIn(
                    genreIdsCaptor.capture(), instrumentIdsCaptor.capture(), excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(Set.of(1L), Set.copyOf(genreIdsCaptor.getValue()));
            assertEquals(Set.of(11L), Set.copyOf(instrumentIdsCaptor.getValue()));
            assertEquals(Set.of(77L, 100L), Set.copyOf(excludedCaptor.getValue()));
            assertEquals(2, pageableCaptor.getValue().getPageSize());
        }

        @Test
        void testFindForUser_NothingFound_ReturnsEmpty() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1)), List.of(instrument(11))));

            // Act
            SequencedSet<InstrumentGenre> result = service.findForUser(USER_ID, 2);

            // Assert
            assertTrue(result.isEmpty());
            verify(instrumentGenreRepository).findByGenreIdInAndInstrumentIdInAndIdNotIn(any(), any(), any(), any());
        }
    }
}
