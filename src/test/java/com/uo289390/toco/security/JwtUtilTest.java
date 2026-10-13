package com.uo289390.toco.security;

import com.uo289390.toco.model.Role;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import com.uo289390.toco.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

/**
 * Unit tests for JwtUtil class.
 * Tests JWT token generation, validation, and extraction.
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private Long testUserId = 42L;
    private String testToken;

    @BeforeEach
    void setUp() {
        // Inject the secret key for testing
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "testSecureKeyForJWTTesting1234567890");
        jwtUtil.init();
        testToken = jwtUtil.generateToken(testUserId, Role.USER);
    }

    @Test
    void testGenerateToken_Success() {
        // Act
        String token = jwtUtil.generateToken(testUserId, Role.USER);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractRole_ReturnsRoleOfToken() {
        assertEquals(Optional.of(Role.USER), jwtUtil.extractRole(jwtUtil.generateToken(1L, Role.USER)));
        assertEquals(Optional.of(Role.ADMIN), jwtUtil.extractRole(jwtUtil.generateToken(1L, Role.ADMIN)));
    }

    @Test
    void testExtractRole_TokenWithoutRoleClaim_ReturnsEmpty() {
        // Tokens issued before roles were added have no role claim
        Key key = Keys.hmacShaKeyFor("testSecureKeyForJWTTesting1234567890".getBytes());
        String legacyToken = Jwts.builder()
                .setSubject("42")
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        assertEquals(Optional.empty(), jwtUtil.extractRole(legacyToken));
        assertEquals(Optional.of(42L), jwtUtil.extractUserId(legacyToken));
    }

    @Test
    void testExtractRole_UnknownRole_ReturnsEmpty() {
        Key key = Keys.hmacShaKeyFor("testSecureKeyForJWTTesting1234567890".getBytes());
        String token = Jwts.builder()
                .setSubject("42")
                .claim(JwtUtil.ROLE_CLAIM, "SUPERUSER")
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        assertEquals(Optional.empty(), jwtUtil.extractRole(token));
    }

    @Test
    void testExtractRole_InvalidToken_ReturnsEmpty() {
        assertEquals(Optional.empty(), jwtUtil.extractRole("not-a-jwt"));
    }

    @Test
    void testGenerateToken_DifferentUsers() {
        // Act
        String token1 = jwtUtil.generateToken(1L, Role.USER);
        String token2 = jwtUtil.generateToken(2L, Role.USER);

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    void testExtractUserId_Success() {
        // Act
        Optional<Long> userId = jwtUtil.extractUserId(testToken);

        // Assert
        assertTrue(userId.isPresent());
        assertEquals(testUserId, userId.get());
    }

    @Test
    void testExtractUserId_InvalidToken() {
        // Act & Assert
        Optional<Long> result = jwtUtil.extractUserId("invalid-token");
        assertTrue(result.isEmpty());
    }

    @Test
    void testExtractUserId_LegacyUsernameToken() {
        // Arrange: token signed with the right key but issued when the subject was the username
        Key key = (Key) ReflectionTestUtils.getField(jwtUtil, "key");
        String legacyToken = Jwts.builder()
                .setSubject("testuser")
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        // Act & Assert: it is rejected, so the user has to log in again
        assertTrue(jwtUtil.extractUserId(legacyToken).isEmpty());
    }

    @Test
    void testValidateToken_Success() {
        // Act
        boolean isValid = jwtUtil.validateToken(testToken, testUserId);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_WrongUserId() {
        // Act
        boolean isValid = jwtUtil.validateToken(testToken, 7L);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Act
        boolean isValid = jwtUtil.validateToken("invalid-token", testUserId);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_ExpiredToken() {
        // This test would require manipulating the token expiration
        // For now, we test with a valid token
        boolean isValid = jwtUtil.validateToken(testToken, testUserId);
        assertTrue(isValid);
    }

    @Test
    void testExtractUserId_ExpiredToken() {
        // Arrange: correctly signed token whose expiration is in the past
        Key key = (Key) ReflectionTestUtils.getField(jwtUtil, "key");
        String expiredToken = Jwts.builder()
                .setSubject(String.valueOf(testUserId))
                .setExpiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(key)
                .compact();

        // Act & Assert
        assertTrue(jwtUtil.extractUserId(expiredToken).isEmpty());
        assertFalse(jwtUtil.validateToken(expiredToken, testUserId));
    }

    @Test
    void testExtractUserId_TokenSignedWithDifferentKey() {
        // Arrange
        Key otherKey = Keys.hmacShaKeyFor("anotherSecureKeyForJWTTesting0987654321".getBytes());
        String foreignToken = Jwts.builder()
                .setSubject(String.valueOf(testUserId))
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otherKey)
                .compact();

        // Act & Assert
        assertTrue(jwtUtil.extractUserId(foreignToken).isEmpty());
        assertFalse(jwtUtil.validateToken(foreignToken, testUserId));
    }

    @Test
    void testExtractUserId_TamperedPayload() {
        // Arrange: replace the payload of a valid token keeping the original signature
        String[] parts = testToken.split("\\.");
        String forgedPayload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"1\",\"exp\":9999999999}".getBytes());
        String tampered = parts[0] + "." + forgedPayload + "." + parts[2];

        // Act & Assert
        assertTrue(jwtUtil.extractUserId(tampered).isEmpty());
        assertFalse(jwtUtil.validateToken(tampered, 1L));
    }

    @Test
    void testExtractUserId_TamperedSignature() {
        // Arrange
        String tampered = testToken.substring(0, testToken.length() - 2)
                + (testToken.endsWith("AA") ? "BB" : "AA");

        // Act & Assert
        assertTrue(jwtUtil.extractUserId(tampered).isEmpty());
    }

    @Test
    void testExtractUserId_UnsignedToken() {
        // Arrange: "alg: none" token must not be accepted
        String unsigned = Jwts.builder()
                .setSubject(String.valueOf(testUserId))
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .compact();

        // Act & Assert
        assertTrue(jwtUtil.extractUserId(unsigned).isEmpty());
    }

    @Test
    void testExtractUserId_NullAndEmptyToken() {
        assertTrue(jwtUtil.extractUserId(null).isEmpty());
        assertTrue(jwtUtil.extractUserId("").isEmpty());
        assertTrue(jwtUtil.extractUserId("   ").isEmpty());
    }

    @Test
    void testExtractUserId_TokenWithoutSubject() {
        // Arrange
        Key key = (Key) ReflectionTestUtils.getField(jwtUtil, "key");
        String noSubject = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        // Act & Assert
        assertTrue(jwtUtil.extractUserId(noSubject).isEmpty());
    }

    @Test
    void testValidateToken_NullTokenOrUserId() {
        assertFalse(jwtUtil.validateToken(null, testUserId));
        assertFalse(jwtUtil.validateToken(testToken, null));
    }

    @Test
    void testGenerateToken_ExpiresInTwentyFourHours() {
        // Arrange
        Key key = (Key) ReflectionTestUtils.getField(jwtUtil, "key");
        long before = System.currentTimeMillis();

        // Act
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(jwtUtil.generateToken(testUserId, Role.USER))
                .getBody().getExpiration();

        // Assert (JWT dates have second precision)
        long expected = before + 86_400_000L;
        assertTrue(Math.abs(expiration.getTime() - expected) < 5_000,
                "Expiration should be ~24h after issuance");
    }

    @Test
    void testGenerateToken_SubjectIsUserId() {
        Key key = (Key) ReflectionTestUtils.getField(jwtUtil, "key");

        String subject = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(jwtUtil.generateToken(123L, Role.USER))
                .getBody().getSubject();

        assertEquals("123", subject);
    }

    @Test
    void testTokenFromOtherInstanceWithSameSecret_IsAccepted() {
        // Arrange
        JwtUtil other = new JwtUtil();
        ReflectionTestUtils.setField(other, "secretKey", "testSecureKeyForJWTTesting1234567890");
        other.init();

        // Act & Assert
        assertTrue(other.validateToken(testToken, testUserId));
    }

    @Test
    void testInit_EmptySecretFallsBackToDefaultKey() {
        // Arrange
        JwtUtil withEmpty = new JwtUtil();
        ReflectionTestUtils.setField(withEmpty, "secretKey", "");

        // Act
        withEmpty.init();
        String token = withEmpty.generateToken(5L, Role.USER);

        // Assert
        assertEquals("cambiarPorUnaClaveSeguraEnProduccion1234567890",
                ReflectionTestUtils.getField(withEmpty, "secretKey"));
        assertEquals(Optional.of(5L), withEmpty.extractUserId(token));
    }

    @Test
    void testInit_NullSecretFallsBackToDefaultKey() {
        // Arrange
        JwtUtil withNull = new JwtUtil();
        ReflectionTestUtils.setField(withNull, "secretKey", null);

        // Act
        withNull.init();

        // Assert
        assertTrue(withNull.validateToken(withNull.generateToken(6L, Role.USER), 6L));
    }

    @Test
    void testInit_SecretTooShortForHmacIsRejected() {
        // Arrange: fewer than 256 bits is not allowed by the HMAC-SHA key factory
        JwtUtil withShort = new JwtUtil();
        ReflectionTestUtils.setField(withShort, "secretKey", "short-secret");

        // Act & Assert: fails fast with a descriptive message instead of jjwt's WeakKeyException
        IllegalStateException ex = assertThrows(IllegalStateException.class, withShort::init);
        assertTrue(ex.getMessage().contains("jwt.secret-key"));
    }

    @Test
    void testInit_SecretOf31BytesIsRejected() {
        // Arrange
        JwtUtil withShort = new JwtUtil();
        ReflectionTestUtils.setField(withShort, "secretKey", "a".repeat(31));

        // Act & Assert
        assertThrows(IllegalStateException.class, withShort::init);
    }

    @Test
    void testInit_MultiByteCharactersCountAsBytes() {
        // Arrange: 16 characters but 32 bytes in UTF-8, enough for HMAC-SHA256
        JwtUtil withMultiByte = new JwtUtil();
        ReflectionTestUtils.setField(withMultiByte, "secretKey", "\u00f1".repeat(16));

        // Act
        withMultiByte.init();

        // Assert
        assertTrue(withMultiByte.validateToken(withMultiByte.generateToken(1L, Role.USER), 1L));
    }

    @Test
    void testInit_SecretOfExactly32CharactersIsAccepted() {
        // Arrange
        JwtUtil withMinimum = new JwtUtil();
        ReflectionTestUtils.setField(withMinimum, "secretKey", "a".repeat(32));

        // Act
        withMinimum.init();

        // Assert
        assertTrue(withMinimum.validateToken(withMinimum.generateToken(8L, Role.USER), 8L));
    }
}
