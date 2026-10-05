package com.demo.demo.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Represents a Spotify user account linked to the application.
 *
 * This entity stores information about users who have authenticated through Spotify,
 * including their Spotify user ID and OAuth tokens. It enables integration with the
 * Spotify Web API for accessing user data and making music-related requests.
 *
 * The access token is used for making API requests, while the refresh token allows
 * the application to obtain a new access token when the current one expires.
 *
 * This class is annotated with JPA annotations to map it to a database table,
 * and it uses Lombok annotations to reduce boilerplate code for getters, setters,
 * constructors, and other common methods.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Entity
@Table(name = "spotify_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyUser {

    /**
     * Unique identifier for the Spotify user record in the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique identifier of the user account in the Spotify service.
     */
    @NotBlank(message = "Spotify user ID cannot be blank")
    @Column(nullable = false, unique = true)
    private String spotifyUserId;

    /**
     * The OAuth access token used to authenticate requests to the Spotify Web API.
     */
    @NotBlank(message = "Access token cannot be blank")
    private String accessToken;

    /**
     * The OAuth refresh token used to obtain a new access token when it expires.
     */
    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;

    /**
     * The timestamp when the access token expires.
     */
    @NotNull(message = "Token expiration cannot be null")
    private Instant tokenExpiration;

    /**
     * The user associated with this Spotify account.
     */
    @OneToOne(mappedBy = "spotifyUser")
    @JsonIgnore
    private User user;
}
