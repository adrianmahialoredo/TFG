package com.demo.demo.utils;

import com.demo.demo.model.Preferences;

/**
 * Parameters of the item list recommendation algorithms.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-09-29
 */
public final class RecommendationConstants {

    public static final int RECOMMENDED_SONGS_COUNT = 10;
    public static final int RECOMMENDED_LESSONS_COUNT = 6;
    public static final int RECOMMENDED_GENRES_COUNT = 3;
    public static final int RECOMMENDED_INSTRUMENT_GENRES_COUNT = 1;

    public static final int NUMBER_OF_MATCHING_INSTRUMENTS_IN_SONG_RECOMMENDATION = 3;

    public static final int NUMBER_OF_GENRES_USED_IN_GENRE_RECOMMENDATION = RECOMMENDED_GENRES_COUNT;
    public static final int NUMBER_OF_FAVOURITE_GENRES_USED_IN_GENRE_RECOMMENDATION = Preferences.MIN_NUMBER_OF_FAVOURITE_GENRES;

    public static final int NUMBER_OF_MAX_TOP_INSTRUMENTS_AND_GENRES_USED_IN_INSTRUMENT_GENRE_RECOMMENDATION = 3;
}
