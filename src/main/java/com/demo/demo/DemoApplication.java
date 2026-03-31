package com.demo.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.demo.demo.config.SpotifyProperties;

/**
 * Main entry point for the MusicApp Spring Boot application.
 *
 * This class initializes the Spring Boot application and enables configuration
 * property scanning for Spotify-related settings.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@SpringBootApplication
@ConfigurationPropertiesScan("com.demo.demo.config")
public class DemoApplication {

	/**
	 * Main method to start the Spring Boot application.
	 *
	 * @param args Command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
