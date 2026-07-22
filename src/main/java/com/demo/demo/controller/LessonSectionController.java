package com.demo.demo.controller;

import com.demo.demo.dto.LessonSectionDTO;
import com.demo.demo.service.LessonSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing lesson sections.
 *
 * Provides endpoints to create, retrieve, update, and delete lesson sections,
 * as well as to retrieve lesson sections by their associated lesson ID.
 *
 * Endpoints:
 * - POST /lesson-sections: Create a new lesson section
 * - GET /lesson-sections/{id}: Retrieve a lesson section by ID
 * - GET /lesson-sections/by-lesson/{lessonId}: Retrieve lesson sections by lesson ID
 * - PUT /lesson-sections/{id}: Update an existing lesson section by ID
 * - DELETE /lesson-sections/{id}: Delete a lesson section by ID
 *
 * @author Adrian Mahia
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/lesson-sections")
public class LessonSectionController {

    private final LessonSectionService lessonSectionService;

    /**
     * Creates a new lesson section.
     *
     * @param dto The LessonSectionDTO containing the details of the section to create
     * @return ResponseEntity with the created LessonSectionDTO and HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<LessonSectionDTO> create(@RequestBody LessonSectionDTO dto) {
        LessonSectionDTO created = lessonSectionService.createLessonSection(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieves a lesson section by its ID.
     *
     * @param id The ID of the LessonSection to retrieve
     * @return ResponseEntity with the LessonSectionDTO if found, or HTTP status 404 (Not Found) if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<LessonSectionDTO> getById(@PathVariable Long id) {
        return lessonSectionService.getLessonSectionById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all lesson sections associated with a specific lesson ID.
     *
     * @param lessonId The ID of the lesson to retrieve sections for
     * @return A list of LessonSectionDTOs associated with the given lesson ID
     */
    @GetMapping("/by-lesson/{lessonId}")
    public List<LessonSectionDTO> getByLessonId(@PathVariable Long lessonId) {
        return lessonSectionService.getLessonSectionsByLessonId(lessonId);
    }

    /**
     * Updates an existing lesson section.
     *
     * @param id The ID of the LessonSection to update
     * @param dto The LessonSectionDTO containing the updated details
     * @return ResponseEntity with the updated LessonSectionDTO if found, or HTTP status 404 (Not Found) if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<LessonSectionDTO> update(@PathVariable Long id, @RequestBody LessonSectionDTO dto) {
        return lessonSectionService.updateLessonSection(id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a lesson section by its ID.
     *
     * @param id The ID of the LessonSection to delete
     * @return ResponseEntity with HTTP status 204 (No Content) if deleted, or HTTP status 404 (Not Found) if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = lessonSectionService.deleteLessonSection(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

