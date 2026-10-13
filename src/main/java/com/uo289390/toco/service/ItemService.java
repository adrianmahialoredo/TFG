package com.uo289390.toco.service;

import com.uo289390.toco.dto.ItemSummaryDTO;

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

    List<ItemSummaryDTO> findAll();

    Optional<ItemSummaryDTO> findById(Long id);

    List<ItemSummaryDTO> findByIds(List<Long> ids);

    List<ItemSummaryDTO> search(String query);

    List<ItemSummaryDTO> findRecommendedForItemList(Long itemListId);
}
