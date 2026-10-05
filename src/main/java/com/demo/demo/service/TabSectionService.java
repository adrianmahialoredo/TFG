package com.demo.demo.service;

import com.demo.demo.dto.TabSectionDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for managing tab sections in a song lesson.
 *
 * Provides methods to create, retrieve, update, and delete tab sections,
 * as well as methods to find tab sections by song lesson ID.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
public interface TabSectionService {

    /**
     * Creates a new TabSection.
     *
     * @param dto The TabSectionDTO containing the details of the section to create
     * @return The created TabSectionDTO
     */
    TabSectionDTO create(TabSectionDTO dto);

    /**
     * Retrieves a TabSection by its ID.
     *
     * @param id The ID of the TabSection to retrieve
     * @return An Optional containing the TabSectionDTO if found, or empty if not found
     */
    Optional<TabSectionDTO> findById(Long id);

    /**
     * Retrieves all TabSections associated with a specific SongLesson ID.
     *
     * @param lessonId The ID of the SongLesson to retrieve sections for
     * @return A list of TabSectionDTOs associated with the given SongLesson ID
     */
    List<TabSectionDTO> findForSongLesson(Long lessonId);

    /**
     * Updates an existing TabSection.
     *
     * @param id The ID of the TabSection to update
     * @param dto The TabSectionDTO containing the updated details
     * @return an Optional containing the updated TabSectionDTO if found, or empty if not found
     */
    Optional<TabSectionDTO> update(Long id, TabSectionDTO dto);

    /**
     * Deletes a TabSection by its ID.
     *
     * @param id The ID of the TabSection to delete
     * @return true if the section was deleted, false if it did not exist
     */
    boolean delete(Long id);

    /**
     * Retrieves all tab sections.
     *
     * @return a list of TabSectionDTO
     */
    List<TabSectionDTO> findAll();

    /**
     * Retrieves the tab sections with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the tab sections
     * @return a list of TabSectionDTO (empty if no IDs are given)
     */
    List<TabSectionDTO> findByIds(List<Long> ids);
}
