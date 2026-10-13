package com.uo289390.toco.security;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;
import jakarta.annotation.PostConstruct;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import com.uo289390.toco.model.Role;

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
     * Minimum length of the secret key in bytes (256 bits, required by HMAC-SHA256).
     */
    private static final int MIN_SECRET_KEY_BYTES = 32;

    /**
     * Name of the claim that carries the user's {@link Role}.
     */
    public static final String ROLE_CLAIM = "role";

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
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        // HMAC-SHA256 requires at least 256 bits: jjwt would fail with a less descriptive WeakKeyException
        if (keyBytes.length < MIN_SECRET_KEY_BYTES) {
            throw new IllegalStateException("JWT secret key is too short: 'jwt.secret-key' must be at least "
                    + MIN_SECRET_KEY_BYTES + " bytes long");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a new JWT token for the specified user.
     *
     * The subject is the user ID and not the username: the username can be changed
     * from the profile, so a token issued before the change would keep pointing to the
     * old username (or to another user that takes it afterwards).
     *
     * The role travels in the {@value #ROLE_CLAIM} claim so the authentication filter can
     * grant authorities without querying the database on every request. A role change
     * therefore takes effect on the user's next login.
     *
     * @param userId The ID of the user to encode in the token
     * @param role The role of the user
     * @return A signed JWT token string
     */
    public String generateToken(Long userId, Role role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim(ROLE_CLAIM, role.name())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    /**
     * Extracts the role from a JWT token.
     *
     * @param token The JWT token string
     * @return The role encoded in the token, or an empty Optional if the token is invalid,
     *         has no role claim (tokens issued before roles were added) or the role is unknown
     */
    public Optional<Role> extractRole(String token) {
        try {
            return Optional.ofNullable(Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get(ROLE_CLAIM, String.class))
                    .map(Role::valueOf);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Could not extract role from JWT: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extracts the user ID from a JWT token.
     *
     * Tokens issued before the subject became the user ID carry a username, which is
     * not numeric, so they are rejected and the user has to log in again.
     *
     * @param token The JWT token string
     * @return The user ID encoded in the token, or an empty Optional if token is invalid
     */
    public Optional<Long> extractUserId(String token) {
        try {
            return Optional.ofNullable(Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject())
                    .map(Long::valueOf);
        } catch (JwtException e) {
            log.error("JWT exception while extracting user ID: {}", e.getMessage());
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            // Also covers NumberFormatException (tokens whose subject is still a username)
            log.error("Illegal argument while extracting user ID from JWT: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Validates a JWT token for a specific user.
     *
     * Checks if the user ID in the token matches the provided one
     * and if the token has not expired.
     *
     * @param token The JWT token string to validate
     * @param userId The user ID to match against the token
     * @return true if the token is valid and not expired, false otherwise
     */
    public boolean validateToken(String token, Long userId) {
        try {
            return extractUserId(token)
                    .filter(userId::equals)
                    .filter(extractedUserId -> !isTokenExpired(token))
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
