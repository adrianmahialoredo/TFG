package com.demo.demo.model;

import java.util.*;
import lombok.*;
import jakarta.persistence.*;

/**
 * Represents a music lesson in the application.
 * 
 * The Lesson entity is used to store information about a lesson, including its association
 * with a specific genre and its sections. It extends the Item class, which provides common
 * attributes for all items in the application.
 * 
 * A lesson can belong to a specific genre, and it can have multiple sections that make up the lesson content, 
 * having at least one section.
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
@Table(name = "songs")
@DiscriminatorValue("LESSON")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Lesson extends Item {

    /**
     * Indicates whether the lesson belongs to a specific genre.
     */
    private boolean belongsToGenre;

    /**
     * Relation with Genre
     * This is a many-to-one relationship, meaning that many lessons can belong to one genre.
     * A lesson can be associated with a genre, but it is not mandatory.
     * If a lesson does not belong to any genre, this field can be null.
     */
    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;   

    /**
     * List of sections that make up the lesson.
     * This is a one-to-many relationship, meaning that a lesson can have multiple sections.
     * Each section is associated with this lesson, and the order of sections is maintained by the orderIndex field.
     */
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<LessonSection> sections = new ArrayList<>();

}
