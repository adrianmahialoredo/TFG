package com.demo.demo.service;

import com.demo.demo.dto.LessonSectionDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for managing lesson sections.
 *
 * Provides methods to create, retrieve, update, and delete lesson sections,
 * as well as methods to find sections by lesson ID.
 *
 * @author Adrian Mahia
 * @version 1.0
 * @since 2025-06-06
 */
public interface LessonSectionService {

    /**
     * Creates a new LessonSection.
     *
     * @param dto The LessonSectionDTO containing the details of the section to create
     * @return The created LessonSectionDTO
     */
    LessonSectionDTO create(LessonSectionDTO dto);

    /**
     * Retrieves a LessonSection by its ID.
     *
     * @param id The ID of the LessonSection to retrieve
     * @return An Optional containing the LessonSectionDTO if found, or empty if not found
     */
    Optional<LessonSectionDTO> findById(Long id);

    /**
     * Retrieves all LessonSections associated with a specific Lesson ID.
     *
     * @param lessonId The ID of the Lesson to retrieve sections for
     * @return A list of LessonSectionDTOs associated with the given Lesson ID
     */
    List<LessonSectionDTO> findForLesson(Long lessonId);

    /**
     * Updates an existing LessonSection.
     *
     * @param id The ID of the LessonSection to update
     * @param dto The LessonSectionDTO containing the updated details
     * @return an Optional containing the updated LessonSectionDTO if found, or empty if not found
     */
    Optional<LessonSectionDTO> update(Long id, LessonSectionDTO dto);

    /**
     * Deletes a LessonSection by its ID.
     *
     * @param id The ID of the LessonSection to delete
     * @return true if the section was deleted, false if it did not exist
     */
    boolean delete(Long id);

    /**
     * Retrieves all lesson sections.
     *
     * @return a list of LessonSectionDTO
     */
    List<LessonSectionDTO> findAll();

    /**
     * Retrieves the lesson sections with the given IDs. IDs that do not exist are ignored.
     *
     * @param ids the IDs of the lesson sections
     * @return a list of LessonSectionDTO (empty if no IDs are given)
     */
    List<LessonSectionDTO> findByIds(List<Long> ids);
}

