package com.demo.demo.dto;

import java.util.List;

import lombok.Data;

@Data
public class InstrumentGenreDTO {
    
    private Long id;
    private Long instrumentId;
    private Long genreId;
    private List<Long> lessonIds;

}
