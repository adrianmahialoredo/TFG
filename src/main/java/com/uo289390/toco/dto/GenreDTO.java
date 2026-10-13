package com.uo289390.toco.dto;

import com.uo289390.toco.model.Item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GenreDTO extends ItemDTO {

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
     * List of IDs of lessons associated with this genre.
     */
    private List<Long> lessonIds;

    @Override
    public Item.Type getType() {
        return Item.Type.Genre;
    }
}
