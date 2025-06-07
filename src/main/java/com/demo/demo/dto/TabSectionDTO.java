package com.demo.demo.dto;

import lombok.Data;

@Data
public class TabSectionDTO {
    
    private Long id;
    private String identifier; // "intro", "solo", etc.
    private String content; // tab real en multilinea
    private Long songLessonId; // ID of the associated SongLesson

}
