package com.demo.demo.dto;

import java.util.List;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * Data Transfer Object for song lesson information.
 *
 * This DTO is used to transfer lesson data for a song between the client and server.
 * It includes the lesson's difficulty level, associated chords, lyrics with chord notations,
 * tab sections, and related multimedia content.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Data
public class SongLessonDTO {
    
    /**
     * The unique identifier of the song lesson.
     */
    private Long id;

    /**
     * The difficulty level of the lesson.
     * Possible values: "EASY", "MEDIUM", "ORIGINAL".
     * Must match one of the allowed difficulty levels.
     */
    @Pattern(regexp = "EASY|MEDIUM|ORIGINAL", message = "Difficulty must be EASY, MEDIUM, or ORIGINAL")
    private String difficulty;

    /**
     * List of IDs of tab sections associated with this lesson.
     */
    private List<Long> tabSectionsIds;

    private List<TabSectionDTO> tabSections;

    /**
     * List of IDs of chords associated with this lesson.
     */
    private List<Long> chordIds;

    /**
     * Lyrics of the song with chord notations in the format "[C]Letra".
     * Maximum 5000 characters to prevent excessive data.
     */
    @Size(max = 5000, message = "Lyrics must not exceed 5000 characters")
    private String chordedLyrics;

    /**
     * URL of the lesson video content.
     * Must be a valid URL format if provided.
     */
    @URL(message = "Video URL must be a valid URL")
    private String videoUrl;

    /**
     * ID of the associated song.
     * Must not be null.
     */
    @NotNull(message = "Song ID is required")
    private Long songId;

    /**
     * ID of the associated instrument.
     * Must not be null.
     */
    @NotNull(message = "Instrument ID is required")
    private Long instrumentId;

}
