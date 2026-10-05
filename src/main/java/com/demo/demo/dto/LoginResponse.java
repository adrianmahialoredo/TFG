package com.demo.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Represents the response returned after a successful user login.
 *
 * This class encapsulates the authentication token and user information
 * that are returned to the client after successful authentication.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * The JWT authentication token for the logged-in user.
     */
    private String token;

    /**
     * The authenticated user's information and details.
     */
    private UserResponseDTO user;

}
