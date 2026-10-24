package com.uo289390.toco.utils;

import static com.uo289390.toco.utils.RecommendationConstants.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.uo289390.toco.model.Preferences;

/**
 * Sanity checks for {@link RecommendationConstants}: every parameter is a positive int constant and
 * the derived/related parameters keep the relations the recommendation algorithms rely on.
 */
class RecommendationConstantsTest {

    private static List<Field> constants() {
        return Arrays.stream(RecommendationConstants.class.getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .toList();
    }

    @Test
    void class_isFinal() {
        assertTrue(Modifier.isFinal(RecommendationConstants.class.getModifiers()));
    }

    @Test
    void everyConstant_isPublicStaticFinalPositiveInt() throws IllegalAccessException {
        assertFalse(constants().isEmpty());
        for (Field field : constants()) {
            int modifiers = field.getModifiers();
            assertTrue(Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers), field.getName());
            assertEquals(int.class, field.getType(), field.getName());
            assertTrue(field.getInt(null) > 0, field.getName() + " must be positive");
        }
    }

    @Test
    void derivedConstants_followTheirSource() {
        assertEquals(RECOMMENDED_GENRES_COUNT, NUMBER_OF_GENRES_USED_IN_GENRE_RECOMMENDATION);
        assertEquals(Preferences.MIN_NUMBER_OF_FAVOURITE_GENRES, NUMBER_OF_FAVOURITE_GENRES_USED_IN_GENRE_RECOMMENDATION);
    }

    @Test
    void favouriteBasedParameters_doNotExceedTheMinimumAUserMustChoose() {
        // A user always has at least the minimum number of favourites, so these slices never come up short
        assertTrue(NUMBER_OF_FAVOURITE_GENRES_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION
                <= Preferences.MIN_NUMBER_OF_FAVOURITE_GENRES);
        assertTrue(NUMBER_OF_FAVOURITE_INSTRUMENTS_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION
                <= Preferences.MIN_NUMBER_OF_FAVOURITE_INSTRUMENTS);
    }

    @Test
    void homeCounts_areSmallerThanFullRecommendationCounts() {
        assertTrue(HOME_RECOMMENDED_SONGS_COUNT <= RECOMMENDED_SONGS_COUNT);
        assertTrue(HOME_RECOMMENDED_LESSONS_COUNT <= RECOMMENDED_LESSONS_COUNT);
        assertTrue(HOME_RECOMMENDED_INSTRUMENT_GENRES_COUNT <= RECOMMENDED_INSTRUMENT_GENRES_COUNT);
        assertTrue(HOME_RECOMMENDED_GENRES_COUNT <= RECOMMENDED_GENRES_COUNT);
    }

    @Test
    void homeCardCandidates_leaveRoomToSkipItemsAlreadyOnHome() {
        assertTrue(FAVOURITE_INSTRUMENT_GENRE_COMBINATIONS_COUNT > HOME_RECOMMENDED_INSTRUMENT_GENRES_COUNT);
        assertTrue(MOST_USED_GENRES_IN_ITEM_LISTS_COUNT > HOME_RECOMMENDED_GENRES_COUNT);
    }
}
