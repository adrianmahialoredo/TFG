package com.demo.demo.service;

import com.demo.demo.dto.SongLessonDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for managing song lessons.
 *
 * Provides methods to create, retrieve, update, and delete song lessons,
 * as well as converting between DTOs and entities.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
public interface SongLessonService {

    SongLessonDTO createSongLesson(SongLessonDTO dto);

    Optional<SongLessonDTO> getSongLessonById(Long id);

    List<SongLessonDTO> getLessonsBySongId(Long songId);

    Optional<SongLessonDTO> updateSongLesson(Long id, SongLessonDTO dto);

    boolean deleteSongLesson(Long id);
}
