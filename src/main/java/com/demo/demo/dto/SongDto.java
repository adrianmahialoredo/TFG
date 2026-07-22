package com.demo.demo.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

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
public class SongDTO extends ItemDTO {

    /**
     * The artist or composer of the song.
     * Must not be blank and must be between 1 and 200 characters.
     */
    @NotBlank(message = "Artist name is required")
    @Size(min = 1, max = 200, message = "Artist must be between 1 and 200 characters")
    private String artist;

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
    private List<Long> lessonIds;

    private List<SongLessonDTO> lessons;
    
}
