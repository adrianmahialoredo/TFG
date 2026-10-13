package com.uo289390.toco.dto;

import java.util.List;
import lombok.Data;

/**
 * Data Transfer Object for user response information.
 *
 * This DTO is used to transfer user data back to the client after
 * login or registration operations. It excludes sensitive information
 * such as passwords and includes user preferences.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Data
public class UserResponseDTO {

    /**
     * The unique identifier of the user.
     */
    private Long id;

    /**
     * The email address of the user.
     */
    private String email;

    /**
     * The full name of the user.
     */
    private String name;

    /**
     * The unique username of the user.
     */
    private String username;

    /**
     * The URL of the user's profile image.
     */
    private String profileImageUrl;

    /**
     * List of IDs of the user's favourite genres.
     */
    private List<Long> favouriteGenresIds;

    /**
     * List of IDs of the user's favourite instruments.
     */
    private List<Long> favouriteInstrumentsIds;

    /**
     * Whether the user has linked a Spotify account.
     */
    private boolean spotifyConnected;

}
