package com.uo289390.toco.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import com.uo289390.toco.service.SpotifyAuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.uo289390.toco.config.SpotifyProperties;
import com.uo289390.toco.model.SpotifyUser;
import com.uo289390.toco.model.User;
import com.uo289390.toco.repository.SpotifyUserRepository;
import com.uo289390.toco.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for SpotifyAuthServiceImpl.
 * Tests the OAuth2 authorization URL, the callback that links a Spotify account
 * to an application user and the access token refresh flow. Spotify is simulated
 * with a {@link MockRestServiceServer}, so no request leaves the test.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SpotifyAuthServiceImplTest {

    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String ME_URL = "https://api.spotify.com/v1/me";
    private static final String CLIENT_ID = "client-id";
    private static final String CLIENT_SECRET = "client-secret";
    private static final String REDIRECT_URI = "http://127.0.0.1:8080/api/spotify/callback";
    private static final Long USER_ID = 1L;

    @Mock
    private SpotifyUserRepository spotifyUserRepository;

    @Mock
    private UserRepository userRepository;

    private MockRestServiceServer server;
    private SpotifyAuthServiceImpl service;
    private User appUser;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        service = new SpotifyAuthServiceImpl(restTemplate, spotifyUserRepository, userRepository,
                new SpotifyProperties(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI));

        appUser = new User();
        appUser.setId(USER_ID);
        appUser.setUsername("appuser");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(appUser));
        when(spotifyUserRepository.findBySpotifyUserId(anyString())).thenReturn(Optional.empty());
    }

    private static String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes(StandardCharsets.UTF_8));
    }

    private static String stateOf(String authorizationUrl) {
        return UriComponentsBuilder.fromUri(URI.create(authorizationUrl)).build().getQueryParams().getFirst("state");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> pendingAuthorizations() throws Exception {
        Field field = SpotifyAuthServiceImpl.class.getDeclaredField("pendingAuthorizations");
        field.setAccessible(true);
        return (Map<String, Object>) field.get(service);
    }

    /**
     * Registers a pending authorization that already expired.
     */
    private void putExpiredState(String state, Long userId) throws Exception {
        Class<?> pendingClass = Class.forName(SpotifyAuthServiceImpl.class.getName() + "$PendingAuthorization");
        Constructor<?> constructor = pendingClass.getDeclaredConstructor(Long.class, Instant.class);
        constructor.setAccessible(true);
        pendingAuthorizations().put(state, constructor.newInstance(userId, Instant.now().minusSeconds(1)));
    }

    private void expectTokenExchange(String code, String responseJson) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", REDIRECT_URI);
        server.expect(requestTo(TOKEN_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, basicAuth()))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().formData(form))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));
    }

    private void expectMe(String accessToken, String responseJson) {
        server.expect(requestTo(ME_URL))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));
    }

    private static final String TOKEN_JSON =
            "{\"access_token\":\"access-1\",\"refresh_token\":\"refresh-1\",\"expires_in\":3600}";

    @Nested
    class BuildAuthorizationUrl {

        @Test
        void testBuildAuthorizationUrl_ContainsAllOAuthParameters() {
            // Act
            String url = service.buildAuthorizationUrl(USER_ID);

            // Assert
            assertTrue(url.startsWith("https://accounts.spotify.com/authorize?"));
            assertTrue(url.contains("client_id=" + CLIENT_ID));
            assertTrue(url.contains("&response_type=code"));
            assertTrue(url.contains("&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)));
            assertTrue(url.contains("&scope=user-read-email%20user-top-read"));
            assertDoesNotThrow(() -> java.util.UUID.fromString(stateOf(url)));
        }

        @Test
        void testBuildAuthorizationUrl_GeneratesDifferentStatesAndRegistersThem() throws Exception {
            // Act
            String first = stateOf(service.buildAuthorizationUrl(USER_ID));
            String second = stateOf(service.buildAuthorizationUrl(2L));

            // Assert
            assertNotEquals(first, second);
            assertTrue(pendingAuthorizations().containsKey(first));
            assertTrue(pendingAuthorizations().containsKey(second));
        }

        @Test
        void testBuildAuthorizationUrl_PurgesExpiredStates() throws Exception {
            // Arrange
            putExpiredState("old-state", USER_ID);

            // Act
            service.buildAuthorizationUrl(USER_ID);

            // Assert
            assertFalse(pendingAuthorizations().containsKey("old-state"));
            assertEquals(1, pendingAuthorizations().size());
        }
    }

    @Nested
    class HandleCallback {

        @Test
        void testHandleCallback_NullOrEmptyCode_ThrowsIllegalArgument() {
            // Arrange
            String state = stateOf(service.buildAuthorizationUrl(USER_ID));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.handleCallback(null, state));
            assertThrows(IllegalArgumentException.class, () -> service.handleCallback("", state));
            server.verify();
            verifyNoInteractions(spotifyUserRepository);
        }

        @Test
        void testHandleCallback_NullState_ThrowsIllegalArgument() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.handleCallback("code", null));
            verify(userRepository, never()).findById(any());
        }

        @Test
        void testHandleCallback_UnknownState_ThrowsIllegalArgument() {
            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.handleCallback("code", "unknown"));
            assertTrue(ex.getMessage().contains("Invalid or expired"));
        }

        @Test
        void testHandleCallback_ExpiredState_ThrowsIllegalArgumentAndConsumesIt() throws Exception {
            // Arrange
            putExpiredState("expired", USER_ID);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.handleCallback("code", "expired"));
            assertFalse(pendingAuthorizations().containsKey("expired"));
            verify(userRepository, never()).findById(any());
        }

        @Test
        void testHandleCallback_UserNotFound_ThrowsIllegalArgument() {
            // Arrange
            String state = stateOf(service.buildAuthorizationUrl(99L));
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.handleCallback("code", state));
            assertEquals("User not found in application", ex.getMessage());
            server.verify();
        }

        @Test
        void testHandleCallback_Success_LinksNewSpotifyUser() {
            // Arrange
            String state = stateOf(service.buildAuthorizationUrl(USER_ID));
            expectTokenExchange("auth-code", TOKEN_JSON);
            expectMe("access-1", "{\"id\":\"spotify-42\"}");
            Instant before = Instant.now();

            // Act
            service.handleCallback("auth-code", state);

            // Assert
            server.verify();
            ArgumentCaptor<SpotifyUser> captor = ArgumentCaptor.forClass(SpotifyUser.class);
            verify(spotifyUserRepository).save(captor.capture());
            SpotifyUser saved = captor.getValue();
            assertEquals("spotify-42", saved.getSpotifyUserId());
            assertEquals("access-1", saved.getAccessToken());
            assertEquals("refresh-1", saved.getRefreshToken());
            assertSame(appUser, saved.getUser());
            assertSame(saved, appUser.getSpotifyUser());
            assertFalse(saved.getTokenExpiration().isBefore(before.plusSeconds(3600)));
            assertFalse(saved.getTokenExpiration().isAfter(Instant.now().plusSeconds(3600)));
            verify(userRepository).save(appUser);
            verify(spotifyUserRepository, never()).flush();
        }

        @Test
        void testHandleCallback_TokenWithoutExpiresIn_LinksWithDefaultLifetime() {
            // Arrange (regression: the cast of a missing expires_in threw a NullPointerException)
            String state = stateOf(service.buildAuthorizationUrl(USER_ID));
            expectTokenExchange("auth-code", "{\"access_token\":\"access-1\",\"refresh_token\":\"refresh-1\"}");
            expectMe("access-1", "{\"id\":\"spotify-42\"}");
            Instant before = Instant.now();

            // Act
            service.handleCallback("auth-code", state);

            // Assert
            ArgumentCaptor<SpotifyUser> captor = ArgumentCaptor.forClass(SpotifyUser.class);
            verify(spotifyUserRepository).save(captor.capture());
            assertFalse(captor.getValue().getTokenExpiration().isBefore(before.plusSeconds(3600)));
            assertFalse(captor.getValue().getTokenExpiration().isAfter(Instant.now().plusSeconds(3600)));
        }

        @Test
        void testHandleCallback_StateCanOnlyBeUsedOnce() {
            // Arrange
            String state = stateOf(service.buildAuthorizationUrl(USER_ID));
            expectTokenExchange("auth-code", TOKEN_JSON);
            expectMe("access-1", "{\"id\":\"spotify-42\"}");
            service.handleCallback("auth-code", state);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.handleCallback("auth-code", state));
        }

        @Test
        void testHandleCallback_UserAlreadyHasSpotifyUser_ReusesIt() {
            // Arrange
            SpotifyUser existing = new SpotifyUser();
            existing.setId(5L);
            existing.setSpotifyUserId("old-account");
            existing.setUser(appUser);
            appUser.setSpotifyUser(existing);
            String state = stateOf(service.buildAuthorizationUrl(USER_ID));
            expectTokenExchange("auth-code", TOKEN_JSON);
            expectMe("access-1", "{\"id\":\"spotify-42\"}");

            // Act
            service.handleCallback("auth-code", state);

            // Assert
            verify(spotifyUserRepository).save(existing);
            assertEquals(5L, existing.getId());
            assertEquals("spotify-42", existing.getSpotifyUserId());
            assertEquals("access-1", existing.getAccessToken());
        }

        @Test
        void testHandleCallback_AccountLinkedToAnotherUser_UnlinksItFirst() {
            // Arrange
            User previousUser = new User();
            previousUser.setId(2L);
            SpotifyUser linkedElsewhere = new SpotifyUser();
            linkedElsewhere.setSpotifyUserId("spotify-42");
            linkedElsewhere.setUser(previousUser);
            previousUser.setSpotifyUser(linkedElsewhere);
            when(spotifyUserRepository.findBySpotifyUserId("spotify-42")).thenReturn(Optional.of(linkedElsewhere));
            String state = stateOf(service.buildAuthorizationUrl(USER_ID));
            expectTokenExchange("auth-code", TOKEN_JSON);
            expectMe("access-1", "{\"id\":\"spotify-42\"}");

            // Act
            service.handleCallback("auth-code", state);

            // Assert
            assertNull(previousUser.getSpotifyUser());
            verify(userRepository).save(previousUser);
            verify(spotifyUserRepository).flush();
            verify(spotifyUserRepository, never()).delete(any());
            assertNotNull(appUser.getSpotifyUser());
            assertNotSame(linkedElsewhere, appUser.getSpotifyUser());
        }

        @Test
        void testHandleCallback_OrphanAccount_IsDeletedFirst() {
            // Arrange
            SpotifyUser orphan = new SpotifyUser();
            orphan.setSpotifyUserId("spotify-42");
            when(spotifyUserRepository.findBySpotifyUserId("spotify-42")).thenReturn(Optional.of(orphan));
            String state = stateOf(service.buildAuthorizationUrl(USER_ID));
            expectTokenExchange("auth-code", TOKEN_JSON);
            expectMe("access-1", "{\"id\":\"spotify-42\"}");

            // Act
            service.handleCallback("auth-code", state);

            // Assert
            verify(spotifyUserRepository).delete(orphan);
            verify(spotifyUserRepository).flush();
        }

        @Test
        void testHandleCallback_AccountAlreadyLinkedToSameUser_IsNotUnlinked() {
            // Arrange
            SpotifyUser own = new SpotifyUser();
            own.setSpotifyUserId("spotify-42");
            own.setUser(appUser);
            appUser.setSpotifyUser(own);
            when(spotifyUserRepository.findBySpotifyUserId("spotify-42")).thenReturn(Optional.of(own));
            String state = stateOf(service.buildAuthorizationUrl(USER_ID));
            expectTokenExchange("auth-code", TOKEN_JSON);
            expectMe("access-1", "{\"id\":\"spotify-42\"}");

            // Act
            service.handleCallback("auth-code", state);

            // Assert
            verify(spotifyUserRepository, never()).delete(any());
            verify(spotifyUserRepository, never()).flush();
            verify(spotifyUserRepository).save(own);
            assertEquals("access-1", own.getAccessToken());
        }

        @Test
        void testHandleCallback_TokenResponseWithoutAccessToken_ThrowsAndSavesNothing() {
            // Arrange
            String state = stateOf(service.buildAuthorizationUrl(USER_ID));
            expectTokenExchange("auth-code", "{\"error\":\"invalid\"}");

            // Act
            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.handleCallback("auth-code", state));

            // Assert
            assertEquals("Error processing Spotify authorization", ex.getMessage());
            assertInstanceOf(IllegalStateException.class, ex.getCause());
            verify(spotifyUserRepository, never()).save(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        void testHandleCallback_TokenEndpointRejectsCode_ThrowsWrappedClientError() {
            // Arrange
            String state = stateOf(service.buildAuthorizationUrl(USER_ID));
            server.expect(requestTo(TOKEN_URL))
                    .andRespond(withBadRequest().body("{\"error\":\"invalid_grant\"}").contentType(MediaType.APPLICATION_JSON));

            // Act
            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.handleCallback("bad-code", state));

            // Assert
            assertInstanceOf(HttpClientErrorException.class, ex.getCause());
            verify(spotifyUserRepository, never()).save(any());
        }

        @Test
        void testHandleCallback_TokenEndpointServerError_ThrowsWrappedServerError() {
            // Arrange
            String state = stateOf(service.buildAuthorizationUrl(USER_ID));
            server.expect(requestTo(TOKEN_URL)).andRespond(withServerError());

            // Act
            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.handleCallback("code", state));

            // Assert
            assertInstanceOf(HttpServerErrorException.class, ex.getCause());
        }

        @Test
        void testHandleCallback_ProfileWithoutId_ThrowsAndSavesNothing() {
            // Arrange
            String state = stateOf(service.buildAuthorizationUrl(USER_ID));
            expectTokenExchange("auth-code", TOKEN_JSON);
            expectMe("access-1", "{\"display_name\":\"no id\"}");

            // Act
            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.handleCallback("auth-code", state));

            // Assert
            assertInstanceOf(IllegalStateException.class, ex.getCause());
            verify(spotifyUserRepository, never()).save(any());
            server.verify();
        }

        @Test
        void testHandleCallback_IllegalArgumentWhileLinking_IsRethrownUnwrapped() {
            // Arrange
            String state = stateOf(service.buildAuthorizationUrl(USER_ID));
            expectTokenExchange("auth-code", TOKEN_JSON);
            expectMe("access-1", "{\"id\":\"spotify-42\"}");
            IllegalArgumentException failure = new IllegalArgumentException("invalid entity");
            when(userRepository.save(appUser)).thenThrow(failure);

            // Act
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.handleCallback("auth-code", state));

            // Assert: kept as a 400-style IllegalArgumentException instead of a wrapped RuntimeException
            assertSame(failure, ex);
            server.verify();
        }

        @Test
        void testHandleCallback_ProfileIdNotAString_ThrowsWrappedRuntimeException() {
            // Arrange
            String state = stateOf(service.buildAuthorizationUrl(USER_ID));
            expectTokenExchange("auth-code", TOKEN_JSON);
            expectMe("access-1", "{\"id\":42}");

            // Act
            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.handleCallback("auth-code", state));

            // Assert
            assertEquals("Error processing Spotify authorization", ex.getMessage());
            assertInstanceOf(ClassCastException.class, ex.getCause());
            verify(spotifyUserRepository, never()).save(any());
        }

        @Test
        void testHandleCallback_ProfileEndpointUnauthorized_ThrowsWrappedClientError() {
            // Arrange
            String state = stateOf(service.buildAuthorizationUrl(USER_ID));
            expectTokenExchange("auth-code", TOKEN_JSON);
            server.expect(requestTo(ME_URL)).andRespond(withBadRequest());

            // Act
            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.handleCallback("auth-code", state));

            // Assert
            assertInstanceOf(HttpClientErrorException.class, ex.getCause());
            verify(spotifyUserRepository, never()).save(any());
        }
    }

    @Nested
    class GetValidAccessToken {

        private SpotifyUser spotifyUser;

        @BeforeEach
        void setUpSpotifyUser() {
            spotifyUser = new SpotifyUser();
            spotifyUser.setSpotifyUserId("spotify-42");
            spotifyUser.setAccessToken("old-access");
            spotifyUser.setRefreshToken("old-refresh");
            when(spotifyUserRepository.findByUserId(USER_ID)).thenReturn(Optional.of(spotifyUser));
        }

        private void expectRefresh(String responseJson) {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "refresh_token");
            form.add("refresh_token", "old-refresh");
            server.expect(requestTo(TOKEN_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(header(HttpHeaders.AUTHORIZATION, basicAuth()))
                    .andExpect(content().formData(form))
                    .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));
        }

        @Test
        void testGetValidAccessToken_NoSpotifyUser_ThrowsEntityNotFound() {
            // Arrange
            when(spotifyUserRepository.findByUserId(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> service.getValidAccessToken(99L));
        }

        @Test
        void testGetValidAccessToken_TokenStillValid_ReturnsItWithoutRefreshing() {
            // Arrange
            spotifyUser.setTokenExpiration(Instant.now().plus(Duration.ofMinutes(30)));

            // Act
            String token = service.getValidAccessToken(USER_ID);

            // Assert
            assertEquals("old-access", token);
            server.verify();
            verify(spotifyUserRepository, never()).save(any());
        }

        @Test
        void testGetValidAccessToken_ExpiringWithinMargin_RefreshesIt() {
            // Arrange
            spotifyUser.setTokenExpiration(Instant.now().plusSeconds(30));
            expectRefresh("{\"access_token\":\"new-access\",\"expires_in\":3600}");

            // Act
            String token = service.getValidAccessToken(USER_ID);

            // Assert
            assertEquals("new-access", token);
            server.verify();
            verify(spotifyUserRepository).save(spotifyUser);
        }

        @Test
        void testGetValidAccessToken_RefreshWithoutExpiresIn_UsesDefaultLifetime() {
            // Arrange (regression: a missing expires_in used to throw a NullPointerException)
            spotifyUser.setTokenExpiration(Instant.now().minusSeconds(10));
            expectRefresh("{\"access_token\":\"new-access\"}");
            Instant before = Instant.now();

            // Act
            String token = service.getValidAccessToken(USER_ID);

            // Assert
            assertEquals("new-access", token);
            assertFalse(spotifyUser.getTokenExpiration().isBefore(before.plusSeconds(3600)));
            assertFalse(spotifyUser.getTokenExpiration().isAfter(Instant.now().plusSeconds(3600)));
            verify(spotifyUserRepository).save(spotifyUser);
        }

        @Test
        void testGetValidAccessToken_RefreshWithNonNumericExpiresIn_UsesDefaultLifetime() {
            // Arrange
            spotifyUser.setTokenExpiration(Instant.now().minusSeconds(10));
            expectRefresh("{\"access_token\":\"new-access\",\"expires_in\":\"soon\"}");
            Instant before = Instant.now();

            // Act
            service.getValidAccessToken(USER_ID);

            // Assert
            assertFalse(spotifyUser.getTokenExpiration().isBefore(before.plusSeconds(3600)));
        }

        @Test
        void testGetValidAccessToken_Expired_RefreshesAndKeepsRefreshTokenWhenNotReturned() {
            // Arrange
            spotifyUser.setTokenExpiration(Instant.now().minusSeconds(10));
            expectRefresh("{\"access_token\":\"new-access\",\"expires_in\":1800}");
            Instant before = Instant.now();

            // Act
            String token = service.getValidAccessToken(USER_ID);

            // Assert
            assertEquals("new-access", token);
            assertEquals("new-access", spotifyUser.getAccessToken());
            assertEquals("old-refresh", spotifyUser.getRefreshToken());
            assertFalse(spotifyUser.getTokenExpiration().isBefore(before.plusSeconds(1800)));
            verify(spotifyUserRepository).save(spotifyUser);
        }

        @Test
        void testGetValidAccessToken_RefreshReturnsNewRefreshToken_ReplacesIt() {
            // Arrange
            spotifyUser.setTokenExpiration(Instant.now().minusSeconds(10));
            expectRefresh("{\"access_token\":\"new-access\",\"refresh_token\":\"new-refresh\",\"expires_in\":3600}");

            // Act
            service.getValidAccessToken(USER_ID);

            // Assert
            assertEquals("new-refresh", spotifyUser.getRefreshToken());
        }

        @Test
        void testGetValidAccessToken_NoExpiration_RefreshesIt() {
            // Arrange
            spotifyUser.setTokenExpiration(null);
            expectRefresh("{\"access_token\":\"new-access\",\"expires_in\":3600}");

            // Act
            String token = service.getValidAccessToken(USER_ID);

            // Assert
            assertEquals("new-access", token);
            assertNotNull(spotifyUser.getTokenExpiration());
        }

        @Test
        void testGetValidAccessToken_RefreshRejected_ThrowsIllegalStateAndSavesNothing() {
            // Arrange
            spotifyUser.setTokenExpiration(Instant.now().minusSeconds(10));
            server.expect(requestTo(TOKEN_URL))
                    .andRespond(withBadRequest().body("{\"error\":\"invalid_grant\"}").contentType(MediaType.APPLICATION_JSON));

            // Act
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.getValidAccessToken(USER_ID));

            // Assert
            assertTrue(ex.getMessage().contains("Vuelve a vincular"));
            assertInstanceOf(HttpClientErrorException.class, ex.getCause());
            assertEquals("old-access", spotifyUser.getAccessToken());
            verify(spotifyUserRepository, never()).save(any());
        }

        @Test
        void testGetValidAccessToken_RefreshResponseWithoutAccessToken_ThrowsIllegalState() {
            // Arrange
            spotifyUser.setTokenExpiration(Instant.now().minusSeconds(10));
            expectRefresh("{\"expires_in\":3600}");

            // Act
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.getValidAccessToken(USER_ID));

            // Assert
            assertEquals("Error al renovar el token de Spotify", ex.getMessage());
            verify(spotifyUserRepository, never()).save(any());
        }

        @Test
        void testGetValidAccessToken_SpotifyServerError_ThrowsIllegalStateAndSavesNothing() {
            // Arrange
            spotifyUser.setTokenExpiration(Instant.now().minusSeconds(10));
            server.expect(requestTo(TOKEN_URL)).andRespond(withServerError());

            // Act & Assert: wrapped like the 4xx case, but with a "try later" message (the link is kept)
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.getValidAccessToken(USER_ID));
            assertInstanceOf(HttpServerErrorException.class, ex.getCause());
            assertTrue(ex.getMessage().contains("Int\u00e9ntalo m\u00e1s tarde"));
            verify(spotifyUserRepository, never()).save(any());
            assertNotNull(spotifyUser.getRefreshToken());
        }
    }
}
