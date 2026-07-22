package com.demo.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.demo.demo.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Explicit Spring Security configuration.
 *
 * This class makes the security setup explicit instead of relying on the
 * implicit registration of {@link JwtAuthenticationFilter} as a servlet filter.
 *
 * IMPORTANT: the current behaviour of the application is preserved. Every
 * endpoint remains publicly accessible ({@code permitAll}); the only structural
 * change is that the JWT filter is now wired explicitly into the Spring Security
 * filter chain, CSRF is disabled (the API is stateless and token-based) and no
 * server-side HTTP session is created.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-06-20
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

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
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

