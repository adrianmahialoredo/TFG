package com.demo.demo.service;

import com.demo.demo.dto.ChordDto;
import com.demo.demo.utils.mapper.DtoMapper;
import com.demo.demo.utils.mapper.EntityMapper;
import com.demo.demo.model.Chord;
import com.demo.demo.repository.ChordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import static com.demo.demo.utils.EntityResolver.findChord;
import static com.demo.demo.utils.EntityResolver.findSongLesson;

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
        Chord chord = entityMapper.chordDtoToChord(dto);
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
    }

}

