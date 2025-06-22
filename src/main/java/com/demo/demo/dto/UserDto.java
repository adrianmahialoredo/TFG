package com.demo.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
public class UserDto {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private String name;

    private String username;

    @NotBlank(message = "Password is required")
    private String password;
    
}
