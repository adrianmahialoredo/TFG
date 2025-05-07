package com.demo.demo.dto;

import java.util.List;

import lombok.*;

@Data
public class SongDto {

    private String title;
    private String artist;
    private String genre;
    private String lyricsURL;
    private String description;
    private List<Long> chordIds;
    private List<String> videos;
    private List<String> tags;
    
}

