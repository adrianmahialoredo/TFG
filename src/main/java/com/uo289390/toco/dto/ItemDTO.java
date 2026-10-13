package com.uo289390.toco.dto;

import lombok.Data;

import com.uo289390.toco.model.Item;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Base Data Transfer Object for items that share common identifiers.
 *
 * This abstract DTO provides common fields for item-like resources.
 *
 * @author Adrian Mahia
 * @version 1.0
 * @since 2026-05-10
 */
@Data
public abstract class ItemDTO {

    /**
     * The unique identifier of the item.
     */
    private Long id;

    /**
     * Name of the item.
     */
    @NotBlank(message = "Item title is required")
    private String title;

    /**
     * URL of the image representing the item.
     */
    private String imageUrl;

    /**
     * Level of the item, represented as a rating between 1 and 5 stars.
     * 0 means the item has no level (e.g. songs).
     */
    @Min(value = 0, message = "Level must be at least 0")
    @Max(value = 5, message = "Level must be at most 5")
    private int level;

    /**
     * Concrete subtype of the item. Serialized as {@code "type"} so the frontend can tell
     * apart the DTOs of a mixed item feed (e.g. recommended items) and store each one in its place.
     * It is read-only: each subclass returns its constant and incoming values are ignored.
     *
     * @return the subtype of the item
     */
    public abstract Item.Type getType();
}
