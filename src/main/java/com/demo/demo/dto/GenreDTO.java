package com.demo.demo.dto;

import java.util.List;

import lombok.Data;

@Data
public class GenreDTO {
    
    private Long id;
    private String name;
    private String description;
    private String bannerUrl;
    private String welcomeIntroVideo;
    private String welcomeSongVideo;
    private List<Long> songIds;
    private List<Long> lessonIds;

}
