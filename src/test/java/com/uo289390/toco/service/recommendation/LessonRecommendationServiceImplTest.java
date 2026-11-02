package com.uo289390.toco.service.recommendation;

import static com.uo289390.toco.service.recommendation.RecommendationFixtures.genre;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.ids;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.instrument;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.lesson;
import static com.uo289390.toco.service.recommendation.RecommendationFixtures.lessons;
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

import com.uo289390.toco.service.recommendation.LessonRecommendationServiceImpl;
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

import com.uo289390.toco.dto.LessonDTO;
import com.uo289390.toco.model.ItemList;
import com.uo289390.toco.model.Lesson;
import com.uo289390.toco.model.User;
import com.uo289390.toco.repository.ItemListRepository;
import com.uo289390.toco.repository.LessonRepository;
import com.uo289390.toco.repository.UserRepository;
import com.uo289390.toco.utils.RecommendationConstants;
import com.uo289390.toco.utils.mapper.DtoMapper;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for LessonRecommendationServiceImpl.
 * Tests the lesson recommendations for an item list, a genre and a user, including
 * how the limit is split between the different sources and the exclusions.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LessonRecommendationServiceImplTest {

    private static final Long ITEM_LIST_ID = 10L;
    private static final Long USER_ID = 20L;
    private static final int USER_LEVEL = 2;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private DtoMapper dtoMapper;

    @Mock
    private ItemListRepository itemListRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemList itemList;

    @Mock
    private User user;

    @InjectMocks
    private LessonRecommendationServiceImpl service;

    @Captor
    private ArgumentCaptor<Collection<Long>> idsCaptor;

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
        when(itemList.getItemsOfType(Lesson.class)).thenReturn(Set.of());
        when(user.getPreferences()).thenReturn(preferences(List.of(), List.of()));
        when(user.getDifficultyLevel()).thenReturn(USER_LEVEL);
        when(user.getIdsExcludedFromRecommendationsOfType(Lesson.class)).thenReturn(ids());

        when(lessonRepository.findByGenreIdAndIdNotIn(anyLong(), any(), any())).thenReturn(List.of());
        when(lessonRepository.findByIdNotIn(any(), any())).thenReturn(List.of());
        when(lessonRepository.findByGenreIdInAndBelongsToGenreTrueAndLevelLessThanEqualAndIdNotIn(any(), anyInt(), any(), any())).thenReturn(List.of());
        when(lessonRepository.findByGenreIdInAndBelongsToGenreTrueAndIdNotIn(any(), any(), any())).thenReturn(List.of());
        when(lessonRepository.findByInstrumentIdInAndBelongsToInstrumentTrueAndLevelLessThanEqualAndIdNotIn(any(), anyInt(), any(), any())).thenReturn(List.of());
        when(lessonRepository.findByInstrumentIdInAndBelongsToInstrumentTrueAndIdNotIn(any(), any(), any())).thenReturn(List.of());
        when(lessonRepository.findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndLevelLessThanEqualAndIdNotIn(anyInt(), any(), any())).thenReturn(List.of());
        when(lessonRepository.findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndIdNotIn(any(), any())).thenReturn(List.of());
    }

    @Nested
    class FindForItemList {

        @Test
        void testFindForItemList_ItemListNotFound_ThrowsEntityNotFound() {
            // Arrange
            when(itemListRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> service.findForItemList(99L, 3));
            verifyNoInteractions(lessonRepository);
        }

        @Test
        void testFindForItemList_NonPositiveLimit_ReturnsEmptyWithoutQueries() {
            // Act
            SequencedSet<Lesson> result = service.findForItemList(ITEM_LIST_ID, 0);

            // Assert
            assertTrue(result.isEmpty());
            verifyNoInteractions(lessonRepository);
        }

        @Test
        void testFindForItemList_Level1_TwoThirdsFromTopGenreExcludingPlaylistLessons() {
            // Arrange
            when(itemList.getItemsOfType(Lesson.class)).thenReturn(Set.of(lesson(50)));
            when(itemList.getTopGenresFromItems(1)).thenReturn(List.of(genre(1)));
            when(lessonRepository.findByGenreIdAndIdNotIn(eq(1L), any(), any())).thenReturn(lessons(1, 2, 3, 4));
            when(lessonRepository.findByIdNotIn(any(), any())).thenReturn(lessons(5, 6));

            // Act
            SequencedSet<Lesson> result = service.findForItemList(ITEM_LIST_ID, 6);

            // Assert
            assertEquals(lessons(1, 2, 3, 4, 5, 6), List.copyOf(result));
            verify(lessonRepository).findByGenreIdAndIdNotIn(eq(1L), excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(4, pageableCaptor.getValue().getPageSize());
            verify(lessonRepository).findByIdNotIn(idsCaptor.capture(), pageableCaptor.capture());
            assertEquals(Set.of(50L, 1L, 2L, 3L, 4L), Set.copyOf(idsCaptor.getValue()));
            assertEquals(2, pageableCaptor.getValue().getPageSize());
        }

        @Test
        void testFindForItemList_LimitOne_SkipsTopGenreLevel() {
            // Arrange
            when(itemList.getTopGenresFromItems(1)).thenReturn(List.of(genre(1)));
            when(lessonRepository.findByIdNotIn(any(), any())).thenReturn(lessons(9));

            // Act
            SequencedSet<Lesson> result = service.findForItemList(ITEM_LIST_ID, 1);

            // Assert
            assertEquals(lessons(9), List.copyOf(result));
            verify(lessonRepository, never()).findByGenreIdAndIdNotIn(anyLong(), any(), any());
        }

        @Test
        void testFindForItemList_Level2_UsesFavouritesSkippingTheTopGenre() {
            // Arrange
            when(itemList.getTopGenresFromItems(1)).thenReturn(List.of(genre(1)));
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1), genre(2)), List.of()));
            when(lessonRepository.findByGenreIdAndIdNotIn(eq(1L), any(), any())).thenReturn(lessons(1, 2));
            when(lessonRepository.findByGenreIdAndIdNotIn(eq(2L), any(), any())).thenReturn(lessons(3));

            // Act
            SequencedSet<Lesson> result = service.findForItemList(ITEM_LIST_ID, 3);

            // Assert
            assertEquals(lessons(1, 2, 3), List.copyOf(result));
            verify(lessonRepository, times(1)).findByGenreIdAndIdNotIn(eq(1L), any(), any());
            verify(lessonRepository).findByGenreIdAndIdNotIn(eq(2L), excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(1, pageableCaptor.getValue().getPageSize());
            verify(lessonRepository, never()).findByIdNotIn(any(), any());
        }

        @Test
        void testFindForItemList_Level2_StopsOnceTheLimitIsReached() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1), genre(2), genre(3)), List.of()));
            when(lessonRepository.findByGenreIdAndIdNotIn(eq(1L), any(), any())).thenReturn(lessons(1, 2));

            // Act
            SequencedSet<Lesson> result = service.findForItemList(ITEM_LIST_ID, 2);

            // Assert
            assertEquals(lessons(1, 2), List.copyOf(result));
            verify(lessonRepository, never()).findByGenreIdAndIdNotIn(eq(2L), any(), any());
            verify(lessonRepository, never()).findByIdNotIn(any(), any());
        }

        @Test
        void testFindForItemList_NoGenresAtAll_AllRandomWithImpossibleExclusion() {
            // Arrange
            when(lessonRepository.findByIdNotIn(any(), any())).thenReturn(lessons(7, 8));

            // Act
            SequencedSet<Lesson> result = service.findForItemList(ITEM_LIST_ID, 3);

            // Assert
            assertEquals(lessons(7, 8), List.copyOf(result));
            verify(lessonRepository).findByIdNotIn(idsCaptor.capture(), pageableCaptor.capture());
            assertEquals(List.of(-1L), List.copyOf(idsCaptor.getValue()));
            assertEquals(3, pageableCaptor.getValue().getPageSize());
        }
    }

    @Nested
    class FindForGenre {

        @Test
        void testFindForGenre_MapsPopularGenreLessons() {
            // Arrange
            List<Lesson> found = lessons(1, 2);
            LessonDTO dto1 = new LessonDTO();
            LessonDTO dto2 = new LessonDTO();
            when(lessonRepository.findByGenreIdAndGenrePopularTrueAndBelongsToInstrumentGenreFalse(eq(5L), any())).thenReturn(found);
            when(dtoMapper.lessonToLessonDto(found.get(0))).thenReturn(dto1);
            when(dtoMapper.lessonToLessonDto(found.get(1))).thenReturn(dto2);

            // Act
            List<LessonDTO> result = service.findForGenre(5L, RecommendationConstants.N_BY_GENRE_RECOMMENDED_LESSONS);

            // Assert
            assertEquals(List.of(dto1, dto2), result);
            verify(lessonRepository).findByGenreIdAndGenrePopularTrueAndBelongsToInstrumentGenreFalse(eq(5L), pageableCaptor.capture());
            assertEquals(RecommendationConstants.N_BY_GENRE_RECOMMENDED_LESSONS, pageableCaptor.getValue().getPageSize());
        }

        @Test
        void testFindForGenre_NoLessons_ReturnsEmptyList() {
            // Arrange
            when(lessonRepository.findByGenreIdAndGenrePopularTrueAndBelongsToInstrumentGenreFalse(anyLong(), any())).thenReturn(List.of());

            // Act
            List<LessonDTO> result = service.findForGenre(5L, 3);

            // Assert
            assertTrue(result.isEmpty());
            verifyNoInteractions(dtoMapper);
        }

        @Test
        void testFindForGenre_NonPositiveLimit_ThrowsIllegalArgument() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.findForGenre(5L, 0));
            verifyNoInteractions(lessonRepository);
        }
    }

    @Nested
    class FindForUser {

        @Test
        void testFindForUser_UserNotFound_ThrowsEntityNotFound() {
            // Arrange
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> service.findForUser(99L, 3));
        }

        @Test
        void testFindForUser_NonPositiveLimit_ReturnsEmptyWithoutQueries() {
            // Act
            SequencedSet<Lesson> result = service.findForUser(USER_ID, 0);

            // Assert
            assertTrue(result.isEmpty());
            verifyNoInteractions(lessonRepository);
        }

        @Test
        void testFindForUser_SplitsLimitBetweenGenresInstrumentsAndGeneric() {
            // Arrange
            when(user.getIdsExcludedFromRecommendationsOfType(Lesson.class)).thenReturn(ids(90L));
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1), genre(2)), List.of(instrument(11), instrument(12))));
            when(user.getTopGenresFromItemLists(RecommendationConstants.N_ITEM_LISTS_GENRES_USED_IN_BY_USER_LESSON_RECOMMENDATION))
                    .thenReturn(List.of(genre(5), genre(1)));
            when(user.getTopInstrumentsFromItemLists(RecommendationConstants.N_ITEM_LISTS_INSTRUMENTS_USED_IN_BY_USER_LESSON_RECOMMENDATION))
                    .thenReturn(List.of(instrument(15)));
            when(lessonRepository.findByGenreIdInAndBelongsToGenreTrueAndLevelLessThanEqualAndIdNotIn(any(), anyInt(), any(), any())).thenReturn(lessons(1, 2));
            when(lessonRepository.findByInstrumentIdInAndBelongsToInstrumentTrueAndLevelLessThanEqualAndIdNotIn(any(), anyInt(), any(), any())).thenReturn(lessons(3));
            when(lessonRepository.findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndLevelLessThanEqualAndIdNotIn(anyInt(), any(), any())).thenReturn(lessons(4, 5, 6));

            // Act
            SequencedSet<Lesson> result = service.findForUser(USER_ID, 6);

            // Assert
            assertEquals(lessons(1, 2, 3, 4, 5, 6), List.copyOf(result));

            // genres: first favourite, then the most used ones; 2 lessons (bigger half of 3)
            verify(lessonRepository).findByGenreIdInAndBelongsToGenreTrueAndLevelLessThanEqualAndIdNotIn(
                    idsCaptor.capture(), eq(USER_LEVEL), any(), pageableCaptor.capture());
            assertEquals(List.of(1L, 5L), List.copyOf(idsCaptor.getValue()));
            assertEquals(2, pageableCaptor.getValue().getPageSize());

            // instruments: first favourite, then the most used ones; 1 lesson
            verify(lessonRepository).findByInstrumentIdInAndBelongsToInstrumentTrueAndLevelLessThanEqualAndIdNotIn(
                    idsCaptor.capture(), eq(USER_LEVEL), any(), pageableCaptor.capture());
            assertEquals(List.of(11L, 15L), List.copyOf(idsCaptor.getValue()));
            assertEquals(1, pageableCaptor.getValue().getPageSize());

            // generic: the other half
            verify(lessonRepository).findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndLevelLessThanEqualAndIdNotIn(
                    eq(USER_LEVEL), excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(3, pageableCaptor.getValue().getPageSize());
            assertTrue(excludedCaptor.getValue().contains(90L));

            // level 2 is not needed
            verify(lessonRepository, never()).findByGenreIdInAndBelongsToGenreTrueAndIdNotIn(any(), any(), any());
            verify(lessonRepository, never()).findByInstrumentIdInAndBelongsToInstrumentTrueAndIdNotIn(any(), any(), any());
            verify(lessonRepository, never()).findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndIdNotIn(any(), any());
        }

        @Test
        void testFindForUser_LimitOne_OnlyGenericLesson() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1)), List.of(instrument(11))));
            when(lessonRepository.findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndLevelLessThanEqualAndIdNotIn(anyInt(), any(), any())).thenReturn(lessons(4));

            // Act
            SequencedSet<Lesson> result = service.findForUser(USER_ID, 1);

            // Assert
            assertEquals(lessons(4), List.copyOf(result));
            verify(lessonRepository, never()).findByGenreIdInAndBelongsToGenreTrueAndLevelLessThanEqualAndIdNotIn(any(), anyInt(), any(), any());
            verify(lessonRepository, never()).findByInstrumentIdInAndBelongsToInstrumentTrueAndLevelLessThanEqualAndIdNotIn(any(), anyInt(), any(), any());
        }

        @Test
        void testFindForUser_NoGenreOrInstrumentCandidates_SkipsThoseQueries() {
            // Arrange
            when(lessonRepository.findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndLevelLessThanEqualAndIdNotIn(anyInt(), any(), any())).thenReturn(lessons(4, 5));

            // Act
            SequencedSet<Lesson> result = service.findForUser(USER_ID, 4);

            // Assert
            assertTrue(result.containsAll(lessons(4, 5)));
            verify(lessonRepository, never()).findByGenreIdInAndBelongsToGenreTrueAndLevelLessThanEqualAndIdNotIn(any(), anyInt(), any(), any());
            verify(lessonRepository, never()).findByInstrumentIdInAndBelongsToInstrumentTrueAndLevelLessThanEqualAndIdNotIn(any(), anyInt(), any(), any());
            verify(lessonRepository, never()).findByGenreIdInAndBelongsToGenreTrueAndIdNotIn(any(), any(), any());
            verify(lessonRepository, never()).findByInstrumentIdInAndBelongsToInstrumentTrueAndIdNotIn(any(), any(), any());
        }

        @Test
        void testFindForUser_GenericShortAtUserLevel_Level2CompletesGenericWithoutLevelFilter() {
            // Arrange (limit 2 -> genres 1, instruments 0, generic 1)
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1)), List.of()));
            when(lessonRepository.findByGenreIdInAndBelongsToGenreTrueAndLevelLessThanEqualAndIdNotIn(any(), anyInt(), any(), any())).thenReturn(lessons(1));
            when(lessonRepository.findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndIdNotIn(any(), any())).thenReturn(lessons(2));

            // Act
            SequencedSet<Lesson> result = service.findForUser(USER_ID, 2);

            // Assert
            assertEquals(lessons(1, 2), List.copyOf(result));
            verify(lessonRepository).findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndIdNotIn(excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(1, pageableCaptor.getValue().getPageSize());
            assertTrue(excludedCaptor.getValue().contains(1L));
            verify(lessonRepository, never()).findByGenreIdInAndBelongsToGenreTrueAndIdNotIn(any(), any(), any());
        }

        @Test
        void testFindForUser_Level2_CompletesEachPartWithoutLevelFilter() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1)), List.of(instrument(11))));
            // limit 8 -> genres 2, instruments 2, generic 4
            when(lessonRepository.findByGenreIdInAndBelongsToGenreTrueAndLevelLessThanEqualAndIdNotIn(any(), anyInt(), any(), any())).thenReturn(lessons(1));
            when(lessonRepository.findByInstrumentIdInAndBelongsToInstrumentTrueAndLevelLessThanEqualAndIdNotIn(any(), anyInt(), any(), any())).thenReturn(lessons(3, 4));
            when(lessonRepository.findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndLevelLessThanEqualAndIdNotIn(anyInt(), any(), any())).thenReturn(lessons(5));
            when(lessonRepository.findByGenreIdInAndBelongsToGenreTrueAndIdNotIn(any(), any(), any())).thenReturn(lessons(2));
            when(lessonRepository.findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndIdNotIn(any(), any())).thenReturn(lessons(6, 7, 8));

            // Act
            SequencedSet<Lesson> result = service.findForUser(USER_ID, 8);

            // Assert
            assertEquals(lessons(1, 3, 4, 5, 2, 6, 7, 8), List.copyOf(result));
            verify(lessonRepository).findByGenreIdInAndBelongsToGenreTrueAndIdNotIn(any(), excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(1, pageableCaptor.getValue().getPageSize());
            verify(lessonRepository, never()).findByInstrumentIdInAndBelongsToInstrumentTrueAndIdNotIn(any(), any(), any());
            verify(lessonRepository).findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndIdNotIn(excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(3, pageableCaptor.getValue().getPageSize());
            assertTrue(excludedCaptor.getValue().containsAll(Set.of(1L, 2L, 3L, 4L, 5L)));
        }

        @Test
        void testFindForUser_NoCandidates_Level3FillsTheUnusedQuotasWithAnyLesson() {
            // Arrange (regression: with no genre/instrument candidates their quotas were 0 and,
            // with limit 4, at most 2 generic lessons were returned even if more lessons existed)
            when(lessonRepository.findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndLevelLessThanEqualAndIdNotIn(anyInt(), any(), any())).thenReturn(lessons(4, 5));
            when(lessonRepository.findByIdNotIn(any(), any())).thenReturn(lessons(6, 7));

            // Act
            SequencedSet<Lesson> result = service.findForUser(USER_ID, 4);

            // Assert
            assertEquals(lessons(4, 5, 6, 7), List.copyOf(result));
            verify(lessonRepository).findByIdNotIn(excludedCaptor.capture(), pageableCaptor.capture());
            assertEquals(2, pageableCaptor.getValue().getPageSize());
            assertTrue(excludedCaptor.getValue().containsAll(Set.of(4L, 5L)));
        }

        @Test
        void testFindForUser_PartsShortEvenWithoutLevel_Level3CompletesTheLimit() {
            // Arrange: limit 4 -> genres 1, instruments 1, generic 2; nothing matches at levels 1-2
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1)), List.of(instrument(11))));
            when(lessonRepository.findByIdNotIn(any(), any())).thenReturn(lessons(1, 2, 3, 4));

            // Act
            SequencedSet<Lesson> result = service.findForUser(USER_ID, 4);

            // Assert
            assertEquals(lessons(1, 2, 3, 4), List.copyOf(result));
            verify(lessonRepository).findByIdNotIn(any(), pageableCaptor.capture());
            assertEquals(4, pageableCaptor.getValue().getPageSize());
        }

        @Test
        void testFindForUser_LimitReachedBeforeLevel3_DoesNotQueryAnyLesson() {
            // Arrange
            when(lessonRepository.findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndLevelLessThanEqualAndIdNotIn(anyInt(), any(), any())).thenReturn(lessons(4));

            // Act
            service.findForUser(USER_ID, 1);

            // Assert
            verify(lessonRepository, never()).findByIdNotIn(any(), any());
        }

        @Test
        void testFindForUser_UsesOnlyFirstFavourite() {
            // Arrange
            when(user.getPreferences()).thenReturn(preferences(List.of(genre(1), genre(2), genre(3)), List.of(instrument(11), instrument(12))));

            // Act
            service.findForUser(USER_ID, 4);

            // Assert
            verify(lessonRepository).findByGenreIdInAndBelongsToGenreTrueAndLevelLessThanEqualAndIdNotIn(idsCaptor.capture(), anyInt(), any(), any());
            assertEquals(List.of(1L), List.copyOf(idsCaptor.getValue()));
            verify(lessonRepository).findByInstrumentIdInAndBelongsToInstrumentTrueAndLevelLessThanEqualAndIdNotIn(idsCaptor.capture(), anyInt(), any(), any());
            assertEquals(List.of(11L), List.copyOf(idsCaptor.getValue()));
        }
    }
}
