package com.uo289390.toco.dto;

import com.uo289390.toco.model.Tune;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object for the tunings of an instrument.
 *
 * The instrument travels as its id. The notes go from the lowest to the highest string,
 * in scientific pitch notation ({@code E2}, {@code F#3}).
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-10-23
 */
@Data
public class TuneDTO {

    /**
     * The unique identifier of the tuning.
     */
    private Long id;

    /**
     * The name of the tuning (e.g. "Estándar", "Drop D").
     */
    @NotBlank(message = "Tune title is required")
    @Size(min = 1, max = 50, message = "Tune title must be between 1 and 50 characters")
    private String title;

    /**
     * The ID of the instrument the tuning belongs to.
     */
    @NotNull(message = "Instrument ID is required")
    private Long instrumentId;

    /**
     * Note of each string, from the lowest to the highest.
     */
    @NotEmpty(message = "Tune must have at least one note")
    private List<@Pattern(regexp = Tune.NOTE_PATTERN, message = "Notes must look like E2 or F#3") String> notes;
}
