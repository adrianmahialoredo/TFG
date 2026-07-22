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

    List<LessonDTO> getAllLessons();

    Optional<LessonDTO> getLessonById(Long id);

    LessonDTO createLesson(LessonDTO dto);

    Optional<LessonDTO> updateLesson(Long id, LessonDTO dto);

    boolean deleteLesson(Long id);

    List<LessonDTO> getLessonsByGenreId(Long genreId);

    List<LessonDTO> getLessonsByIds(List<Long> ids);

    List<LessonDTO> getHomeLessonsPopular();

    List<LessonDTO> getHomeLessonsNew();

    List<LessonDTO> getHomeLessonsRecommended();
}