package com.uo289390.toco.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO específico para login.
 * Evita reutilizar UserDto completo, ya que login solo requiere credenciales.
 */
@Data
public class LoginRequestDto {

    /**
     * Email or username of the user. The JSON field {@code email} is still accepted
     * (alias), so older clients and the data loading scripts keep working.
     */
    @JsonAlias("email")
    @NotBlank(message = "Email or username is required")
    private String identifier;

    /**
     * Only presence is checked: the strength policy belongs to registration
     * ({@link UserDto}). Enforcing a (different) policy here rejected valid
     * credentials before they were compared; wrong ones still get a 401.
     */
    @NotBlank(message = "Password is required")
    private String password;
}
