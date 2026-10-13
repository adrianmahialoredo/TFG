package com.uo289390.toco.controller;

import com.uo289390.toco.dto.InstrumentGenreDTO;
import com.uo289390.toco.service.InstrumentGenreService;
import com.uo289390.toco.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import com.uo289390.toco.dto.validation.OnCreate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * REST controller for instrument-genres (an instrument applied to a music genre).
 *
 * Base path: {@code /api/instrument-genres}
 *
 * Endpoints:
 * - GET    /api/instrument-genres                              Retrieve all instrument-genres
 * - POST   /api/instrument-genres/by-ids                       Retrieve the instrument-genres with the given IDs (IDs in the body)
 * - GET    /api/instrument-genres/{id}                         Retrieve an instrument-genre by ID
 * - GET    /api/instrument-genres/by-genre/{genreId}           Retrieve the instrument-genres of a genre
 * - GET    /api/instrument-genres/by-instrument/{instrumentId} Retrieve the instrument-genres of an instrument
 * - POST   /api/instrument-genres                              Create an instrument-genre
 * - PUT    /api/instrument-genres/{id}                         Update an instrument-genre
 * - DELETE /api/instrument-genres/{id}                         Delete an instrument-genre
 *
 * Responses: 200 OK, 201 Created (create), 204 No Content (delete) and 404 Not Found
 * when the instrument-genre does not exist. Other errors are translated by
 * {@link GlobalExceptionHandler}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequestMapping("/api/instrument-genres")
@RequiredArgsConstructor
public class InstrumentGenreController {

    private final InstrumentGenreService instrumentGenreService;

    /**
     * Retrieves all instrument-genres.
     *
     * @return 200 OK with the list of instrument-genres
     */
    @GetMapping
    public ResponseEntity<List<InstrumentGenreDTO>> getAll() {
        return ResponseEntity.ok(instrumentGenreService.findAll());
    }

    /**
     * Retrieves the instrument-genres with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the instrument-genres
     * @return 200 OK with the instrument-genres found
     */
    @PostMapping("/by-ids")
    public ResponseEntity<List<InstrumentGenreDTO>> getByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(instrumentGenreService.findByIds(ids));
    }

    /**
     * Retrieves an instrument-genre by its ID.
     *
     * @param id the ID of the instrument-genre
     * @return 200 OK with the instrument-genre, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<InstrumentGenreDTO> getById(@PathVariable Long id) {
        return ResponseEntity.of(instrumentGenreService.findById(id));
    }

    /**
     * Retrieves the instrument-genres of a genre.
     *
     * @param genreId the ID of the genre
     * @return 200 OK with the instrument-genres of the genre
     */
    @GetMapping("/by-genre/{genreId}")
    public ResponseEntity<List<InstrumentGenreDTO>> getForGenre(@PathVariable Long genreId) {
        return ResponseEntity.ok(instrumentGenreService.findForGenre(genreId));
    }

    /**
     * Retrieves the instrument-genres of an instrument.
     *
     * @param instrumentId the ID of the instrument
     * @return 200 OK with the instrument-genres of the instrument
     */
    @GetMapping("/by-instrument/{instrumentId}")
    public ResponseEntity<List<InstrumentGenreDTO>> getForInstrument(@PathVariable Long instrumentId) {
        return ResponseEntity.ok(instrumentGenreService.findForInstrument(instrumentId));
    }

    /**
     * Creates an instrument-genre.
     *
     * @param dto the instrument-genre to create
     * @return 201 Created with the created instrument-genre
     */
    @PostMapping
    public ResponseEntity<InstrumentGenreDTO> create(@Validated(OnCreate.class) @RequestBody InstrumentGenreDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(instrumentGenreService.create(dto));
    }

    /**
     * Updates an instrument-genre.
     *
     * @param id  the ID of the instrument-genre
     * @param dto the new data of the instrument-genre
     * @return 200 OK with the updated instrument-genre, or 404 Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<InstrumentGenreDTO> update(@PathVariable Long id, @Valid @RequestBody InstrumentGenreDTO dto) {
        return ResponseEntity.of(instrumentGenreService.update(id, dto));
    }

    /**
     * Deletes an instrument-genre.
     *
     * @param id the ID of the instrument-genre
     * @return 204 No Content, or 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return instrumentGenreService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
