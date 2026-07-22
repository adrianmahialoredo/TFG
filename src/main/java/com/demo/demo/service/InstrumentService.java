package com.demo.demo.service;

import com.demo.demo.dto.InstrumentDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for managing instruments.
 *
 * Provides methods to create, read, update, and delete instruments,
 * as well as methods to fetch instruments by ID and convert them to DTOs.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
public interface InstrumentService {

    /**
     * Fetches all instruments from the repository and converts them to DTOs.
     *
     * @return List of InstrumentDTOs representing all instruments.
     */
    List<InstrumentDTO> getAllInstruments();

    /**
     * Fetches an instrument by its ID and converts it to a DTO.
     *
     * @param id The ID of the instrument to fetch.
     * @return An Optional containing the InstrumentDTO if found, otherwise empty.
     */
    Optional<InstrumentDTO> getInstrumentById(Long id);

    /**
     * Creates a new instrument from the provided DTO and saves it to the repository.
     *
     * @param dto The InstrumentDTO containing the details of the instrument to create.
     * @return The created InstrumentDTO.
     */
    InstrumentDTO createInstrument(InstrumentDTO dto);

    /**
     * Updates an existing instrument with the provided DTO and saves it to the repository.
     *
     * @param id The ID of the instrument to update.
     * @param dto The InstrumentDTO containing the updated details of the instrument.
     * @return An Optional containing the updated InstrumentDTO if found, otherwise empty.
     */
    Optional<InstrumentDTO> updateInstrument(Long id, InstrumentDTO dto);

    /**
     * Deletes an instrument by its ID.
     *
     * @param id The ID of the instrument to delete.
     * @return true if the instrument was deleted, false if it did not exist.
     */
    boolean deleteInstrument(Long id);
}
