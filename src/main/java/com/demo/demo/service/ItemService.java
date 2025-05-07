package com.demo.demo.service;

import org.springframework.stereotype.Service;

import com.demo.demo.dto.SongDto;
import com.demo.demo.map.SongMapper;
import com.demo.demo.model.Item;
import com.demo.demo.model.Song;
import com.demo.demo.repository.ItemRepository;

import lombok.AllArgsConstructor;

import java.util.List;

@Service
@AllArgsConstructor
public class ItemService {
    
    private final ItemRepository itemRepository;
    private final SongMapper songMapper;

    public List<Item> getAllItemsOrdered() {
        return itemRepository.findAllByOrderByNameAsc();
    }

    public void deleteItem(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    public Item saveSong(SongDto song) {
        
        Song songEntity = songMapper.toEntity(song);
        return itemRepository.save(songEntity);

    }

    public List<Item> getAllSongsOrdered() {
        return itemRepository.findAllByTypeOrderByNameAsc(Item.Type.Song);
    }
}

