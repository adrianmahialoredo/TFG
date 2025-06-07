package com.demo.demo.dto;

import java.util.List;
import lombok.Data;

@Data
public class InstrumentDTO {
    
    private Long id;
    private String imageUrl;
    private String name;
    private List<Long> songIds;

}
