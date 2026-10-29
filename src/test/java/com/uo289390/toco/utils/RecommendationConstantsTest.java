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
        assertEquals(N_RECOMMENDED_GENRES, N_GENRES_USED_IN_GENRE_RECOMMENDATION);
        assertEquals(Preferences.MIN_N_FAVOURITE_GENRES, N_FAVOURITE_GENRES_USED_IN_GENRE_RECOMMENDATION);
    }

    @Test
    void favouriteBasedParameters_doNotExceedTheMinimumAUserMustChoose() {
        // A user always has at least the minimum number of favourites, so these slices never come up short
        assertTrue(N_FAVOURITE_GENRES_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION
                <= Preferences.MIN_N_FAVOURITE_GENRES);
        assertTrue(N_FAVOURITE_INSTRUMENTS_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION
                <= Preferences.MIN_N_FAVOURITE_INSTRUMENTS);
    }

    @Test
    void homeCounts_areSmallerThanFullRecommendationCounts() {
        assertTrue(N_HOME_RECOMMENDED_SONGS <= N_RECOMMENDED_SONGS);
        assertTrue(N_HOME_RECOMMENDED_LESSONS <= N_RECOMMENDED_LESSONS);
        assertTrue(N_HOME_RECOMMENDED_INSTRUMENT_GENRES <= N_RECOMMENDED_INSTRUMENT_GENRES);
        assertTrue(N_HOME_RECOMMENDED_GENRES <= N_RECOMMENDED_GENRES);
    }

    @Test
    void homeCardCandidates_leaveRoomToSkipItemsAlreadyOnHome() {
        assertTrue(N_FAVOURITE_INSTRUMENT_GENRE_COMBINATIONS > N_HOME_RECOMMENDED_INSTRUMENT_GENRES);
        assertTrue(N_MOST_USED_GENRES_IN_ITEM_LISTS > N_HOME_RECOMMENDED_GENRES);
    }
}
