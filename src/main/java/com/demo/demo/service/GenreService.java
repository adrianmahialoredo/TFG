package com.demo.demo.service;

import com.demo.demo.dto.GenreDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for managing genres.
 *
 * Provides methods to create, read, update, and delete genres,
 * as well as methods to fetch genres by ID and convert them to DTOs.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
public interface GenreService {

    /**
     * Fetches all genres from the repository and converts them to DTOs.
     *
     * @return List of GenreDTOs representing all genres.
     */
    List<GenreDTO> getAllGenres();

    /**
     * Fetches a genre by its ID and converts it to a DTO.
     *
     * @param id The ID of the genre to fetch.
     * @return An Optional containing the GenreDTO if found, otherwise empty.
     */
    Optional<GenreDTO> getGenreById(Long id);

    /**
     * Creates a new genre from the provided DTO and saves it to the repository.
     *
     * @param dto The GenreDTO containing the details of the genre to create.
     * @return The created GenreDTO.
     */
    GenreDTO createGenre(GenreDTO dto);

    /**
     * Updates an existing genre identified by its ID with the details from the provided DTO.
     *
     * @param id  The ID of the genre to update.
     * @param dto The GenreDTO containing the updated details of the genre.
     * @return An Optional containing the updated GenreDTO if found, otherwise empty.
     */
    Optional<GenreDTO> updateGenre(Long id, GenreDTO dto);

    /**
     * Deletes a genre by its ID.
     *
     * @param id The ID of the genre to delete.
     * @return true if the genre was deleted, false if it did not exist.
     */
    boolean deleteGenre(Long id);
}
