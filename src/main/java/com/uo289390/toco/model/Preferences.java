package com.uo289390.toco.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the musical preferences owned by a {@link User}.
 *
 * This entity groups the musical preferences that previously lived directly
 * inside {@link User}: the favourite genres, favourite instruments and the
 * desired difficulty level. It shares its primary key with the owning
 * {@code User} through {@link MapsId}, so {@code preferences.id == user.id}.
 * Thanks to that, the existing {@code user_favorite_genres} and
 * {@code user_favorite_instruments} join tables keep using the {@code user_id}
 * column and their stored data remains valid.
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
@Table(name = "preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Preferences {

    public static final int MIN_NUMBER_OF_FAVOURITE_GENRES = 2;
    public static final int MIN_NUMBER_OF_FAVOURITE_INSTRUMENTS = 2;

    /**
     * Unique identifier for the preferences, shared with the owning
     * {@link User} via {@link MapsId}.
     */
    @Id
    private Long id;

    /**
     * The {@link User} that owns these preferences. The preferences entity
     * shares the user's primary key.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    /**
     * The user's desired difficulty level.
     */
    @Column(name = "difficulty_level", nullable = false)
    @Min(value = 0, message = "Difficulty level must be at least 0")
    @Max(value = 100, message = "Difficulty level must be at most 100")
    private int difficultyLevel = 0;    // Between 0 and 100

    /**
     * The user's favourite musical genres.
     */
    @ManyToMany
    @JoinTable(
        name = "user_favorite_genres",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> favoriteGenres = new HashSet<>();

    /**
     * The user's favourite musical instruments.
     */
    @ManyToMany
    @JoinTable(
        name = "user_favorite_instruments",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "instrument_id")
    )
    private Set<Instrument> favoriteInstruments = new HashSet<>();

}

