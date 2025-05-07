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

@Slf4j
@RequiredArgsConstructor
@Service
public class SpotifyRecommendationService {

    private final RestTemplate restTemplate;
    private final SpotifyUserRepository spotifyUserRepository;


    /*
     * Método para obtener recomendaciones de Spotify.
     * Recibe el token de acceso del usuario y hace una llamada a la API de Spotify
     * para obtener recomendaciones basadas en los artistas favoritos del usuario.
     * 
     * @param accessToken El token de acceso del usuario.
     * @return Una lista de recomendaciones de canciones.
     * 
     * @throws RuntimeException Si no se encuentran artistas favoritos del usuario.
     * 
     */
    public List<Map<String, Object>> getRecommendations(String spotifyUserId) {

        // Obtener el usuario de Spotify desde la base de datos
        SpotifyUser spotifyUser = spotifyUserRepository.findBySpotifyUserId(spotifyUserId)
                .orElseThrow(() -> new RuntimeException("Usuario de Spotify no encontrado"));

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
            throw new RuntimeException("No se encontraron artistas del usuario");
        }

        String seedArtist = (String) artists.get(0).get("id");

        // 2. Pedir recomendaciones
        String url = "https://api.spotify.com/v1/recommendations?seed_artists=" + seedArtist + "&limit=10";
        log.info("Requesting recommendations from Spotify API: {}", url);

        try {
            ResponseEntity<Map<String, Object>> recResponse = restTemplate.exchange(
                    url, HttpMethod.GET, request, (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            return (List<Map<String, Object>>) recResponse.getBody().get("tracks");
        } catch (Exception e) {
            log.error("Error fetching recommendations from Spotify API: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching recommendations from Spotify API", e);
        }
    }

}
