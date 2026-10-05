package com.demo.demo.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.util.*;
import org.springframework.web.client.RestTemplate;
import com.demo.demo.config.SpotifyProperties;
import com.demo.demo.model.SpotifyUser;
import com.demo.demo.model.User;
import com.demo.demo.repository.SpotifyUserRepository;
import com.demo.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link SpotifyAuthService}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpotifyAuthServiceImpl implements SpotifyAuthService {

    /**
     * Time the user has to complete the Spotify login before the {@code state} expires.
     */
    private static final Duration STATE_TTL = Duration.ofMinutes(10);

    /**
     * Access tokens are refreshed this long before they expire, so they never expire mid-request.
     */
    private static final Duration TOKEN_REFRESH_MARGIN = Duration.ofMinutes(1);

    private static final String SPOTIFY_TOKEN_URL = "https://accounts.spotify.com/api/token";

    private final RestTemplate restTemplate;
    private final SpotifyUserRepository spotifyUserRepository;
    private final UserRepository userRepository;
    private final SpotifyProperties spotifyProperties;

    /**
     * Pending authorizations: state -> user that started the login. Kept in memory (single instance).
     */
    private final Map<String, PendingAuthorization> pendingAuthorizations = new ConcurrentHashMap<>();

    private record PendingAuthorization(String username, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    @Override
    public String buildAuthorizationUrl(String username) {
        pendingAuthorizations.values().removeIf(PendingAuthorization::isExpired);

        String state = UUID.randomUUID().toString();
        pendingAuthorizations.put(state, new PendingAuthorization(username, Instant.now().plus(STATE_TTL)));

        return "https://accounts.spotify.com/authorize?" +
                "client_id=" + spotifyProperties.getClientId() +
                "&response_type=code" +
                "&redirect_uri=" + URLEncoder.encode(spotifyProperties.getRedirectUri(), StandardCharsets.UTF_8) + // Uso de StandardCharsets
                "&scope=user-read-email%20user-top-read" +
                "&state=" + state;
    }

    @Override
    @Transactional
    public void handleCallback(String code, String state) {
        // Validar código
        if (code == null || code.isEmpty()) {
            log.error("Empty authorization code received from Spotify");
            throw new IllegalArgumentException("Authorization code cannot be empty");
        }

        // Recuperar (y consumir) el usuario que inició el login a partir del state
        String username = Optional.ofNullable(state)
                .map(pendingAuthorizations::remove)
                .filter(pending -> !pending.isExpired())
                .map(PendingAuthorization::username)
                .orElseThrow(() -> {
                    log.error("Unknown or expired Spotify state received");
                    return new IllegalArgumentException("Invalid or expired Spotify authorization. Please try again.");
                });

        // Validar que el usuario exista en la aplicación
        User appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found for username: {}", username);
                    return new IllegalArgumentException("User not found in application");
                });

        log.info("Processing Spotify authorization callback for user: {}", username);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(spotifyProperties.getClientId(), spotifyProperties.getClientSecret());

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", spotifyProperties.getRedirectUri());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    SPOTIFY_TOKEN_URL, HttpMethod.POST, request, (Class<Map<String, Object>>) (Class<?>) Map.class);

            Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                    .filter(tokenBody -> tokenBody.get("access_token") != null)
                    .orElseThrow(() -> {
                        log.error("Failed to obtain access token from Spotify");
                        return new IllegalStateException("Error al obtener los tokens de Spotify");
                    });

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

            String spotifyUserId = Optional.ofNullable(userResponse.getBody())
                    .map(userBody -> (String) userBody.get("id"))
                    .orElseThrow(() -> {
                        log.error("Failed to retrieve Spotify user ID from API");
                        return new IllegalStateException("Error al obtener el ID del usuario de Spotify");
                    });

            log.info("Successfully retrieved Spotify user ID: {}", spotifyUserId);
            
            // Una cuenta de Spotify solo puede estar vinculada a un usuario (spotifyUserId es único):
            // si ya lo estaba a otro, se le desvincula y gana la vinculación más reciente.
            unlinkFromOtherUser(spotifyUserId, appUser);

            // Reutilizar el SpotifyUser de este usuario si ya tenía uno (aunque fuera de otra cuenta de Spotify)
            SpotifyUser spotifyUser = Optional.ofNullable(appUser.getSpotifyUser())
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

    /**
     * Frees a Spotify account that is linked to a different application user (or to none), so it
     * can be linked to {@code appUser}.
     *
     * The record cannot be moved from one user to another: {@code User.spotifyUser} has
     * {@code orphanRemoval}, so unlinking it deletes it. The flush is required because Hibernate
     * runs inserts before deletes, and the new record reuses the same unique {@code spotifyUserId}.
     */
    private void unlinkFromOtherUser(String spotifyUserId, User appUser) {
        spotifyUserRepository.findBySpotifyUserId(spotifyUserId)
                .filter(existing -> existing.getUser() == null || !existing.getUser().getId().equals(appUser.getId()))
                .ifPresent(existing -> {
                    Optional.ofNullable(existing.getUser()).ifPresentOrElse(
                            previousUser -> {
                                log.info("Spotify user {} was linked to application user {}, unlinking it",
                                        spotifyUserId, previousUser.getUsername());
                                previousUser.setSpotifyUser(null);
                                userRepository.save(previousUser);
                            },
                            () -> spotifyUserRepository.delete(existing));
                    spotifyUserRepository.flush();
                });
    }

    /**
     * Runs in its own transaction: callers (e.g. the recommendation services) are read-only
     * transactions, where Hibernate does not flush and the refreshed token would be lost.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String getValidAccessToken(Long userId) {
        SpotifyUser spotifyUser = spotifyUserRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("Spotify user not found for user: {}", userId);
                    return new EntityNotFoundException("Usuario de Spotify no encontrado");
                });

        boolean stillValid = Optional.ofNullable(spotifyUser.getTokenExpiration())
                .map(expiration -> Instant.now().plus(TOKEN_REFRESH_MARGIN).isBefore(expiration))
                .orElse(false);

        if (stillValid) {
            return spotifyUser.getAccessToken();
        }

        log.info("Spotify access token expired for user {}, refreshing it", userId);
        refreshAccessToken(spotifyUser);
        spotifyUserRepository.save(spotifyUser);
        return spotifyUser.getAccessToken();
    }

    /**
     * Asks Spotify for a new access token using the refresh token and stores it in the entity.
     * Spotify may also return a new refresh token; if it does, it replaces the old one.
     */
    private void refreshAccessToken(SpotifyUser spotifyUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(spotifyProperties.getClientId(), spotifyProperties.getClientSecret());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", spotifyUser.getRefreshToken());

        Map<String, Object> responseBody;
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    SPOTIFY_TOKEN_URL, HttpMethod.POST, new HttpEntity<>(body, headers),
                    (Class<Map<String, Object>>) (Class<?>) Map.class);
            responseBody = response.getBody();
        } catch (HttpClientErrorException e) {
            // 400 invalid_grant: the user revoked the access or the refresh token is no longer valid
            log.error("Spotify rejected the token refresh for Spotify user {}: {}", spotifyUser.getSpotifyUserId(), e.getResponseBodyAsString());
            throw new IllegalStateException("La conexión con Spotify ha caducado. Vuelve a vincular tu cuenta.", e);
        }

        String accessToken = Optional.ofNullable(responseBody)
                .map(tokenBody -> (String) tokenBody.get("access_token"))
                .orElseThrow(() -> {
                    log.error("Spotify token refresh response without access token");
                    return new IllegalStateException("Error al renovar el token de Spotify");
                });
        int expiresIn = ((Number) responseBody.get("expires_in")).intValue();

        spotifyUser.setAccessToken(accessToken);
        spotifyUser.setTokenExpiration(Instant.now().plusSeconds(expiresIn));
        Optional.ofNullable((String) responseBody.get("refresh_token"))
                .ifPresent(spotifyUser::setRefreshToken);

        log.debug("Spotify access token refreshed for Spotify user {}", spotifyUser.getSpotifyUserId());
    }
}
