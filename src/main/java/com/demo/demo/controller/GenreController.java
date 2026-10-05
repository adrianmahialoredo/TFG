package com.demo.demo.controller;

import com.demo.demo.dto.GenreDTO;
import com.demo.demo.service.GenreService;
import com.demo.demo.dto.validation.OnCreate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * REST controller for music genres.
 *
 * Base path: {@code /api/genres}
 *
 * Endpoints:
 * - GET    /api/genres        Retrieve all genres
 * - POST   /api/genres/by-ids Retrieve the genres with the given IDs (IDs in the body)
 * - GET    /api/genres/{id}   Retrieve a genre by ID
 * - POST   /api/genres        Create a genre
 * - PUT    /api/genres/{id}   Update a genre
 * - DELETE /api/genres/{id}   Delete a genre
 *
 * Responses: 200 OK, 201 Created (create), 204 No Content (delete) and 404 Not Found
 * when the genre does not exist. Other errors are translated by
 * {@link com.demo.demo.exception.GlobalExceptionHandler}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    /**
     * Retrieves all genres.
     *
     * @return 200 OK with the list of genres
     */
    @GetMapping
    public ResponseEntity<List<GenreDTO>> getAll() {
        return ResponseEntity.ok(genreService.findAll());
    }

    /**
     * Retrieves the genres with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the genres
     * @return 200 OK with the genres found
     */
    @PostMapping("/by-ids")
    public ResponseEntity<List<GenreDTO>> getByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(genreService.findByIds(ids));
    }

    /**
     * Retrieves a genre by its ID.
     *
     * @param id the ID of the genre
     * @return 200 OK with the genre, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<GenreDTO> getById(@PathVariable Long id) {
        return ResponseEntity.of(genreService.findById(id));
    }

    /**
     * Creates a genre.
     *
     * @param dto the genre to create
     * @return 201 Created with the created genre
     */
    @PostMapping
    public ResponseEntity<GenreDTO> create(@Validated(OnCreate.class) @RequestBody GenreDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(genreService.create(dto));
    }

    /**
     * Updates a genre.
     *
     * @param id  the ID of the genre
     * @param dto the new data of the genre
     * @return 200 OK with the updated genre, or 404 Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<GenreDTO> update(@PathVariable Long id, @Valid @RequestBody GenreDTO dto) {
        return ResponseEntity.of(genreService.update(id, dto));
    }

    /**
     * Deletes a genre.
     *
     * @param id the ID of the genre
     * @return 204 No Content, or 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return genreService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
