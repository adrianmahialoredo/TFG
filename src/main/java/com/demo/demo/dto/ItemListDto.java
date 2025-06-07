package com.demo.demo.dto;

import java.util.List;
import lombok.Data;

@Data
public class ItemListDTO {

    private Long id;
    private String name;
    private String imageUrl;
    private Long userId; 
    private List<Long> itemIds; // Lista de IDs de items asociados a la lista

}
