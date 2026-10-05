package com.demo.demo.service;

import com.demo.demo.dto.LessonDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for managing lessons.
 *
 * Exposes flat DTO responses for the frontend while supporting cascading
 * creation from data-loading scripts.
 *
 * @author Adrián Mahía
 * @version 1.2
 */
public interface LessonService {

    List<LessonDTO> findAll();

    Optional<LessonDTO> findById(Long id);

    LessonDTO create(LessonDTO dto);

    Optional<LessonDTO> update(Long id, LessonDTO dto);

    boolean delete(Long id);

    List<LessonDTO> findForGenre(Long genreId);

    List<LessonDTO> findByIds(List<Long> ids);

    List<Long> findIdsPopular();

    List<Long> findIdsNew();

    List<Long> findIdsRecommended();
}