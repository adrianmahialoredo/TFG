package com.uo289390.toco.service;

import com.uo289390.toco.dto.ItemDTO;
import com.uo289390.toco.dto.SongDTO;

import java.util.List;

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
