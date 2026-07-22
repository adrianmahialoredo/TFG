package com.demo.demo.model;

import java.util.HashSet;
import java.util.Set;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Represents a musical genre associated with an instrument.
 * 
 * The InstrumentGenreScreen entity is used to store information about the relationship
 * between a musical instrument and its associated genres. It supports many-to-one
 * relationships with both the Instrument and Genre entities, as well as a many-to-many
 * relationship with the Lesson entity.
 * 
 * An InstrumentGenreScreen can be associated with one Instrument and one Genre,
 * but it can have multiple Lessons associated with it.
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
@Table(name = "instrument_genres")
@DiscriminatorValue("INSTRUMENT_GENRE")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InstrumentGenre extends Item {

    /**
     * Relation InstrumentGenreScreen - Instrument
     * This is a many-to-one relationship, meaning that many InstrumentGenres can use the same Instrument.
     * An InstrumentGenreScreen can be associated with one Instrument, but an Instrument can be used in many InstrumentGenres.
     */
    @NotNull(message = "Instrument cannot be null")
    @ManyToOne
    private Instrument instrument;

    /**
     * Relation InstrumentGenreScreen - Genre
     * This is a many-to-one relationship, meaning that many InstrumentGenres can belong to the same Genre.
     * An InstrumentGenreScreen can only be associated with one Genre, but a Genre can have many InstrumentGenres.
     */
    @NotNull(message = "Genre cannot be null")
    @ManyToOne
    private Genre genre;

    /**
     * Relation InstrumentGenreScreen - Lesson
     * This is a many-to-many relationship, meaning that many InstrumentGenres can be associated with many Lessons.
     * An InstrumentGenreScreen can have multiple Lessons, and a Lesson can be associated with multiple InstrumentGenres.
     */
    @ManyToMany
    @JoinTable(
        name = "instrument_genre_lesson",
        joinColumns = @JoinColumn(name = "instrument_genre_id"),
        inverseJoinColumns = @JoinColumn(name = "lesson_id")
    )
    private Set<Lesson> lessons = new HashSet<>();
}
