package com.uo289390.toco.dto;

import com.uo289390.toco.model.Item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

import jakarta.validation.constraints.*;

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
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
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

    @Override
    public Item.Type getType() {
        return Item.Type.InstrumentGenre;
    }
}
