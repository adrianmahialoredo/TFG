package com.demo.demo.dto;

import java.util.List;

import lombok.Data;

@Data
public class GenreDTO {
    
    private Long id;
    private String name;
    private String description;
    private String bannerUrl;
    private String imageUrl;
    private String welcomeIntroVideoId;
    private String welcomeSongVideoId;
    private List<Long> songIds;
    private List<Long> lessonIds;

}
