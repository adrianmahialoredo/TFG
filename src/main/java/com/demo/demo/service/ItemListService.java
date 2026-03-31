package com.demo.demo.service;

import org.springframework.stereotype.Service;
import com.demo.demo.dto.ItemListDTO;
import com.demo.demo.model.*;
import com.demo.demo.repository.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing item lists.
 * 
 * Provides methods to create, retrieve, update, and delete item lists,
 * as well as adding and removing items from lists.
 * 
 * This service interacts with repositories to perform database operations
 * and handles the conversion of entities to DTOs for API responses.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemListService {

    private final ItemListRepository itemListRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    /**
     * Retrieves all item lists and converts them to DTOs.
     *
     * @return A list of ItemListDTOs representing all item lists.
     */
    public List<ItemListDTO> getAllItemLists() {
        return itemListRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all item list IDs associated with a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of item list IDs for the user.
     */
    public List<Long> getAllItemListIdsByUserId(Long userId) {
        return itemListRepository.findIdsByUserId(userId);
    }

    /**
     * Retrieves all item lists by a list of IDs and converts them to DTOs.
     *
     * @param ids The list of item list IDs to retrieve.
     * @return A list of ItemListDTOs representing all item lists with the provided IDs.
     */
    public List<ItemListDTO> getAllItemListsByIds(List<Long> ids) {
        return itemListRepository.findAllById(ids).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves an item list by its ID and converts it to a DTO.
     *
     * @param id The ID of the item list to retrieve.
     * @return An Optional containing the ItemListDTO if found, otherwise empty.
     */
    public Optional<ItemListDTO> getItemListById(Long id) {
        return itemListRepository.findById(id)
                .map(this::toDTO);
    }

    /**
     * Creates a new item list from the provided DTO and saves it to the repository.
     *
     * @param dto The ItemListDTO containing the details of the list to create.
     * @return The created ItemListDTO.
     */
    public ItemListDTO createItemList(ItemListDTO dto) {
        ItemList list = toEntity(dto);
        return toDTO(itemListRepository.save(list));
    }

    /**
     * Updates an existing item list with the provided DTO.
     *
     * @param id  The ID of the item list to update.
     * @param dto The ItemListDTO containing the updated details.
     * @return An Optional containing the updated ItemListDTO if found, otherwise empty.
     */
    public Optional<ItemListDTO> updateItemList(Long id, ItemListDTO dto) {
        return itemListRepository.findById(id).map(
                existing -> toDTO(itemListRepository.save(updateItemListFromDto(existing, dto)))
        );
    }

    /**
     * Deletes an item list by its ID.
     *
     * @param id The ID of the item list to delete.
     * @return true if the item list was deleted, false if it did not exist.
     */
    public boolean deleteItemList(Long id) {
        if (!itemListRepository.existsById(id)) {
            return false;
        }
        itemListRepository.deleteById(id);
        return true;
    }

    /**
     * Adds an item to an existing item list.
     *
     * @param listId The ID of the item list to add the item to.
     * @param itemId The ID of the item to add.
     * @return An Optional containing the updated ItemListDTO if successful, otherwise empty.
     */
    public Optional<ItemListDTO> addItemToList(Long listId, Long itemId) {
        return itemListRepository.findById(listId).map(list -> {
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
            list.addItem(item);
            return toDTO(itemListRepository.save(list));
        });
    }

    /**
     * Removes an item from an existing item list.
     *
     * @param listId The ID of the item list to remove the item from.
     * @param itemId The ID of the item to remove.
     * @return An Optional containing the updated ItemListDTO if successful, otherwise empty.
     */
    public Optional<ItemListDTO> removeItemFromList(Long listId, Long itemId) {
        return itemListRepository.findById(listId).map(list -> {
            list.getItems().removeIf(item -> item.getId().equals(itemId));
            return toDTO(itemListRepository.save(list));
        });
    }

    // ------------
    // DTO MAPPING
    // ------------

    /**
     * Converts an ItemList entity to an ItemListDTO.
     *
     * @param entity The ItemList entity to convert.
     * @return The converted ItemListDTO.
     */
    private ItemListDTO toDTO(ItemList entity) {
        ItemListDTO dto = new ItemListDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setImageUrl(entity.getImageUrl());
        dto.setUserId(entity.getUser().getId());
        dto.setItemIds(entity.getItems().stream()
                .map(Item::getId)
                .collect(Collectors.toList()));
        return dto;
    }

    /**
     * Converts an ItemListDTO to an ItemList entity.
     *
     * @param dto The ItemListDTO to convert.
     * @return The converted ItemList entity.
     */
    private ItemList toEntity(ItemListDTO dto) {
        ItemList list = new ItemList();
        list.setName(dto.getName());
        list.setImageUrl(dto.getImageUrl());
        list.setUser(resolveUser(dto.getUserId()));
        list.setItems(resolveItems(dto.getItemIds()));
        return list;
    }

    /**
     * Updates an existing ItemList entity with values from the provided DTO.
     *
     * @param existing The existing ItemList entity to update.
     * @param dto      The ItemListDTO containing the updated details.
     * @return The updated ItemList entity.
     */
    private ItemList updateItemListFromDto(ItemList existing, ItemListDTO dto) {
        existing.setName(dto.getName());
        existing.setImageUrl(dto.getImageUrl());
        existing.setUser(resolveUser(dto.getUserId()));
        existing.setItems(resolveItems(dto.getItemIds()));
        return existing;
    }

    /**
     * Resolves a list of item IDs to their corresponding Item entities.
     *
     * @param itemIds The list of item IDs to resolve.
     * @return A list of Item entities corresponding to the provided IDs.
     */
    private List<Item> resolveItems(List<Long> itemIds) {
        if (itemIds == null) return new ArrayList<>();
        return itemIds.stream()
                .map(id -> itemRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Item not found: " + id)))
                .collect(Collectors.toList());
    }

    /**
     * Resolves a User entity by its ID.
     *
     * @param userId The ID of the user to resolve.
     * @return The resolved User entity, or null if the userId is null.
     * @throws IllegalArgumentException if the user is not found.
     */
    private User resolveUser(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

}

