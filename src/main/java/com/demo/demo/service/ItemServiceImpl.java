package com.demo.demo.service;

import com.demo.demo.dto.ItemDTO;
import com.demo.demo.dto.ItemSummaryDTO;
import com.demo.demo.utils.mapper.DtoMapper;
import com.demo.demo.model.ItemList;
import com.demo.demo.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.demo.demo.utils.EntityResolver.resolveItemList;
import static com.demo.demo.utils.EntityResolver.findItem;

/**
 * Default implementation of {@link ItemService}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    /**
     * Number of items returned for the Home screen carousels
     * ("Recientes" and "Recomendaciones").
     */
    public static final int HOME_ITEMS_COUNT = 12;

    private final ItemRepository itemRepository;
    private final DtoMapper dtoMapper;

    @Override
    public List<ItemSummaryDTO> findAll() {
        return dtoMapper.itemsToItemSummaryDTOs(itemRepository.findAll());
    }

    @Override
    public Optional<ItemSummaryDTO> findById(Long id) {
        return findItem(id).map(dtoMapper::itemToItemSummaryDto);
    }

    @Override
    public List<ItemSummaryDTO> findByIds(List<Long> ids) {
        return itemRepository.findAllById(ids).stream()
                .map(dtoMapper::itemToItemSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemSummaryDTO> search(String query) {
        return itemRepository.findByTitleContainingIgnoreCase(query).stream()
                .map(dtoMapper::itemToItemSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemSummaryDTO> findRecommendedForItemList(Long id) {

        ItemList list = resolveItemList(id);


        return itemRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(dtoMapper::itemToItemSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDTO> findRecent() {
        return itemRepository.findByLastTimeAccessedIsNotNullOrderByLastTimeAccessedDesc(PageRequest.of(0, HOME_ITEMS_COUNT)).stream()
                .map(dtoMapper::itemToItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<ItemSummaryDTO> markAsAccessed(Long id) {
        return findItem(id)
                .map(item -> {
                    item.setLastTimeAccessed(LocalDateTime.now());
                    return itemRepository.save(item);
                })
                .map(dtoMapper::itemToItemSummaryDto);
    }
}

