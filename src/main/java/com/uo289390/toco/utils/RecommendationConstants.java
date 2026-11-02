package com.uo289390.toco.utils;

import com.uo289390.toco.model.Preferences;

/**
 * Parameters of the item list recommendation algorithms.
 *
 * Naming convention (N = number of):
 * - {@code N_[CONTEXT_]RECOMMENDED_<ITEMS>}: how many items a recommendation returns.
 * - {@code N_<ITEMS>_USED_IN_[CONTEXT_]<TARGET>_RECOMMENDATION}: how many items an algorithm uses to recommend.
 * - {@code DEFAULT_N_...} / {@code MAX_N_...}: default and maximum values of a request parameter.
 *
 * Without context the recommendation is for an item list (or the generic one of its type).
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-09-29
 */
public final class RecommendationConstants {

    public static final int N_ITEM_LISTS_TO_EXCLUDE_FROM_RECOMMENDATIONS = 3;

    // Item list recommendations
    public static final int N_RECOMMENDED_SONGS = 10;
    public static final int N_RECOMMENDED_LESSONS = 6;
    public static final int N_RECOMMENDED_GENRES = 1;
    public static final int N_RECOMMENDED_INSTRUMENT_GENRES = 3;

    public static final int N_MATCHING_INSTRUMENTS_USED_IN_SONG_RECOMMENDATION = 3;

    public static final int N_GENRES_USED_IN_GENRE_RECOMMENDATION = N_RECOMMENDED_GENRES;
    public static final int N_FAVOURITE_GENRES_USED_IN_GENRE_RECOMMENDATION = Preferences.MIN_N_FAVOURITE_GENRES;

    public static final int N_MAX_TOP_INSTRUMENTS_AND_GENRES_USED_IN_INSTRUMENT_GENRE_RECOMMENDATION = 3;

    // By genre and by user recommendations
    public static final int N_BY_GENRE_RECOMMENDED_LESSONS = 8;
    public static final int N_BY_USER_RECOMMENDED_SONGS = 12;
    public static final int N_GENRES_USED_IN_BY_USER_SONG_RECOMMENDATION = 4;
    public static final int N_INSTRUMENTS_USED_IN_BY_USER_SONG_RECOMMENDATION = 4;

    public static final int N_FAVOURITE_GENRES_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION = 2;
    public static final int N_ITEM_LISTS_GENRES_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION = 2;
    public static final int N_FAVOURITE_INSTRUMENTS_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION = 2;
    public static final int N_ITEM_LISTS_INSTRUMENTS_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION = 2;

    public static final int N_ITEM_LISTS_GENRES_USED_IN_BY_USER_LESSON_RECOMMENDATION = 2;
    public static final int N_ITEM_LISTS_INSTRUMENTS_USED_IN_BY_USER_LESSON_RECOMMENDATION = 2;

    // Home recommendations
    public static final int N_HOME_RECOMMENDED_SONGS = 6;
    public static final int N_HOME_RECOMMENDED_LESSONS = 3;
    public static final int N_HOME_RECOMMENDED_INSTRUMENT_GENRES = 2;
    public static final int N_HOME_RECOMMENDED_GENRES = 1;

    /** Candidates for the Home "Prueba esta combinación" card (the app shows the first one not already on the Home). */
    public static final int N_FAVOURITE_INSTRUMENT_GENRE_COMBINATIONS = 5;

    /** Candidates for the Home "Explora este género" card (the app shows the first one not already on the Home). */
    public static final int N_MOST_USED_GENRES_IN_ITEM_LISTS = 5;

    // Favourite artists (the most repeated artists of the songs in the user's lists) and their songs
    public static final int DEFAULT_N_TOP_ARTISTS = 5;
    public static final int MAX_N_TOP_ARTISTS = 10;
    public static final int MAX_N_ARTISTS_IN_SONGS_BY_ARTISTS = 10;
    public static final int DEFAULT_N_SONGS_BY_ARTISTS = 10;
    public static final int MAX_N_SONGS_BY_ARTISTS = 50;

    // Spotify recommendations (the user's top songs and artists in Spotify)
    public static final int N_SPOTIFY_RECOMMENDED_SONGS = 6;
    public static final int N_SPOTIFY_RECOMMENDED_LESSONS = 3;
    public static final int N_SPOTIFY_RECOMMENDED_INSTRUMENT_GENRES = 2;
    public static final int N_SPOTIFY_RECOMMENDED_GENRES = 1;

    public static final int N_TOP_ARTISTS_USED_IN_SPOTIFY_SONG_RECOMMENDATION = 5;
    public static final int N_TOP_GENRES_USED_IN_SPOTIFY_INSTRUMENT_GENRE_RECOMMENDATION = 5;
    public static final int N_TOP_INSTRUMENTS_USED_IN_SPOTIFY_INSTRUMENT_GENRE_RECOMMENDATION = 5;
}
