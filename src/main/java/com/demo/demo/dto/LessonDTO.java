package com.demo.demo.dto;

import java.util.List;
import lombok.*;

/**
 * Data Transfer Object for musical lesson information.
 *
 * This DTO is used to transfer lesson data between the client and server
 * during API operations. It includes the lesson's basic information,
 * associated genre, and sections.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Data
public class LessonDTO extends ItemDTO {

    /**
     * Indicates whether the lesson belongs to a specific genre.
     */
    private boolean belongsToGenre;

    /**
     * The ID of the genre this lesson belongs to (if applicable).
     */
    private Long genreId;

    /**
     * List of IDs of sections that make up this lesson.
     */
    private List<Long> sectionIds;

    private List<LessonSectionDTO> sections;

    /**
     * Indicates whether the lesson is featured as popular on the Home screen.
     */
    private boolean popular;
}
