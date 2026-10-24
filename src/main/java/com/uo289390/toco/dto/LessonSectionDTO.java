package com.uo289390.toco.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * Data Transfer Object for lesson section information.
 *
 * This DTO is used to transfer lesson section data between the client and server
 * during API operations. It includes the section's basic information and its
 * associated lesson.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Data
public class LessonSectionDTO {

    /**
     * The unique identifier of the lesson section.
     */
    private Long id;

    /**
     * The title of the lesson section.
     * Must not be blank and must be between 1 and 200 characters.
     */
    @NotBlank(message = "Section title is required")
    @Size(min = 1, max = 200, message = "Section title must be between 1 and 200 characters")
    private String title;

    /**
     * The HTML content of the lesson section.
     * Must not be blank.
     */
    @NotBlank(message = "Section content is required")
    private String htmlContent;

    /**
     * Optional URL of a support video for the section (a YouTube link).
     */
    @URL(message = "Video URL must be a valid URL")
    private String videoUrl;

    /**
     * The ID of the lesson this section belongs to.
     */
    @NotNull(message = "Lesson ID is required")
    private Long lessonId;

    /**
     * The order index of the section within the lesson.
     */
    @Min(value = 0, message = "Order index must be 0 or greater")
    private int orderIndex;
}

