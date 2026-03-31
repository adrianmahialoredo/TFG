package com.demo.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.demo.demo.service.SpotifyRecommendationService;

import lombok.RequiredArgsConstructor;

/**
 * Controller for managing Spotify music recommendations.
 *
 * Provides endpoints to retrieve personalized music recommendations from Spotify
 * based on the user's favorite artists and listening history.
 *
 * Endpoints:
 * - GET /api/spotify/recommendations/{spotifyUserId}: Fetch song recommendations for a Spotify user
 *
 * This controller uses the SpotifyRecommendationService to fetch recommendations
 * from the Spotify Web API.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/spotify")
public class SpotifyRecommendationController {

    /**
     * Service for fetching Spotify recommendations.
     */
    private final SpotifyRecommendationService recommendationService;

    /**
     * Retrieves music recommendations for a Spotify user.
     *
     * Fetches personalized song recommendations based on the user's top artists
     * and listening history from Spotify.
     *
     * @param spotifyUserId The unique identifier of the Spotify user
     * @return ResponseEntity containing a list of recommended songs
     */
    @GetMapping("/recommendations/{spotifyUserId}")
    public ResponseEntity<?> getRecommendations(@PathVariable String spotifyUserId) {
        List<Map<String, Object>> recs = recommendationService.getRecommendations(spotifyUserId);
        return ResponseEntity.ok(recs);
    }
}
