package com.uo289390.toco.controller;

import com.uo289390.toco.dto.LoginRequestDto;
import com.uo289390.toco.dto.LoginResponse;
import com.uo289390.toco.dto.UserDto;
import com.uo289390.toco.dto.VerificationCodeRequestDto;
import com.uo289390.toco.service.EmailVerificationService;
import com.uo289390.toco.service.UserService;
import com.uo289390.toco.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import com.uo289390.toco.dto.validation.OnCreate;
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
 * - POST   /api/auth/verification-code Send a code to the email of a future registration
 * - POST   /api/auth/register          Register a user (with that code) and log them in
 * - POST   /api/auth/login             Log a user in (with their email or username)
 *
 * Register and login return a {@link LoginResponse} with the JWT and the user.
 *
 * Responses: 200 OK (login), 201 Created (register), 204 No Content (verification code),
 * 400 Bad Request on validation errors or a wrong code, 401 Unauthorized on wrong credentials
 * and 429 Too Many Requests if a code is asked for again too soon. Errors are translated by
 * {@link GlobalExceptionHandler}.
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
    private final EmailVerificationService emailVerificationService;

    /**
     * Sends a verification code to the email of a registration that has not been made yet.
     *
     * @param dto the email to verify
     * @return 204 No Content once the code has been sent
     */
    @PostMapping("/verification-code")
    public ResponseEntity<Void> sendVerificationCode(@Valid @RequestBody VerificationCodeRequestDto dto) {
        emailVerificationService.sendCode(dto.getEmail());
        return ResponseEntity.noContent().build();
    }

    /**
     * Registers a user and logs them in straight away.
     *
     * The email must have been verified first: the body carries the code sent by
     * {@link #sendVerificationCode}. It is only consumed once the user is saved, so a
     * registration rejected for another reason can be retried with the same code.
     *
     * @param dto the user to register, with its verification code
     * @return 201 Created with the JWT and the registered user
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Validated(OnCreate.class) @RequestBody UserDto dto) {
        emailVerificationService.checkCode(dto.getEmail(), dto.getVerificationCode());
        userService.register(dto);
        emailVerificationService.consume(dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.login(dto.getEmail(), dto.getPassword()));
    }

    /**
     * Logs a user in with their email or their username (field {@code identifier};
     * {@code email} is accepted as an alias).
     *
     * @param dto the credentials of the user
     * @return 200 OK with the JWT and the user
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequestDto dto) {
        LoginResponse response = userService.login(dto.getIdentifier(), dto.getPassword());
        log.debug("JWT generated successfully during login flow");
        return ResponseEntity.ok(response);
    }
}
