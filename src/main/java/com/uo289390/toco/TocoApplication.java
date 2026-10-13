package com.uo289390.toco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

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
@ConfigurationPropertiesScan("com.uo289390.toco.config")
public class TocoApplication {

	/**
	 * Main method to start the Spring Boot application.
	 *
	 * @param args Command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(TocoApplication.class, args);
	}

}
