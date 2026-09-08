package com.demo.demo.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.*;
import org.springframework.web.client.RestTemplate;
import com.demo.demo.config.SpotifyProperties;
import com.demo.demo.model.SpotifyUser;
import com.demo.demo.model.User;
import com.demo.demo.repository.SpotifyUserRepository;
import com.demo.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link SpotifyAuthService}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Slf4j
@AllArgsConstructor
@Service
public class SpotifyAuthServiceImpl implements SpotifyAuthService {

    private final RestTemplate restTemplate;
    private final SpotifyUserRepository spotifyUserRepository;
    private final UserRepository userRepository;
    private final SpotifyProperties spotifyProperties; 

    @Override
    public String buildAuthorizationUrl() {
        return "https://accounts.spotify.com/authorize?" +
                "client_id=" + spotifyProperties.getClientId() +
                "&response_type=code" +
                "&redirect_uri=" + URLEncoder.encode(spotifyProperties.getRedirectUri(), StandardCharsets.UTF_8) + // Uso de StandardCharsets
                "&scope=user-read-email%20user-top-read";
    }

    @Override
    public void handleCallback(String code, String username) {
        // Validar código
        if (code == null || code.isEmpty()) {
            log.error("Empty authorization code received from Spotify");
            throw new IllegalArgumentException("Authorization code cannot be empty");
        }

        // Validar que el usuario exista en la aplicación
        User appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found for username: {}", username);
                    return new IllegalArgumentException("User not found in application");
                });

        log.info("Processing Spotify authorization callback for user: {}", username);

        try {
            String url = "https://accounts.spotify.com/api/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(spotifyProperties.getClientId(), spotifyProperties.getClientSecret());

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", spotifyProperties.getRedirectUri());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, (Class<Map<String, Object>>) (Class<?>) Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || responseBody.get("access_token") == null) {
                log.error("Failed to obtain access token from Spotify");
                throw new RuntimeException("Error al obtener los tokens de Spotify");
            }

            String accessToken = (String) responseBody.get("access_token");
            String refreshToken = (String) responseBody.get("refresh_token");
            int expiresIn = (int) responseBody.get("expires_in");

            log.debug("Successfully obtained access token from Spotify");

            // Obtener el user_id de Spotify
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.set("Authorization", "Bearer " + accessToken);
            HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                    "https://api.spotify.com/v1/me", HttpMethod.GET, userRequest, (Class<Map<String, Object>>) (Class<?>) Map.class);

            Map<String, Object> userBody = userResponse.getBody();
            if (userBody == null || userBody.get("id") == null) {
                log.error("Failed to retrieve Spotify user ID from API");
                throw new RuntimeException("Error al obtener el ID del usuario de Spotify");
            }
            String spotifyUserId = (String) userBody.get("id");

            log.info("Successfully retrieved Spotify user ID: {}", spotifyUserId);
            
            // Buscar si ya existe un SpotifyUser para este Spotify ID
            SpotifyUser spotifyUser = spotifyUserRepository.findBySpotifyUserId(spotifyUserId)
                    .orElseGet(SpotifyUser::new);

            spotifyUser.setSpotifyUserId(spotifyUserId);
            spotifyUser.setAccessToken(accessToken);
            spotifyUser.setRefreshToken(refreshToken);
            spotifyUser.setTokenExpiration(Instant.now().plusSeconds(expiresIn));
            
            // Asociar el SpotifyUser con el usuario autenticado de la aplicación
            spotifyUser.setUser(appUser);
            appUser.setSpotifyUser(spotifyUser);

            spotifyUserRepository.save(spotifyUser);
            userRepository.save(appUser);
            log.info("Spotify user {} linked to application user {} successfully", spotifyUserId, username);

        } catch (IllegalArgumentException e) {
            log.error("Invalid argument in Spotify callback: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error handling Spotify callback: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing Spotify authorization", e);
        }
    }

}

