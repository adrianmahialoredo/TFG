package com.demo.demo.controller;

import com.demo.demo.model.Song;
import com.demo.demo.service.SongService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/songs")
public class SongController {
    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @GetMapping
    public List<Song> getAllSongs() {
        return songService.getAllSongs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Song> getSongById(@PathVariable Long id) {
        Optional<Song> song = Optional.ofNullable(songService.getSongById(id));
        return song.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Song createSong(@RequestBody Song song) {
        return songService.saveSong(song);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Song> updateSong(@PathVariable Long id, @RequestBody Song songDetails) {
        Song existingSong = songService.getSongById(id);
        if (existingSong == null) {
            return ResponseEntity.notFound().build();
        }
        existingSong.setTitle(songDetails.getTitle());
        existingSong.setArtist(songDetails.getArtist());
        existingSong.setGenre(songDetails.getGenre());
        existingSong.setLyricsURL(songDetails.getLyricsURL());
        existingSong.setDescription(songDetails.getDescription());
        existingSong.setChords(songDetails.getChords());
        existingSong.setVideos(songDetails.getVideos());
        existingSong.setTags(songDetails.getTags());
        return ResponseEntity.ok(songService.saveSong(existingSong));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }
}
