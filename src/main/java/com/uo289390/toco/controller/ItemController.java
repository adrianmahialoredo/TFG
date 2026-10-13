package com.uo289390.toco.controller;

import com.uo289390.toco.dto.ItemSummaryDTO;
import com.uo289390.toco.service.ItemService;
import com.uo289390.toco.exception.GlobalExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for items: the common, read-only view of songs, lessons, genres and instrument-genres.
 *
 * Base path: {@code /api/items}
 *
 * Endpoints:
 * - GET    /api/items                                      Retrieve the summary of every item (the app catalog)
 * - POST   /api/items/by-ids                               Retrieve the items with the given IDs (IDs in the body)
 * - GET    /api/items/{id}                                 Retrieve an item by ID
 * - GET    /api/items/search?q={query}                     Search items by title
 * - GET    /api/items/recommended                          Retrieve the recommended items for the user
 * - GET    /api/items/recommended/by-item-list/{itemListId} Retrieve the recommended items for an item list
 *
 * Responses: 200 OK and 404 Not Found when the item does not exist.
 * Other errors are translated by {@link GlobalExceptionHandler}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /**
     * Retrieves the summary of every item. The app loads it at startup as its local
     * catalog (e.g. to search items to add to a list without calling the backend).
     *
     * @return 200 OK with all the items
     */
    @GetMapping
    public ResponseEntity<List<ItemSummaryDTO>> getAll() {
        return ResponseEntity.ok(itemService.findAll());
    }

    /**
     * Retrieves the items with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the items
     * @return 200 OK with the items found
     */
    @PostMapping("/by-ids")
    public ResponseEntity<List<ItemSummaryDTO>> getByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(itemService.findByIds(ids));
    }

    /**
     * Retrieves an item by its ID.
     *
     * @param id the ID of the item
     * @return 200 OK with the item, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemSummaryDTO> getById(@PathVariable Long id) {
        return ResponseEntity.of(itemService.findById(id));
    }

    /**
     * Searches items whose title contains the given text (case insensitive).
     *
     * @param query the text to search for
     * @return 200 OK with the matching items
     */
    @GetMapping("/search")
    public ResponseEntity<List<ItemSummaryDTO>> search(@RequestParam("q") String query) {
        return ResponseEntity.ok(itemService.search(query));
    }

    /**
     * Retrieves the recommended items for an item list.
     *
     * @param itemListId the ID of the item list
     * @return 200 OK with the recommended items
     */
    @GetMapping("/recommended/by-item-list/{itemListId}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.ownsItemList(#itemListId, authentication)")
    public ResponseEntity<List<ItemSummaryDTO>> getRecommendedForItemList(@PathVariable Long itemListId) {
        return ResponseEntity.ok(itemService.findRecommendedForItemList(itemListId));
    }
}
