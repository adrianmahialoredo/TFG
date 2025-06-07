package com.demo.demo.controller;

import com.demo.demo.dto.ItemListDTO;
import com.demo.demo.service.ItemListService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;

/**
 * Controller class for managing item lists.
 * 
 * Provides endpoints to create, retrieve, update, and delete item lists,
 * as well as adding and removing items from lists.
 * 
 * This controller interacts with the ItemListService to perform operations
 * and returns appropriate HTTP responses.
 * 
 * Endpoints:
 * - GET /item-lists: Retrieve all item lists
 * - GET /item-lists/{id}: Retrieve a specific item list by ID
 * - POST /item-lists: Create a new item list
 * - PUT /item-lists/{id}: Update an existing item list by ID
 * - DELETE /item-lists/{id}: Delete an item list by ID
 * - PUT /item-lists/{listId}/add-item/{itemId}: Add an item to an item list
 * - PUT /item-lists/{listId}/remove-item/{itemId}: Remove an item from an item list
 * 
 * This controller is annotated with @RestController to handle RESTful requests
 * and @RequestMapping to define the base path for all endpoints.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequestMapping("/item-lists")
@RequiredArgsConstructor
public class ItemListController {

    private final ItemListService itemListService;

    /**
     * Retrieves all item lists.
     *
     * @return A list of ItemListDTOs representing all item lists.
     */
    @GetMapping
    public List<ItemListDTO> getAllItemLists() {
        return itemListService.getAllItemLists();
    }

    /**
     * Retrieves an item list by its ID.
     *
     * @param id The ID of the item list to retrieve.
     * @return An ItemListDTO if found, otherwise a 404 Not Found response.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemListDTO> getItemListById(@PathVariable Long id) {
        return itemListService.getItemListById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new item list.
     *
     * @param dto The ItemListDTO containing the details of the list to create.
     * @return The created ItemListDTO with a 201 Created status.
     */
    @PostMapping
    public ResponseEntity<ItemListDTO> createItemList(@RequestBody ItemListDTO dto) {
        ItemListDTO created = itemListService.createItemList(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing item list.
     *
     * @param id The ID of the item list to update.
     * @param dto The ItemListDTO containing the updated details.
     * @return The updated ItemListDTO if found, otherwise a 404 Not Found response.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ItemListDTO> updateItemList(@PathVariable Long id, @RequestBody ItemListDTO dto) {
        return itemListService.updateItemList(id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes an item list by its ID.
     *
     * @param id The ID of the item list to delete.
     * @return A 204 No Content response if deleted, otherwise a 404 Not Found response.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemList(@PathVariable Long id) {
        boolean deleted = itemListService.deleteItemList(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Adds an item to an item list.
     *
     * @param listId The ID of the item list to add the item to.
     * @param itemId The ID of the item to add.
     * @return The updated ItemListDTO if successful, otherwise a 404 Not Found response.
     */
    @PutMapping("/{listId}/add-item/{itemId}")
    public ResponseEntity<ItemListDTO> addItemToList(@PathVariable Long listId, @PathVariable Long itemId) {
        return itemListService.addItemToList(listId, itemId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Removes an item from an item list.
     *
     * @param listId The ID of the item list to remove the item from.
     * @param itemId The ID of the item to remove.
     * @return The updated ItemListDTO if successful, otherwise a 404 Not Found response.
     */
    @PutMapping("/{listId}/remove-item/{itemId}")
    public ResponseEntity<ItemListDTO> removeItemFromList(@PathVariable Long listId, @PathVariable Long itemId) {
        return itemListService.removeItemFromList(listId, itemId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
