package com.demo.demo.model;

import java.util.*;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a musical instrument in the system.
 * 
 * The entity is used to store information about various musical instruments,
 * including their name, image URL, and the songs associated with them.
 * 
 * The Instrument entity is designed to be flexible, allowing for different types of instruments
 * such as guitars, pianos, drums, and others. It supports a many-to-many relationship
 * with the Song entity, meaning that an instrument can be associated with multiple songs,
 * and a song can feature multiple instruments.
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
@Table(name = "instruments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Instrument {

    /**
     * Unique identifier for the instrument.
     * This is the primary key of the Instrument entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * URL of the image representing the item.
     * This field is optional and can be null.
     */
    @Column(nullable = false)
    protected String imageUrl;

    /**
     * Name of the instrument.
     * This field is required and must be unique.
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Set of songs associated with the instrument.
     * This is a many-to-many relationship, meaning that an instrument can be associated with multiple songs,
     * and a song can feature multiple instruments.
     */
    @ManyToMany
    @JoinTable(
        name = "instrument_song",
        joinColumns = @JoinColumn(name = "instrument_id"),
        inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private Set<Song> songs = new HashSet<>();

}

