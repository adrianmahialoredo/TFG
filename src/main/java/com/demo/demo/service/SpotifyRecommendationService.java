package com.demo.demo.service;

import java.util.List;
import java.util.Map;

/**
 * Service contract for managing Spotify music recommendations.
 *
 * Provides methods to fetch personalized music recommendations from the Spotify
 * API based on the user's favorite artists.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
public interface SpotifyRecommendationService {

    /**
     * Retrieves music recommendations from Spotify based on the user's top artists.
     *
     * @param spotifyUserId The unique identifier of the Spotify user
     * @return A list of maps containing song recommendation data from Spotify
     */
    List<Map<String, Object>> getRecommendations(String spotifyUserId);
}
