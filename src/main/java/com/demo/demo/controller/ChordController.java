package com.demo.demo.controller;

import com.demo.demo.dto.ChordDto;
import com.demo.demo.model.Chord;
import com.demo.demo.service.ChordService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/chords")
public class ChordController {
    
    private final ChordService chordService;

    public ChordController(ChordService chordService) {
        this.chordService = chordService;
    }

    @GetMapping
    public List<Chord> getAllChords() {
        return chordService.getAllChords();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Chord> getChordById(@PathVariable Long id) {
        Optional<Chord> chord = chordService.getChordById(id);
        return chord.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Chord createChord(@Valid @RequestBody ChordDto chord) {
        return chordService.saveChord(chord);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChord(@PathVariable Long id) {
        chordService.deleteChord(id);
        return ResponseEntity.noContent().build();
    }
}

