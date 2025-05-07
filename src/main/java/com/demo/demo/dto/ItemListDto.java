package com.demo.demo.dto;

import lombok.Data;

@Data
public class ItemListDto {

    private Long id;
    private String name;
    private String imageUrl;
    private boolean favourite;
    private Long userId; 

}
