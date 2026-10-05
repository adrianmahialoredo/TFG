package com.demo.demo.controller;

import com.demo.demo.dto.ItemDTO;
import com.demo.demo.dto.SongDTO;
import com.demo.demo.service.SpotifyRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the music recommendations obtained from Spotify.
 *
 * Base path: {@code /api/spotify}
 *
 * Endpoints:
 * - GET    /api/spotify/recommendations/{spotifyUserId} Retrieve song recommendations for a Spotify user
 *
 * The Spotify login flow lives in {@link SpotifyAuthController}.
 *
 * Responses: 200 OK. Errors are translated by {@link com.demo.demo.exception.GlobalExceptionHandler}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
public class SpotifyRecommendationController {

    private final SpotifyRecommendationService spotifyRecommendationService;

    @GetMapping("/recommendations/items/{userId}")
    public ResponseEntity<List<ItemDTO>> getItemsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(spotifyRecommendationService.findItemsForUser(userId));
    }

    @GetMapping("/recommendations/songs/{userId}")
    public ResponseEntity<List<SongDTO>> getSongsForUser(@PathVariable Long userId, @RequestParam int limit) {
        return ResponseEntity.ok(spotifyRecommendationService.findSongsForUser(userId,  limit));
    }
}
