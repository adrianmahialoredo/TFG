package com.demo.demo.controller;

import com.demo.demo.dto.UserDto;
import com.demo.demo.model.User;
import com.demo.demo.service.UserService;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    /*
     * POST	/api/auth/register	
     * Registra un nuevo usuario con los datos proporcionados en el UserDto.	
     * Cuerpo: UserDto (correo, contraseña, etc.)	
     * 200 OK: "User registered successfully: <email>"	
     * 400 Bad Request: Mensaje de error en la validación del DTO.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDto userDto) {
        // El DTO es validado antes de pasar a UserService
        try {
            // Llamada al servicio para registrar al usuario
            User user = userService.registerUser(userDto);
            return ResponseEntity.ok("User registered successfully: " + user.getEmail());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /*
     * POST	/api/auth/login	Autentica al usuario y genera un token JWT.	
     * Cuerpo: UserDto (correo, contraseña)	
     * 200 OK: { "token": "<JWT_token>" }	
     * 401 Unauthorized: "Invalid credentials"
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDto userDto) {
        // El DTO es validado antes de pasar a UserService
        try {
            String token = userService.loginUser(userDto);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

}
