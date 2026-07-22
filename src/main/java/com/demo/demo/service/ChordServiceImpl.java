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
 * Default implementation of {@link ChordService}.
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
public class ChordServiceImpl implements ChordService {

    private final ChordRepository chordRepository;

    @Override
    public List<ChordDto> getAllChords() {
        return chordRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ChordDto> getChordById(Long id) {
        return chordRepository.findById(id).map(this::toDto);
    }

    @Override
    public ChordDto createChord(ChordDto dto) {
        Chord chord = toEntity(dto);
        return toDto(chordRepository.save(chord));
    }

    @Override
    public Optional<ChordDto> updateChord(Long id, ChordDto dto) {
        return chordRepository.findById(id).map(existing -> {
            updateEntityFromDto(existing, dto);
            return toDto(chordRepository.save(existing));
        });
    }

    @Override
    public void deleteChord(Long id) {
        if (!chordRepository.existsById(id)) {
            throw new EntityNotFoundException("Chord not found with id " + id);
        }
        chordRepository.deleteById(id);
    }

    @Override
    public List<ChordDto> getAllChordsByIds(List<Long> ids) {
        return chordRepository.findAllById(ids).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getChordsBySongLessonId(Long songLessonId) {
        return chordRepository.findAllBySongLessonId(songLessonId).stream()
                .map(Chord::getId)
                .collect(Collectors.toList());
    }

    // ------------
    // DTO MAPPING
    // ------------

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

    private Chord toEntity(ChordDto dto) {
        Chord chord = new Chord();
        chord.setId(dto.getId());
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

