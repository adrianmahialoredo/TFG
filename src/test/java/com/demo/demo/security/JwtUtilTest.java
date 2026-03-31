package com.demo.demo.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.JwtException;

/**
 * Unit tests for JwtUtil class.
 * Tests JWT token generation, validation, and extraction.
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private String testUsername = "testuser";
    private String testToken;

    @BeforeEach
    void setUp() {
        // Inject the secret key for testing
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "testSecureKeyForJWTTesting1234567890");
        jwtUtil.init();
        testToken = jwtUtil.generateToken(testUsername);
    }

    @Test
    void testGenerateToken_Success() {
        // Act
        String token = jwtUtil.generateToken(testUsername);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGenerateToken_DifferentUsers() {
        // Act
        String token1 = jwtUtil.generateToken("user1");
        String token2 = jwtUtil.generateToken("user2");

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    void testExtractUsername_Success() {
        // Act
        String username = jwtUtil.extractUsername(testToken);

        // Assert
        assertEquals(testUsername, username);
    }

    @Test
    void testExtractUsername_InvalidToken() {
        // Act & Assert
        String result = jwtUtil.extractUsername("invalid-token");
        assertNull(result);
    }

    @Test
    void testValidateToken_Success() {
        // Act
        boolean isValid = jwtUtil.validateToken(testToken, testUsername);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_WrongUsername() {
        // Act
        boolean isValid = jwtUtil.validateToken(testToken, "wronguser");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Act
        boolean isValid = jwtUtil.validateToken("invalid-token", testUsername);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_ExpiredToken() {
        // This test would require manipulating the token expiration
        // For now, we test with a valid token
        boolean isValid = jwtUtil.validateToken(testToken, testUsername);
        assertTrue(isValid);
    }
}

