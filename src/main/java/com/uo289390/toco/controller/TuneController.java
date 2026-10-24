package com.uo289390.toco.controller;

import com.uo289390.toco.dto.TuneDTO;
import com.uo289390.toco.dto.validation.OnCreate;
import com.uo289390.toco.exception.GlobalExceptionHandler;
import com.uo289390.toco.service.TuneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the tunings used by the tuner.
 *
 * Base path: {@code /api/tunes}
 *
 * Endpoints:
 * - GET    /api/tunes                               Retrieve all tunings
 * - POST   /api/tunes/by-ids                        Retrieve the tunings with the given IDs (IDs in the body)
 * - GET    /api/tunes/{id}                          Retrieve a tuning by ID
 * - GET    /api/tunes/by-instrument/{instrumentId}  Retrieve the tunings of an instrument
 * - POST   /api/tunes                               Create a tuning
 * - PUT    /api/tunes/{id}                          Update a tuning
 * - DELETE /api/tunes/{id}                          Delete a tuning
 *
 * Responses: 200 OK, 201 Created (create), 204 No Content (delete) and 404 Not Found
 * when the tuning does not exist. Other errors (e.g. an instrument that does not exist)
 * are translated by {@link GlobalExceptionHandler}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-10-23
 */
@RestController
@RequestMapping("/api/tunes")
@RequiredArgsConstructor
public class TuneController {

    private final TuneService tuneService;

    /**
     * Retrieves all tunings.
     *
     * @return 200 OK with the list of tunings
     */
    @GetMapping
    public ResponseEntity<List<TuneDTO>> getAll() {
        return ResponseEntity.ok(tuneService.findAll());
    }

    /**
     * Retrieves the tunings with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the tunings
     * @return 200 OK with the tunings found
     */
    @PostMapping("/by-ids")
    public ResponseEntity<List<TuneDTO>> getByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(tuneService.findByIds(ids));
    }

    /**
     * Retrieves a tuning by its ID.
     *
     * @param id the ID of the tuning
     * @return 200 OK with the tuning, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<TuneDTO> getById(@PathVariable Long id) {
        return ResponseEntity.of(tuneService.findById(id));
    }

    /**
     * Retrieves the tunings of an instrument.
     *
     * @param instrumentId the ID of the instrument
     * @return 200 OK with the tunings (empty if the instrument has none)
     */
    @GetMapping("/by-instrument/{instrumentId}")
    public ResponseEntity<List<TuneDTO>> getForInstrument(@PathVariable Long instrumentId) {
        return ResponseEntity.ok(tuneService.findForInstrument(instrumentId));
    }

    /**
     * Creates a tuning.
     *
     * @param dto the tuning to create
     * @return 201 Created with the created tuning, 400 Bad Request if the body is invalid
     * or 404 Not Found if the instrument does not exist
     */
    @PostMapping
    public ResponseEntity<TuneDTO> create(@Validated(OnCreate.class) @RequestBody TuneDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tuneService.create(dto));
    }

    /**
     * Updates a tuning.
     *
     * @param id  the ID of the tuning
     * @param dto the new data of the tuning
     * @return 200 OK with the updated tuning, or 404 Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<TuneDTO> update(@PathVariable Long id, @Valid @RequestBody TuneDTO dto) {
        return ResponseEntity.of(tuneService.update(id, dto));
    }

    /**
     * Deletes a tuning.
     *
     * @param id the ID of the tuning
     * @return 204 No Content, or 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return tuneService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
