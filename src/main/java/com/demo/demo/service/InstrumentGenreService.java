package com.demo.demo.service;

import com.demo.demo.dto.InstrumentGenreDTO;
import com.demo.demo.model.*;
import com.demo.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing instrument genres.
 * 
 * Provides methods to create, read, update, and delete instrument genres,
 * as well as methods to find instrument genres by instrument or genre IDs.
 * 
 * This service uses repositories to interact with the database
 * and convert entities to DTOs for API responses.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 * 
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstrumentGenreService {

    private final InstrumentGenreRepository instrumentGenreRepository;
    private final InstrumentRepository instrumentRepository;
    private final GenreRepository genreRepository;
    private final LessonRepository lessonRepository;

    /**
     * Fetches all instrument genres from the repository and converts them to DTOs.
     * 
     * @return List of InstrumentGenreDTOs representing all instrument genres.
     */
    public List<InstrumentGenreDTO> getAllInstrumentGenres() {
        return instrumentGenreRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetches an instrument genre by its ID and converts it to a DTO.
     * 
     * @param id The ID of the instrument genre to fetch.
     * @return An Optional containing the InstrumentGenreDTO if found, otherwise empty.
     */
    public Optional<InstrumentGenreDTO> getInstrumentGenreById(Long id) {
        return instrumentGenreRepository.findById(id)
                .map(this::toDTO);
    }

    /**
     * Creates a new instrument genre from the provided DTO and saves it to the repository.
     * 
     * @param dto The InstrumentGenreDTO containing the details of the instrument genre to create.
     * @return The created InstrumentGenreDTO.
     */
    public InstrumentGenreDTO createInstrumentGenre(InstrumentGenreDTO dto) {
        InstrumentGenre ig = toEntity(dto);
        return toDTO(instrumentGenreRepository.save(ig));
    }

    /**
     * Updates an existing instrument genre identified by its ID with the details from the provided DTO.
     * 
     * @param id  The ID of the instrument genre to update.
     * @param dto The InstrumentGenreDTO containing the updated details of the instrument genre.
     * @return An Optional containing the updated InstrumentGenreDTO if found, otherwise empty.
     */
    public Optional<InstrumentGenreDTO> updateInstrumentGenre(Long id, InstrumentGenreDTO dto) {
        return instrumentGenreRepository.findById(id).map(existing -> {
            return toDTO(instrumentGenreRepository.save(updateInstrumentGenre(existing, dto)));
        });
    }

    /**
     * Deletes an instrument genre by its ID.
     * 
     * @param id The ID of the instrument genre to delete.
     * @return true if the deletion was successful, false if the instrument genre was not found.
     */
    public boolean deleteInstrumentGenre(Long id) {
        if (!instrumentGenreRepository.existsById(id)) return false;
        instrumentGenreRepository.deleteById(id);
        return true;
    }

    /**
     * Finds instrument genres by genre ID.
     * 
     * @param genreId The ID of the genre to search for.
     * @return List of InstrumentGenreDTOs associated with the specified genre ID.
     */
    public List<InstrumentGenreDTO> findByGenreId(Long genreId) {
        return instrumentGenreRepository.findByGenreId(genreId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds instrument genres by instrument ID.
     * 
     * @param instrumentId The ID of the instrument to search for.
     * @return List of InstrumentGenreDTOs associated with the specified instrument ID.
     */
    public List<InstrumentGenreDTO> findByInstrumentId(Long instrumentId) {
        return instrumentGenreRepository.findByInstrumentId(instrumentId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // -----------------------
    // Mapping Methods
    // -----------------------

    /**
     * Converts an InstrumentGenre entity to its corresponding DTO.
     * 
     * @param ig The InstrumentGenre entity to convert.
     * @return InstrumentGenreDTO representing the entity.
     */
    private InstrumentGenreDTO toDTO(InstrumentGenre ig) {
        InstrumentGenreDTO dto = new InstrumentGenreDTO();
        dto.setId(ig.getId());
        dto.setInstrumentId(ig.getInstrument().getId());
        dto.setGenreId(ig.getGenre().getId());
        dto.setLessonIds(ig.getLessons().stream()
                .map(Lesson::getId)
                .collect(Collectors.toList()));
        return dto;
    }

    /**
     * Converts an InstrumentGenreDTO to its corresponding entity.
     * 
     * @param dto The InstrumentGenreDTO to convert.
     * @return InstrumentGenre entity representing the DTO.
     */
    private InstrumentGenre toEntity(InstrumentGenreDTO dto) {
        InstrumentGenre ig = new InstrumentGenre();
        ig.setInstrument(resolveInstrument(dto.getInstrumentId()));
        ig.setGenre(resolveGenre(dto.getGenreId()));
        ig.setLessons(resolveLessons(dto.getLessonIds()));
        return ig;
    }

    /**
     * Updates an existing InstrumentGenre entity with the details from the provided DTO.
     * 
     * @param existing The existing InstrumentGenre entity to update.
     * @param dto      The InstrumentGenreDTO containing the updated details.
     * @return The updated InstrumentGenre entity.
     */
    private InstrumentGenre updateInstrumentGenre(InstrumentGenre existing, InstrumentGenreDTO dto) {
        existing.setInstrument(resolveInstrument(dto.getInstrumentId()));
        existing.setGenre(resolveGenre(dto.getGenreId()));
        existing.setLessons(resolveLessons(dto.getLessonIds()));
        return existing;
    }

    /**
     * Resolves an Instrument entity by its ID.
     * 
     * @param id The ID of the instrument to resolve.
     * @return The resolved Instrument entity.
     * @throws IllegalArgumentException if the instrument is not found.
     */
    private Instrument resolveInstrument(Long id) {
        return instrumentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + id));
    }

    /**
     * Resolves a Genre entity by its ID.
     * 
     * @param id The ID of the genre to resolve.
     * @return The resolved Genre entity.
     * @throws IllegalArgumentException if the genre is not found.
     */
    private Genre resolveGenre(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Genre not found: " + id));
    }

    /**
     * Resolves a list of lesson IDs to a set of Lesson entities.
     * 
     * @param lessonIds The list of lesson IDs to resolve.
     * @return A set of Lesson entities corresponding to the provided IDs.
     * @throws IllegalArgumentException if any lesson is not found.
     */
    private Set<Lesson> resolveLessons(List<Long> lessonIds) {
        if (lessonIds == null || lessonIds.isEmpty()) {
            return new HashSet<>();
        }
        return lessonIds.stream()
                .map(id -> lessonRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + id)))
                .collect(Collectors.toSet());
    }

}
