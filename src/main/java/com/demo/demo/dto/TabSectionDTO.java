package com.demo.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Data Transfer Object for tab section information.
 *
 * This DTO is used to transfer tab section data between the client and server.
 * A tab section represents a portion of a musical tablature for a song lesson,
 * identified by a section name such as "intro" or "solo".
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Data
public class TabSectionDTO {
    
    /**
     * The unique identifier of the tab section.
     */
    private Long id;

    /**
     * The identifier of this tab section.
     * Examples: "intro", "solo", "verse", "chorus", etc.
     * Must not be blank and must be between 1 and 50 characters.
     */
    @NotBlank(message = "Tab section identifier is required")
    @Size(min = 1, max = 50, message = "Identifier must be between 1 and 50 characters")
    private String identifier;

    /**
     * The actual tab content in multi-line format.
     * Must not be blank and must be less than 10000 characters.
     */
    @NotBlank(message = "Tab section content is required")
    @Size(min = 1, max = 10000, message = "Content must be between 1 and 10000 characters")
    private String content;

    /**
     * The ID of the associated song lesson.
     * Must not be null.
     */
    @NotNull(message = "Song lesson ID is required")
    private Long songLessonId;

}
