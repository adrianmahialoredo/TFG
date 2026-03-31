package com.demo.demo.service;

import com.demo.demo.dto.ChordDto;
import com.demo.demo.model.Chord;
import com.demo.demo.repository.ChordRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing chords.
 * 
 * Provides methods to create, retrieve, update, and delete chords,
 * as well as converting between Chord entities and ChordDto objects.
 * 
 * This service interacts with the ChordRepository to perform database operations
 * and handles the conversion of entities to DTOs for API responses.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChordService {

    private final ChordRepository chordRepository;

    /**
     * Retrieves all chords and converts them to DTOs.
     *
     * @return A list of ChordDto representing all chords.
     */
    public List<ChordDto> getAllChords() {
        return chordRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a chord by its ID and converts it to a DTO.
     *
     * @param id The ID of the chord to retrieve.
     * @return An Optional containing the ChordDto if found, otherwise empty.
     */
    public Optional<ChordDto> getChordById(Long id) {
        return chordRepository.findById(id).map(this::toDto);
    }

    /**
     * Creates a new chord from the provided DTO and saves it to the repository.
     *
     * @param dto The ChordDto containing the details of the chord to create.
     * @return The created ChordDto.
     */
    public ChordDto createChord(ChordDto dto) {
        Chord chord = toEntity(dto);
        return toDto(chordRepository.save(chord));
    }

    /**
     * Updates an existing chord with the provided DTO.
     *
     * @param id  The ID of the chord to update.
     * @param dto The ChordDto containing the updated details.
     * @return An Optional containing the updated ChordDto if found, otherwise empty.
     */
    public Optional<ChordDto> updateChord(Long id, ChordDto dto) {
        return chordRepository.findById(id).map(existing -> {
            updateEntityFromDto(existing, dto);
            return toDto(chordRepository.save(existing));
        });
    }

    /**
     * Deletes a chord by its ID.
     *
     * @param id The ID of the chord to delete.
     * @throws EntityNotFoundException if the chord with the given ID does not exist.
     */
    public void deleteChord(Long id) {
        if (!chordRepository.existsById(id)) {
            throw new EntityNotFoundException("Chord not found with id " + id);
        }
        chordRepository.deleteById(id);
    }

    /**
     * Retrieves all chords by a list of chord IDs and converts them to DTOs.
     *
     * @param ids The list of chord IDs to retrieve.
     * @return A list of ChordDto representing all chords with the provided IDs.
     */
    public List<ChordDto> getAllChordsByIds(List<Long> ids) {
        return chordRepository.findAllById(ids).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all chord IDs associated with a specific song lesson.
     *
     * @param songLessonId The ID of the song lesson.
     * @return A list of chord IDs associated with the song lesson.
     */
    public List<Long> getChordsBySongLessonId(Long songLessonId) {
        return chordRepository.findAllBySongLessonId(songLessonId).stream()
                .map(Chord::getId)
                .collect(Collectors.toList());
    }

    // ------------
    // DTO MAPPING
    // ------------

    /**
     * Converts a Chord entity to a ChordDto.
     *
     * @param chord The Chord entity to convert.
     * @return The converted ChordDto.
     */
    private ChordDto toDto(Chord chord) {
        ChordDto dto = new ChordDto();
        dto.setId(chord.getId());
        dto.setName(chord.getName());
        dto.setNotes(chord.getNotes());
        dto.setImageUrl(chord.getImageUrl());
        dto.setOpen(chord.isOpen());
        dto.setBarre(chord.isBarre());
        dto.setPowerChord(chord.isPowerChord());
        dto.setMinor(chord.isMinor());
        dto.setMajor(chord.isMajor());
        dto.setSeventh(chord.isSeventh());
        dto.setCapo(chord.isCapo());
        dto.setCapoFret(chord.getCapoFret());
        return dto;
    }

    /**
     * Converts a ChordDto to a Chord entity.
     *
     * @param dto The ChordDto to convert.
     * @return The converted Chord entity.
     */
    private Chord toEntity(ChordDto dto) {
        Chord chord = new Chord();
        chord.setName(dto.getName());
        chord.setNotes(dto.getNotes());
        chord.setImageUrl(dto.getImageUrl());
        chord.setOpen(dto.isOpen());
        chord.setBarre(dto.isBarre());
        chord.setPowerChord(dto.isPowerChord());
        chord.setMinor(dto.isMinor());
        chord.setMajor(dto.isMajor());
        chord.setSeventh(dto.isSeventh());
        chord.setCapo(dto.isCapo());
        chord.setCapoFret(dto.getCapoFret());
        return chord;
    }

    /**
     * Updates an existing Chord entity with the values from the provided ChordDto.
     *
     * @param chord The Chord entity to update.
     * @param dto   The ChordDto containing the updated values.
     */
    private void updateEntityFromDto(Chord chord, ChordDto dto) {
        chord.setName(dto.getName());
        chord.setNotes(dto.getNotes());
        chord.setImageUrl(dto.getImageUrl());
        chord.setOpen(dto.isOpen());
        chord.setBarre(dto.isBarre());
        chord.setPowerChord(dto.isPowerChord());
        chord.setMinor(dto.isMinor());
        chord.setMajor(dto.isMajor());
        chord.setSeventh(dto.isSeventh());
        chord.setCapo(dto.isCapo());
        chord.setCapoFret(dto.getCapoFret());
    }

}


