package com.demo.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.demo.service.SpotifyAuthService;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/spotify")
public class SpotifyAuthController {

    private final SpotifyAuthService spotifyAuthService;

    /*
     *  Endpoint para iniciar el flujo de autenticación con Spotify.
     *  Redirige al usuario a la página de inicio de sesión de Spotify.
     */
    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException, java.io.IOException {
        response.sendRedirect(spotifyAuthService.buildAuthorizationUrl());
    }

    /*
     * Endpoint de callback para manejar la respuesta de Spotify después de la autenticación.
     * Recibe el código de autorización y lo utiliza para obtener los tokens de acceso y actualización.
     * Guarda los tokens en la base de datos.
     * 
     * @param code El código de autorización recibido de Spotify.
     * @return Respuesta con un mensaje de éxito.
     */
    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam("code") String code) {
        spotifyAuthService.handleCallback(code);
        return ResponseEntity.ok("¡Login con Spotify completado y tokens guardados!");
    }
}
