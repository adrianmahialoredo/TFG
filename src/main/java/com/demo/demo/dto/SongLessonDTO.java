package com.demo.demo.dto;

import java.util.List;

import lombok.Data;

@Data
public class SongLessonDTO {
    
    private Long id;
    private String difficulty; // Difficulty level of the lesson ("EASY", "MEDIUM", "ORIGINAL")
    private List<Long> tabSectionsIds; // List of Tab Section IDs associated with this lesson
    private List<Long> chordIds; // List of Chord IDs associated with this lesson
    private String chordedLyrics; // Chorded lyrics in the format "[C]Letra"
    private String videoUrl; // URL of the lesson video
    private Long songId; // ID of the associated Song
    private Long instrumentId; // ID of the associated Instrument

}
