package com.demo.demo.controller;

import com.demo.demo.dto.UserDto;
import com.demo.demo.model.User;
import com.demo.demo.service.UserService;
import com.demo.demo.model.LoginResponse;
import lombok.RequiredArgsConstructor;

import java.io.Console;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDto userDto) {
        try {
            User user = userService.registerUser(userDto);
            return ResponseEntity.ok("User registered successfully: " + user.getEmail());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDto userDto) {
        try {
            // Ahora loginUser devuelve un objeto que incluye token y usuario
            LoginResponse response = userService.loginUser(userDto);
            
            Console console = System.console();
            console.printf(response.getToken());

            return ResponseEntity.ok(Map.of(
                "token", response.getToken(),
                "user", response.getUser()
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

}

