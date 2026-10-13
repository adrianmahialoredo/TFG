package com.uo289390.toco.service;

import com.uo289390.toco.dto.SongLessonDTO;

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

    SongLessonDTO create(SongLessonDTO dto);

    Optional<SongLessonDTO> findById(Long id);

    List<SongLessonDTO> findByIds(List<Long> ids);

    List<SongLessonDTO> findForSong(Long songId);

    Optional<SongLessonDTO> update(Long id, SongLessonDTO dto);

    boolean delete(Long id);

    /**
     * Retrieves all song lessons.
     *
     * @return a list of SongLessonDTO
     */
    List<SongLessonDTO> findAll();
}
