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
 * Service class for managing song lessons.
 * 
 * Provides methods to create, retrieve, update, and delete song lessons,
 * as well as converting between DTOs and entities.
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
public class SongLessonService {

    private final SongLessonRepository songLessonRepository;
    private final SongRepository songRepository;
    private final InstrumentRepository instrumentRepository;
    private final ChordRepository chordRepository;

    /**
     * Creates a new song lesson from the provided DTO and saves it to the repository.
     *
     * @param dto The SongLessonDTO containing the details of the lesson to create.
     * @return The created SongLessonDTO.
     */
    public SongLessonDTO createSongLesson(SongLessonDTO dto) {
        SongLesson lesson = toEntity(dto);
        return toDTO(songLessonRepository.save(lesson));
    }

    /**
     * Retrieves a song lesson by its ID and converts it to a DTO.
     *
     * @param id The ID of the song lesson to retrieve.
     * @return An Optional containing the SongLessonDTO if found, otherwise empty.
     */
    public Optional<SongLessonDTO> getSongLessonById(Long id) {
        return songLessonRepository.findById(id).map(this::toDTO);
    }

    /**
     * Retrieves all song lessons associated with a specific song ID and converts them to DTOs.
     *
     * @param songId The ID of the song to find lessons for.
     * @return A list of SongLessonDTOs associated with the given song ID.
     */
    public List<SongLessonDTO> getLessonsBySongId(Long songId) {
        return songLessonRepository.findBySongId(songId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing song lesson with the provided DTO.
     *
     * @param id  The ID of the song lesson to update.
     * @param dto The SongLessonDTO containing the updated details.
     * @return An Optional containing the updated SongLessonDTO if found, otherwise empty.
     */
    public Optional<SongLessonDTO> updateSongLesson(Long id, SongLessonDTO dto) {
        return songLessonRepository.findById(id).map(existing -> {
            updateEntity(existing, dto);
            return toDTO(songLessonRepository.save(existing));
        });
    }

    /**
     * Deletes a song lesson by its ID.
     *
     * @param id The ID of the song lesson to delete.
     * @return true if the song lesson was deleted, false if it did not exist.
     */
    public boolean deleteSongLesson(Long id) {
        if (!songLessonRepository.existsById(id)) return false;
        songLessonRepository.deleteById(id);
        return true;
    }

    // ------------------------------
    // DTO Mapping Helpers
    // ------------------------------

    /**
     * Converts a SongLesson entity to a SongLessonDTO.
     *
     * @param lesson The SongLesson entity to convert.
     * @return The converted SongLessonDTO.
     */
    private SongLessonDTO toDTO(SongLesson lesson) {
        SongLessonDTO dto = new SongLessonDTO();
        dto.setId(lesson.getId());
        dto.setDifficulty(lesson.getDifficulty().toString());
        dto.setCordedLyrics(lesson.getChordedLyrics());
        dto.setVideoUrl(lesson.getVideoUrl());
        dto.setSongId(lesson.getSong().getId());
        dto.setInstrumentId(lesson.getInstrument().getId());
        dto.setChordIds(lesson.getChords().stream().map(Chord::getId).collect(Collectors.toList()));
        dto.setTabSectionsIds(lesson.getTabSections().stream().map(TabSection::getId).collect(Collectors.toList()));
        return dto;
    }

    /**
     * Converts a SongLessonDTO to a SongLesson entity.
     *
     * @param dto The SongLessonDTO to convert.
     * @return The converted SongLesson entity.
     */
    private SongLesson toEntity(SongLessonDTO dto) {
        SongLesson lesson = new SongLesson();
        lesson.setDifficulty(SongLesson.DifficultyLevel.valueOf(dto.getDifficulty()));
        lesson.setChordedLyrics(dto.getCordedLyrics());
        lesson.setVideoUrl(dto.getVideoUrl());
        lesson.setSong(resolveSong(dto.getSongId()));
        lesson.setInstrument(resolveInstrument(dto.getInstrumentId()));
        lesson.setChords(resolveChords(dto.getChordIds()));
        lesson.setTabSections(new ArrayList<>()); // Se agregarán aparte
        return lesson;
    }

    /**
     * Updates an existing SongLesson entity with the details from the provided DTO.
     *
     * @param existing The existing SongLesson entity to update.
     * @param dto      The SongLessonDTO containing the updated details.
     */
    private void updateEntity(SongLesson existing, SongLessonDTO dto) {
        existing.setDifficulty(SongLesson.DifficultyLevel.valueOf(dto.getDifficulty()));
        existing.setChordedLyrics(dto.getCordedLyrics());
        existing.setVideoUrl(dto.getVideoUrl());

        if (!existing.getSong().getId().equals(dto.getSongId())) {
            existing.setSong(resolveSong(dto.getSongId()));
        }

        if (!existing.getInstrument().getId().equals(dto.getInstrumentId())) {
            existing.setInstrument(resolveInstrument(dto.getInstrumentId()));
        }

        existing.setChords(resolveChords(dto.getChordIds()));
    }

    /**
     * Resolves a Song entity by its ID.
     *
     * @param songId The ID of the song to resolve.
     * @return The resolved Song entity.
     * @throws IllegalArgumentException if the song is not found.
     */
    private Song resolveSong(Long songId) {
        return songRepository.findById(songId)
                .orElseThrow(() -> new IllegalArgumentException("Song not found: " + songId));
    }

    /**
     * Resolves an Instrument entity by its ID.
     *
     * @param instrumentId The ID of the instrument to resolve.
     * @return The resolved Instrument entity.
     * @throws IllegalArgumentException if the instrument is not found.
     */
    private Instrument resolveInstrument(Long instrumentId) {
        return instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + instrumentId));
    }

    /**
     * Resolves a list of Chord entities by their IDs.
     *
     * @param chordIds The list of chord IDs to resolve.
     * @return A list of resolved Chord entities.
     * @throws IllegalArgumentException if any chord is not found.
     */
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

