package com.uo289390.toco.service.recommendation;

import static com.uo289390.toco.service.recommendation.RecommendationFixtures.genre;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.ids;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.instrument;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.instrumentGenre;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.preferences;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.registerResolver;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.song;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.songs;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.Set;

import com.uo289390.toco.service.recommendation.SongRecommendationServiceImpl;
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
import com.uo289390.toco.model.Preferences;
import com.uo289390.toco.model.Song;
import com.uo289390.toco.model.User;
import com.uo289390.toco.repository.ItemListRepository;
import com.uo289390.toco.repository.SongRepository;
import com.uo289390.toco.repository.UserRepository;
import com.uo289390.toco.utils.RecommendationConstants;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for SongRecommendationServiceImpl.
 * Tests the priority levels of the song recommendations for an item list and for a user,
 * how the limit is split between genres and instruments and the exclusions.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SongRecommendationServiceImplTest {

    private static final Long ITEM_LIST_ID = 10L;
    private static final Long USER_ID = 20L;
    private static final int USER_LEVEL = 4;

    @Mock
    private SongRepository songRepository;

    @Mock
    private ItemListRepository itemListRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemList itemList;

    @Mock
    private User user;

    @InjectMocks
    private SongRecommendationServiceImpl service;

    @Captor
    private ArgumentCaptor<Collection<Long>> excludedCaptor;

    @Captor
    private ArgumentCaptor<Collection<Instrument>> instrumentsCaptor;

    @Captor
    private ArgumentCaptor<Collection<Genre>> genresCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @BeforeEach
    void setUp() {
        registerResolver(itemListRepository, userRepository);
        when(itemListRepository.findById(ITEM_LIST_ID)).thenReturn(Optional.of(itemList));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(itemList.getUser()).thenReturn(user);
        when(itemList.getSongs()).thenReturn(Set.of());
        when(user.getPreferences()).thenReturn(preferences(List.of(), List.of()));
        when(user.getDifficultyLevel()).thenReturn(USER_LEVEL);
        when(user.getIdsFromItemListsOfType(Song.class)).thenReturn(ids());

        when(songRepository.findDistinctByGenresIdAndInstrumentsInAndIdNotIn(anyLong(), any(), any(), any())).thenReturn(List.of());
        when(songRepository.findByGenresIdAndIdNotIn(anyLong(), any(), any())).thenReturn(List.of());
        when(songRepository.findDistinctByInstrumentsInAndIdNotIn(any(), any(), any())).thenReturn(List.of());
        when(songRepository.findDistinctByGenresInAndInstrumentsInAndIdNotIn(any(), any(), any(), any())).thenReturn(List.of());
        when(songRepository.findByGenresIdAndLevelLessThanEqualAndIdNotIn(anyLong(), anyInt(), any(), any())).thenReturn(List.of());
        when(songRepository.findByInstrumentsIdAndLevelLessThanEqualAndIdNotIn(anyLong(), anyInt(), any(), any())).thenReturn(List.of());
        when(songRepository.findByInstrumentsIdAndIdNotIn(anyLong(), any(), any())).thenReturn(List.of());
    }

    @Nested
    class FindForItemList {

        @Test
        void testFindForItemList_ItemListNotFound_ThrowsEntityNotFound() {
            // Arrange
            when(itemListRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> service.findForItemList(99L, 5));
            verifyNoInteractions(songRepository);
        }

        @Test
        void testFindForItemList_NonPositiveLimit_ReturnsEmptyWithoutQueries() {
            // Act
            SequencedSet<Song> result = service.findForItemList(ITEM_LIST_ID, 0);

            // Assert
            assertTrue(result.isEmpty());
            verifyNoInteractions(songRepository);
        }

        @Test
        void testFindForItemList_Level1_GenreAndTopInstrumentsFillLimit() {
            // Arrange
            int topN = RecommendationConstants.NUMBER_OF_MATCHING_INSTRUMENTS_IN_SONG_RECOMMENDATION;
            List<Instrument> topInstruments = List.of(instrument(11), instrument(12));
            when(itemList.getSongs()).thenReturn(Set.of(song(90)));
            when(itemList.getMainGenreFromSongs()).thenReturn(Optional.of(genre(1)));
            when(itemList.getTopInstrumentsFromSongs(topN)).thenReturn(topInstruments);
            when(songRepository.findDistinctByGenresIdAndInstrumentsInAndIdNotIn(eq(1L), any(), any(), any())).thenReturn(songs(1, 2, 3));

            // Act
            SequencedSet<Song> result = service.findForItemList(ITEM_LIST_ID, 3);

            // Assert
            assertEquals(songs(1, 2, 3), List.copyOf(result));
            verify(itemList).getTopInstrumentsFromSongs(topN);
            verify(songRepository).findDistinctByGenresIdAndInstrumentsInAndIdNotIn(
                    eq(1L), instrumentsCaptor.capture(), excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(topInstruments, List.copyOf(instrumentsCaptor.getValue()));
            assertTrue(excludedCaptor.getValue().contains(90L));
            assertEquals(3, pageableCaptor.getValue().getPageSize());
            verify(songRepository, never()).findByGenresIdAndIdNotIn(anyLong(), any(), any());
            verify(songRepository, never()).findDistinctByInstrumentsInAndIdNotIn(any(), any(), any());
        }

        @Test
        void testFindForItemList_OnlyGenreTakesPriorityOverMainGenreFromSongs() {
            // Arrange
            when(itemList.getOnlyGenre()).thenReturn(Optional.of(genre(7)));
            when(itemList.getMainGenreFromSongs()).thenReturn(Optional.of(genre(1)));
            when(itemList.getTopInstrumentsFromSongs(anyInt())).thenReturn(List.of(instrument(11)));
            when(songRepository.findDistinctByGenresIdAndInstrumentsInAndIdNotIn(eq(7L), any(), any(), any())).thenReturn(songs(1));

            // Act
            SequencedSet<Song> result = service.findForItemList(ITEM_LIST_ID, 1);

            // Assert
            assertEquals(songs(1), List.copyOf(result));
            verify(itemList, never()).getMainGenreFromSongs();
        }

        @Test
        void testFindForItemList_Level2_SplitsMissingWithBiggerHalfForGenre() {
            // Arrange
            List<Instrument> topInstruments = List.of(instrument(11));
            when(itemList.getMainGenreFromSongs()).thenReturn(Optional.of(genre(1)));
            when(itemList.getTopInstrumentsFromSongs(anyInt())).thenReturn(topInstruments);
            when(songRepository.findDistinctByGenresIdAndInstrumentsInAndIdNotIn(eq(1L), any(), any(), any())).thenReturn(songs(1, 2));
            when(songRepository.findByGenresIdAndIdNotIn(eq(1L), any(), any())).thenReturn(songs(3, 4));
            when(songRepository.findDistinctByInstrumentsInAndIdNotIn(any(), any(), any())).thenReturn(songs(5));

            // Act (missing after level 1 = 3 -> 2 by genre, 1 by instruments)
            SequencedSet<Song> result = service.findForItemList(ITEM_LIST_ID, 5);

            // Assert
            assertEquals(songs(1, 2, 3, 4, 5), List.copyOf(result));
            verify(songRepository).findByGenresIdAndIdNotIn(eq(1L), excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(2, pageableCaptor.getValue().getPageSize());
            verify(songRepository).findDistinctByInstrumentsInAndIdNotIn(eq(topInstruments), excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(1, pageableCaptor.getValue().getPageSize());
            assertTrue(excludedCaptor.getValue().containsAll(Set.of(1L, 2L, 3L, 4L)));
        }

        @Test
        void testFindForItemList_Level2_MissingOne_NoInstrumentQuery() {
            // Arrange
            when(itemList.getMainGenreFromSongs()).thenReturn(Optional.of(genre(1)));
            when(itemList.getTopInstrumentsFromSongs(anyInt())).thenReturn(List.of(instrument(11)));
            when(songRepository.findDistinctByGenresIdAndInstrumentsInAndIdNotIn(eq(1L), any(), any(), any())).thenReturn(songs(1));
            when(songRepository.findByGenresIdAndIdNotIn(eq(1L), any(), any())).thenReturn(songs(2));

            // Act
            SequencedSet<Song> result = service.findForItemList(ITEM_LIST_ID, 2);

            // Assert
            assertEquals(songs(1, 2), List.copyOf(result));
            verify(songRepository, never()).findDistinctByInstrumentsInAndIdNotIn(any(), any(), any());
        }

        @Test
        void testFindForItemList_NoTopInstruments_OnlyGenreQueries() {
            // Arrange
            when(itemList.getMainGenreFromSongs()).thenReturn(Optional.of(genre(1)));
            when(songRepository.findByGenresIdAndIdNotIn(eq(1L), any(), any())).thenReturn(songs(1, 2));

            // Act
            SequencedSet<Song> result = service.findForItemList(ITEM_LIST_ID, 4);

            // Assert
            assertEquals(songs(1, 2), List.copyOf(result));
            verify(songRepository, never()).findDistinctByGenresIdAndInstrumentsInAndIdNotIn(anyLong(), any(), any(), any());
            verify(songRepository, never()).findDistinctByInstrumentsInAndIdNotIn(any(), any(), any());
            verify(songRepository).findByGenresIdAndIdNotIn(eq(1L), any(), pageableCaptor.capture());
            assertEquals(2, pageableCaptor.getValue().getPageSize());
        }

        @Test
        void testFindForItemList_NoGenre_SkipsGenreQueries() {
            // Arrange
            when(itemList.getTopInstrumentsFromSongs(anyInt())).thenReturn(List.of(instrument(11)));
            when(songRepository.findDistinctByInstrumentsInAndIdNotIn(any(), any(), any())).thenReturn(songs(5, 6));

            // Act
            SequencedSet<Song> result = service.findForItemList(ITEM_LIST_ID, 4);

            // Assert
            assertEquals(songs(5, 6), List.copyOf(result));
            verify(songRepository, never()).findDistinctByGenresIdAndInstrumentsInAndIdNotIn(anyLong(), any(), any(), any());
            verify(songRepository, never()).findByGenresIdAndIdNotIn(anyLong(), any(), any());
        }

        @Test
        void testFindForItemList_Level3a_UsesOnlyInstrumentGenreOfThePlaylist() {
            // Arrange
            Instrument igInstrument = instrument(30);
            InstrumentGenre onlyInstrumentGenre = instrumentGenre(300, genre(3), igInstrument);
            when(itemList.getOnlyInstrumentGenre()).thenReturn(Optional.of(onlyInstrumentGenre));
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1)), List.of(instrument(11))));
            when(songRepository.findDistinctByGenresIdAndInstrumentsInAndIdNotIn(eq(3L), any(), any(), any())).thenReturn(songs(7, 8));

            // Act
            SequencedSet<Song> result = service.findForItemList(ITEM_LIST_ID, 2);

            // Assert
            assertEquals(songs(7, 8), List.copyOf(result));
            verify(songRepository).findDistinctByGenresIdAndInstrumentsInAndIdNotIn(
                    eq(3L), instrumentsCaptor.capture(), excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(Set.of(igInstrument), Set.copyOf(instrumentsCaptor.getValue()));
            assertEquals(List.of(-1L), List.copyOf(excludedCaptor.getValue()));
            assertEquals(2, pageableCaptor.getValue().getPageSize());
            verify(songRepository, never()).findDistinctByGenresInAndInstrumentsInAndIdNotIn(any(), any(), any(), any());
        }

        @Test
        void testFindForItemList_IncompleteInstrumentGenre_FallsBackToUserPreferences() {
            // Arrange
            InstrumentGenre withoutInstrument = instrumentGenre(300, genre(3), null);
            when(itemList.getOnlyInstrumentGenre()).thenReturn(Optional.of(withoutInstrument));
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1)), List.of(instrument(11))));
            when(songRepository.findDistinctByGenresInAndInstrumentsInAndIdNotIn(any(), any(), any(), any())).thenReturn(songs(9));

            // Act
            SequencedSet<Song> result = service.findForItemList(ITEM_LIST_ID, 2);

            // Assert
            assertEquals(songs(9), List.copyOf(result));
            verify(songRepository, never()).findDistinctByGenresIdAndInstrumentsInAndIdNotIn(anyLong(), any(), any(), any());
        }

        @Test
        void testFindForItemList_Level3b_UsesLimitedRandomFavourites() {
            // Arrange
            List<Genre> favouriteGenres = List.of(genre(1), genre(2), genre(3), genre(4));
            List<Instrument> favouriteInstruments = List.of(instrument(11), instrument(12), instrument(13));
            when(user.getPreferences()).thenReturn(preferences(favouriteGenres, favouriteInstruments));
            when(songRepository.findDistinctByGenresInAndInstrumentsInAndIdNotIn(any(), any(), any(), any())).thenReturn(songs(1, 2));

            // Act
            SequencedSet<Song> result = service.findForItemList(ITEM_LIST_ID, 3);

            // Assert
            assertEquals(songs(1, 2), List.copyOf(result));
            verify(songRepository).findDistinctByGenresInAndInstrumentsInAndIdNotIn(
                    genresCaptor.capture(), instrumentsCaptor.capture(), excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(Preferences.MIN_NUMBER_OF_FAVOURITE_GENRES, genresCaptor.getValue().size());
            assertTrue(favouriteGenres.containsAll(genresCaptor.getValue()));
            assertEquals(Preferences.MIN_NUMBER_OF_FAVOURITE_INSTRUMENTS, instrumentsCaptor.getValue().size());
            assertTrue(favouriteInstruments.containsAll(instrumentsCaptor.getValue()));
            assertEquals(3, pageableCaptor.getValue().getPageSize());
        }

        @Test
        void testFindForItemList_Level3b_NoFavouriteInstruments_NoQuery() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1)), List.of()));

            // Act
            SequencedSet<Song> result = service.findForItemList(ITEM_LIST_ID, 3);

            // Assert
            assertTrue(result.isEmpty());
            verify(songRepository, never()).findDistinctByGenresInAndInstrumentsInAndIdNotIn(any(), any(), any(), any());
        }

        @Test
        void testFindForItemList_Level3b_NoFavouriteGenres_NoQuery() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(), List.of(instrument(11))));

            // Act
            SequencedSet<Song> result = service.findForItemList(ITEM_LIST_ID, 3);

            // Assert
            assertTrue(result.isEmpty());
            verify(songRepository, never()).findDistinctByGenresInAndInstrumentsInAndIdNotIn(any(), any(), any(), any());
        }
    }

    @Nested
    class FindForUser {

        @Test
        void testFindForUser_UserNotFound_ThrowsEntityNotFound() {
            // Arrange
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> service.findForUser(99L, 4));
        }

        @Test
        void testFindForUser_NonPositiveLimit_ReturnsEmptyWithoutQueries() {
            // Act
            SequencedSet<Song> result = service.findForUser(USER_ID, 0);

            // Assert
            assertTrue(result.isEmpty());
            verifyNoInteractions(songRepository);
        }

        @Test
        void testFindForUser_NoGenresNorInstruments_ReturnsEmptyWithoutQueries() {
            // Act
            SequencedSet<Song> result = service.findForUser(USER_ID, 6);

            // Assert
            assertTrue(result.isEmpty());
            verifyNoInteractions(songRepository);
        }

        @Test
        void testFindForUser_EvenLimit_HalfByGenreHalfByInstrument() {
            // Arrange
            when(user.getIdsFromItemListsOfType(Song.class)).thenReturn(ids(99L));
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1)), List.of(instrument(11))));
            when(songRepository.findByGenresIdAndLevelLessThanEqualAndIdNotIn(eq(1L), anyInt(), any(), any())).thenReturn(songs(1, 2));
            when(songRepository.findByInstrumentsIdAndLevelLessThanEqualAndIdNotIn(eq(11L), anyInt(), any(), any())).thenReturn(songs(3, 4));

            // Act
            SequencedSet<Song> result = service.findForUser(USER_ID, 4);

            // Assert
            assertEquals(songs(1, 2, 3, 4), List.copyOf(result));
            verify(songRepository).findByGenresIdAndLevelLessThanEqualAndIdNotIn(eq(1L), eq(USER_LEVEL), excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(2, pageableCaptor.getValue().getPageSize());
            assertTrue(excludedCaptor.getValue().contains(99L));
            verify(songRepository).findByInstrumentsIdAndLevelLessThanEqualAndIdNotIn(eq(11L), eq(USER_LEVEL), any(), pageableCaptor.capture());
            assertEquals(2, pageableCaptor.getValue().getPageSize());
            verify(songRepository, never()).findByGenresIdAndIdNotIn(anyLong(), any(), any());
            verify(songRepository, never()).findByInstrumentsIdAndIdNotIn(anyLong(), any(), any());
        }

        @Test
        void testFindForUser_GenresAreListTopsCompletedWithFavouritesUpToMax() {
            // Arrange (half of 6 = 3 split among 4 genres -> 1, 1, 1, 0)
            int listGenresTopN = RecommendationConstants.NUMBER_OF_GENRES_USED_IN_BY_USER_SONG_RECOMMENDATION - 1;
            when(user.getTopGenresFromItemLists(listGenresTopN)).thenReturn(List.of(genre(1), genre(2)));
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(2), genre(3), genre(4), genre(5)), List.of()));
            when(songRepository.findByGenresIdAndLevelLessThanEqualAndIdNotIn(eq(1L), anyInt(), any(), any())).thenReturn(songs(1));
            when(songRepository.findByGenresIdAndLevelLessThanEqualAndIdNotIn(eq(2L), anyInt(), any(), any())).thenReturn(songs(2));
            when(songRepository.findByGenresIdAndLevelLessThanEqualAndIdNotIn(eq(3L), anyInt(), any(), any())).thenReturn(songs(3));
            when(songRepository.findByGenresIdAndIdNotIn(eq(1L), any(), any())).thenReturn(songs(4, 5, 6));

            // Act
            SequencedSet<Song> result = service.findForUser(USER_ID, 6);

            // Assert
            assertEquals(songs(1, 2, 3, 4, 5, 6), List.copyOf(result));
            verify(user).getTopGenresFromItemLists(listGenresTopN);
            verify(songRepository, never()).findByGenresIdAndLevelLessThanEqualAndIdNotIn(eq(4L), anyInt(), any(), any());
            verify(songRepository, never()).findByGenresIdAndLevelLessThanEqualAndIdNotIn(eq(5L), anyInt(), any(), any());
            // level 3: everything missing from the first genre
            verify(songRepository).findByGenresIdAndIdNotIn(eq(1L), excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(3, pageableCaptor.getValue().getPageSize());
            verify(songRepository, never()).findByGenresIdAndIdNotIn(eq(2L), any(), any());
        }

        @Test
        void testFindForUser_InstrumentsCappedAtConfiguredMaximum() {
            // Arrange (half of 8 = 4 split among 4 instruments -> 1 each)
            int listInstrumentsTopN = RecommendationConstants.NUMBER_OF_INSTRUMENTS_USED_IN_BY_USER_SONG_RECOMMENDATION - 1;
            when(user.getTopInstrumentsFromItemLists(listInstrumentsTopN)).thenReturn(List.of(instrument(11), instrument(12), instrument(13)));
            when(user.getPreferences()).thenReturn(preferences(List.of(), List.of(instrument(14), instrument(15))));

            // Act
            service.findForUser(USER_ID, 8);

            // Assert
            verify(user).getTopInstrumentsFromItemLists(listInstrumentsTopN);
            for (long id = 11; id <= 14; id++) {
                verify(songRepository).findByInstrumentsIdAndLevelLessThanEqualAndIdNotIn(eq(id), eq(USER_LEVEL), any(), pageableCaptor.capture());
                assertEquals(1, pageableCaptor.getValue().getPageSize());
            }
            verify(songRepository, never()).findByInstrumentsIdAndLevelLessThanEqualAndIdNotIn(eq(15L), anyInt(), any(), any());
        }

        @Test
        void testFindForUser_RemainderGoesToTheFirstGenres() {
            // Arrange (half of 6 = 3 split among 2 genres -> 2, 1)
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1), genre(2)), List.of()));

            // Act
            service.findForUser(USER_ID, 6);

            // Assert
            verify(songRepository).findByGenresIdAndLevelLessThanEqualAndIdNotIn(eq(1L), anyInt(), any(), pageableCaptor.capture());
            assertEquals(2, pageableCaptor.getValue().getPageSize());
            verify(songRepository).findByGenresIdAndLevelLessThanEqualAndIdNotIn(eq(2L), anyInt(), any(), pageableCaptor.capture());
            assertEquals(1, pageableCaptor.getValue().getPageSize());
        }

        @Test
        void testFindForUser_OddLimit_ExtraSongFromMainGenre() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1)), List.of(instrument(11))));
            when(songRepository.findByGenresIdAndLevelLessThanEqualAndIdNotIn(eq(1L), anyInt(), any(), any())).thenReturn(songs(1));
            when(songRepository.findByInstrumentsIdAndLevelLessThanEqualAndIdNotIn(eq(11L), anyInt(), any(), any())).thenReturn(songs(2));
            when(songRepository.findByGenresIdAndIdNotIn(eq(1L), any(), any())).thenReturn(songs(3));

            // Act
            SequencedSet<Song> result = service.findForUser(USER_ID, 3);

            // Assert
            assertEquals(songs(1, 2, 3), List.copyOf(result));
            verify(songRepository, times(1)).findByGenresIdAndIdNotIn(eq(1L), excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(1, pageableCaptor.getValue().getPageSize());
            assertTrue(excludedCaptor.getValue().containsAll(Set.of(1L, 2L)));
        }

        @Test
        void testFindForUser_OddLimitWithoutGenres_NoExtraGenreQuery() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(), List.of(instrument(11))));
            when(songRepository.findByInstrumentsIdAndLevelLessThanEqualAndIdNotIn(eq(11L), anyInt(), any(), any())).thenReturn(songs(1));
            when(songRepository.findByInstrumentsIdAndIdNotIn(eq(11L), any(), any())).thenReturn(songs(2, 3));

            // Act
            SequencedSet<Song> result = service.findForUser(USER_ID, 3);

            // Assert
            assertEquals(songs(1, 2, 3), List.copyOf(result));
            verify(songRepository, never()).findByGenresIdAndIdNotIn(anyLong(), any(), any());
            verify(songRepository).findByInstrumentsIdAndIdNotIn(eq(11L), any(), pageableCaptor.capture());
            assertEquals(2, pageableCaptor.getValue().getPageSize());
        }

        @Test
        void testFindForUser_Level4_CompletesByInstrumentsAfterGenres() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1)), List.of(instrument(11), instrument(12))));
            when(songRepository.findByGenresIdAndIdNotIn(eq(1L), any(), any())).thenReturn(songs(1));
            when(songRepository.findByInstrumentsIdAndIdNotIn(eq(11L), any(), any())).thenReturn(songs(2));
            when(songRepository.findByInstrumentsIdAndIdNotIn(eq(12L), any(), any())).thenReturn(songs(3));

            // Act
            SequencedSet<Song> result = service.findForUser(USER_ID, 4);

            // Assert
            assertEquals(songs(1, 2, 3), List.copyOf(result));
            verify(songRepository).findByGenresIdAndIdNotIn(eq(1L), any(), pageableCaptor.capture());
            assertEquals(4, pageableCaptor.getValue().getPageSize());
            verify(songRepository).findByInstrumentsIdAndIdNotIn(eq(11L), any(), pageableCaptor.capture());
            assertEquals(3, pageableCaptor.getValue().getPageSize());
            verify(songRepository).findByInstrumentsIdAndIdNotIn(eq(12L), excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(2, pageableCaptor.getValue().getPageSize());
            assertTrue(excludedCaptor.getValue().containsAll(Set.of(1L, 2L, 3L)));
        }

        @Test
        void testFindForUser_LimitOne_OnlyOddSongQuery() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1)), List.of(instrument(11))));
            when(songRepository.findByGenresIdAndIdNotIn(eq(1L), any(), any())).thenReturn(songs(1));

            // Act
            SequencedSet<Song> result = service.findForUser(USER_ID, 1);

            // Assert
            assertEquals(songs(1), List.copyOf(result));
            verify(songRepository, never()).findByGenresIdAndLevelLessThanEqualAndIdNotIn(anyLong(), anyInt(), any(), any());
            verify(songRepository, never()).findByInstrumentsIdAndLevelLessThanEqualAndIdNotIn(anyLong(), anyInt(), any(), any());
        }
    }
}
