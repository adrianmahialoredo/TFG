package com.demo.demo.service;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.demo.demo.dto.InstrumentDTO;
import com.demo.demo.model.*;
import com.demo.demo.repository.*;
import lombok.AllArgsConstructor;

/**
 * Service class for managing instruments.
 * 
 * Provides methods to create, read, update, and delete instruments,
 * as well as methods to fetch instruments by ID and convert them to DTOs.
 * 
 * This service uses repositories to interact with the database
 * and convert entities to DTOs for API responses.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@AllArgsConstructor
@Service
public class InstrumentService {
    
    private final InstrumentRepository instrumentRepository;
    private final SongRepository songRepository;

    /**
     * Fetches all instruments from the repository and converts them to DTOs.
     * 
     * @return List of InstrumentDTOs representing all instruments.
     */
    public List<InstrumentDTO> getAllInstruments() {
        return instrumentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetches an instrument by its ID and converts it to a DTO.
     * 
     * @param id The ID of the instrument to fetch.
     * @return An Optional containing the InstrumentDTO if found, otherwise empty.
     */
    public Optional<InstrumentDTO> getInstrumentById(Long id) {
        return instrumentRepository.findById(id)
                .map(this::toDTO);
    }

    /**
     * Creates a new instrument from the provided DTO and saves it to the repository.
     * 
     * @param dto The InstrumentDTO containing the details of the instrument to create.
     * @return The created InstrumentDTO.
     */
    public InstrumentDTO createInstrument(InstrumentDTO dto) {
        Instrument instrument = toEntity(dto);
        return toDTO(instrumentRepository.save(instrument));
    }

    /**
     * Updates an existing instrument with the provided DTO and saves it to the repository.
     * 
     * @param id The ID of the instrument to update.
     * @param dto The InstrumentDTO containing the updated details of the instrument.
     * @return An Optional containing the updated InstrumentDTO if found, otherwise empty.
     */
    public Optional<InstrumentDTO> updateInstrument(Long id, InstrumentDTO dto) {
        return instrumentRepository.findById(id).map(existing -> {
            return toDTO(instrumentRepository.save(updateInstrumentFromDTO(existing, dto)));
        });
    }

    /**
     * Deletes an instrument by its ID.
     * 
     * @param id The ID of the instrument to delete.
     * @return true if the instrument was deleted, false if it did not exist.
     */
    public boolean deleteInstrument(Long id) {
        if (!instrumentRepository.existsById(id)) {
            return false;
        }
        instrumentRepository.deleteById(id);
        return true;
    }

    // ----------------------
    // DTO Mapping Utilities
    // ----------------------

    /**
     * Converts an Instrument entity to its corresponding DTO.
     * 
     * @param instrument The Instrument entity to convert.
     * @return InstrumentDTO representing the instrument.
     */
    private InstrumentDTO toDTO(Instrument instrument) {
        InstrumentDTO dto = new InstrumentDTO();
        dto.setId(instrument.getId());
        dto.setName(instrument.getName());
        dto.setImageUrl(instrument.getImageUrl());
        dto.setSongIds(instrument.getSongs()
                .stream()
                .map(Song::getId)
                .collect(Collectors.toList()));
        return dto;
    }

    /**
     * Converts an InstrumentDTO to its corresponding entity.
     * 
     * @param dto The InstrumentDTO to convert.
     * @return Instrument entity representing the instrument.
     */
    private Instrument toEntity(InstrumentDTO dto) {
        Instrument instrument = new Instrument();
        instrument.setName(dto.getName());
        instrument.setImageUrl(dto.getImageUrl());
        instrument.setSongs(resolveSongs(dto.getSongIds()));
        return instrument;
    }

    /**
     * Updates an existing Instrument entity with the details from the provided DTO.
     * 
     * @param existing The existing Instrument entity to update.
     * @param dto The InstrumentDTO containing the updated details.
     * @return The updated Instrument entity.
     */
    private Instrument updateInstrumentFromDTO(Instrument existing, InstrumentDTO dto) {
        existing.setName(dto.getName());
        existing.setImageUrl(dto.getImageUrl());
        existing.setSongs(resolveSongs(dto.getSongIds()));
        return existing;
    }

    /**
     * Resolves a set of Song entities by their IDs.
     * 
     * @param songIds The list of song IDs to resolve.
     * @return A set of Song entities corresponding to the provided IDs.
     */
    private Set<Song> resolveSongs(List<Long> songIds) {
        return songIds.stream()
                .map(id -> songRepository.findById(id).orElseThrow(() ->
                        new IllegalArgumentException("Song not found: " + id)))
                .collect(Collectors.toSet());
    }

}
