package com.demo.demo.repository;

import com.demo.demo.model.Chord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for managing Chord entities.
 *
 * This interface extends JpaRepository to provide CRUD operations
 * for Chord entities in the database.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Repository
public interface ChordRepository extends JpaRepository<Chord, Long> {

    /**
     * Finds all chords by a list of chord IDs.
     *
     * @param ids List of chord IDs to search for.
     * @return A list of Chord entities matching the provided IDs.
     */
    @NonNull
    List<Chord> findAllById(@NonNull Iterable<Long> ids);

    /**
     * Finds all chords associated with a specific song lesson.
     *
     * @param songLessonId The ID of the song lesson.
     * @return A list of Chord entities associated with the song lesson.
     */
    @Query(
        "SELECT c " +
        "FROM Chord c " +
        "JOIN SongLesson sl ON c MEMBER OF sl.chords " +
        "WHERE sl.id = :songLessonId"
    )
    List<Chord> findAllBySongLessonId(@Param("songLessonId") Long songLessonId);
}
