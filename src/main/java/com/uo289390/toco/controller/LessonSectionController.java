package com.uo289390.toco.controller;

import com.uo289390.toco.dto.LessonSectionDTO;
import com.uo289390.toco.service.LessonSectionService;
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
 * REST controller for the sections of a lesson.
 *
 * Base path: {@code /api/lesson-sections}
 *
 * Endpoints:
 * - GET    /api/lesson-sections                      Retrieve all lesson sections
 * - POST   /api/lesson-sections/by-ids               Retrieve the lesson sections with the given IDs (IDs in the body)
 * - GET    /api/lesson-sections/{id}                 Retrieve a lesson section by ID
 * - GET    /api/lesson-sections/by-lesson/{lessonId} Retrieve the sections of a lesson
 * - POST   /api/lesson-sections                      Create a lesson section
 * - PUT    /api/lesson-sections/{id}                 Update a lesson section
 * - DELETE /api/lesson-sections/{id}                 Delete a lesson section
 *
 * Responses: 200 OK, 201 Created (create), 204 No Content (delete) and 404 Not Found
 * when the lesson section does not exist. Other errors are translated by
 * {@link GlobalExceptionHandler}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequestMapping("/api/lesson-sections")
@RequiredArgsConstructor
public class LessonSectionController {

    private final LessonSectionService lessonSectionService;

    /**
     * Retrieves all lesson sections.
     *
     * @return 200 OK with the list of lesson sections
     */
    @GetMapping
    public ResponseEntity<List<LessonSectionDTO>> getAll() {
        return ResponseEntity.ok(lessonSectionService.findAll());
    }

    /**
     * Retrieves the lesson sections with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the lesson sections
     * @return 200 OK with the lesson sections found
     */
    @PostMapping("/by-ids")
    public ResponseEntity<List<LessonSectionDTO>> getByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(lessonSectionService.findByIds(ids));
    }

    /**
     * Retrieves a lesson section by its ID.
     *
     * @param id the ID of the lesson section
     * @return 200 OK with the lesson section, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<LessonSectionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.of(lessonSectionService.findById(id));
    }

    /**
     * Retrieves the sections of a lesson.
     *
     * @param lessonId the ID of the lesson
     * @return 200 OK with the sections of the lesson
     */
    @GetMapping("/by-lesson/{lessonId}")
    public ResponseEntity<List<LessonSectionDTO>> getForLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(lessonSectionService.findForLesson(lessonId));
    }

    /**
     * Creates a lesson section.
     *
     * @param dto the lesson section to create
     * @return 201 Created with the created lesson section
     */
    @PostMapping
    public ResponseEntity<LessonSectionDTO> create(@Validated(OnCreate.class) @RequestBody LessonSectionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonSectionService.create(dto));
    }

    /**
     * Updates a lesson section.
     *
     * @param id  the ID of the lesson section
     * @param dto the new data of the lesson section
     * @return 200 OK with the updated lesson section, or 404 Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<LessonSectionDTO> update(@PathVariable Long id, @Valid @RequestBody LessonSectionDTO dto) {
        return ResponseEntity.of(lessonSectionService.update(id, dto));
    }

    /**
     * Deletes a lesson section.
     *
     * @param id the ID of the lesson section
     * @return 204 No Content, or 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return lessonSectionService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
