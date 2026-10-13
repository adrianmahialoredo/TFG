package com.uo289390.toco.service;

import com.uo289390.toco.dto.InstrumentDTO;

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
    List<InstrumentDTO> findAll();

    /**
     * Fetches an instrument by its ID and converts it to a DTO.
     *
     * @param id The ID of the instrument to fetch.
     * @return An Optional containing the InstrumentDTO if found, otherwise empty.
     */
    Optional<InstrumentDTO> findById(Long id);

    /**
     * Creates a new instrument from the provided DTO and saves it to the repository.
     *
     * @param dto The InstrumentDTO containing the details of the instrument to create.
     * @return The created InstrumentDTO.
     */
    InstrumentDTO create(InstrumentDTO dto);

    /**
     * Updates an existing instrument with the provided DTO and saves it to the repository.
     *
     * @param id The ID of the instrument to update.
     * @param dto The InstrumentDTO containing the updated details of the instrument.
     * @return An Optional containing the updated InstrumentDTO if found, otherwise empty.
     */
    Optional<InstrumentDTO> update(Long id, InstrumentDTO dto);

    /**
     * Deletes an instrument by its ID.
     *
     * @param id The ID of the instrument to delete.
     * @return true if the instrument was deleted, false if it did not exist.
     */
    boolean delete(Long id);

    /**
     * Retrieves the instruments with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the instruments
     * @return a list of InstrumentDTO (empty if no IDs are given)
     */
    List<InstrumentDTO> findByIds(List<Long> ids);
}
