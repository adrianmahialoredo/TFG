package com.demo.demo.controller;

import com.demo.demo.dto.GenreDTO;
import com.demo.demo.service.GenreService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import lombok.RequiredArgsConstructor;

/**
 * Controller for managing music genres.
 * 
 * Provides endpoints to create, read, update, and delete genres.
 * Also allows retrieval of all genres or a specific genre by ID.
 * 
 * This controller uses the GenreService to handle business logic
 * and convert entities to DTOs for API responses.
 * 
 * Endpoints:
 * - GET /genres: Fetch all genres
 * - GET /genres/{id}: Fetch a specific genre by ID
 * - POST /genres: Create a new genre
 * - PUT /genres/{id}: Update an existing genre by ID
 * - DELETE /genres/{id}: Delete a genre by ID
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
@RequestMapping("/genres")
public class GenreController {

    private final GenreService genreService;

    /**
     * Endpoint to retrieve all genres.
     *
     * @return List of GenreDTO
     */
    @GetMapping
    public List<GenreDTO> getAllGenres() {
        return genreService.getAllGenres();
    }

    /**
     * Endpoint to retrieve a genre by its ID.
     *
     * @param id the ID of the genre
     * @return ResponseEntity containing GenreDTO or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<GenreDTO> getGenreById(@PathVariable Long id) {
        return genreService.getGenreById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint to create a new genre.
     *
     * @param genreDTO the DTO containing genre details
     * @return ResponseEntity containing the created GenreDTO
     */
    @PostMapping
    public ResponseEntity<GenreDTO> createGenre(@RequestBody GenreDTO genreDTO) {
        GenreDTO created = genreService.createGenre(genreDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Endpoint to update an existing genre.
     *
     * @param id the ID of the genre to update
     * @param genreDTO the DTO containing updated genre details
     * @return ResponseEntity containing the updated GenreDTO or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<GenreDTO> updateGenre(@PathVariable Long id, @RequestBody GenreDTO genreDTO) {
        return genreService.updateGenre(id, genreDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint to delete a genre by its ID.
     *
     * @param id the ID of the genre to delete
     * @return ResponseEntity with no content if deleted, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
        boolean deleted = genreService.deleteGenre(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
