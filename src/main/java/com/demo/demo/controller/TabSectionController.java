package com.demo.demo.controller;

import com.demo.demo.dto.TabSectionDTO;
import com.demo.demo.service.TabSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller for managing tab sections in a song lesson.
 * 
 * Provides endpoints to create, retrieve, update, and delete tab sections,
 * as well as to retrieve tab sections by their associated song lesson ID.
 * 
 * This controller uses the TabSectionService to handle business logic
 * and convert entities to DTOs for API responses.
 * 
 * Endpoints:
 * - POST /tab-sections: Create a new tab section
 * - GET /tab-sections/{id}: Retrieve a tab section by ID
 * - GET /tab-sections/by-lesson/{lessonId}: Retrieve tab sections by song lesson ID
 * - PUT /tab-sections/{id}: Update an existing tab section by ID
 * - DELETE /tab-sections/{id}: Delete a tab section by ID
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
@RequestMapping("/tab-sections")
public class TabSectionController {

    private final TabSectionService tabSectionService;

    /**
     * Creates a new tab section.
     * 
     * @param dto The TabSectionDTO containing the details of the section to create
     * @return ResponseEntity with the created TabSectionDTO and HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<TabSectionDTO> create(@RequestBody TabSectionDTO dto) {
        TabSectionDTO created = tabSectionService.createTabSection(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieves a tab section by its ID.
     * 
     * @param id The ID of the TabSection to retrieve
     * @return ResponseEntity with the TabSectionDTO if found, or HTTP status 404 (Not Found) if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<TabSectionDTO> getById(@PathVariable Long id) {
        return tabSectionService.getTabSectionById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all tab sections associated with a specific song lesson ID.
     * 
     * @param lessonId The ID of the song lesson to retrieve sections for
     * @return A list of TabSectionDTOs associated with the given song lesson ID
     */
    @GetMapping("/by-lesson/{lessonId}")
    public List<TabSectionDTO> getByLessonId(@PathVariable Long lessonId) {
        return tabSectionService.getTabSectionsBySongLessonId(lessonId);
    }

    /**
     * Updates an existing tab section.
     * 
     * @param id The ID of the TabSection to update
     * @param dto The TabSectionDTO containing the updated details
     * @return ResponseEntity with the updated TabSectionDTO if found, or HTTP status 404 (Not Found) if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<TabSectionDTO> update(@PathVariable Long id, @RequestBody TabSectionDTO dto) {
        return tabSectionService.updateTabSection(id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a tab section by its ID.
     * 
     * @param id The ID of the TabSection to delete
     * @return ResponseEntity with HTTP status 204 (No Content) if deleted, or HTTP status 404 (Not Found) if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = tabSectionService.deleteTabSection(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

