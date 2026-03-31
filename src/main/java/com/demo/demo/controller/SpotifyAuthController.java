package com.demo.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.demo.service.SpotifyAuthService;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Controller for managing Spotify OAuth2 authentication.
 *
 * Provides endpoints to initiate Spotify login flow and handle the OAuth2 callback.
 * Users are redirected to Spotify's authorization page and then back to the callback
 * endpoint with an authorization code.
 *
 * Endpoints:
 * - GET /api/spotify/login: Initiate Spotify login flow
 * - GET /api/spotify/callback: Handle Spotify OAuth2 callback
 *
 * This controller uses the SpotifyAuthService to handle the authentication logic
 * and token management.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/spotify")
public class SpotifyAuthController {

    /**
     * Service for handling Spotify authentication operations.
     */
    private final SpotifyAuthService spotifyAuthService;

    /**
     * Initiates the Spotify OAuth2 authentication flow.
     *
     * Redirects the user to Spotify's authorization page where they can
     * grant the application permission to access their Spotify account.
     *
     * @param response The HTTP response to redirect the user
     * @throws IOException if an I/O error occurs during the redirect
     * @throws java.io.IOException if an I/O error occurs during the redirect
     */
    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException, java.io.IOException {
        response.sendRedirect(spotifyAuthService.buildAuthorizationUrl());
    }

    /**
     * Handles the Spotify OAuth2 authorization callback.
     *
     * This endpoint is called by Spotify after the user authorizes the application.
     * It receives an authorization code that is exchanged for access and refresh tokens.
     * The tokens are then associated with the authenticated application user and saved to the database.
     *
     * @param code The authorization code received from Spotify
     * @return ResponseEntity with a success message
     */
    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam("code") String code) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("User not authenticated. Please login first.");
        }
        
        String username = authentication.getName();
        spotifyAuthService.handleCallback(code, username);
        return ResponseEntity.ok("¡Login con Spotify completado y tokens guardados!");
    }
}
