package com.uo289390.toco.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request of a verification code for the email of a registration.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-10-13
 */
@Data
public class VerificationCodeRequestDto {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
}
