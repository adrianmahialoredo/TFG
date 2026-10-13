package com.uo289390.toco.service;

import com.uo289390.toco.dto.ItemListDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for managing item lists.
 *
 * Provides methods to create, retrieve, update, and delete item lists,
 * as well as adding and removing items from lists.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
public interface ItemListService {

    /**
     * Retrieves all item lists and converts them to DTOs.
     *
     * @return A list of ItemListDTOs representing all item lists.
     */
    List<ItemListDTO> findAll();

    /**
     * Retrieves all item list IDs associated with a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of item list IDs for the user.
     */
    List<Long> findIdsForUser(Long userId);

    /**
     * Retrieves all item lists by a list of IDs and converts them to DTOs.
     *
     * @param ids The list of item list IDs to retrieve.
     * @return A list of ItemListDTOs representing all item lists with the provided IDs.
     */
    List<ItemListDTO> findByIds(List<Long> ids);

    /**
     * Retrieves an item list by its ID and converts it to a DTO.
     *
     * @param id The ID of the item list to retrieve.
     * @return An Optional containing the ItemListDTO if found, otherwise empty.
     */
    Optional<ItemListDTO> findById(Long id);

    /**
     * Creates a new item list from the provided DTO and saves it to the repository.
     *
     * @param dto The ItemListDTO containing the details of the list to create.
     * @return The created ItemListDTO.
     */
    ItemListDTO create(ItemListDTO dto);

    /**
     * Updates an existing item list with the provided DTO.
     *
     * @param id  The ID of the item list to update.
     * @param dto The ItemListDTO containing the updated details.
     * @return An Optional containing the updated ItemListDTO if found, otherwise empty.
     */
    Optional<ItemListDTO> update(Long id, ItemListDTO dto);

    /**
     * Deletes an item list by its ID.
     *
     * @param id The ID of the item list to delete.
     * @return true if the item list was deleted, false if it did not exist.
     */
    boolean delete(Long id);

    /**
     * Adds an item to an existing item list.
     *
     * @param listId The ID of the item list to add the item to.
     * @param itemId The ID of the item to add.
     * @return An Optional containing the updated ItemListDTO if successful, otherwise empty.
     */
    Optional<ItemListDTO> addItem(Long listId, Long itemId);

    /**
     * Removes an item from an existing item list.
     *
     * @param listId The ID of the item list to remove the item from.
     * @param itemId The ID of the item to remove.
     * @return An Optional containing the updated ItemListDTO if successful, otherwise empty.
     */
    Optional<ItemListDTO> removeItem(Long listId, Long itemId);
}
