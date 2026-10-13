package com.uo289390.toco.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents user application settings.
 *
 * This entity stores user-specific settings and preferences for the application,
 * such as notification and sound preferences. These settings allow users to customize
 * their experience within the application.
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
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

    /**
     * Unique identifier for the user settings.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Flag indicating whether notifications are enabled.
     */
    private boolean notifications;

    /**
     * Flag indicating whether sound is enabled.
     */
    private boolean sound;

}
