package com.demo.demo.service;

import com.demo.demo.model.Item;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for managing generic items.
 *
 * Provides read operations used to build the Home screen sections
 * (recent and recommended items) and basic item lookups/search.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
public interface ItemService {

    Optional<Item> getItemById(Long id);

    List<Item> getItemsByIds(List<Long> ids);

    List<Item> searchItems(String query);

    List<Item> getRecommendedItems();

    List<Item> getRecentItems();

    List<Item> getRecommendations();

    Optional<Item> markAsAccessed(Long id);
}
