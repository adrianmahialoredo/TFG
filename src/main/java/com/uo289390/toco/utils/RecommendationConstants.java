package com.uo289390.toco.utils;

import com.uo289390.toco.model.Preferences;

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
    public static final int RECOMMENDED_GENRES_COUNT = 1;
    public static final int RECOMMENDED_INSTRUMENT_GENRES_COUNT = 3;

    public static final int NUMBER_OF_MATCHING_INSTRUMENTS_IN_SONG_RECOMMENDATION = 3;

    public static final int NUMBER_OF_GENRES_USED_IN_GENRE_RECOMMENDATION = RECOMMENDED_GENRES_COUNT;
    public static final int NUMBER_OF_FAVOURITE_GENRES_USED_IN_GENRE_RECOMMENDATION = Preferences.MIN_NUMBER_OF_FAVOURITE_GENRES;

    public static final int NUMBER_OF_MAX_TOP_INSTRUMENTS_AND_GENRES_USED_IN_INSTRUMENT_GENRE_RECOMMENDATION = 3;
    public static final int NUMBER_OF_TOP_ARTISTS_TO_FETCH_FROM_SPOTIFY = 5;

    public static final int BY_GENRE_RECOMMENDED_LESSONS_COUNT = 8;
    public static final int BY_USER_RECOMMENDED_SONGS_COUNT = 12;
    public static final int NUMBER_OF_GENRES_USED_IN_BY_USER_SONG_RECOMMENDATION = 4;
    public static final int NUMBER_OF_INSTRUMENTS_USED_IN_BY_USER_SONG_RECOMMENDATION = 4;

    public static final int NUMBER_OF_FAVOURITE_GENRES_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION = 2;
    public static final int NUMBER_OF_ITEM_LISTS_GENRES_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION = 2;
    public static final int NUMBER_OF_FAVOURITE_INSTRUMENTS_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION = 2;
    public static final int NUMBER_OF_ITEM_LISTS_INSTRUMENTS_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION = 2;

    public static final int NUMBER_OF_ITEM_LISTS_GENRES_USED_IN_BY_USER_LESSON_RECOMMENDATION = 2;
    public static final int NUMBER_OF_ITEM_LISTS_INSTRUMENTS_USED_IN_BY_USER_LESSON_RECOMMENDATION = 2;

    public static final int HOME_RECOMMENDED_SONGS_COUNT = 6;
    public static final int HOME_RECOMMENDED_LESSONS_COUNT = 3;
    public static final int HOME_RECOMMENDED_INSTRUMENT_GENRES_COUNT = 2;
    public static final int HOME_RECOMMENDED_GENRES_COUNT = 1;
}
