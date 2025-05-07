package com.demo.demo.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets; // Importación corregida
import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType; // Importación corregida
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.demo.demo.config.SpotifyProperties;
import com.demo.demo.model.SpotifyUser;
import com.demo.demo.repository.SpotifyUserRepository;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Value;

@AllArgsConstructor
@Service
public class SpotifyAuthService {


    private final RestTemplate restTemplate;
    private final SpotifyUserRepository userRepository;
    private final SpotifyProperties spotifyProperties; // Asegúrate de que esta clase esté correctamente configurada

    /*
     * Genera la URL de autorización para Spotify.
     * Esta URL se utiliza para redirigir al usuario a la página de inicio de sesión de Spotify.
     * El usuario debe iniciar sesión y autorizar la aplicación para acceder a su información.
     * Una vez que el usuario autoriza la aplicación, Spotify redirige al usuario de vuelta a la URL de redirección especificada,
     * con un código de autorización.
     * Este código se puede usar para obtener un token de acceso y un token de actualización.
     * 
     * @return La URL de autorización de Spotify.
     * 
     */
    public String buildAuthorizationUrl() {

        return "https://accounts.spotify.com/authorize?" +
                "client_id=" + spotifyProperties.getClientId() +
                "&response_type=code" +
                "&redirect_uri=" + URLEncoder.encode(spotifyProperties.getRedirectUri(), StandardCharsets.UTF_8) + // Uso de StandardCharsets
                "&scope=user-read-email%20user-top-read";

    }

    /*
     * Maneja el callback de Spotify después de que el usuario autoriza la aplicación.
     * Este método se llama con el código de autorización que Spotify envía a la URL de redirección.
     * Utiliza este código para solicitar un token de acceso y un token de actualización.
     * Después de obtener los tokens, también obtiene el ID del usuario de Spotify.
     * Finalmente, guarda o actualiza la información del usuario en la base de datos.
     * 
     * @param code El código de autorización recibido de Spotify.
     * 
     * @throws RuntimeException Si hay un error al obtener los tokens o el ID del usuario.
     * @throws NullPointerException Si el cuerpo de la respuesta es nulo o si no se encuentra el ID del usuario.
     * @throws ClassCastException Si hay un error al convertir el cuerpo de la respuesta a un mapa.
     * @throws IllegalStateException Si hay un error al obtener el ID del usuario de Spotify.
     * @throws Exception Si hay un error inesperado al manejar el callback.
     * 
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
    
}catch (Exception e) {
        throw new RuntimeException("Error al manejar el callback de Spotify", e);
    }
}
}
