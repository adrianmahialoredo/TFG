package com.demo.demo.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Represents a musical genre associated with an instrument.
 * 
 * The InstrumentGenreScreen entity is used to store information about the relationship
 * between a musical instrument and its associated genres. It supports many-to-one
 * relationships with both the Instrument and Genre entities, as well as an ordered one-to-many
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
@NoArgsConstructor
@AllArgsConstructor
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
     * Relation InstrumentGenre - Lesson
     * This is a one-to-many relationship, meaning that an InstrumentGenre can have many Lessons,
     * and a Lesson belongs to at most one InstrumentGenre ({@link Lesson#getInstrumentGenre()} owns the relationship).
     * Lessons are ordered by their position inside the InstrumentGenre.
     */
    @OneToMany(mappedBy = "instrumentGenre")
    @OrderBy("indexInInstrumentGenre ASC")
    @JsonIgnore
    private Set<Lesson> lessons = new LinkedHashSet<>();

    /**
     * Replaces the lessons of the InstrumentGenre keeping both sides of the relationship in sync:
     * the given lessons join this InstrumentGenre in the given order (their index is their position)
     * and the lessons that are no longer included leave it.
     *
     * @param newLessons the lessons of the InstrumentGenre, in order
     */
    public void setLessons(Collection<Lesson> newLessons) {
        List<Lesson> ordered = newLessons == null ? List.of() : new ArrayList<>(new LinkedHashSet<>(newLessons));

        for (Lesson previous : lessons) {
            if (!ordered.contains(previous)) {
                previous.leaveInstrumentGenre();
            }
        }
        lessons.clear();

        int index = 0;
        for (Lesson lesson : ordered) {
            InstrumentGenre current = lesson.getInstrumentGenre();
            if (current != null && current != this) {
                current.getLessons().remove(lesson);
            }
            lesson.joinInstrumentGenre(this, index++);
            lessons.add(lesson);
        }
    }
}
