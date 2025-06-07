package com.demo.demo.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.demo.demo.dto.InstrumentGenreDTO;
import com.demo.demo.service.InstrumentGenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Controller for managing instrument-genres.
 * 
 * Provides endpoints to create, read, update, and delete instrument-genres.
 * Also allows retrieval of instrument-genres by instrument ID or genre ID.
 * 
 * This controller uses the InstrumentGenreService to handle business logic
 * and convert entities to DTOs for API responses.
 * 
 * Endpoints:
 * - GET /instrument-genres: Fetch all instrument-genres
 * - GET /instrument-genres/{id}: Fetch a specific instrument-genre by ID
 * - GET /instrument-genres/by-genre/{genreId}: Fetch instrument-genres by genre ID
 * - GET /instrument-genres/by-instrument/{instrumentId}: Fetch instrument-genres by instrument ID
 * - POST /instrument-genres: Create a new instrument-genre
 * - PUT /instrument-genres/{id}: Update an existing instrument-genre by ID
 * - DELETE /instrument-genres/{id}: Delete an instrument-genre by ID
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
@RequestMapping("/instrument-genres")
public class InstrumentGenreController {

    private final InstrumentGenreService instrumentGenreService;

    /**
     * Endpoint to retrieve all instrument-genres.
     *
     * @return List of InstrumentGenreDTO
     */
    @GetMapping
    public List<InstrumentGenreDTO> getAllInstrumentGenres() {
        return instrumentGenreService.getAllInstrumentGenres();
    }

    /**
     * Endpoint to retrieve an instrument-genre by its ID.
     *
     * @param id the ID of the instrument-genre
     * @return ResponseEntity containing InstrumentGenreDTO or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<InstrumentGenreDTO> getInstrumentGenreById(@PathVariable Long id) {
        return instrumentGenreService.getInstrumentGenreById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint to create a new instrument-genre.
     *
     * @param dto the DTO containing instrument-genre details
     * @return ResponseEntity containing the created InstrumentGenreDTO
     */
    @PostMapping
    public ResponseEntity<InstrumentGenreDTO> createInstrumentGenre(@RequestBody InstrumentGenreDTO dto) {
        InstrumentGenreDTO created = instrumentGenreService.createInstrumentGenre(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Endpoint to update an existing instrument-genre.
     *
     * @param id the ID of the instrument-genre to update
     * @param dto the DTO containing updated instrument-genre details
     * @return ResponseEntity containing the updated InstrumentGenreDTO or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<InstrumentGenreDTO> updateInstrumentGenre(@PathVariable Long id, @RequestBody InstrumentGenreDTO dto) {
        return instrumentGenreService.updateInstrumentGenre(id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint to delete an instrument-genre by its ID.
     *
     * @param id the ID of the instrument-genre to delete
     * @return ResponseEntity with no content if deleted, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstrumentGenre(@PathVariable Long id) {
        boolean deleted = instrumentGenreService.deleteInstrumentGenre(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Endpoint to retrieve instrument-genres by instrument ID.
     *
     * @param instrumentId the ID of the instrument
     * @return List of InstrumentGenreDTO
     */
    @GetMapping("/by-genre/{genreId}")
    public List<InstrumentGenreDTO> findByGenreId(@PathVariable Long genreId) {
        return instrumentGenreService.findByGenreId(genreId);
    }

    /**
     * Endpoint to retrieve instrument-genres by genre ID.
     *
     * @param instrumentId the ID of the instrument
     * @return List of InstrumentGenreDTO
     */
    @GetMapping("/by-instrument/{instrumentId}")
    public List<InstrumentGenreDTO> findByInstrumentId(@PathVariable Long instrumentId) {
        return instrumentGenreService.findByInstrumentId(instrumentId);
    }

}