package com.demo.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/**
 * Represents a musical chord with its properties and associated notes.
 * 
 * This entity is used to store chord information in the database,
 * including the chord name, notes, image URL, and various chord characteristics.
 * 
 * The Chord entity is designed to be flexible, allowing for different types of chords
 * such as major, minor, seventh, and others. It also supports capo placement
 * for guitar chords, indicating the fret number where the capo is placed.
 * 
 * The notes are stored as a list of strings, allowing for easy representation
 * of the notes that make up the chord. The image URL field can be used
 * to store a visual representation of the chord, such as a chord diagram.
 * 
 * This class is annotated with JPA annotations to map it to a database table,
 * and it uses Lombok annotations to reduce boilerplate code for getters, setters,
 * constructors, and the builder pattern.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Entity
@Table(name = "chords")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chord {

    /**
     * Unique identifier for the chord.
     * This is the primary key of the Chord entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Name of the chord.
     * This field is required and must be unique.
     */
    @Column(nullable = false, unique = true)
    private String name;
    
    /**
     * List of notes that make up the chord.
     * This field is required and stored in a separate table.
     */
    @ElementCollection
    @CollectionTable(name = "chord_notes", joinColumns = @JoinColumn(name = "chord_id"))
    @Column(name = "note")
    private List<String> notes;
    
    /**
     * URL of the image representing the chord.
     * This field is required and can be used to display a chord diagram.
     */
    @Column(nullable = false)
    private String imageUrl;

    /**
     * Indicates whether the chord is open.
     * An open chord is one that includes open strings.
     */
    private boolean open;

    /**
     * Indicates whether the chord is barre.
     * A barre chord is one where one finger presses down multiple strings across a fret.
     */
    private boolean barre;

    /**
     * Indicates whether the chord is a power chord.
     * A power chord is a simplified version of a major or minor chord, typically used in rock music.
     */
    private boolean powerChord;

    /**
     * Indicates whether the chord is a suspended chord.
     * A suspended chord replaces the third with either a second or a fourth.
     */
    private boolean minor;

    /**
     * Indicates whether the chord is a major chord.
     * A major chord consists of a root note, a major third, and a perfect fifth.
     */
    private boolean major;

    /**
     * Indicates whether the chord is a seventh chord.
     * A seventh chord includes the root, third, fifth, and a seventh note.
     */
    private boolean seventh;

    /**
     * Indicates whether the chord is a diminished chord.
     * A diminished chord consists of a root, minor third, and diminished fifth.
     */
    private boolean capo;

    /**
     * Fret number for capo placement, if applicable.
     * This field is used to indicate where the capo is placed on the guitar neck.
     */
    private int capoFret; 

}

