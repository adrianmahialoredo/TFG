package com.demo.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/**
 * Entity representing a chord.
 */
@Entity
@Table(name = "chords")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @ElementCollection
    @CollectionTable(name = "chord_notes", joinColumns = @JoinColumn(name = "chord_id"))
    @Column(name = "note")
    private List<String> notes;
    
    private String imageUrl;
    private boolean open;
    private boolean barre;
    private boolean powerChord;
    private boolean minor;
    private boolean major;
    private boolean seventh;

}

