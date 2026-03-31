package com.demo.demo.dto;

import java.util.List;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.URL;

/**
 * Data Transfer Object for musical song information.
 *
 * This DTO is used to transfer song data between the client and server
 * during API operations. It includes the song's basic information,
 * associated genres, instruments, and lessons.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Data
public class SongDTO {

    /**
     * The unique identifier of the song.
     */
    private Long id;

    /**
     * The title of the song.
     * Must not be blank and must be between 1 and 200 characters.
     */
    @NotBlank(message = "Song title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    /**
     * The artist or composer of the song.
     * Must not be blank and must be between 1 and 200 characters.
     */
    @NotBlank(message = "Artist name is required")
    @Size(min = 1, max = 200, message = "Artist must be between 1 and 200 characters")
    private String artist;

    /**
     * The URL of the song's cover image.
     * Must be a valid URL format if provided.
     */
    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;

    /**
     * List of IDs of genres associated with this song.
     */
    private List<Long> genreIds;

    /**
     * List of IDs of instruments featured in this song.
     */
    private List<Long> instrumentIds;

    /**
     * List of IDs of lessons available for this song.
     */
    private List<Long> lessons;
    
}
