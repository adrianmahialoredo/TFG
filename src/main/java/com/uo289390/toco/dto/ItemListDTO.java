package com.uo289390.toco.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


/**
 * Data Transfer Object for user item lists (playlists / collections).
 *
 * This DTO is used to transfer item list data between the client and server
 * during API operations.
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
     * Name of the item list.
     */
    private String name;

    /**
     * URL of the image representing the item list.
     */
    private String imageUrl;

    /**
     * Identifier of the user that owns this list.
     */
    private Long userId;

    /**
     * Identifiers of the items contained in this list.
     */
    private List<Long> itemIds = new ArrayList<>();
}

