package com.uo289390.toco.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.uo289390.toco.model.Role;

/**
 * JWT Authentication Filter for Spring Security.
 *
 * This filter intercepts incoming HTTP requests and extracts JWT tokens from
 * the Authorization header. It validates the tokens and establishes the authentication
 * context for authenticated users.
 *
 * The filter expects tokens in the format: Authorization: Bearer {token}
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Prefix Spring Security expects on role authorities ({@code hasRole("ADMIN")} checks {@code ROLE_ADMIN}).
     */
    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * JWT utility for token validation and extraction.
     */
    private final JwtUtil jwtUtil;

    /**
     * Filters incoming requests to extract and validate JWT tokens.
     *
     * Extracts the JWT token from the Authorization header, validates it,
     * and sets up the authentication context if the token is valid.
     *
     * @param request The HTTP request
     * @param response The HTTP response
     * @param filterChain The filter chain to continue processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract the token from the Authorization header, only if it starts with "Bearer "
            Optional.ofNullable(request.getHeader("Authorization"))
                    .filter(header -> header.startsWith(BEARER_PREFIX))
                    .map(header -> header.substring(BEARER_PREFIX.length()))
                    .ifPresent(token ->
                            // Validate the token
                            jwtUtil.extractUserId(token).ifPresentOrElse(userId -> {
                                if (jwtUtil.validateToken(token, userId)) {
                                    // Set up the authentication context for the authenticated user.
                                    // The principal is the user ID, so Authentication#getName returns it as a string.
                                    // Tokens without a role claim (issued before roles existed) count as USER.
                                    Role role = jwtUtil.extractRole(token).orElse(Role.USER);
                                    UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(userId, null,
                                                List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role.name())));
                                    SecurityContextHolder.getContext().setAuthentication(authentication);
                                    log.debug("JWT authentication successful for user id: {}", userId);
                                } else {
                                    log.warn("JWT token validation failed for request: {}", request.getRequestURI());
                                }
                            }, () -> log.warn("JWT token validation failed for request: {}", request.getRequestURI())));
        } catch (Exception e) {
            log.error("Error in JWT authentication filter: {}", e.getMessage(), e);
            // Continue with the filter chain even if there's an error
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

}
