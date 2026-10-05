package com.demo.demo.model;

import com.demo.demo.utils.Identifiable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
public class Instrument implements Identifiable<Long> {

    /**
     * Unique identifier for the instrument.
     * This is the primary key of the Instrument entity.
     */
    @Id
    private Long id;

    /**
     * URL of the image representing the item.
     * This field is optional and can be null.
     */
    @Column(nullable = false)
    @NotBlank(message = "Image URL cannot be blank")
    @Pattern(regexp = "^https?://.+", message = "Image URL must be a valid HTTP/HTTPS URL")
    protected String imageUrl;

    /**
     * Name of the instrument.
     * This field is required and must be unique.
     */
    @Column(nullable = false, unique = true)
    @NotBlank(message = "Instrument name cannot be blank")
    @Size(min = 2, max = 100, message = "Instrument name must be between 2 and 100 characters")
    private String name;

    /**
     * Two Instruments are equal when they have the same id, so collections (e.g. {@link java.util.Set})
     * never hold the same Instrument twice even if it was loaded in different places or is a Hibernate proxy.
     * Ids are assigned manually before the entity is added to any collection, so the hash is stable.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Instrument other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(getId());
    }
}
