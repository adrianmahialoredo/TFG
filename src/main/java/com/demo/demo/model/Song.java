package com.demo.demo.model;

import java.util.List;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "songs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Song extends Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String artist;
    private String genre;
    private String lyricsURL;
    private String description;

    @ManyToMany
    @JoinTable(
        name = "song_chords",
        joinColumns = @JoinColumn(name = "song_id"),
        inverseJoinColumns = @JoinColumn(name = "chord_id")
    )
    private List<Chord> chords; // La canción conoce los acordes, pero el acorde NO conoce las canciones

    @ElementCollection
    @CollectionTable(name = "song_videos", joinColumns = @JoinColumn(name = "song_id"))
    @Column(name = "video_url")
    private List<String> videos;

    @ElementCollection
    @CollectionTable(name = "song_tags", joinColumns = @JoinColumn(name = "song_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", genre='" + genre + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

