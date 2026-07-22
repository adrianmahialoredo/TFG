package com.demo.demo.controller;

import com.demo.demo.service.LessonService;
import com.demo.demo.dto.LessonDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * Controller for managing lessons.
 * 
 * Provides endpoints to create, read, update, and delete lessons.
 * As well as to fetch all lessons or a specific lesson by ID.
 * 
 * This controller uses the LessonService to handle business logic
 * and convert entities to DTOs for API responses.
 * 
 * Endpoints:
 * - GET /lessons: Fetch all lessons
 * - GET /lessons/{id}: Fetch a specific lesson by ID
 * - GET /lessons/by-genre/{genreId}: Fetch all lessons by genre ID
 * - GET /lessons/ids: Fetch multiple lessons by IDs
 * - POST /lessons: Create a new lesson
 * - PUT /lessons/{id}: Update an existing lesson by ID
 * - DELETE /lessons/{id}: Delete a lesson by ID
 * 
 * This controller is annotated with @RestController to handle RESTful requests
 * and @RequestMapping to define the base path for all endpoints.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 * @todo Completado - Implementación lista
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/lessons")
public class LessonController {

    private final LessonService lessonService;

    /**
     * Fetches all lessons from the service and returns them as a list of LessonDTOs.
     *
     * @return List of LessonDTOs representing all lessons.
     */
    @GetMapping
    public List<LessonDTO> getAllLessons() {
        return lessonService.getAllLessons();
    }

    /**
     * Fetches a specific lesson by its ID and returns it as a LessonDTO.
     *
     * @param id The ID of the lesson to fetch.
     * @return ResponseEntity containing the LessonDTO if found, or 404 Not Found if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LessonDTO> getLessonById(@PathVariable Long id) {
        return lessonService.getLessonById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new lesson from the provided LessonDTO and returns the created lesson.
     *
     * @param lessonDto The LessonDTO containing the details of the lesson to create.
     * @return ResponseEntity containing the created LessonDTO with status 201 Created.
     */
    @PostMapping
    public ResponseEntity<LessonDTO> createLesson(@RequestBody LessonDTO lessonDto) {
        LessonDTO created = lessonService.createLesson(lessonDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing lesson identified by its ID with the provided LessonDTO.
     *
     * @param id The ID of the lesson to update.
     * @param lessonDto The LessonDTO containing the updated details of the lesson.
     * @return ResponseEntity containing the updated LessonDTO if successful, or 404 Not Found if not found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LessonDTO> updateLesson(@PathVariable Long id, @RequestBody LessonDTO lessonDto) {
        return lessonService.updateLesson(id, lessonDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a lesson identified by its ID.
     *
     * @param id The ID of the lesson to delete.
     * @return ResponseEntity with status 204 No Content if successful, or 404 Not Found if not found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        if (lessonService.deleteLesson(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Fetches all lessons by a specific genre and returns them as a list of LessonDTOs.
     *
     * @param genreId The ID of the genre to filter lessons by.
     * @return List of LessonDTOs representing all lessons from the specified genre.
     */
    @GetMapping("/by-genre/{genreId}")
    public List<LessonDTO> getLessonsByGenreId(@PathVariable Long genreId) {
        return lessonService.getLessonsByGenreId(genreId);
    }

    /**
     * Fetches all lessons by a list of IDs and returns them as a list of LessonDTOs.
     *
     * @param ids The list of lesson IDs to fetch.
     * @return List of LessonDTOs representing all lessons with the specified IDs.
     */
    @PostMapping("/ids")
    public List<LessonDTO> getLessonsByIds(@RequestBody List<Long> ids) {
        return lessonService.getLessonsByIds(ids);
    }

    /**
     * Fetches all popular lessons globally for the Home screen.
     *
     * @return List of LessonDTOs representing all popular lessons globally.
     */
    @GetMapping("/home/popular")
    public List<LessonDTO> getHomeLessonsPopular() {
        return lessonService.getHomeLessonsPopular();
    }

    /**
     * Fetches the newest lessons globally for the Home screen.
     *
     * @return List of LessonDTOs representing the newest lessons.
     */
    @GetMapping("/home/new")
    public List<LessonDTO> getHomeLessonsNew() {
        return lessonService.getHomeLessonsNew();
    }

    /**
     * Fetches recommended lessons for the user for the Home screen.
     *
     * @return List of LessonDTOs representing recommended lessons.
     */
    @GetMapping("/home/recommended")
    public List<LessonDTO> getHomeLessonsRecommended() {
        return lessonService.getHomeLessonsRecommended();
    }
}


