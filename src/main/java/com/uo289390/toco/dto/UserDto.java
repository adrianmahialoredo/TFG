package com.uo289390.toco.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uo289390.toco.dto.validation.OnCreate;
import com.uo289390.toco.model.Preferences;

/**
 * Data Transfer Object for user information.
 *
 * This DTO is used to transfer user data between the client and server,
 * particularly during user registration and login operations. It includes
 * validation annotations to ensure data integrity.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Data
public class UserDto {

    /**
     * The unique identifier for the user.
     */
    private Long id;

    /**
     * The email address of the user.
     * Must be in valid email format and is required.
     */
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    /**
     * The full name of the user.
     * Must be between 2 and 100 characters.
     */
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    /**
     * The unique username of the user.
     * Must be between 3 and 20 characters.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    /**
     * The URL of the user's profile image.
     * Must be a valid URL format if provided.
     */
    @Pattern(regexp = "^https?://.+|^$", message = "Profile image URL must be a valid HTTP/HTTPS URL or empty")
    private String profileImageUrl;

    /**
     * The password of the user.
     * This is required and should be transmitted securely.
     * Must be at least 8 characters and contain uppercase, digit and special character.
     */
    @NotBlank(message = "Password is required", groups = OnCreate.class)
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        // Same rule as the app: any character that is not a letter, digit or whitespace counts as a symbol
        regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9\\s])\\S{8,}$",
        message = "Password must contain an uppercase letter, a digit and a symbol, and no spaces"
    )
    private String password;
    
    /**
     * List of IDs of the user's favourite genres.
     * At least {@link Preferences#MIN_N_FAVOURITE_GENRES} are always required.
     */
    @NotNull(message = "Favourite genres are required")
    @Size(min = Preferences.MIN_N_FAVOURITE_GENRES,
          message = "At least " + Preferences.MIN_N_FAVOURITE_GENRES + " favourite genres are required")
    private List<Long> favouriteGenresIds;

    /**
     * List of IDs of the user's favourite instruments.
     * At least {@link Preferences#MIN_N_FAVOURITE_INSTRUMENTS} are always required.
     */
    @NotNull(message = "Favourite instruments are required")
    @Size(min = Preferences.MIN_N_FAVOURITE_INSTRUMENTS,
          message = "At least " + Preferences.MIN_N_FAVOURITE_INSTRUMENTS + " favourite instruments are required")
    private List<Long> favouriteInstrumentsIds;

    /**
     * Whether the user has linked a Spotify account.
     * Computed by the server: ignored when it comes in a request body.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean spotifyConnected;

    /**
     * Self-assessed musical level of the user (0 to 5, like the item stars).
     * Editable from the profile ({@code PUT /api/users/{id}}); null in a request keeps the current
     * one. Ignored by the registration, which starts every user at the default level.
     */
    @Min(value = 0, message = "Difficulty level must be at least 0")
    @Max(value = 5, message = "Difficulty level must be at most 5")
    private Integer difficultyLevel;

    /**
     * Code sent to the email by {@code POST /api/auth/verification-code}. Only used (and
     * required) by the registration; never returned.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String verificationCode;

}
