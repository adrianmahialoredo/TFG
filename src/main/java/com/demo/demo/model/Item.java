package com.demo.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Represents a generic item in the system.
 * 
 * The Item class serves as a base entity for various types of items such as songs, lessons, genres, and instrument genres.
 * It includes common attributes like name, description, image URL, level (rating), creation timestamp, and type.
 * 
 * The point of this class is to provide a unified structure for items that can be added to item lists,
 * allowing for easy management and categorization of different item types within the application.
 * 
 * The type is represented as an enum, allowing for easy categorization of items.
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
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Item {

    /**
     * Unique identifier for the item.
     * This is the primary key of the Item entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    /**
     * Name of the item.
     * This field is optional and can be null.
     */
    @Column(nullable = true)
    @NotBlank(message = "Item name cannot be blank")
    protected String name;

    /**
     * URL of the image representing the item.
     * This field is optional and can be null.
     */
    @Column(nullable = false)
    @NotBlank(message = "Image URL cannot be blank")
    protected String imageUrl;

    /**
     * Level of the item, represented as a rating between 1 and 5 stars.
     * This field is optional and can be null.
     */
    @Column(nullable = true)
    @Min(value = 1, message = "Level must be at least 1")
    @Max(value = 5, message = "Level must be at most 5")
    protected int level;  // between 1 and 5 stars

    /**
     * Timestamp indicating when the item was created.
     * This field is automatically set to the current date and time when the item is created.
     */
    @Column(nullable = true)
    protected LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Type of the item, represented as an enum.
     * This field is used to categorize the item into different types such as Song, Lesson, Genre, or InstrumentGenre.
     */
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    protected Type type;

    /**
     * Enum representing the different types of items.
     * This enum is used to categorize items into specific types such as Song, Lesson, Genre, or InstrumentGenre.
     */
    public enum Type {
        Song, Lesson, Genre, InstrumentGenre
    }

}
