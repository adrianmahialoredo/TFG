package com.demo.demo.model;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * Represents a musical genre in the system.
 * 
 * The Genre entity is used to store information about various musical genres,
 * including their description, banner URL, and welcome videos.
 * 
 * The Genre entity is designed to be flexible, allowing for different types of genres
 * such as rock, pop, jazz, classical, and others. It supports a one-to-many relationship
 * with the Lesson entity, meaning that a genre can have multiple lessons associated with it.
 * 
 * Additionally, it supports a many-to-many relationship with the Song entity,
 * meaning that many songs can belong to many genres.
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
@Table(name = "genres")
@DiscriminatorValue("GENRE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Genre extends Item {
    
    /**
     * Description of the genre.
     */
    @Lob
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Description cannot be blank")
    private String description;

    /**
     * URL of the banner image for the genre.
     * This field is required and must not be blank.
     */
    @NotBlank(message = "Banner URL cannot be blank")
    @Pattern(regexp = "^https?://.+", message = "Banner URL must be a valid HTTP/HTTPS URL")
    private String bannerUrl;

    /**
     * URL of the welcome introduction video for the genre.
     * This field is required and must not be blank.
     */
    @NotBlank(message = "Welcome intro video URL cannot be blank")
    @Column(name = "welcome_intro_video", nullable = false)
    private String welcomeIntroVideoId;

    /**
     * URL of the welcome song video for the genre.
     * This field is required and must not be blank.
     */
    @NotBlank(message = "Welcome song video URL cannot be blank")
    @Column(name = "welcome_song_video", nullable = false)
    private String welcomeSongVideoId;


    /**
     * Relation with Lesson
     * This is a one-to-many relationship, meaning that one genre can have many lessons.
     */
    @OneToMany(mappedBy = "genre")
    @JsonIgnore
    private Set<Lesson> lessons = new HashSet<>();
}
