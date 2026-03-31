package com.demo.demo.dto;

import java.util.List;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * Data Transfer Object for musical instrument information.
 *
 * This DTO is used to transfer instrument data between the client and server
 * during API operations. It includes the instrument's name, image, and
 * associated songs.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Data
public class InstrumentDTO {
    
    /**
     * The unique identifier of the instrument.
     */
    private Long id;

    /**
     * The URL of the image representing the instrument.
     * Must be a valid URL format if provided.
     */
    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;

    /**
     * The name of the musical instrument.
     * Must not be blank and must be between 1 and 100 characters.
     */
    @NotBlank(message = "Instrument name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    /**
     * List of IDs of songs that feature this instrument.
     */
    private List<Long> songIds;

}
