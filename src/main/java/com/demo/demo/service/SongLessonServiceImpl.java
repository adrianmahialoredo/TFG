package com.demo.demo.service;

import com.demo.demo.dto.SongLessonDTO;
import com.demo.demo.model.*;
import com.demo.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

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
@Slf4j
@Service
@RequiredArgsConstructor
public class SongLessonServiceImpl implements SongLessonService {

    private final SongLessonRepository songLessonRepository;
    private final SongRepository songRepository;
    private final InstrumentRepository instrumentRepository;
    private final ChordRepository chordRepository;

    @Override
    public SongLessonDTO createSongLesson(SongLessonDTO dto) {
        SongLesson lesson = toEntity(dto);
        return toDTO(songLessonRepository.save(lesson));
    }

    @Override
    public Optional<SongLessonDTO> getSongLessonById(Long id) {
        return songLessonRepository.findById(id).map(this::toDTO);
    }

    @Override
    public List<SongLessonDTO> getLessonsBySongId(Long songId) {
        return songLessonRepository.findBySongId(songId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SongLessonDTO> updateSongLesson(Long id, SongLessonDTO dto) {
        return songLessonRepository.findById(id).map(existing -> {
            updateEntity(existing, dto);
            return toDTO(songLessonRepository.save(existing));
        });
    }

    @Override
    public boolean deleteSongLesson(Long id) {
        if (!songLessonRepository.existsById(id)) return false;
        songLessonRepository.deleteById(id);
        return true;
    }

    // ------------------------------
    // DTO Mapping Helpers
    // ------------------------------

    private SongLessonDTO toDTO(SongLesson lesson) {
        SongLessonDTO dto = new SongLessonDTO();
        dto.setId(lesson.getId());
        dto.setDifficulty(lesson.getDifficulty().toString());
        dto.setChordedLyrics(lesson.getChordedLyrics());
        dto.setVideoUrl(lesson.getVideoUrl());
        dto.setSongId(lesson.getSong().getId());
        dto.setInstrumentId(lesson.getInstrument().getId());
        dto.setChordIds(lesson.getChords().stream().map(Chord::getId).collect(Collectors.toList()));
        dto.setTabSectionsIds(lesson.getTabSections().stream().map(TabSection::getId).collect(Collectors.toList()));
        return dto;
    }

    private SongLesson toEntity(SongLessonDTO dto) {
        SongLesson lesson = new SongLesson();
        lesson.setId(dto.getId());
        lesson.setDifficulty(SongLesson.DifficultyLevel.valueOf(dto.getDifficulty()));
        lesson.setChordedLyrics(dto.getChordedLyrics());
        lesson.setVideoUrl(dto.getVideoUrl());
        lesson.setSong(resolveSong(dto.getSongId()));
        lesson.setInstrument(resolveInstrument(dto.getInstrumentId()));
        lesson.setChords(resolveChords(dto.getChordIds()));
        lesson.setTabSections(new ArrayList<>()); // Se agregarán aparte
        return lesson;
    }

    private void updateEntity(SongLesson existing, SongLessonDTO dto) {
        existing.setDifficulty(SongLesson.DifficultyLevel.valueOf(dto.getDifficulty()));
        existing.setChordedLyrics(dto.getChordedLyrics());
        existing.setVideoUrl(dto.getVideoUrl());

        if (!existing.getSong().getId().equals(dto.getSongId())) {
            existing.setSong(resolveSong(dto.getSongId()));
        }

        if (!existing.getInstrument().getId().equals(dto.getInstrumentId())) {
            existing.setInstrument(resolveInstrument(dto.getInstrumentId()));
        }

        existing.setChords(resolveChords(dto.getChordIds()));
    }

    private Song resolveSong(Long songId) {
        return songRepository.findById(songId)
                .orElseThrow(() -> new IllegalArgumentException("Song not found: " + songId));
    }

    private Instrument resolveInstrument(Long instrumentId) {
        return instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + instrumentId));
    }

    private List<Chord> resolveChords(List<Long> chordIds) {
        if (chordIds == null || chordIds.isEmpty()) {
            return new ArrayList<>();
        }
        return chordIds.stream()
                .map(id -> chordRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Chord not found: " + id)))
                .collect(Collectors.toList());
    }

}

