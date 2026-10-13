package com.uo289390.toco.controller;

import com.uo289390.toco.dto.InstrumentDTO;
import com.uo289390.toco.service.InstrumentService;
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
 * REST controller for musical instruments.
 *
 * Base path: {@code /api/instruments}
 *
 * Endpoints:
 * - GET    /api/instruments        Retrieve all instruments
 * - POST   /api/instruments/by-ids Retrieve the instruments with the given IDs (IDs in the body)
 * - GET    /api/instruments/{id}   Retrieve an instrument by ID
 * - POST   /api/instruments        Create an instrument
 * - PUT    /api/instruments/{id}   Update an instrument
 * - DELETE /api/instruments/{id}   Delete an instrument
 *
 * Responses: 200 OK, 201 Created (create), 204 No Content (delete) and 404 Not Found
 * when the instrument does not exist. Other errors are translated by
 * {@link GlobalExceptionHandler}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
public class InstrumentController {

    private final InstrumentService instrumentService;

    /**
     * Retrieves all instruments.
     *
     * @return 200 OK with the list of instruments
     */
    @GetMapping
    public ResponseEntity<List<InstrumentDTO>> getAll() {
        return ResponseEntity.ok(instrumentService.findAll());
    }

    /**
     * Retrieves the instruments with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the instruments
     * @return 200 OK with the instruments found
     */
    @PostMapping("/by-ids")
    public ResponseEntity<List<InstrumentDTO>> getByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(instrumentService.findByIds(ids));
    }

    /**
     * Retrieves an instrument by its ID.
     *
     * @param id the ID of the instrument
     * @return 200 OK with the instrument, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<InstrumentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.of(instrumentService.findById(id));
    }

    /**
     * Creates an instrument.
     *
     * @param dto the instrument to create
     * @return 201 Created with the created instrument
     */
    @PostMapping
    public ResponseEntity<InstrumentDTO> create(@Validated(OnCreate.class) @RequestBody InstrumentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(instrumentService.create(dto));
    }

    /**
     * Updates an instrument.
     *
     * @param id  the ID of the instrument
     * @param dto the new data of the instrument
     * @return 200 OK with the updated instrument, or 404 Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<InstrumentDTO> update(@PathVariable Long id, @Valid @RequestBody InstrumentDTO dto) {
        return ResponseEntity.of(instrumentService.update(id, dto));
    }

    /**
     * Deletes an instrument.
     *
     * @param id the ID of the instrument
     * @return 204 No Content, or 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return instrumentService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
