package com.uo289390.toco.security;

import com.uo289390.toco.model.Role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.List;

import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Unit tests for JwtAuthenticationFilter class.
 * Tests how the Authorization header is parsed, when the security context is
 * populated and that the filter chain always continues.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest("GET", "/api/users/me");
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void assertChainContinued() {
        assertSame(request, filterChain.getRequest(), "The filter chain must always continue");
    }

    @Test
    void testNoAuthorizationHeader_LeavesContextEmpty() throws Exception {
        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtil);
        assertChainContinued();
    }

    @Test
    void testNonBearerHeader_IsIgnored() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtil);
        assertChainContinued();
    }

    @Test
    void testLowercaseBearerPrefix_IsIgnored() throws Exception {
        request.addHeader("Authorization", "bearer some-token");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void testBearerPrefixWithoutSpace_IsIgnored() throws Exception {
        request.addHeader("Authorization", "Bearertoken");

        filter.doFilter(request, response, filterChain);

        verifyNoInteractions(jwtUtil);
        assertChainContinued();
    }

    @Test
    void testValidToken_PopulatesSecurityContextWithUserId() throws Exception {
        request.addHeader("Authorization", "Bearer valid-token");
        when(jwtUtil.extractUserId("valid-token")).thenReturn(Optional.of(42L));
        when(jwtUtil.validateToken("valid-token", 42L)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(42L, authentication.getPrincipal());
        assertEquals("42", authentication.getName());
        assertNull(authentication.getCredentials());
        // No role claim (token issued before roles existed): counts as a regular user
        assertEquals(List.of("ROLE_USER"), authorities(authentication));
        assertTrue(authentication.isAuthenticated());
        assertChainContinued();
    }

    @Test
    void testValidTokenWithAdminRole_GrantsRoleAdmin() throws Exception {
        request.addHeader("Authorization", "Bearer admin-token");
        when(jwtUtil.extractUserId("admin-token")).thenReturn(Optional.of(1L));
        when(jwtUtil.validateToken("admin-token", 1L)).thenReturn(true);
        when(jwtUtil.extractRole("admin-token")).thenReturn(Optional.of(Role.ADMIN));

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(List.of("ROLE_ADMIN"), authorities(authentication));
        assertChainContinued();
    }

    private static List<String> authorities(Authentication authentication) {
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }

    @Test
    void testTokenIsPassedWithoutBearerPrefix() throws Exception {
        request.addHeader("Authorization", "Bearer abc.def.ghi");
        when(jwtUtil.extractUserId("abc.def.ghi")).thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        verify(jwtUtil).extractUserId("abc.def.ghi");
    }

    @Test
    void testUnparseableToken_LeavesContextEmpty() throws Exception {
        request.addHeader("Authorization", "Bearer garbage");
        when(jwtUtil.extractUserId("garbage")).thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtUtil, never()).validateToken(anyString(), anyLong());
        assertChainContinued();
    }

    @Test
    void testTokenFailingValidation_LeavesContextEmpty() throws Exception {
        request.addHeader("Authorization", "Bearer expired-token");
        when(jwtUtil.extractUserId("expired-token")).thenReturn(Optional.of(7L));
        when(jwtUtil.validateToken("expired-token", 7L)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertChainContinued();
    }

    @Test
    void testEmptyBearerToken_LeavesContextEmpty() throws Exception {
        request.addHeader("Authorization", "Bearer ");
        when(jwtUtil.extractUserId("")).thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertChainContinued();
    }

    @Test
    void testExceptionInJwtUtil_IsSwallowedAndChainContinues() throws Exception {
        request.addHeader("Authorization", "Bearer boom");
        when(jwtUtil.extractUserId(any())).thenThrow(new RuntimeException("unexpected"));

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertChainContinued();
        assertEquals(200, response.getStatus());
    }

    @Test
    void testExceptionDuringValidation_IsSwallowedAndChainContinues() throws Exception {
        request.addHeader("Authorization", "Bearer boom");
        when(jwtUtil.extractUserId("boom")).thenReturn(Optional.of(1L));
        when(jwtUtil.validateToken("boom", 1L)).thenThrow(new IllegalStateException("unexpected"));

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertChainContinued();
    }

    @Test
    void testWithRealJwtUtil_EndToEnd() throws Exception {
        JwtUtil realJwtUtil = new JwtUtil();
        org.springframework.test.util.ReflectionTestUtils.setField(realJwtUtil, "secretKey",
                "testSecureKeyForJWTTesting1234567890");
        realJwtUtil.init();
        JwtAuthenticationFilter realFilter = new JwtAuthenticationFilter(realJwtUtil);
        request.addHeader("Authorization", "Bearer " + realJwtUtil.generateToken(99L, Role.USER));

        realFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("99", authentication.getName());
        assertChainContinued();
    }
}
