package com.demo.demo.service;

import com.demo.demo.dto.ItemDTO;
import com.demo.demo.dto.SongDTO;

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

    List<ItemDTO> findItemsForUser(Long id);

    List<SongDTO> findSongsForUser(Long id, int limit);
}
