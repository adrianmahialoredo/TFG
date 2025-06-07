package com.demo.demo.controller;

import com.demo.demo.dto.ChordDto;
import com.demo.demo.service.ChordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Controller class for managing chords.
 * 
 * Provides endpoints to create, retrieve, update, and delete chords.
 * 
 * This controller interacts with the ChordService to perform operations
 * and returns appropriate HTTP responses.
 * 
 * Endpoints:
 * - GET /chords: Retrieve all chords
 * - GET /chords/{id}: Retrieve a specific chord by ID
 * - POST /chords: Create a new chord
 * - PUT /chords/{id}: Update an existing chord by ID
 * - DELETE /chords/{id}: Delete a chord by ID
 * 
 * This controller is annotated with @RestController to handle RESTful requests
 * and @RequestMapping to define the base path for all endpoints.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequestMapping("/chords")
@RequiredArgsConstructor
public class ChordController {

    private final ChordService chordService;

    /**
     * Retrieves all chords.
     *
     * @return A list of ChordDto representing all chords.
     */
    @GetMapping
    public List<ChordDto> getAllChords() {
        return chordService.getAllChords();
    }

    /**
     * Retrieves a chord by its ID.
     *
     * @param id The ID of the chord to retrieve.
     * @return A ResponseEntity containing the ChordDto if found, otherwise a 404 Not Found response.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChordDto> getChordById(@PathVariable Long id) {
        return chordService.getChordById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new chord.
     *
     * @param chordDto The ChordDto containing the details of the chord to create.
     * @return A ResponseEntity containing the created ChordDto with a 201 Created status.
     */
    @PostMapping
    public ResponseEntity<ChordDto> createChord(@RequestBody ChordDto chordDto) {
        ChordDto created = chordService.createChord(chordDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing chord.
     *
     * @param id The ID of the chord to update.
     * @param chordDto The ChordDto containing the updated details.
     * @return A ResponseEntity containing the updated ChordDto if found, otherwise a 404 Not Found response.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ChordDto> updateChord(@PathVariable Long id, @RequestBody ChordDto chordDto) {
        return chordService.updateChord(id, chordDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a chord by its ID.
     *
     * @param id The ID of the chord to delete.
     * @return A ResponseEntity with a 204 No Content status if successful.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChord(@PathVariable Long id) {
        chordService.deleteChord(id);
    }

}

