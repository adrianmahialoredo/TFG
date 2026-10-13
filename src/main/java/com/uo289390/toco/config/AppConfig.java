package com.uo289390.toco.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

/**
 * Application configuration class for Spring Boot.
 *
 * Defines and configures beans for RestTemplate and BCryptPasswordEncoder
 * that are used throughout the application for making HTTP requests and
 * encrypting passwords respectively.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Configuration
public class AppConfig {

    /**
     * Creates and configures a RestTemplate bean.
     *
     * RestTemplate is used for making HTTP requests to external APIs,
     * such as the Spotify Web API, within the application.
     *
     * @return A new RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Creates and configures a BCryptPasswordEncoder bean.
     *
     * BCryptPasswordEncoder is used to hash and verify user passwords,
     * ensuring secure storage and validation of credentials.
     *
     * @return A new BCryptPasswordEncoder instance
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
