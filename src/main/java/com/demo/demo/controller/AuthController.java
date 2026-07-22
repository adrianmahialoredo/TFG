package com.demo.demo.controller;

import com.demo.demo.dto.UserDto;
import com.demo.demo.service.UserService;
import com.demo.demo.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody UserDto userDto) {
        // 1. Registramos al usuario (esto guarda en DB y encripta password)
        userService.registerUser(userDto);

        // 2. Generamos el token y la respuesta de login inmediatamente
        LoginResponse response = userService.loginUser(userDto);

        // 3. Devolvemos el objeto que el Frontend ya sabe leer
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDto userDto) {
        // loginUser devuelve un objeto que incluye token y usuario
        LoginResponse response = userService.loginUser(userDto);

        log.debug("JWT generated successfully during login flow");

        return ResponseEntity.ok(Map.of(
            "token", response.getToken(),
            "user", response.getUser()
        ));
    }

}
