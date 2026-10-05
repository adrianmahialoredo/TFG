package com.demo.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Configuration properties for Spotify API integration.
 *
 * This class is responsible for binding Spotify-related configuration properties
 * from the application properties file to provide the necessary credentials
 * and URI information for OAuth2 authentication with Spotify.
 *
 * Properties are read from the application.properties or application.yml file
 * with the prefix "spotify".
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Data
@ConfigurationProperties(prefix = "spotify")
@Validated
public class SpotifyProperties {

    /**
     * The Spotify API client ID used for OAuth2 authentication.
     */
    @NotBlank(message = "Spotify client ID cannot be blank")
    private final String clientId;

    /**
     * The Spotify API client secret used for OAuth2 authentication.
     * This should be kept confidential and not exposed in client-side code.
     */
    @NotBlank(message = "Spotify client secret cannot be blank")
    private final String clientSecret;

    /**
     * The redirect URI that Spotify uses to return the authorization code
     * during the OAuth2 authentication flow.
     */
    @NotBlank(message = "Spotify redirect URI cannot be blank")
    @Pattern(regexp = "^https?://.+", message = "Redirect URI must be a valid HTTP/HTTPS URL")
    private final String redirectUri;
}
