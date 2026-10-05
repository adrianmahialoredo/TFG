package com.demo.demo.security;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;
import java.util.Optional;
import jakarta.annotation.PostConstruct;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

/**
 * Utility class for JWT (JSON Web Token) operations.
 *
 * Provides methods to generate, validate, and extract information from JWT tokens.
 * Tokens are signed using HMAC-SHA algorithms and have an expiration time of 24 hours.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * Secret key used for signing and validating JWT tokens.
     * This is injected from application.properties using the jwt.secret-key property.
     * If not provided, a default value is used (should be changed in production).
     */
    @Value("${jwt.secret-key:cambiarPorUnaClaveSeguraEnProduccion1234567890}")
    private String secretKey;

    /**
     * Token expiration time in milliseconds (24 hours).
     */
    private static final long EXPIRATION_TIME = 86400000;

    /**
     * The cryptographic key used for signing tokens.
     */
    private Key key;

    /**
     * Initializes the cryptographic key after the properties are injected.
     */
    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isEmpty()) {
            log.warn("WARNING: JWT secret key is not configured. Using default insecure key. Configure 'jwt.secret-key' in application.properties for production.");
            secretKey = "cambiarPorUnaClaveSeguraEnProduccion1234567890";
        }
        if (secretKey.length() < 32) {
            log.warn("WARNING: JWT secret key is too short. Minimum recommended length is 32 characters.");
        }
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Generates a new JWT token for the specified username.
     *
     * @param username The username to encode in the token
     * @return A signed JWT token string
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    /**
     * Extracts the username from a JWT token.
     *
     * @param token The JWT token string
     * @return The username encoded in the token, or an empty Optional if token is invalid
     */
    public Optional<String> extractUsername(String token) {
        try {
            return Optional.ofNullable(Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject());
        } catch (JwtException e) {
            log.error("JWT exception while extracting username: {}", e.getMessage());
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument while extracting username from JWT: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Validates a JWT token for a specific username.
     *
     * Checks if the username in the token matches the provided username
     * and if the token has not expired.
     *
     * @param token The JWT token string to validate
     * @param username The username to match against the token
     * @return true if the token is valid and not expired, false otherwise
     */
    public boolean validateToken(String token, String username) {
        try {
            return extractUsername(token)
                    .filter(username::equals)
                    .filter(extractedUsername -> !isTokenExpired(token))
                    .isPresent();
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a JWT token has expired.
     *
     * @param token The JWT token string to check
     * @return true if the token has expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration()
                    .before(new Date());
        } catch (JwtException e) {
            log.error("JWT exception while checking expiration: {}", e.getMessage());
            return true;
        }
    }
}
