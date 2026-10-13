package com.uo289390.toco.service;

import com.uo289390.toco.model.ItemList;
import com.uo289390.toco.repository.ItemListRepository;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.mapper.EntityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.uo289390.toco.dto.ItemListDTO;
import com.uo289390.toco.model.*;
import com.uo289390.toco.repository.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import static com.uo289390.toco.utils.EntityResolver.resolveItem;
import static com.uo289390.toco.utils.EntityResolver.resolveItems;
import static com.uo289390.toco.utils.EntityResolver.resolveUser;
import static com.uo289390.toco.utils.EntityResolver.findItemList;
import static com.uo289390.toco.utils.EntityResolver.findUser;

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
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemListServiceImpl implements ItemListService {

    private final ItemListRepository itemListRepository;
    private final SongService songService;
    private final DtoMapper dtoMapper;
    private final EntityMapper entityMapper;

    @Override
    public List<ItemListDTO> findAll() {
        return itemListRepository.findAll().stream()
                .map(dtoMapper::itemListToItemListDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findIdsForUser(Long userId) {
        return findUser(userId)
                .map(user -> user.getItemLists().stream()
                        .map(ItemList::getId)
                        .collect(Collectors.toList()))
                .orElseGet(List::of);
    }

    @Override
    public List<ItemListDTO> findByIds(List<Long> ids) {
        return itemListRepository.findAllById(ids).stream()
                .map(dtoMapper::itemListToItemListDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ItemListDTO> findById(Long id) {
        return findItemList(id)
                .map(dtoMapper::itemListToItemListDto);
    }

    @Override
    @Transactional
    public ItemListDTO create(ItemListDTO dto) {
        ItemList list = entityMapper.itemListDtoToItemList(dto, resolveUser(dto.getUserId()), resolveItems(dto.getItemIds()));
        return dtoMapper.itemListToItemListDto(itemListRepository.save(list));
    }

    @Override
    @Transactional
    public Optional<ItemListDTO> update(Long id, ItemListDTO dto) {
        return findItemList(id).map(
                existing -> dtoMapper.itemListToItemListDto(itemListRepository.save(updateItemListFromDto(existing, dto)))
        );
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!itemListRepository.existsById(id)) {
            return false;
        }
        itemListRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public Optional<ItemListDTO> addItem(Long listId, Long itemId) {
        return findItemList(listId).map(list -> {
            // ItemList.addItem ignora el item si ya está en la lista (sin duplicados en itemlist_items)
            list.addItem(resolveItem(itemId));
            return dtoMapper.itemListToItemListDto(itemListRepository.save(list));
        });
    }

    @Override
    @Transactional
    public Optional<ItemListDTO> removeItem(Long listId, Long itemId) {
        return findItemList(listId).map(list -> {
            list.getItems().removeIf(item -> item.getId().equals(itemId));
            return dtoMapper.itemListToItemListDto(itemListRepository.save(list));
        });
    }

    private ItemList updateItemListFromDto(ItemList existing, ItemListDTO dto) {
        existing.setName(dto.getName());
        existing.setImageUrl(dto.getImageUrl());
        existing.setUser(resolveUser(dto.getUserId()));
        existing.setItems(resolveItems(dto.getItemIds()));
        return existing;
    }

}

