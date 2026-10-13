package com.uo289390.toco.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.uo289390.toco.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Explicit Spring Security configuration.
 *
 * Access rules:
 * - Public: {@code /api/auth/**} (login and register), {@code /api/spotify/callback} (Spotify
 *   redirects the browser there without the JWT; the user is identified by the OAuth state),
 *   {@code /actuator/health} and {@code /error}.
 * - ADMIN only: creating, updating and deleting catalog content (genres, songs, lessons,
 *   chords, instruments...), listing and creating users directly, listing every item list and
 *   the rest of the actuator.
 * - Any other request requires a valid JWT. Batch reads ({@code POST /api/<resource>/by-ids})
 *   are reads, so any authenticated user can perform them.
 * - Ownership of user data (profile, favourites, item lists, personal recommendations), both for
 *   reading and for writing, is checked per endpoint with {@code @PreAuthorize} and
 *   {@code OwnershipChecker} (method security).
 *
 * Unauthenticated requests get 401 (not 403) so the app can tell an expired session apart from
 * a forbidden action. CSRF is disabled (the API is stateless and token-based) and no server-side
 * HTTP session is created.
 *
 * @author Adrián Mahía
 * @version 1.1
 * @since 2026-06-20
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Catalog resources: everyone authenticated can read them, only admins can modify them.
     */
    private static final String[] CATALOG_PATHS = {
            "/api/genres/**",
            "/api/songs/**",
            "/api/song-lessons/**",
            "/api/tab-sections/**",
            "/api/chords/**",
            "/api/lessons/**",
            "/api/lesson-sections/**",
            "/api/instruments/**",
            "/api/instrument-genres/**"
    };

    /**
     * JWT filter responsible for populating the security context from the
     * Authorization header.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Defines the application's security filter chain.
     *
     * @param http the {@link HttpSecurity} builder provided by Spring Security
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the chain cannot be built
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/spotify/callback", "/actuator/health", "/error").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // The registration form lets new users pick favourite genres and instruments before having a token
                        .requestMatchers(HttpMethod.GET, "/api/genres", "/api/instruments").permitAll()
                        // Batch reads use POST (the IDs travel in the body) but do not modify anything
                        .requestMatchers(HttpMethod.POST, "/api/*/by-ids").authenticated()
                        .requestMatchers(HttpMethod.POST, CATALOG_PATHS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, CATALOG_PATHS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, CATALOG_PATHS).hasRole("ADMIN")
                        // Users register through /api/auth/register; the raw collection is for admins
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/item-lists").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
