package com.demo.demo.controller;

import com.demo.demo.dto.LoginRequestDto;
import com.demo.demo.dto.LoginResponse;
import com.demo.demo.dto.UserDto;
import com.demo.demo.service.UserService;
import jakarta.validation.Valid;
import com.demo.demo.dto.validation.OnCreate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

/**
 * REST controller for authentication (registration and login).
 *
 * Base path: {@code /api/auth}
 *
 * Endpoints:
 * - POST   /api/auth/register Register a user and log them in
 * - POST   /api/auth/login    Log a user in
 *
 * Both endpoints return a {@link LoginResponse} with the JWT and the user.
 *
 * Responses: 200 OK (login), 201 Created (register), 400 Bad Request on validation errors
 * and 401 Unauthorized on wrong credentials. Errors are translated by
 * {@link com.demo.demo.exception.GlobalExceptionHandler}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Registers a user and logs them in straight away.
     *
     * @param dto the user to register
     * @return 201 Created with the JWT and the registered user
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Validated(OnCreate.class) @RequestBody UserDto dto) {
        userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.login(dto.getEmail(), dto.getPassword()));
    }

    /**
     * Logs a user in.
     *
     * @param dto the credentials of the user
     * @return 200 OK with the JWT and the user
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequestDto dto) {
        LoginResponse response = userService.login(dto.getEmail(), dto.getPassword());
        log.debug("JWT generated successfully during login flow");
        return ResponseEntity.ok(response);
    }
}
