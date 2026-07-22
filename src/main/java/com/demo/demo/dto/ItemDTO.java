package com.demo.demo.dto;

import com.demo.demo.model.Item;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Base Data Transfer Object for items that share common identifiers.
 *
 * This abstract DTO provides common fields for item-like resources.
 *
 * @author Adrian Mahia
 * @version 1.0
 * @since 2026-05-10
 */
@Getter
@Setter
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
     */
    @Min(value = 1, message = "Level must be at least 1")
    @Max(value = 5, message = "Level must be at most 5")
    private int level;
}
