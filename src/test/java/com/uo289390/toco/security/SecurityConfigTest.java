package com.uo289390.toco.security;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import jakarta.servlet.Filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uo289390.toco.config.SecurityConfig;

/**
 * Tests for SecurityConfig class.
 * Verifies, through a probe controller, that the security filter chain requires
 * authentication (401 without a valid token), has CSRF disabled, creates no HTTP session and
 * populates the security context from a Bearer token via JwtAuthenticationFilter.
 * The per-route access rules are covered in {@link AccessRulesTest}.
 */
@WebMvcTest(controllers = SecurityConfigTest.ProbeController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityConfigTest.ProbeController.class})
class SecurityConfigTest {

    /**
     * Minimal controller exposing the authenticated principal, used only by this test.
     */
    @RestController
    @RequestMapping("/probe")
    static class ProbeController {

        @GetMapping
        String whoAmI(Authentication authentication) {
            return authentication == null ? "anonymous" : authentication.getName();
        }

        @PostMapping
        String write() {
            return "written";
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @MockitoBean
    private JwtUtil jwtUtil;

    private void authenticateAs(String token, long userId) {
        when(jwtUtil.extractUserId(token)).thenReturn(Optional.of(userId));
        when(jwtUtil.validateToken(token, userId)).thenReturn(true);
    }

    @Test
    void testAnonymousRequest_IsUnauthorized() throws Exception {
        mockMvc.perform(get("/probe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testStateChangingRequestWithoutCsrfToken_IsPermitted() throws Exception {
        authenticateAs("good", 42L);

        mockMvc.perform(post("/probe").header("Authorization", "Bearer good")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("written"));
    }

    @Test
    void testUnknownRoute_AnonymousIsUnauthorized() throws Exception {
        mockMvc.perform(get("/does-not-exist"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUnknownRoute_AuthenticatedReachesMvc() throws Exception {
        authenticateAs("good", 42L);

        // Unmapped routes reach MVC and yield 404 rather than 401/403
        int status = mockMvc.perform(get("/does-not-exist").header("Authorization", "Bearer good"))
                .andReturn().getResponse().getStatus();
        assertTrue(status != 401 && status != 403, "Unexpected security status " + status);
    }

    @Test
    void testOtherMethodsWithoutCsrf_AreNotForbidden() throws Exception {
        authenticateAs("good", 42L);

        int putStatus = mockMvc.perform(put("/probe").header("Authorization", "Bearer good"))
                .andReturn().getResponse().getStatus();
        int deleteStatus = mockMvc.perform(delete("/probe").header("Authorization", "Bearer good"))
                .andReturn().getResponse().getStatus();

        // No handler for PUT/DELETE: 405 proves the request passed the security chain (not 403)
        assertTrue(putStatus == 405, "PUT status " + putStatus);
        assertTrue(deleteStatus == 405, "DELETE status " + deleteStatus);
    }

    @Test
    void testValidBearerToken_AuthenticatesRequest() throws Exception {
        authenticateAs("good", 42L);

        mockMvc.perform(get("/probe").header("Authorization", "Bearer good"))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }

    @Test
    void testInvalidBearerToken_IsUnauthorized() throws Exception {
        when(jwtUtil.extractUserId("bad")).thenReturn(Optional.empty());

        mockMvc.perform(get("/probe").header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAuthenticationDoesNotLeakBetweenRequests() throws Exception {
        authenticateAs("good", 42L);

        mockMvc.perform(get("/probe").header("Authorization", "Bearer good"))
                .andExpect(content().string("42"));
        mockMvc.perform(get("/probe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testStatelessPolicy_CreatesNoSession() throws Exception {
        when(jwtUtil.extractUserId("good")).thenReturn(Optional.of(42L));
        when(jwtUtil.validateToken("good", 42L)).thenReturn(true);

        MvcResult result = mockMvc.perform(get("/probe").header("Authorization", "Bearer good"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andReturn();

        assertNull(result.getRequest().getSession(false), "No HTTP session must be created");
    }

    @Test
    void testFilterChain_ContainsJwtFilterBeforeAnonymousAndNoCsrfFilter() {
        List<Filter> filters = securityFilterChain.getFilters();

        int jwtIndex = indexOf(filters, JwtAuthenticationFilter.class);
        int anonymousIndex = indexOf(filters, AnonymousAuthenticationFilter.class);

        assertTrue(jwtIndex >= 0, "JwtAuthenticationFilter must be registered in the chain");
        assertTrue(anonymousIndex < 0 || jwtIndex < anonymousIndex,
                "JwtAuthenticationFilter must run before the anonymous filter");
        assertTrue(indexOf(filters, CsrfFilter.class) < 0, "CSRF must be disabled");
    }

    private static int indexOf(List<Filter> filters, Class<?> type) {
        for (int i = 0; i < filters.size(); i++) {
            if (type.isInstance(filters.get(i))) {
                return i;
            }
        }
        return -1;
    }
}
