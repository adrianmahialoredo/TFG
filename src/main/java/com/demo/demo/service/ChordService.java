package com.demo.demo.service;

import com.demo.demo.dto.ChordDto;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for managing chords.
 *
 * Provides methods to create, retrieve, update, and delete chords,
 * as well as converting between Chord entities and ChordDto objects.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
public interface ChordService {

    /**
     * Retrieves all chords and converts them to DTOs.
     *
     * @return A list of ChordDto representing all chords.
     */
    List<ChordDto> findAll();

    /**
     * Retrieves a chord by its ID and converts it to a DTO.
     *
     * @param id The ID of the chord to retrieve.
     * @return An Optional containing the ChordDto if found, otherwise empty.
     */
    Optional<ChordDto> findById(Long id);

    /**
     * Creates a new chord from the provided DTO and saves it to the repository.
     *
     * @param dto The ChordDto containing the details of the chord to create.
     * @return The created ChordDto.
     */
    ChordDto create(ChordDto dto);

    /**
     * Updates an existing chord with the provided DTO.
     *
     * @param id  The ID of the chord to update.
     * @param dto The ChordDto containing the updated details.
     * @return An Optional containing the updated ChordDto if found, otherwise empty.
     */
    Optional<ChordDto> update(Long id, ChordDto dto);

    /**
     * Deletes a chord by its ID.
     *
     * @param id The ID of the chord to delete.
     * @return true if the chord was deleted, false if it did not exist.
     */
    boolean delete(Long id);

    /**
     * Retrieves all chords by a list of chord IDs and converts them to DTOs.
     *
     * @param ids The list of chord IDs to retrieve.
     * @return A list of ChordDto representing all chords with the provided IDs.
     */
    List<ChordDto> findByIds(List<Long> ids);

    /**
     * Retrieves all chord IDs associated with a specific song lesson.
     *
     * @param songLessonId The ID of the song lesson.
     * @return A list of chord IDs associated with the song lesson.
     */
    List<Long> findIdsForSongLesson(Long songLessonId);
}
