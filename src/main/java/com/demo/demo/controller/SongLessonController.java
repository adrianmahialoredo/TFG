package com.demo.demo.controller;

import com.demo.demo.dto.SongLessonDTO;
import com.demo.demo.service.SongLessonService;
import com.demo.demo.dto.validation.OnCreate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * REST controller for song lessons (a song taught for an instrument and difficulty).
 *
 * Base path: {@code /api/song-lessons}
 *
 * Endpoints:
 * - GET    /api/song-lessons                  Retrieve all song lessons
 * - POST   /api/song-lessons/by-ids           Retrieve the song lessons with the given IDs (IDs in the body)
 * - GET    /api/song-lessons/{id}             Retrieve a song lesson by ID
 * - GET    /api/song-lessons/by-song/{songId} Retrieve the lessons of a song
 * - POST   /api/song-lessons                  Create a song lesson
 * - PUT    /api/song-lessons/{id}             Update a song lesson
 * - DELETE /api/song-lessons/{id}             Delete a song lesson
 *
 * Responses: 200 OK, 201 Created (create), 204 No Content (delete) and 404 Not Found
 * when the song lesson does not exist. Other errors are translated by
 * {@link com.demo.demo.exception.GlobalExceptionHandler}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequestMapping("/api/song-lessons")
@RequiredArgsConstructor
public class SongLessonController {

    private final SongLessonService songLessonService;

    /**
     * Retrieves all song lessons.
     *
     * @return 200 OK with the list of song lessons
     */
    @GetMapping
    public ResponseEntity<List<SongLessonDTO>> getAll() {
        return ResponseEntity.ok(songLessonService.findAll());
    }

    /**
     * Retrieves the song lessons with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the song lessons
     * @return 200 OK with the song lessons found
     */
    @PostMapping("/by-ids")
    public ResponseEntity<List<SongLessonDTO>> getByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(songLessonService.findByIds(ids));
    }

    /**
     * Retrieves a song lesson by its ID.
     *
     * @param id the ID of the song lesson
     * @return 200 OK with the song lesson, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<SongLessonDTO> getById(@PathVariable Long id) {
        return ResponseEntity.of(songLessonService.findById(id));
    }

    /**
     * Retrieves the lessons of a song.
     *
     * @param songId the ID of the song
     * @return 200 OK with the lessons of the song
     */
    @GetMapping("/by-song/{songId}")
    public ResponseEntity<List<SongLessonDTO>> getForSong(@PathVariable Long songId) {
        return ResponseEntity.ok(songLessonService.findForSong(songId));
    }

    /**
     * Creates a song lesson.
     *
     * @param dto the song lesson to create
     * @return 201 Created with the created song lesson
     */
    @PostMapping
    public ResponseEntity<SongLessonDTO> create(@Validated(OnCreate.class) @RequestBody SongLessonDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(songLessonService.create(dto));
    }

    /**
     * Updates a song lesson.
     *
     * @param id  the ID of the song lesson
     * @param dto the new data of the song lesson
     * @return 200 OK with the updated song lesson, or 404 Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<SongLessonDTO> update(@PathVariable Long id, @Valid @RequestBody SongLessonDTO dto) {
        return ResponseEntity.of(songLessonService.update(id, dto));
    }

    /**
     * Deletes a song lesson.
     *
     * @param id the ID of the song lesson
     * @return 204 No Content, or 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return songLessonService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
