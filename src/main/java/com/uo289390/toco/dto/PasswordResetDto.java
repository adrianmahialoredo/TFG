package com.uo289390.toco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * New password of a password reset, with the email and the code that authorise it.
 *
 * The new password follows the same policy as the registration ({@link UserDto}).
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-11-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PasswordResetDto extends PasswordResetCodeDto {

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9\\s])\\S{8,}$",
        message = "Password must contain an uppercase letter, a digit and a symbol, and no spaces"
    )
    private String newPassword;
}
