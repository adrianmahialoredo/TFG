package com.demo.demo.controller;

import com.demo.demo.dto.SongLessonDTO;
import com.demo.demo.service.SongLessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller for managing song lessons.
 *
 * Provides endpoints to create, read, update, and delete song lessons,
 * as well as to fetch all lessons associated with a specific song.
 *
 * This controller uses the SongLessonService to handle business logic
 * and convert entities to DTOs for API responses.
 *
 * Endpoints:
 * - POST /song-lessons: Create a new song lesson
 * - GET /song-lessons/{id}: Retrieve a specific song lesson by ID
 * - GET /song-lessons/by-song/{songId}: Retrieve all lessons for a specific song ID
 * - PUT /song-lessons/{id}: Update an existing song lesson by ID
 * - DELETE /song-lessons/{id}: Delete a song lesson by ID
 *
 * This controller is annotated with @RestController to handle RESTful requests
 * and @RequestMapping to define the base path for all endpoints.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/song-lessons")
public class SongLessonController {

    private final SongLessonService songLessonService;

    /**
     * Creates a new song lesson.
     *
     * @param dto The SongLessonDTO containing the details of the lesson to create.
     * @return ResponseEntity with the created SongLessonDTO and HTTP status 201 Created.
     */
    @PostMapping
    public ResponseEntity<SongLessonDTO> createSongLesson(@RequestBody SongLessonDTO dto) {
        SongLessonDTO created = songLessonService.createSongLesson(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieves a song lesson by its ID.
     *
     * @param id The ID of the song lesson to retrieve.
     * @return ResponseEntity with the SongLessonDTO if found, or 404 Not Found if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SongLessonDTO> getSongLessonById(@PathVariable Long id) {
        return songLessonService.getSongLessonById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all song lessons associated with a specific song ID.
     *
     * @param songId The ID of the song to find lessons for.
     * @return List of SongLessonDTOs associated with the given song ID.
     */
    @GetMapping("/by-song/{songId}")
    public List<SongLessonDTO> getLessonsBySongId(@PathVariable Long songId) {
        return songLessonService.getLessonsBySongId(songId);
    }

    /**
     * Updates an existing song lesson identified by its ID.
     *
     * @param id The ID of the song lesson to update.
     * @param dto The SongLessonDTO containing the updated details of the lesson.
     * @return ResponseEntity with the updated SongLessonDTO if successful, or 404 Not Found if not found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SongLessonDTO> updateSongLesson(@PathVariable Long id, @RequestBody SongLessonDTO dto) {
        return songLessonService.updateSongLesson(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a song lesson identified by its ID.
     *
     * @param id The ID of the song lesson to delete.
     * @return ResponseEntity with HTTP status 204 No Content if deleted, or 404 Not Found if not found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSongLesson(@PathVariable Long id) {
        boolean deleted = songLessonService.deleteSongLesson(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

}
