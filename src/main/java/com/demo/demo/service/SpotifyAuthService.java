package com.demo.demo.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets; // Importación corregida
import java.time.Instant;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.*;
import org.springframework.web.client.RestTemplate;
import com.demo.demo.config.SpotifyProperties;
import com.demo.demo.model.SpotifyUser;
import com.demo.demo.repository.SpotifyUserRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class SpotifyAuthService {


    private final RestTemplate restTemplate;
    private final SpotifyUserRepository userRepository;
    private final SpotifyProperties spotifyProperties; 

    /**
     * Build the authorization URL for Spotify.
     * 
     * This method constructs the URL that the user will be redirected to in order to authorize
     * the application to access their Spotify account. It includes the client ID, response type,
     * redirect URI, and required scopes.
     * 
     * @return The authorization URL as a String.
     */
    public String buildAuthorizationUrl() {

        return "https://accounts.spotify.com/authorize?" +
                "client_id=" + spotifyProperties.getClientId() +
                "&response_type=code" +
                "&redirect_uri=" + URLEncoder.encode(spotifyProperties.getRedirectUri(), StandardCharsets.UTF_8) + // Uso de StandardCharsets
                "&scope=user-read-email%20user-top-read";

    }

    /**
     * Manage the callback from Spotify after user authorization.
     * 
     * This method is called when Spotify redirects the user back to the application
     * with an authorization code. The method exchanges the code for an access token and a refresh token,
     * and retrieves the user's Spotify ID.
     * 
     * It uses the access token to make a request to the Spotify API to get the user's ID,
     * and then saves or updates the user's information in the database.
     * 
     * @param code The authorization code received from Spotify after user authorization.
     * @throws RuntimeException if there is an error obtaining the tokens or user ID from Spotify.
     */
    public void handleCallback(String code) {

        try {

            String url = "https://accounts.spotify.com/api/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // Uso de MediaType
            headers.setBasicAuth(spotifyProperties.getClientId(), spotifyProperties.getClientSecret());

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", spotifyProperties.getRedirectUri());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange( // Tipos genéricos corregidos
                    url, HttpMethod.POST, request, (Class<Map<String, Object>>) (Class<?>) Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || responseBody.get("access_token") == null) {
                throw new RuntimeException("Error al obtener los tokens de Spotify");
            }

            String accessToken = (String) responseBody.get("access_token");
            String refreshToken = (String) responseBody.get("refresh_token");
            int expiresIn = (int) responseBody.get("expires_in");

            // Ahora obtenemos el user_id de Spotify
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.set("Authorization", "Bearer " + accessToken);
            HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange( // Tipos genéricos corregidos
                    "https://api.spotify.com/v1/me", HttpMethod.GET, userRequest, (Class<Map<String, Object>>) (Class<?>) Map.class);

            Map<String, Object> userBody = userResponse.getBody();
            if (userBody == null || userBody.get("id") == null) { // Verificación nula
                throw new RuntimeException("Error al obtener el ID del usuario de Spotify");
            }
            String spotifyUserId = (String) userBody.get("id");

            // Guardar o actualizar el usuario
            SpotifyUser user = userRepository.findBySpotifyUserId(spotifyUserId)
                    .orElseGet(SpotifyUser::new);

            user.setSpotifyUserId(spotifyUserId);
            user.setAccessToken(accessToken);
            user.setRefreshToken(refreshToken);
            user.setTokenExpiration(Instant.now().plusSeconds(expiresIn));

            userRepository.save(user);
        
        } catch (Exception e) {
            throw new RuntimeException("Error al manejar el callback de Spotify", e);
        }
    }
    
}
