package com.demo.demo.dto;

import java.util.List;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * Data Transfer Object for musical chord information.
 *
 * This DTO is used to transfer chord data between the client and server
 * during API operations. It includes the chord's name, notes, visual representation,
 * and various chord characteristics.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Data
public class ChordDto {
    
    /**
     * The unique identifier of the chord.
     */
    private Long id;

    /**
     * The name of the chord.
     * Must not be blank and must be between 1 and 50 characters.
     */
    @NotBlank(message = "Chord name is required")
    @Size(min = 1, max = 50, message = "Chord name must be between 1 and 50 characters")
    private String name;

    /**
     * List of notes that compose the chord.
     */
    private List<String> notes;

    /**
     * The URL of the chord diagram image.
     * Must be a valid URL format if provided.
     */
    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;

    /**
     * Indicates whether the chord is an open chord (can be played from the open string).
     */
    private boolean open;

    /**
     * Indicates whether the chord is a barre chord (requires barring across multiple strings).
     */
    private boolean barre;

    /**
     * Indicates whether the chord is a power chord.
     */
    private boolean powerChord;

    /**
     * Indicates whether the chord is a minor chord.
     */
    private boolean minor;

    /**
     * Indicates whether the chord is a major chord.
     */
    private boolean major;

    /**
     * Indicates whether the chord is a seventh chord.
     */
    private boolean seventh;

    /**
     * Indicates whether the chord requires capo placement.
     */
    private boolean capo;

    /**
     * The fret number where the capo should be placed (only relevant if capo is true).
     * Must be between 0 and 12 if capo is true.
     */
    @Min(value = 0, message = "Capo fret must be at least 0")
    @Max(value = 12, message = "Capo fret must be at most 12")
    private int capoFret; 

}
