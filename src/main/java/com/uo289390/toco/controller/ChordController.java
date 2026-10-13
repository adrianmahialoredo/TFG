package com.uo289390.toco.controller;

import com.uo289390.toco.dto.ChordDto;
import com.uo289390.toco.service.ChordService;
import com.uo289390.toco.dto.validation.OnCreate;
import com.uo289390.toco.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * REST controller for chords.
 *
 * Base path: {@code /api/chords}
 *
 * Endpoints:
 * - GET    /api/chords                                  Retrieve all chords
 * - POST   /api/chords/by-ids                           Retrieve the chords with the given IDs (IDs in the body)
 * - GET    /api/chords/{id}                             Retrieve a chord by ID
 * - GET    /api/chords/by-song-lesson/{songLessonId}/ids Retrieve the IDs of the chords of a song lesson
 * - POST   /api/chords                                  Create a chord
 * - PUT    /api/chords/{id}                             Update a chord
 * - DELETE /api/chords/{id}                             Delete a chord
 *
 * Responses: 200 OK, 201 Created (create), 204 No Content (delete) and 404 Not Found
 * when the chord does not exist. Other errors are translated by
 * {@link GlobalExceptionHandler}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequestMapping("/api/chords")
@RequiredArgsConstructor
public class ChordController {

    private final ChordService chordService;

    /**
     * Retrieves all chords.
     *
     * @return 200 OK with the list of chords
     */
    @GetMapping
    public ResponseEntity<List<ChordDto>> getAll() {
        return ResponseEntity.ok(chordService.findAll());
    }

    /**
     * Retrieves the chords with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the chords
     * @return 200 OK with the chords found
     */
    @PostMapping("/by-ids")
    public ResponseEntity<List<ChordDto>> getByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(chordService.findByIds(ids));
    }

    /**
     * Retrieves a chord by its ID.
     *
     * @param id the ID of the chord
     * @return 200 OK with the chord, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChordDto> getById(@PathVariable Long id) {
        return ResponseEntity.of(chordService.findById(id));
    }

    /**
     * Retrieves the IDs of the chords used in a song lesson.
     *
     * @param songLessonId the ID of the song lesson
     * @return 200 OK with the chord IDs
     */
    @GetMapping("/by-song-lesson/{songLessonId}/ids")
    public ResponseEntity<List<Long>> getIdsForSongLesson(@PathVariable Long songLessonId) {
        return ResponseEntity.ok(chordService.findIdsForSongLesson(songLessonId));
    }

    /**
     * Creates a chord.
     *
     * @param dto the chord to create
     * @return 201 Created with the created chord
     */
    @PostMapping
    public ResponseEntity<ChordDto> create(@Validated(OnCreate.class) @RequestBody ChordDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chordService.create(dto));
    }

    /**
     * Updates a chord.
     *
     * @param id  the ID of the chord
     * @param dto the new data of the chord
     * @return 200 OK with the updated chord, or 404 Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<ChordDto> update(@PathVariable Long id, @Valid @RequestBody ChordDto dto) {
        return ResponseEntity.of(chordService.update(id, dto));
    }

    /**
     * Deletes a chord.
     *
     * @param id the ID of the chord
     * @return 204 No Content, or 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return chordService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
