package com.demo.demo.model;

import com.demo.demo.utils.Identifiable;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Song.class, name = "Song"),
        @JsonSubTypes.Type(value = Lesson.class, name = "Lesson"),
        @JsonSubTypes.Type(value = Genre.class, name = "Genre"),
        @JsonSubTypes.Type(value = InstrumentGenre.class, name = "InstrumentGenre")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Item implements Identifiable<Long> {

    /**
     * Unique identifier for the item.
     * This is the primary key of the Item entity.
     */
    @Id
    protected Long id;

    /**
     * Name of the item.
     * This field is optional and can be null.
     */
    @Column(nullable = false)
    @NotBlank(message = "Item title cannot be blank")
    protected String title;

    /**
     * URL of the image representing the item.
     * This field is optional and can be null.
     */
    protected String imageUrl;

    /**
     * Level of the item, represented as a rating between 1 and 5 stars.
     * This field is optional and can be null.
     */
    //@Min(value = 1, message = "Level must be at least 1")
    //@Max(value = 5, message = "Level must be at most 5")
    protected int level;  // between 1 and 5 stars

    /**
     * Timestamp indicating when the item was created.
     * This field is automatically set to the current date and time when the item is created.
     */
    protected LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp indicating the last time this item was accessed/opened.
     * It is updated every time a user opens the item's detail screen and it is
     * used to build the "Recientes" (recent) section on the Home screen.
     * Remains {@code null} until the item is accessed for the first time.
     */
    protected LocalDateTime lastTimeAccessed;

    /**
     * Type of the item, represented as an enum.
     * This field is used to categorize the item into different types such as Song, Lesson, Genre, or InstrumentGenreScreen.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    protected Type type;

    /**
     * Enum representing the different types of items.
     * This enum is used to categorize items into specific types such as Song, Lesson, Genre, or InstrumentGenreScreen.
     */
    public enum Type {
        Song, Lesson, Genre, InstrumentGenre
    }

    /**
     * Two Items are equal when they have the same id, so collections (e.g. {@link java.util.Set})
     * never hold the same Item twice even if it was loaded in different places or is a Hibernate proxy.
     * Ids are assigned manually before the entity is added to any collection, so the hash is stable.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(getId());
    }
}
