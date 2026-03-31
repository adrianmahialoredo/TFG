package com.demo.demo.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

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
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@$!%*?&])[A-Za-z0-9@$!%*?&]{8,}$",
        message = "Password must contain uppercase letter, digit and special character (@$!%*?&)"
    )
    private String password;
    
    /**
     * List of IDs of the user's favourite genres.
     */
    private List<Long> favouriteGenresIds;

    /**
     * List of IDs of the user's favourite instruments.
     */
    private List<Long> favouriteInstrumentsIds;

}
