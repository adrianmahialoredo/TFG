package com.demo.demo.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
public class UserDto {

    private Long id;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private String name;

    private String username;

    private String profileImageUrl;

    @NotBlank(message = "Password is required")
    private String password;
    
    private List<Long> favouriteGenresIds;
    private List<Long> favouriteInstrumentsIds;

}
