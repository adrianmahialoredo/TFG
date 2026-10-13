package com.uo289390.toco.dto;

import com.uo289390.toco.model.Item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

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
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
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
     * Indicates whether the lesson belongs to a specific instrument-genre.
     */
    private boolean belongsToInstrumentGenre;

    /**
     * The ID of the instrument-genre this lesson belongs to (if applicable).
     */
    private Long instrumentGenreId;

    /**
     * Position of the lesson inside its instrument-genre (0-based).
     */
    private int indexInInstrumentGenre;

    /**
     * The ID of the instrument this lesson is about (if applicable).
     */
    private Long instrumentId;

    /**
     * List of IDs of sections that make up this lesson.
     */
    private List<Long> sectionIds;

    private List<LessonSectionDTO> sections;

    /**
     * Indicates whether the lesson is featured as popular on the Home screen.
     */
    private boolean popular;

    @Override
    public Item.Type getType() {
        return Item.Type.Lesson;
    }
}
