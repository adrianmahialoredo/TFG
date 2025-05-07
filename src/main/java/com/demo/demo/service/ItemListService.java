package com.demo.demo.service;

import org.springframework.stereotype.Service;

import com.demo.demo.dto.ItemListDto;
import com.demo.demo.map.ItemListMapper;
import com.demo.demo.model.Item;
import com.demo.demo.model.ItemList;
import com.demo.demo.repository.ItemListRepository;
import com.demo.demo.repository.ItemRepository;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Service
public class ItemListService {

    private final ItemListRepository itemListRepository;
    private final ItemRepository itemRepository;
    private final ItemListMapper itemListMapper;

    // Obtener todas las listas de un usuario
    public List<ItemList> getListsByUser(Long userId) {
        return itemListRepository.findByUserId(userId);
    }

    // Crear una nueva lista
    public ItemList saveItemList(ItemListDto itemList) {

        ItemList itemListEntity = itemListMapper.toEntity(itemList);
        return itemListRepository.save(itemListEntity);

    }

    // Eliminar una lista
    public void deleteItemList(Long listId) {
        itemListRepository.deleteById(listId);
    }

    // Agregar un Item a una lista
    public ItemList addItemToList(Long listId, Long itemId) {
        ItemList itemList = itemListRepository.findById(listId).orElseThrow(() -> new RuntimeException("ItemList not found"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));
        itemList.addItem(item);
        return itemListRepository.save(itemList);
    }

    // Quitar un Item de una lista
    public ItemList removeItemFromList(Long listId, Long itemId) {
        ItemList itemList = itemListRepository.findById(listId).orElseThrow();
        Item item = itemRepository.findById(itemId).orElseThrow();
        itemList.getItems().remove(item);
        return itemListRepository.save(itemList);
    }
}
