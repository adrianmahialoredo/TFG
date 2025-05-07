package com.demo.demo.map;

import org.springframework.stereotype.Component;

import com.demo.demo.dto.ItemListDto;
import com.demo.demo.model.ItemList;
import com.demo.demo.repository.UserRepository;

import lombok.*;

@Component
@RequiredArgsConstructor
public class ItemListMapper {
    
    private final UserRepository userRepository;

    public ItemList toEntity(ItemListDto dto) {

        ItemList itemList = new ItemList();

        itemList.setId(dto.getId());
        itemList.setName(dto.getName());
        itemList.setImageUrl(dto.getImageUrl());
        itemList.setFavourite(dto.isFavourite());
        itemList.setUser(userRepository.findById(dto.getUserId()).orElseThrow(() -> new RuntimeException("User not found")));
        
        return itemList;

    }


}
