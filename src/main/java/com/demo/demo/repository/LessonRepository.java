package com.demo.demo.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.demo.model.Lesson;

import java.util.Collection;
import java.util.List;

/**
 * Repository interface for managing Lesson entities.
 *
 * All the queries are derived from the method names.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /**
     * Finds the lessons that belong to a genre.
     *
     * @param genreId the ID of the genre
     * @return the lessons of the genre
     */
    List<Lesson> findByGenreIdAndBelongsToGenreTrue(Long genreId);

    /**
     * Finds the popular lessons.
     *
     * @return the popular lessons
     */
    List<Lesson> findByPopularTrue();

    /**
     * Finds the lessons of a genre, excluding the ones with the given IDs.
     *
     * @param genreId      the ID of the genre
     * @param excludingIds the IDs of the lessons to exclude
     * @return the lessons of the genre, excluding the given ones
     */
    List<Lesson> findByGenreIdAndIdNotIn(Long genreId, Collection<Long> excludingIds, Pageable pageable);

    /**
     * Finds all the lessons ordered from newest to oldest.
     *
     * @return the lessons, newest first
     */
    List<Lesson> findAllByOrderByCreatedAtDescIdDesc();

    /**
     * Finds all the lessons ordered by level (from easiest to hardest).
     * Used as the recommended lessons until a real recommendation algorithm exists.
     *
     * @return the lessons, easiest first
     */
    List<Lesson> findAllByOrderByLevelAscIdAsc();

    List<Lesson> findByIdNotIn(List<Long> excludingIds, Pageable pageable);
}
