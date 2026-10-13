package com.uo289390.toco.controller;

import com.uo289390.toco.dto.TabSectionDTO;
import com.uo289390.toco.service.TabSectionService;
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
 * REST controller for the tab sections of a song lesson.
 *
 * Base path: {@code /api/tab-sections}
 *
 * Endpoints:
 * - GET    /api/tab-sections                                Retrieve all tab sections
 * - POST   /api/tab-sections/by-ids                         Retrieve the tab sections with the given IDs (IDs in the body)
 * - GET    /api/tab-sections/{id}                           Retrieve a tab section by ID
 * - GET    /api/tab-sections/by-song-lesson/{songLessonId}  Retrieve the tab sections of a song lesson
 * - POST   /api/tab-sections                                Create a tab section
 * - PUT    /api/tab-sections/{id}                           Update a tab section
 * - DELETE /api/tab-sections/{id}                           Delete a tab section
 *
 * Responses: 200 OK, 201 Created (create), 204 No Content (delete) and 404 Not Found
 * when the tab section does not exist. Other errors are translated by
 * {@link GlobalExceptionHandler}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequestMapping("/api/tab-sections")
@RequiredArgsConstructor
public class TabSectionController {

    private final TabSectionService tabSectionService;

    /**
     * Retrieves all tab sections.
     *
     * @return 200 OK with the list of tab sections
     */
    @GetMapping
    public ResponseEntity<List<TabSectionDTO>> getAll() {
        return ResponseEntity.ok(tabSectionService.findAll());
    }

    /**
     * Retrieves the tab sections with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the tab sections
     * @return 200 OK with the tab sections found
     */
    @PostMapping("/by-ids")
    public ResponseEntity<List<TabSectionDTO>> getByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(tabSectionService.findByIds(ids));
    }

    /**
     * Retrieves a tab section by its ID.
     *
     * @param id the ID of the tab section
     * @return 200 OK with the tab section, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<TabSectionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.of(tabSectionService.findById(id));
    }

    /**
     * Retrieves the tab sections of a song lesson.
     *
     * @param songLessonId the ID of the song lesson
     * @return 200 OK with the tab sections of the song lesson
     */
    @GetMapping("/by-song-lesson/{songLessonId}")
    public ResponseEntity<List<TabSectionDTO>> getForSongLesson(@PathVariable Long songLessonId) {
        return ResponseEntity.ok(tabSectionService.findForSongLesson(songLessonId));
    }

    /**
     * Creates a tab section.
     *
     * @param dto the tab section to create
     * @return 201 Created with the created tab section
     */
    @PostMapping
    public ResponseEntity<TabSectionDTO> create(@Validated(OnCreate.class) @RequestBody TabSectionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tabSectionService.create(dto));
    }

    /**
     * Updates a tab section.
     *
     * @param id  the ID of the tab section
     * @param dto the new data of the tab section
     * @return 200 OK with the updated tab section, or 404 Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<TabSectionDTO> update(@PathVariable Long id, @Valid @RequestBody TabSectionDTO dto) {
        return ResponseEntity.of(tabSectionService.update(id, dto));
    }

    /**
     * Deletes a tab section.
     *
     * @param id the ID of the tab section
     * @return 204 No Content, or 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return tabSectionService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
