package com.uo289390.toco.controller;

import com.uo289390.toco.dto.ItemDTO;
import com.uo289390.toco.dto.SongDTO;
import com.uo289390.toco.service.SpotifyRecommendationService;
import com.uo289390.toco.exception.GlobalExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Responses: 200 OK. Errors are translated by {@link GlobalExceptionHandler}.
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
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#userId, authentication)")
    public ResponseEntity<List<ItemDTO>> getItemsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(spotifyRecommendationService.findItemsForUser(userId));
    }

    @GetMapping("/recommendations/songs/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#userId, authentication)")
    public ResponseEntity<List<SongDTO>> getSongsForUser(@PathVariable Long userId, @RequestParam int limit) {
        return ResponseEntity.ok(spotifyRecommendationService.findSongsForUser(userId,  limit));
    }
}
