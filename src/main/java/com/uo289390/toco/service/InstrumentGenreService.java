package com.uo289390.toco.service;

import com.uo289390.toco.dto.InstrumentGenreDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for managing instrument genres.
 *
 * Provides methods to create, read, update, and delete instrument genres,
 * as well as methods to find instrument genres by instrument or genre IDs.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
public interface InstrumentGenreService {

    /**
     * Fetches all instrument genres from the repository and converts them to DTOs.
     *
     * @return List of InstrumentGenreDTOs representing all instrument genres.
     */
    List<InstrumentGenreDTO> findAll();

    /**
     * Fetches an instrument genre by its ID and converts it to a DTO.
     *
     * @param id The ID of the instrument genre to fetch.
     * @return An Optional containing the InstrumentGenreDTO if found, otherwise empty.
     */
    Optional<InstrumentGenreDTO> findById(Long id);

    /**
     * Creates a new instrument genre from the provided DTO and saves it to the repository.
     *
     * @param dto The InstrumentGenreDTO containing the details of the instrument genre to create.
     * @return The created InstrumentGenreDTO.
     */
    InstrumentGenreDTO create(InstrumentGenreDTO dto);

    /**
     * Updates an existing instrument genre identified by its ID with the details from the provided DTO.
     *
     * @param id  The ID of the instrument genre to update.
     * @param dto The InstrumentGenreDTO containing the updated details of the instrument genre.
     * @return An Optional containing the updated InstrumentGenreDTO if found, otherwise empty.
     */
    Optional<InstrumentGenreDTO> update(Long id, InstrumentGenreDTO dto);

    /**
     * Deletes an instrument genre by its ID.
     *
     * @param id The ID of the instrument genre to delete.
     * @return true if the deletion was successful, false if the instrument genre was not found.
     */
    boolean delete(Long id);

    /**
     * Finds instrument genres by genre ID.
     *
     * @param genreId The ID of the genre to search for.
     * @return List of InstrumentGenreDTOs associated with the specified genre ID.
     */
    List<InstrumentGenreDTO> findForGenre(Long genreId);

    /**
     * Finds instrument genres by instrument ID.
     *
     * @param instrumentId The ID of the instrument to search for.
     * @return List of InstrumentGenreDTOs associated with the specified instrument ID.
     */
    List<InstrumentGenreDTO> findForInstrument(Long instrumentId);

    /**
     * Retrieves the instrument-genres with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the instrument-genres
     * @return a list of InstrumentGenreDTO (empty if no IDs are given)
     */
    List<InstrumentGenreDTO> findByIds(List<Long> ids);
}
