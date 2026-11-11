package com.uo289390.toco.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Email and code of a password reset, to check the code before asking for the new password.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-11-11
 */
@Data
public class PasswordResetCodeDto {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Verification code is required")
    private String code;
}
