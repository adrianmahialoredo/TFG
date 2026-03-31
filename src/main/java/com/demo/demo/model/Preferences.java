package com.demo.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Represents user musical preferences.
 *
 * This entity stores information about a user's musical preferences,
 * including their preferred instrument and favorite musical genre.
 * These preferences can be used to personalize the learning experience
 * and provide relevant recommendations.
 *
 * This class is annotated with JPA annotations to map it to a database table,
 * and it uses Lombok annotations to reduce boilerplate code for getters, setters,
 * constructors, and other common methods.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Preferences {

    /**
     * Unique identifier for the user preferences.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user's preferred musical instrument.
     */
    @NotBlank(message = "Preferred instrument cannot be blank")
    @Size(min = 2, max = 100, message = "Instrument name must be between 2 and 100 characters")
    private String preferredInstrument;

    /**
     * The user's preferred musical genre.
     */
    @NotBlank(message = "Musical genre cannot be blank")
    @Size(min = 2, max = 100, message = "Genre name must be between 2 and 100 characters")
    private String musicalGenre;

}

