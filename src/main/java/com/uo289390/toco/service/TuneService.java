package com.uo289390.toco.service;

import com.uo289390.toco.dto.TuneDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for managing the tunings used by the tuner.
 *
 * Provides methods to create, retrieve, update, and delete tunings,
 * as well as the tunings of an instrument.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-10-23
 */
public interface TuneService {

    /**
     * Retrieves all tunings.
     *
     * @return A list of TuneDTO representing all tunings.
     */
    List<TuneDTO> findAll();

    /**
     * Retrieves the tunings with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids The list of tuning IDs to retrieve.
     * @return A list of TuneDTO with the tunings found.
     */
    List<TuneDTO> findByIds(List<Long> ids);

    /**
     * Retrieves a tuning by its ID.
     *
     * @param id The ID of the tuning to retrieve.
     * @return An Optional containing the TuneDTO if found, otherwise empty.
     */
    Optional<TuneDTO> findById(Long id);

    /**
     * Retrieves the tunings of an instrument, in id order.
     *
     * @param instrumentId The ID of the instrument.
     * @return A list of TuneDTO, empty if the instrument has none.
     */
    List<TuneDTO> findForInstrument(Long instrumentId);

    /**
     * Creates a new tuning.
     *
     * @param dto The TuneDTO with the data of the tuning.
     * @return The created TuneDTO.
     * @throws jakarta.persistence.EntityNotFoundException if the instrument does not exist
     */
    TuneDTO create(TuneDTO dto);

    /**
     * Updates an existing tuning.
     *
     * @param id  The ID of the tuning to update.
     * @param dto The TuneDTO with the new data.
     * @return An Optional containing the updated TuneDTO if found, otherwise empty.
     * @throws jakarta.persistence.EntityNotFoundException if the new instrument does not exist
     */
    Optional<TuneDTO> update(Long id, TuneDTO dto);

    /**
     * Deletes a tuning by its ID.
     *
     * @param id The ID of the tuning to delete.
     * @return true if the tuning was deleted, false if it did not exist.
     */
    boolean delete(Long id);
}
