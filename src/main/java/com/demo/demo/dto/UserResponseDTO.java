package com.demo.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String email;
    private String name; // o lo que tengas de usuario público

}