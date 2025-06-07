package com.demo.demo.controller;

import com.demo.demo.dto.InstrumentDTO;
import com.demo.demo.service.InstrumentService;

import java.util.List;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import lombok.*;

/**
 * Controller for managing musical instruments.
 * 
 * Provides endpoints to create, read, update, and delete instruments.
 * Also allows retrieval of all instruments or a specific instrument by ID.
 * 
 * This controller uses the InstrumentService to handle business logic
 * and convert entities to DTOs for API responses.
 * 
 * Endpoints:
 * - GET /instruments: Fetch all instruments
 * - GET /instruments/{id}: Fetch a specific instrument by ID
 * - POST /instruments: Create a new instrument
 * - PUT /instruments/{id}: Update an existing instrument by ID
 * - DELETE /instruments/{id}: Delete an instrument by ID
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
@RequestMapping("/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    /**
     * Endpoint to retrieve all instruments.
     *
     * @return List of InstrumentDTO
     */
    @GetMapping
    public List<InstrumentDTO> getAllInstruments() {
        return instrumentService.getAllInstruments();
    }

    /**
     * Endpoint to retrieve an instrument by its ID.
     *
     * @param id the ID of the instrument
     * @return ResponseEntity containing InstrumentDTO or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<InstrumentDTO> getInstrumentById(@PathVariable Long id) {
        return instrumentService.getInstrumentById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint to create a new instrument.
     *
     * @param instrumentDTO the DTO containing instrument details
     * @return ResponseEntity containing the created InstrumentDTO
     */
    @PostMapping
    public ResponseEntity<InstrumentDTO> createInstrument(@RequestBody InstrumentDTO instrumentDTO) {
        InstrumentDTO created = instrumentService.createInstrument(instrumentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Endpoint to update an existing instrument.
     *
     * @param id the ID of the instrument to update
     * @param instrumentDTO the DTO containing updated instrument details
     * @return ResponseEntity containing the updated InstrumentDTO or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<InstrumentDTO> updateInstrument(@PathVariable Long id, @RequestBody InstrumentDTO instrumentDTO) {
        return instrumentService.updateInstrument(id, instrumentDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint to delete an instrument by its ID.
     *
     * @param id the ID of the instrument to delete
     * @throws ResponseStatusException if the instrument is not found
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInstrument(@PathVariable Long id) {
        boolean deleted = instrumentService.deleteInstrument(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instrument not found");
        }
    }
    
}
