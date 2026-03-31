package com.demo.demo.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object for musical genre information.
 *
 * This DTO is used to transfer genre data between the client and server
 * during API operations. It includes the genre's basic information,
 * media URLs, and associations with songs and lessons.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Data
public class GenreDTO {
    
    /**
     * The unique identifier of the genre.
     */
    private Long id;

    /**
     * The name of the musical genre.
     * Must not be blank and must be between 1 and 100 characters.
     */
    @NotBlank(message = "Genre name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    /**
     * A detailed description of the genre.
     * Must be less than 1000 characters if provided.
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    /**
     * The URL of the banner image for the genre.
     * Must be a valid URL format if provided.
     */
    @Pattern(regexp = "^https?://.+|^$", message = "Banner URL must be a valid HTTP/HTTPS URL or empty")
    private String bannerUrl;

    /**
     * The URL of the image representing the genre.
     * Must be a valid URL format if provided.
     */
    @Pattern(regexp = "^https?://.+|^$", message = "Image URL must be a valid HTTP/HTTPS URL or empty")
    private String imageUrl;

    /**
     * The Spotify video ID for the genre's welcome introduction video.
     */
    @Size(max = 100, message = "Welcome intro video ID must not exceed 100 characters")
    private String welcomeIntroVideoId;

    /**
     * The Spotify video ID for the genre's welcome song video.
     */
    @Size(max = 100, message = "Welcome song video ID must not exceed 100 characters")
    private String welcomeSongVideoId;

    /**
     * List of IDs of songs associated with this genre.
     */
    private List<Long> songIds;

    /**
     * List of IDs of lessons associated with this genre.
     */
    private List<Long> lessonIds;

}
