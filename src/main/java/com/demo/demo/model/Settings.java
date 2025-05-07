package com.demo.demo.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing user settings.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean notifications;
    private boolean sound;

    // Getters and setters
}
