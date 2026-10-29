package com.uo289390.toco.controller;

import com.uo289390.toco.dto.ItemDTO;
import com.uo289390.toco.dto.SongDTO;
import com.uo289390.toco.service.recommendation.SpotifyRecommendationService;
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
 * - GET    /api/spotify/recommendations/items/{userId}           Retrieve items of every type recommended from the user's Spotify
 * - GET    /api/spotify/recommendations/songs/{userId}?limit={n} Retrieve the songs recommended from the user's Spotify
 *
 * {@code userId} is the application user ID, who must have a linked Spotify account.
 * The Spotify login flow lives in {@link SpotifyAuthController}.
 *
 * Responses: 200 OK (an empty list if none of the user's Spotify songs is in the database),
 * 403 Forbidden for another user. Errors are translated by {@link GlobalExceptionHandler}.
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
