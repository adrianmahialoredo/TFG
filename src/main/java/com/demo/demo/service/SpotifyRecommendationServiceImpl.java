package com.demo.demo.service;

import java.util.Map; 
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.demo.demo.repository.SpotifyUserRepository;
import com.demo.demo.model.SpotifyUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link SpotifyRecommendationService}.
 *
 * Retrieves personalized music recommendations from the Spotify API based on
 * the user's top artists.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SpotifyRecommendationServiceImpl implements SpotifyRecommendationService {

    /**
     * RestTemplate for making HTTP requests to external APIs.
     */
    private final RestTemplate restTemplate;

    /**
     * Repository for accessing Spotify user data from the database.
     */
    private final SpotifyUserRepository spotifyUserRepository;

    @Override
    public List<Map<String, Object>> getRecommendations(String spotifyUserId) {
        
        // Validar entrada
        if (spotifyUserId == null || spotifyUserId.isEmpty()) {
            log.error("Invalid Spotify user ID: {}", spotifyUserId);
            throw new IllegalArgumentException("Spotify user ID cannot be null or empty");
        }

        log.info("Fetching recommendations for Spotify user: {}", spotifyUserId);

        // Obtener el usuario de Spotify desde la base de datos
        SpotifyUser spotifyUser = spotifyUserRepository.findBySpotifyUserId(spotifyUserId)
                .orElseThrow(() -> {
                    log.error("Spotify user not found: {}", spotifyUserId);
                    return new RuntimeException("Usuario de Spotify no encontrado");
                });

        String accessToken = spotifyUser.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // 1. Obtener artistas top del usuario
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.spotify.com/v1/me/top/artists?limit=2",
                HttpMethod.GET, request, Map.class
        );

        List<Map<String, Object>> artists = (List<Map<String, Object>>)
                ((Map<String, Object>) response.getBody()).get("items");

        if (artists == null || artists.isEmpty()) {
            log.warn("No se encontraron artistas favoritos para el usuario con ID: {}", spotifyUserId);
            throw new RuntimeException("No se encontraron artistas del usuario");
        }

        String seedArtist = (String) artists.get(0).get("id");
        if (seedArtist == null || seedArtist.isEmpty()) {
            log.error("El ID del artista semilla está vacío o es nulo para el usuario con ID: {}", spotifyUserId);
            throw new RuntimeException("El ID del artista semilla no es válido");
        }

        // 2. Pedir recomendaciones
        String url = "https://api.spotify.com/v1/artists/" + seedArtist + "/top-tracks";
        log.info("Seed artist: {}", seedArtist);
        log.info("Requesting recommendations from Spotify API: {}", url);

        try {
            ResponseEntity<Map<String, Object>> recResponse = restTemplate.exchange(
                    url, HttpMethod.GET, request, (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (recResponse.getStatusCode().is2xxSuccessful() && recResponse.getBody() != null) {
                List<Map<String, Object>> recommendations = (List<Map<String, Object>>) recResponse.getBody().get("tracks");
                log.info("Successfully fetched {} recommendations for user: {}", 
                    recommendations != null ? recommendations.size() : 0, spotifyUserId);
                return recommendations;
            } else {
                log.error("Spotify API returned non-successful status: {} with body: {}",
                        recResponse.getStatusCode(), recResponse.getBody());
                throw new RuntimeException("Error fetching recommendations from Spotify API");
            }
        } catch (Exception e) {
            log.error("Error fetching recommendations from Spotify API: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching recommendations from Spotify API", e);
        }
    }

}

