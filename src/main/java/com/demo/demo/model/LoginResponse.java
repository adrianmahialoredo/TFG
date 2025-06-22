package com.demo.demo.model;

import com.demo.demo.dto.UserResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LoginResponse {

    private String token;
    private UserResponseDTO user;

}
