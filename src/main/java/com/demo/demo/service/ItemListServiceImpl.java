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
 * Default implementation of {@link ItemListService}.
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
public class ItemListServiceImpl implements ItemListService {

    private final ItemListRepository itemListRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public List<ItemListDTO> getAllItemLists() {
        return itemListRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getAllItemListIdsByUserId(Long userId) {
        return itemListRepository.findIdsByUserId(userId);
    }

    @Override
    public List<ItemListDTO> getAllItemListsByIds(List<Long> ids) {
        return itemListRepository.findAllById(ids).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ItemListDTO> getItemListById(Long id) {
        return itemListRepository.findById(id)
                .map(this::toDTO);
    }

    @Override
    public ItemListDTO createItemList(ItemListDTO dto) {
        ItemList list = toEntity(dto);
        return toDTO(itemListRepository.save(list));
    }

    @Override
    public Optional<ItemListDTO> updateItemList(Long id, ItemListDTO dto) {
        return itemListRepository.findById(id).map(
                existing -> toDTO(itemListRepository.save(updateItemListFromDto(existing, dto)))
        );
    }

    @Override
    public boolean deleteItemList(Long id) {
        if (!itemListRepository.existsById(id)) {
            return false;
        }
        itemListRepository.deleteById(id);
        return true;
    }

    @Override
    public Optional<ItemListDTO> addItemToList(Long listId, Long itemId) {
        return itemListRepository.findById(listId).map(list -> {
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
            // Evitamos duplicados en la tabla intermedia itemlist_items
            boolean alreadyPresent = list.getItems().stream()
                    .anyMatch(existing -> existing.getId().equals(itemId));
            if (!alreadyPresent) {
                list.addItem(item);
            }
            return toDTO(itemListRepository.save(list));
        });
    }

    @Override
    public Optional<ItemListDTO> removeItemFromList(Long listId, Long itemId) {
        return itemListRepository.findById(listId).map(list -> {
            list.getItems().removeIf(item -> item.getId().equals(itemId));
            return toDTO(itemListRepository.save(list));
        });
    }

    // ------------
    // DTO MAPPING
    // ------------

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

    private ItemList toEntity(ItemListDTO dto) {
        ItemList list = new ItemList();
        list.setId(dto.getId());
        list.setName(dto.getName());
        list.setImageUrl(dto.getImageUrl());
        list.setUser(resolveUser(dto.getUserId()));
        list.setItems(resolveItems(dto.getItemIds()));
        return list;
    }

    private ItemList updateItemListFromDto(ItemList existing, ItemListDTO dto) {
        existing.setName(dto.getName());
        existing.setImageUrl(dto.getImageUrl());
        existing.setUser(resolveUser(dto.getUserId()));
        existing.setItems(resolveItems(dto.getItemIds()));
        return existing;
    }

    private List<Item> resolveItems(List<Long> itemIds) {
        if (itemIds == null) return new ArrayList<>();
        return itemIds.stream()
                .map(id -> itemRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Item not found: " + id)))
                .collect(Collectors.toList());
    }

    private User resolveUser(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

}

