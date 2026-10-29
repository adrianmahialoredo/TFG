package com.uo289390.toco.service;

import com.uo289390.toco.dto.SongLessonDTO;
import com.uo289390.toco.model.SongLesson;
import com.uo289390.toco.repository.SongLessonRepository;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.mapper.EntityMapper;
import com.uo289390.toco.model.*;
import com.uo289390.toco.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static com.uo289390.toco.utils.EntityResolver.resolveChords;
import static com.uo289390.toco.utils.EntityResolver.resolveInstrument;
import static com.uo289390.toco.utils.EntityResolver.resolveSong;
import static com.uo289390.toco.utils.EntityResolver.findSongLesson;

/**
 * Default implementation of {@link SongLessonService}.
 *
 * This service interacts with repositories to perform database operations
 * and handles the conversion of entities to DTOs for API responses.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SongLessonServiceImpl implements SongLessonService {

    private final SongLessonRepository songLessonRepository;
    private final DtoMapper dtoMapper;
    private final EntityMapper entityMapper;

    @Override
    @Transactional
    public SongLessonDTO create(SongLessonDTO dto) {
        Song song = resolveSong(dto.getSongId());
        Instrument instrument = resolveInstrument(dto.getInstrumentId());
        SongLesson lesson = entityMapper.songLessonDtoToSongLesson(
                dto,
                song,
                instrument,
                resolveChords(dto.getChordIds(), instrument)
        );
        return dtoMapper.songLessonToSongLessonDto(songLessonRepository.save(lesson));
    }

    @Override
    public List<SongLessonDTO> findAll() {
        return songLessonRepository.findAll().stream()
                .map(dtoMapper::songLessonToSongLessonDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SongLessonDTO> findById(Long id) {
        return findSongLesson(id).map(dtoMapper::songLessonToSongLessonDto);
    }

    @Override
    public List<SongLessonDTO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return songLessonRepository.findAllById(ids)
                .stream()
                .map(dtoMapper::songLessonToSongLessonDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongLessonDTO> findForSong(Long songId) {
        return songLessonRepository.findBySongId(songId)
                .stream()
                .map(dtoMapper::songLessonToSongLessonDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<SongLessonDTO> update(Long id, SongLessonDTO dto) {
        return findSongLesson(id).map(existing -> {
            updateEntity(existing, dto);
            return dtoMapper.songLessonToSongLessonDto(songLessonRepository.save(existing));
        });
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!songLessonRepository.existsById(id)) return false;
        songLessonRepository.deleteById(id);
        return true;
    }

    // ------------------------------
    // DTO Mapping Helpers
    // ------------------------------


    private void updateEntity(SongLesson existing, SongLessonDTO dto) {
        existing.setDifficulty(SongLesson.DifficultyLevel.from(dto.getDifficulty()));
        existing.setChordedLyrics(dto.getChordedLyrics());
        existing.setVideoUrl(dto.getVideoUrl());

        if (!existing.getSong().getId().equals(dto.getSongId())) {
            existing.setSong(resolveSong(dto.getSongId()));
        }

        if (!existing.getInstrument().getId().equals(dto.getInstrumentId())) {
            existing.setInstrument(resolveInstrument(dto.getInstrumentId()));
        }

        existing.setChords(resolveChords(dto.getChordIds(), existing.getInstrument()));
    }

}

