package com.uo289390.toco.model;

import com.uo289390.toco.utils.Identifiable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents a tuning of an instrument (e.g. "Estándar" or "Drop D" for the guitar).
 *
 * The tuner of the app offers every tuning of the catalog: it shows one button per string
 * and compares the pitch captured by the microphone with the note of the selected string.
 *
 * The notes are written in scientific pitch notation (note name, optional sharp and octave:
 * {@code E2}, {@code F#3}), one per string, from the lowest (thickest) string to the highest.
 * Their frequencies are not stored: the app derives them from the note (A4 = 440 Hz).
 *
 * Like the rest of the catalog, the id is assigned manually (it comes from {@code data/tunes.json}).
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-10-23
 */
@Entity
@Table(name = "tunes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tune implements Identifiable<Long> {

    /**
     * Valid note of a string: note name (A-G), optional sharp and octave (0-8).
     */
    public static final String NOTE_PATTERN = "^[A-G]#?[0-8]$";

    /**
     * Unique identifier for the tuning.
     * This is the primary key of the Tune entity.
     */
    @Id
    private Long id;

    /**
     * Name of the tuning (e.g. "Estándar", "Drop D").
     * The app shows it next to the instrument name: "Guitarra (Drop D)".
     */
    @Column(nullable = false)
    @NotBlank(message = "Tune title cannot be blank")
    private String title;

    /**
     * Instrument the tuning belongs to. An instrument can have several tunings.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    @NotNull(message = "Tune must belong to an instrument")
    private Instrument instrument;

    /**
     * Note of each string, from the lowest to the highest.
     * The order is part of the data (it is the order of the strings), so it is a list with an
     * explicit order column.
     */
    @ElementCollection
    @CollectionTable(name = "tune_notes", joinColumns = @JoinColumn(name = "tune_id"))
    @OrderColumn(name = "string_index")
    @Column(name = "note", nullable = false)
    @NotEmpty(message = "Tune must have at least one note")
    private List<@Pattern(regexp = NOTE_PATTERN, message = "Notes must look like E2 or F#3") String> notes;

    /**
     * Two Tunes are equal when they have the same id, so collections (e.g. {@link java.util.Set})
     * never hold the same Tune twice even if it was loaded in different places or is a Hibernate proxy.
     * Ids are assigned manually before the entity is added to any collection, so the hash is stable.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tune other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(getId());
    }
}
