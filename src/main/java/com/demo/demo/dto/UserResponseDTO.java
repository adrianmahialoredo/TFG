package com.demo.demo.dto;

import java.util.List;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.URL;

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
@NoArgsConstructor
public class UserResponseDTO {

    /**
     * The unique identifier of the user.
     */
    @NotNull(message = "User ID is required")
    private Long id;

    /**
     * The email address of the user.
     */
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    /**
     * The full name of the user.
     */
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    /**
     * The unique username of the user.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    /**
     * The URL of the user's profile image.
     * Must be a valid URL format if provided.
     */
    @URL(message = "Profile image URL must be a valid URL")
    private String profileImageUrl;

    /**
     * List of IDs of the user's favourite genres.
     */
    private List<Long> favouriteGenresIds;

    /**
     * List of IDs of the user's favourite instruments.
     */
    private List<Long> favouriteInstrumentsIds;

}