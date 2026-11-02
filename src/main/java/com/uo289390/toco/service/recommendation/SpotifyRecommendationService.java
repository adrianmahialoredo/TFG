package com.uo289390.toco.service.recommendation;

import com.uo289390.toco.dto.ItemDTO;
import com.uo289390.toco.dto.SongDTO;

import java.util.List;

/**
 * Service contract for managing Spotify music recommendations.
 *
 * Provides methods to fetch personalized music recommendations from the Spotify
 * API based on the user's top songs and artists. Nothing already in too many of the user's lists is recommended.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
public interface SpotifyRecommendationService {

    /**
     * Items of every type recommended from the user's Spotify: the songs of {@link #findSongsForUser}
     * plus lessons, instrument-genres and genres taken from the genres and instruments of those songs.
     *
     * @param id the application user ID
     * @return the recommended items, shuffled; empty if none of the Spotify songs is in the database
     */
    List<ItemDTO> findItemsForUser(Long id);

    /**
     * The user's top songs in Spotify that exist in the database, completed with songs
     * of their top artists when there are not enough.
     *
     * @param id    the application user ID
     * @param limit maximum number of songs; empty if it is not positive
     * @return the recommended songs, in priority order
     */
    List<SongDTO> findSongsForUser(Long id, int limit);
}
