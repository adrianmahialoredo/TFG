package com.uo289390.toco.controller;

import com.uo289390.toco.dto.LessonDTO;
import com.uo289390.toco.service.LessonService;
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
 * REST controller for lessons.
 *
 * Base path: {@code /api/lessons}
 *
 * Endpoints:
 * - GET    /api/lessons                    Retrieve all lessons
 * - POST   /api/lessons/by-ids             Retrieve the lessons with the given IDs (IDs in the body)
 * - GET    /api/lessons/{id}               Retrieve a lesson by ID
 * - GET    /api/lessons/by-genre/{genreId} Retrieve the lessons that belong to a genre
 * - GET    /api/lessons/popular/ids        Retrieve the IDs of the popular lessons
 * - GET    /api/lessons/new/ids            Retrieve the IDs of the newest lessons
 * - GET    /api/lessons/recommended/ids    Retrieve the IDs of the recommended lessons
 * - POST   /api/lessons                    Create a lesson
 * - PUT    /api/lessons/{id}               Update a lesson
 * - DELETE /api/lessons/{id}               Delete a lesson
 *
 * Responses: 200 OK, 201 Created (create), 204 No Content (delete) and 404 Not Found
 * when the lesson does not exist. Other errors are translated by
 * {@link GlobalExceptionHandler}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    /**
     * Retrieves all lessons.
     *
     * @return 200 OK with the list of lessons
     */
    @GetMapping
    public ResponseEntity<List<LessonDTO>> getAll() {
        return ResponseEntity.ok(lessonService.findAll());
    }

    /**
     * Retrieves the lessons with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the lessons
     * @return 200 OK with the lessons found
     */
    @PostMapping("/by-ids")
    public ResponseEntity<List<LessonDTO>> getByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(lessonService.findByIds(ids));
    }

    /**
     * Retrieves a lesson by its ID.
     *
     * @param id the ID of the lesson
     * @return 200 OK with the lesson, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<LessonDTO> getById(@PathVariable Long id) {
        return ResponseEntity.of(lessonService.findById(id));
    }

    /**
     * Retrieves the lessons that belong to a genre.
     *
     * @param genreId the ID of the genre
     * @return 200 OK with the lessons of the genre
     */
    @GetMapping("/by-genre/{genreId}")
    public ResponseEntity<List<LessonDTO>> getByGenre(@PathVariable Long genreId) {
        return ResponseEntity.ok(lessonService.findForGenre(genreId));
    }

    @GetMapping("/by-instrument/{id}/popular/ids")
    public ResponseEntity<List<Long>> getIdsInstrumentPopular(@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.findIdsInstrumentPopular(id));
    }

    /**
     * Retrieves the popular lessons.
     *
     * @return 200 OK with the IDs of the popular lessons
     */
    @GetMapping("/popular/ids")
    public ResponseEntity<List<Long>> getIdsPopular() {
        return ResponseEntity.ok(lessonService.findIdsPopular());
    }

    /**
     * Retrieves the newest lessons.
     *
     * @return 200 OK with the IDs of the newest lessons
     */
    @GetMapping("/new/ids")
    public ResponseEntity<List<Long>> getIdsNew() {
        return ResponseEntity.ok(lessonService.findIdsNew());
    }

    /**
     * Creates a lesson.
     *
     * @param dto the lesson to create
     * @return 201 Created with the created lesson
     */
    @PostMapping
    public ResponseEntity<LessonDTO> create(@Validated(OnCreate.class) @RequestBody LessonDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.create(dto));
    }

    /**
     * Updates a lesson.
     *
     * @param id  the ID of the lesson
     * @param dto the new data of the lesson
     * @return 200 OK with the updated lesson, or 404 Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<LessonDTO> update(@PathVariable Long id, @Valid @RequestBody LessonDTO dto) {
        return ResponseEntity.of(lessonService.update(id, dto));
    }

    /**
     * Deletes a lesson.
     *
     * @param id the ID of the lesson
     * @return 204 No Content, or 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return lessonService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
