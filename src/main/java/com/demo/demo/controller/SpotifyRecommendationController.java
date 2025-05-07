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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/spotify")
public class SpotifyRecommendationController {

    private final SpotifyRecommendationService recommendationService;

    @GetMapping("/recommendations/{spotifyUserId}")
    public ResponseEntity<?> getRecommendations(@PathVariable String spotifyUserId) {
        List<Map<String, Object>> recs = recommendationService.getRecommendations(spotifyUserId);
        return ResponseEntity.ok(recs);
    }
}
