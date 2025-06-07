package com.demo.demo.dto;

import java.util.List;
import lombok.*;

@Data
public class SongDTO {

    private Long id;
    private String title;
    private String artist;
    private List<Long> genreIds;
    private List<Long> instrumentIds;
    private List<Long> lessons;
    
}

