package com.uo289390.toco.dto;

import com.uo289390.toco.model.Item;
import lombok.Data;

/**
 * Lightweight, read-only view of any item (song, lesson, genre or instrument-genre).
 *
 * Used by mixed item feeds (catalog, recommendations, item lists) so the
 * frontend can render a card and route to the detail screen without loading the
 * full DTO of each subtype.
 *
 * It is intentionally independent from {@link ItemDTO}: that class is the base of
 * the full, writable DTOs (with validation and subtype fields), while this one is
 * a minimal output-only projection.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-10-04
 */
@Data
public class ItemSummaryDTO {

    /**
     * The unique identifier of the item.
     */
    private Long id;

    /**
     * Name of the item.
     */
    private String title;

    /**
     * URL of the image representing the item.
     */
    private String imageUrl;

    /**
     * Concrete subtype of the item, used by the frontend to route to its detail screen.
     */
    private Item.Type type;

    /**
     * Level of the item (1 to 5 stars); 0 when it has no level (e.g. songs).
     */
    private int level;

    /**
     * Artist of the song, shown as the card subtitle. Only set for songs; {@code null} otherwise.
     */
    private String artist;
}
