package com.demo.demo.service;

/**
 * Service contract for the Spotify OAuth2 authorization flow.
 *
 * Builds the authorization URL and handles the callback exchange to link a
 * Spotify account with an application user.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
public interface SpotifyAuthService {

    /**
     * Build the authorization URL for Spotify.
     *
     * @return The authorization URL as a String.
     */
    String buildAuthorizationUrl();

    /**
     * Manage the callback from Spotify after user authorization.
     *
     * @param code     The authorization code received from Spotify.
     * @param username The username of the authenticated application user linking Spotify.
     */
    void handleCallback(String code, String username);
}
