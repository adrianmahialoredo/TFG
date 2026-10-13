package com.uo289390.toco.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import com.uo289390.toco.controller.SpotifyAuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.uo289390.toco.config.SecurityConfig;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.SpotifyAuthService;

/**
 * Web layer tests for {@link SpotifyAuthController}.
 * Covers the authenticated login URL endpoint (resolving the current user from the JWT)
 * and the HTML OAuth callback page.
 */
@WebMvcTest(SpotifyAuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SpotifyAuthControllerTest {

    private static final String VALID_TOKEN = "valid-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpotifyAuthService spotifyAuthService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private void givenValidTokenForUser(Long userId) {
        when(jwtUtil.extractUserId(VALID_TOKEN)).thenReturn(Optional.of(userId));
        when(jwtUtil.validateToken(VALID_TOKEN, userId)).thenReturn(true);
    }

    // ---------- /login ----------

    @Test
    void getLoginUrl_authenticated_returnsAuthUrlForCurrentUser() throws Exception {
        givenValidTokenForUser(42L);
        when(spotifyAuthService.buildAuthorizationUrl(42L)).thenReturn("https://accounts.spotify.com/authorize?state=x");

        mockMvc.perform(get("/api/spotify/login")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authUrl").value("https://accounts.spotify.com/authorize?state=x"));

        verify(spotifyAuthService).buildAuthorizationUrl(42L);
    }

    @Test
    void getLoginUrl_anonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/spotify/login"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));

        verifyNoInteractions(spotifyAuthService);
    }

    @Test
    void getLoginUrl_invalidToken_returnsUnauthorized() throws Exception {
        when(jwtUtil.extractUserId("bad-token")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/spotify/login")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer bad-token"))
                .andExpect(status().isUnauthorized());

        verify(spotifyAuthService, never()).buildAuthorizationUrl(anyLong());
    }

    @Test
    void getLoginUrl_expiredToken_returnsUnauthorized() throws Exception {
        when(jwtUtil.extractUserId(VALID_TOKEN)).thenReturn(Optional.of(42L));
        when(jwtUtil.validateToken(VALID_TOKEN, 42L)).thenReturn(false);

        mockMvc.perform(get("/api/spotify/login")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN))
                .andExpect(status().isUnauthorized());

        verify(spotifyAuthService, never()).buildAuthorizationUrl(anyLong());
    }

    @Test
    void getLoginUrl_headerWithoutBearerPrefix_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/spotify/login")
                        .header(HttpHeaders.AUTHORIZATION, VALID_TOKEN))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(jwtUtil);
    }

    @Test
    void getLoginUrl_jwtUtilThrows_isTreatedAsAnonymous() throws Exception {
        when(jwtUtil.extractUserId(anyString())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/spotify/login")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN))
                .andExpect(status().isUnauthorized());
    }

    // ---------- /callback ----------

    @Test
    void callback_success_returnsHtmlConfirmationPage() throws Exception {
        mockMvc.perform(get("/api/spotify/callback")
                        .param("code", "auth-code")
                        .param("state", "signed-state"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("<!doctype html>")))
                .andExpect(content().string(containsString("Spotify conectado")));

        verify(spotifyAuthService).handleCallback("auth-code", "signed-state");
    }

    @Test
    void callback_doesNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/spotify/callback")
                        .param("code", "auth-code")
                        .param("state", "signed-state"))
                .andExpect(status().isOk());

        verifyNoInteractions(jwtUtil);
    }

    @Test
    void callback_withError_returnsBadRequestPageAndSkipsService() throws Exception {
        mockMvc.perform(get("/api/spotify/callback")
                        .param("error", "access_denied")
                        .param("state", "signed-state"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("No se ha conectado Spotify (access_denied)")));

        verify(spotifyAuthService, never()).handleCallback(any(), any());
    }

    @Test
    void callback_withError_escapesHtmlInErrorParameter() throws Exception {
        mockMvc.perform(get("/api/spotify/callback")
                        .param("error", "<script>alert(1)</script>"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("&lt;script>alert(1)&lt;/script>")))
                .andExpect(content().string(not(containsString("<script>"))));
    }

    @Test
    void callback_withoutParameters_delegatesNullsToService() throws Exception {
        mockMvc.perform(get("/api/spotify/callback"))
                .andExpect(status().isOk());

        verify(spotifyAuthService).handleCallback(null, null);
    }

    @Test
    void callback_serviceFailure_doesNotReturnSuccess() throws Exception {
        doThrow(new IllegalArgumentException("Invalid state"))
                .when(spotifyAuthService).handleCallback("auth-code", "tampered");

        mockMvc.perform(get("/api/spotify/callback")
                        .param("code", "auth-code")
                        .param("state", "tampered"))
                .andExpect(status().isBadRequest());
    }
}
