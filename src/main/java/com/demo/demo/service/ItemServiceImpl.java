package com.demo.demo.service;

import com.demo.demo.model.Item;
import com.demo.demo.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link ItemService}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Service
public class ItemServiceImpl implements ItemService {

    /**
     * Number of items returned for the Home screen carousels
     * ("Recientes" and "Recomendaciones").
     */
    public static final int HOME_ITEMS_COUNT = 12;

    private final ItemRepository itemRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    @Override
    public List<Item> getItemsByIds(List<Long> ids) {
        return itemRepository.findAllById(ids);
    }

    @Override
    public List<Item> searchItems(String query) {
        return itemRepository.findByTitleContainingIgnoreCase(query);
    }

    @Override
    public List<Item> getRecommendedItems() {
        return itemRepository.findTop20ByOrderByCreatedAtDesc();
    }

    @Override
    public List<Item> getRecentItems() {
        return itemRepository.findRecentItems(HOME_ITEMS_COUNT);
    }

    @Override
    public List<Item> getRecommendations() {
        return itemRepository.findRecommendations(HOME_ITEMS_COUNT);
    }

    @Override
    public Optional<Item> markAsAccessed(Long id) {
        return itemRepository.findById(id)
                .map(item -> {
                    item.setLastTimeAccessed(LocalDateTime.now());
                    return itemRepository.save(item);
                });
    }
}

