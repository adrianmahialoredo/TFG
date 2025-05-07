package com.demo.demo.dto;

import java.util.List;

import lombok.*;

@Data
public class ChordDto {
    
    private Long id;
    private String name;
    private List<String> notes;
    private String imageUrl;
    private boolean open;
    private boolean barre;
    private boolean powerChord;
    private boolean minor;
    private boolean major;
    private boolean seventh;

}
