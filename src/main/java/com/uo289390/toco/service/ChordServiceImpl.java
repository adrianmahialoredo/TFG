package com.uo289390.toco.service;

import com.uo289390.toco.dto.ChordDto;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.mapper.EntityMapper;
import com.uo289390.toco.model.Chord;
import com.uo289390.toco.repository.ChordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import static com.uo289390.toco.utils.EntityResolver.findChord;
import static com.uo289390.toco.utils.EntityResolver.findSongLesson;
import static com.uo289390.toco.utils.EntityResolver.resolveInstrument;

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
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChordServiceImpl implements ChordService {

    private final ChordRepository chordRepository;
    private final DtoMapper dtoMapper;
    private final EntityMapper entityMapper;

    @Override
    public List<ChordDto> findAll() {
        return chordRepository.findAll().stream()
                .map(dtoMapper::chordToChordDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ChordDto> findById(Long id) {
        return findChord(id).map(dtoMapper::chordToChordDto);
    }

    @Override
    @Transactional
    public ChordDto create(ChordDto dto) {
        Chord chord = entityMapper.chordDtoToChord(dto, resolveInstrument(dto.getInstrumentId()));
        return dtoMapper.chordToChordDto(chordRepository.save(chord));
    }

    @Override
    @Transactional
    public Optional<ChordDto> update(Long id, ChordDto dto) {
        return findChord(id).map(existing -> {
            updateEntityFromDto(existing, dto);
            return dtoMapper.chordToChordDto(chordRepository.save(existing));
        });
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!chordRepository.existsById(id)) {
            return false;
        }
        chordRepository.deleteById(id);
        return true;
    }

    @Override
    public List<ChordDto> findByIds(List<Long> ids) {
        return chordRepository.findAllById(ids).stream()
                .map(dtoMapper::chordToChordDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findIdsForSongLesson(Long songLessonId) {
        return findSongLesson(songLessonId)
                .map(songLesson -> songLesson.getChords().stream()
                        .map(Chord::getId)
                        .collect(Collectors.toList()))
                .orElseGet(List::of);
    }

    // ------------
    // DTO MAPPING
    // ------------


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

        if (chord.getInstrument() == null || !chord.getInstrument().getId().equals(dto.getInstrumentId())) {
            chord.setInstrument(resolveInstrument(dto.getInstrumentId()));
        }
    }

}

