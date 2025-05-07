package com.demo.demo.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing user preferences.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Preferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String preferredInstrument;
    private String musicalGenre;

}

