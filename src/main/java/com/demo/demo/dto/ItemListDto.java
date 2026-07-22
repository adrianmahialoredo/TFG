package com.demo.demo.dto;

import java.util.List;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * Data Transfer Object for item list information.
 *
 * This DTO is used to transfer data about user-created item lists
 * between the client and server. An item list is a collection of items
 * (songs, genres, lessons, instrument-genres) that a user can organize
 * and manage.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Data
public class ItemListDTO {

    /**
     * The unique identifier of the item list.
     */
    private Long id;

    /**
     * The name of the item list.
     * Must not be blank and must be between 1 and 100 characters.
     */
    @NotBlank(message = "Item list name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    /**
     * The URL of the image representing the item list.
     * Must be a valid URL format if provided.
     */
    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;

    /**
     * The ID of the user who owns this item list.
     * Must not be null.
     */
    @NotNull(message = "User ID is required")
    private Long userId;

    /**
     * List of IDs of items associated with this list.
     */
    private List<Long> itemIds;
}
