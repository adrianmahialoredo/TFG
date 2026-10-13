package com.uo289390.toco.controller;

import com.uo289390.toco.dto.ItemListDTO;
import com.uo289390.toco.service.ItemListService;
import com.uo289390.toco.dto.validation.OnCreate;
import com.uo289390.toco.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * REST controller for the item lists of the users.
 *
 * Base path: {@code /api/item-lists}
 *
 * Endpoints:
 * - GET    /api/item-lists                         Retrieve all item lists
 * - POST   /api/item-lists/by-ids                  Retrieve the item lists with the given IDs (IDs in the body)
 * - GET    /api/item-lists/{id}                    Retrieve an item list by ID
 * - GET    /api/item-lists/by-user/{userId}/ids    Retrieve the IDs of the item lists of a user
 * - POST   /api/item-lists                         Create an item list
 * - PUT    /api/item-lists/{id}                    Update an item list
 * - DELETE /api/item-lists/{id}                    Delete an item list
 * - POST   /api/item-lists/{listId}/items/{itemId} Add an item to an item list
 * - DELETE /api/item-lists/{listId}/items/{itemId} Remove an item from an item list
 *
 * Responses: 200 OK, 201 Created (create), 204 No Content (delete) and 404 Not Found
 * when the item list does not exist. Other errors are translated by
 * {@link GlobalExceptionHandler}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequestMapping("/api/item-lists")
@RequiredArgsConstructor
public class ItemListController {

    private final ItemListService itemListService;

    /**
     * Retrieves all item lists.
     *
     * @return 200 OK with the list of item lists
     */
    @GetMapping
    public ResponseEntity<List<ItemListDTO>> getAll() {
        return ResponseEntity.ok(itemListService.findAll());
    }

    /**
     * Retrieves the item lists with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the item lists
     * @return 200 OK with the item lists found
     */
    @PostMapping("/by-ids")
    @PreAuthorize("hasRole('ADMIN') or @ownership.ownsAllItemLists(#ids, authentication)")
    public ResponseEntity<List<ItemListDTO>> getByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(itemListService.findByIds(ids));
    }

    /**
     * Retrieves an item list by its ID.
     *
     * @param id the ID of the item list
     * @return 200 OK with the item list, or 404 Not Found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.ownsItemList(#id, authentication)")
    public ResponseEntity<ItemListDTO> getById(@PathVariable Long id) {
        return ResponseEntity.of(itemListService.findById(id));
    }

    /**
     * Retrieves the IDs of the item lists of a user.
     *
     * @param userId the ID of the user
     * @return 200 OK with the item list IDs
     */
    @GetMapping("/by-user/{userId}/ids")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#userId, authentication)")
    public ResponseEntity<List<Long>> getIdsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(itemListService.findIdsForUser(userId));
    }

    /**
     * Creates an item list.
     *
     * @param dto the item list to create
     * @return 201 Created with the created item list
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#dto.userId, authentication)")
    public ResponseEntity<ItemListDTO> create(@Validated(OnCreate.class) @RequestBody ItemListDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemListService.create(dto));
    }

    /**
     * Updates an item list.
     *
     * @param id  the ID of the item list
     * @param dto the new data of the item list
     * @return 200 OK with the updated item list, or 404 Not Found
     */
    @PutMapping("/{id}")
    // The body's userId becomes the owner, so it must be the caller too (no handing lists to others)
    @PreAuthorize("hasRole('ADMIN') or (@ownership.ownsItemList(#id, authentication) and @ownership.isSelf(#dto.userId, authentication))")
    public ResponseEntity<ItemListDTO> update(@PathVariable Long id, @Valid @RequestBody ItemListDTO dto) {
        return ResponseEntity.of(itemListService.update(id, dto));
    }

    /**
     * Deletes an item list.
     *
     * @param id the ID of the item list
     * @return 204 No Content, or 404 Not Found
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.ownsItemList(#id, authentication)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return itemListService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Adds an item to an item list. Adding an item that is already in the list has no effect.
     *
     * @param listId the ID of the item list
     * @param itemId the ID of the item to add
     * @return 200 OK with the updated item list, or 404 Not Found if the list does not exist
     */
    @PostMapping("/{listId}/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.ownsItemList(#listId, authentication)")
    public ResponseEntity<ItemListDTO> addItem(@PathVariable Long listId, @PathVariable Long itemId) {
        return ResponseEntity.of(itemListService.addItem(listId, itemId));
    }

    /**
     * Removes an item from an item list.
     *
     * @param listId the ID of the item list
     * @param itemId the ID of the item to remove
     * @return 200 OK with the updated item list, or 404 Not Found if the list does not exist
     */
    @DeleteMapping("/{listId}/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.ownsItemList(#listId, authentication)")
    public ResponseEntity<ItemListDTO> removeItem(@PathVariable Long listId, @PathVariable Long itemId) {
        return ResponseEntity.of(itemListService.removeItem(listId, itemId));
    }
}
