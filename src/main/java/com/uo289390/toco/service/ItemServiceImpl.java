package com.uo289390.toco.service;

import com.uo289390.toco.dto.ItemSummaryDTO;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.uo289390.toco.utils.EntityResolver.findItem;

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
}

