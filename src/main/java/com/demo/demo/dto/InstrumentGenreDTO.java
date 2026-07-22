package com.demo.demo.dto;

import java.util.List;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Data Transfer Object for instrument-genre relationship information.
 *
 * This DTO is used to transfer data about the relationship between
 * a musical instrument and a genre, including associated lessons.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Data
public class InstrumentGenreDTO extends ItemDTO {

    /**
     * The ID of the associated instrument.
     * Must not be null.
     */
    @NotNull(message = "Instrument ID is required")
    private Long instrumentId;

    /**
     * The ID of the associated genre.
     * Must not be null.
     */
    @NotNull(message = "Genre ID is required")
    private Long genreId;

    /**
     * List of IDs of lessons associated with this instrument-genre combination.
     */
    private List<Long> lessonIds;

}
