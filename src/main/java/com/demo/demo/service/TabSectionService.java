package com.demo.demo.service;

import com.demo.demo.dto.TabSectionDTO;
import com.demo.demo.model.*;
import com.demo.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing tab sections in a song lesson.
 * 
 * Provides methods to create, retrieve, update, and delete tab sections.
 * as well as methods to find tab sections by song lesson ID.
 * 
 * This service uses repositories to interact with the database
 * and convert entities to DTOs for API responses.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Service
@RequiredArgsConstructor
public class TabSectionService {

    private final TabSectionRepository tabSectionRepository;
    private final SongLessonRepository songLessonRepository;


    /**
     * Creates a new TabSection.
     * 
     * @param dto The TabSectionDTO containing the details of the section to create
     * @return The created TabSectionDTO
     */
    public TabSectionDTO createTabSection(TabSectionDTO dto) {
        TabSection section = toEntity(dto);
        return toDTO(tabSectionRepository.save(section));
    }

    /**
     * Retrieves a TabSection by its ID.
     * 
     * @param id The ID of the TabSection to retrieve
     * @return An Optional containing the TabSectionDTO if found, or empty if not found
     */
    public Optional<TabSectionDTO> getTabSectionById(Long id) {
        return tabSectionRepository.findById(id).map(this::toDTO);
    }

    /**
     * Retrieves all TabSections associated with a specific SongLesson ID.
     * 
     * @param lessonId The ID of the SongLesson to retrieve sections for
     * @return A list of TabSectionDTOs associated with the given SongLesson ID
     */
    public List<TabSectionDTO> getTabSectionsBySongLessonId(Long lessonId) {
        return tabSectionRepository.findBySongLessonId(lessonId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing TabSection.
     * 
     * @param id The ID of the TabSection to update
     * @param dto The TabSectionDTO containing the updated details
     * @return an Optional containing the updated TabSectionDTO if found, or empty if not found
     */
    public Optional<TabSectionDTO> updateTabSection(Long id, TabSectionDTO dto) {
        return tabSectionRepository.findById(id).map(existing -> {
            updateEntity(existing, dto);
            return toDTO(tabSectionRepository.save(existing));
        });
    }

    /**
     * Deletes a TabSection by its ID.
     * 
     * @param id The ID of the TabSection to delete
     * @return true if the section was deleted, false if it did not exist
     */
    public boolean deleteTabSection(Long id) {
        if (!tabSectionRepository.existsById(id)) return false;
        tabSectionRepository.deleteById(id);
        return true;
    }

    // ------------- Mapping -------------

    /**
     * Converts a TabSection entity to a TabSectionDTO.
     * 
     * @param section The TabSection entity to convert
     * @return The corresponding TabSectionDTO
     */
    private TabSectionDTO toDTO(TabSection section) {
        TabSectionDTO dto = new TabSectionDTO();
        dto.setId(section.getId());
        dto.setIdentifier(section.getIdentifier());
        dto.setContent(section.getContent());
        dto.setSongLessonId(section.getSongLesson().getId());
        return dto;
    }

    /**
     * Converts a TabSectionDTO to a TabSection entity.
     * 
     * @param dto The TabSectionDTO to convert
     * @return The corresponding TabSection entity
     */
    private TabSection toEntity(TabSectionDTO dto) {
        TabSection section = new TabSection();
        section.setIdentifier(dto.getIdentifier());
        section.setContent(dto.getContent());
        section.setSongLesson(resolveSongLesson(dto.getSongLessonId()));
        return section;
    }

    /**
     * Updates an existing TabSection entity with the values from a TabSectionDTO.
     * 
     * @param section The TabSection entity to update
     * @param dto The TabSectionDTO containing the updated values
     */
    private void updateEntity(TabSection section, TabSectionDTO dto) {
        section.setIdentifier(dto.getIdentifier());
        section.setContent(dto.getContent());

        if (!section.getSongLesson().getId().equals(dto.getSongLessonId())) {
            section.setSongLesson(resolveSongLesson(dto.getSongLessonId()));
        }
    }

    /**
     * Resolves a SongLesson by its ID.
     * 
     * @param id The ID of the SongLesson to resolve
     * @return The resolved SongLesson entity
     * @throws IllegalArgumentException if the SongLesson with the given ID does not exist
     */
    private SongLesson resolveSongLesson(Long id) {
        return songLessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SongLesson not found: " + id));
    }

}
